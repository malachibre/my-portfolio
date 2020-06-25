// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.HashSet;
import java.util.stream.Collectors;

public final class FindMeetingQuery {

  private static Comparator<Event> ORDER_BY_EVENT_START = (Event a, Event b) ->
    TimeRange.ORDER_BY_START.compare(a.getWhen(), b.getWhen());

  /** 
   * First Solution: 
   * Separate {@code events} in two lists based on attendee type.
   * Call .findOpenTimeRanges() on each list
   * Return a collection with valid time slots accroding to requesst.
   *
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    // If the request has no attendees, the open time slot is the entire day.
    if (request.getAttendees().isEmpty() && 
        request.getOptionalAttendees().isEmpty()) {
        return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    // Filter out events with attendees not in {@code request}.
    List<Event> eventsList = events.stream()
                           .filter(event -> 
                                !Collections.disjoint(request.getAttendees(),
                                                      event.getAttendees()) || 
                                !Collections.disjoint(request.getOptionalAttendees(),
                                                      event.getAttendees()))
                           .sorted(ORDER_BY_EVENT_START)
                           .collect(Collectors.toList());

    List<TimeRange> eventTimes = eventsList.stream()
                                           .map(Event::getWhen)
                                           .collect(Collectors.toList());


    Collection<TimeRange> allAvailableTimeRanges = 
        findOpenTimeRanges(eventTimes, request.getDuration());

    List<TimeRange> mandatoryEventTimes = events.stream()
                           .filter(event -> !Collections.disjoint(request.getAttendees(), event.getAttendees()))
                           .sorted(ORDER_BY_EVENT_START)
                           .map(Event::getWhen)
                           .collect(Collectors.toList());

    List<Event> optionalEvents = eventsList.stream()
                           .filter(event -> 
                             !Collections.disjoint(request.getOptionalAttendees(), event.getAttendees()))

                           .collect(Collectors.toList());

    Collection<TimeRange> mandatoryTRs = findOpenTimeRanges(mandatoryEventTimes, request.getDuration());

    Collection<TimeRange> maxOptions = findPossibleTimeSlots(optionalEvents,
                                                             mandatoryTRs, 
                                                             request);

    if (allAvailableTimeRanges.isEmpty() && !mandatoryTRs.isEmpty()) {
      if (maxOptions.isEmpty()) {
          return mandatoryTRs;
      }
      return maxOptions;
    }

    if (mandatoryTRs.isEmpty() && !request.getOptionalAttendees().isEmpty()) {
      return filterMaxOptions(Arrays.asList(TimeRange.WHOLE_DAY),
                        optionalEvents, 
                        request);

    }

    return allAvailableTimeRanges;
  }

  /** 
   * This is a helper method for .query(). 
   * It works by finding all the availabe {@code TimeRange} objects that 
   * qualify for request. 
   * @param separateTimeRanges - Sorted list of time ranges
   * @param minimumDuration - The minimum amount of time a non-conflicting 
   *                          time slot can be so that it is added to the 
   *                          returned collection.
   * @return - All the time slots that do not conflict with any element in
   *          separateTimeRanges and is also longer than minimumDuration.
   */
  public Collection<TimeRange> findOpenTimeRanges(Collection<TimeRange> seperateTimeRanges, 
                                                  long minimumDuration) {

    // Resulting Collection
    Collection<TimeRange> openTimeRanges = new ArrayList<>();

    int currentMinTime = TimeRange.START_OF_DAY;
    for (TimeRange tr : seperateTimeRanges) {
        
        // If the end time of the current {@code TimeRange} is less than
        // {@code currentMinTime} then it should be disregarded 
        // as it will not change  the value of {@code currentMinTime}
        if (tr.end() <= currentMinTime) {
            continue;
        }

        // If the current {@code TimeRange} contains {@code currentMinTime},
        // it will change the value of {@code currentMinTime}, but will not
        // influence the start time of the current open time slot.
        else {
            if (!tr.contains(currentMinTime)) {
              TimeRange newTR = TimeRange.fromStartEnd(currentMinTime, tr.start(), false);
              // the duration of the current time slot needs to be greater than the
              // duration of {@code request} to be considered
              if(newTR.duration() >= minimumDuration) {
                openTimeRanges.add(newTR);
              }           
            }
          currentMinTime = tr.end();
        } 
    }
    // Adding the final available time slot if possible.
    TimeRange newTR = TimeRange.fromStartEnd(currentMinTime, TimeRange.END_OF_DAY, true);
    if (newTR.duration() >= minimumDuration) {
      openTimeRanges.add(newTR);
    }
    return openTimeRanges;
  }

  /**
   * Creates a new Collection of TimeRange objects that contains time slots
   * that permits the avialblity of ALL mandatory employees and AT LEAST 1
   * optional attendee.
   * Optional Event  :           |--C--|
   * Mandaotry Events  :       |--A--|     |--B--|
   * Available TimeRange :              |--|
   * @param eventsList - Sorted list of events by start time
   *                     to determine an available time
   *                     for {@code request}
   *
   * @param mandatoryTRs - Collection of {@code TimeRange} objects that permit
   *                       permit availablity of all mandatory attendees in
   *                        {@code request}
   *
   * @param request - Request to find open TimeRange objects with.
   *
   * @return A collection of {@code TimeRange} objects that contain availability
   *         for all mandatory attendees and at least one optional attendee.
   * 
   */
  public Collection<TimeRange> findPossibleTimeSlots(Collection<Event> optionalEvents,
                                                     Collection<TimeRange> mandatoryTRs,
                                                     MeetingRequest request) {
    
    // Resulting Collection
    Collection<TimeRange> possibleTimeSlots = new ArrayList<>();
    
    // Iterates through each {@code TimeRange} in {@code mandatoryTRs}
    // and each {@code Event} in {@code optionalEvents} and creates 
    // TimeRange objects that allow an optional attendee to fit inside
    // the same time span as an TimeRange object in mandatoryTRs.
    for (TimeRange tr : mandatoryTRs) {
      // current Min/Max time are set to -1 until a candidate is found for
      // each value.
      int currentMinTime = -1;
      int currentMaxTime = -1;
      for (Event ev : optionalEvents) {
        // If the event starts after the end of this available time slot,
        // it can not fit inside the time span determined by this time slot
        // so it is skipped. 
        if (ev.getWhen().start() >= tr.end()) {
          continue;
        }
        if (ev.getWhen().start() <= tr.start()) {
          // if the event starts and ends before this {@code TimeRange}
          // or starts before and ends after this TimeRange, it is
          // also not able to fit inside the time span determined
          // by the TimeRange.
          if (ev.getWhen().end() >= tr.end() ||
              ev.getWhen().end() <= tr.start()) {
            continue;
          } 
          // If the event end before this TimeRange and ends during it,
          // it marks the start of a possible new TimeRange that fits inside
          // the bounds of the current TimeRange.
          currentMinTime = ev.getWhen().end();
        }
        // If the event starts before the end of this TimeRange,
        // it marks the end of a new possible TimeRange.
        if (ev.getWhen().start() < tr.end()) {
          currentMaxTime = ev.getWhen().start();
        }
        // Once a pair is found, the TimeRange determined by current Min/Max time
        // is added to {@code possibleTimeSlots} and these values are reset.
        if (currentMinTime != -1 &&
            currentMaxTime - currentMinTime >= request.getDuration()) {
            possibleTimeSlots.add(TimeRange.fromStartEnd(currentMinTime,
                                                      currentMaxTime,
                                                      false));
            currentMinTime = -1;
            currentMaxTime = -1;
        }
      }
    }
    return filterMaxOptions(possibleTimeSlots, optionalEvents, request);
  }

  /**
   * Filters the Collection returned by {@clink findPossibleTimeSlots} to
   * find the time slots that permit the avilability of the highest possible
   * optional attendees.
   * 
   * @param possibleTimeSlots - {@code TimeRange} Collection returned by 
   *                            {@link findPossibleTimeSlots}.
   * @param optionalEvents - Events that contain optional attendees determined by {@code request}.
   * 
   * @param request - Request to find open TimeRange objects for.
   *
   * @return {@code TimeRange} Collection that allows the highest possible
   *         optional Attendees and ALL mandatory attendees to attend the event
   *         given in {@code request}.
   */
  public Collection<TimeRange> filterMaxOptions(Collection<TimeRange> possibleTimeSlots,
                                                Collection<Event> optionalEvents,
                                                MeetingRequest request) {
    // Resulting Collection
    Collection<TimeRange> results = new ArrayList<>();
    int maxOptionalAttendees = 0;
    for (TimeRange tr : possibleTimeSlots) {
      Collection<String> uniqueAttendees = new HashSet<>();
      for (Event ev : optionalEvents) {
        if (ev.getWhen().overlaps(tr)){
          for (String attendee : ev.getAttendees()) {
              if (request.getOptionalAttendees().contains(attendee) &&
                  !uniqueAttendees.contains(attendee)){
                uniqueAttendees.add(attendee);
              }
          }
        }
      }
      if (uniqueAttendees.size() >= maxOptionalAttendees) {
        // If this TimeRange allows more optionalAttendees than than the
        // the current value, the resulting list should be cleared
        // as none of those TimeRange objects allow more optionalAttendees
        // than the current TimeRange.
        if (uniqueAttendees.size() > maxOptionalAttendees) {
          results.clear();
        }
        results.add(tr);
        maxOptionalAttendees = uniqueAttendees.size();
      }

    }
    return results;
  }

}
