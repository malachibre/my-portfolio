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
import java.util.List;
import java.util.stream.Collectors;

public final class FindMeetingQuery {

  /** First Solution: 
   * Separate {@code events} in two lists based on attendee type.
   * Call .queryHelper() on each list
   * Return a collection with valid time slots accroding to requesst.
   *
   * This solution is O(nlog_n) becuase of the use of the stream.sort() method. 
   * Although, all of the test cases use Event Collections that are 
   * already in order, which would the removal of .sort() and 
   * lower the algorithmic complexity to O(n).
   * But it's not safe to assume the Event Collection will always be in order 
   * before the method call.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    // If the request has no attendees, the open time slot is the entire day.
    if (request.getAttendees().isEmpty() && 
        request.getOptionalAttendees().isEmpty()) {
        return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    // Sort {@code events} by starting time.
    List<Event> eventsList = new ArrayList(events);
    eventsList.sort((Event a, Event b) -> {
        return TimeRange.ORDER_BY_START.compare(a.getWhen(), b.getWhen());
    });

    // Filter out events with attendees not in {@code request}.
    eventsList = eventsList.stream()
                           .filter(e -> request.getAttendees()
                                               .containsAll(e.getAttendees()) || 
                                        request.getOptionalAttendees()
                                               .containsAll(e.getAttendees()))
                           .collect(Collectors.toList());
    List<Event> mandatoryEvents = eventsList.stream()
                           .filter(e -> request.getAttendees()
                                               .containsAll(e.getAttendees()))
                           .collect(Collectors.toList());
    
    Collection<TimeRange> allTRs = queryHelper(eventsList, request);

    // If there are no attendes, there could still be optional attendees
    // so all valid {@code TimeRange} objects are returned.
    if (request.getAttendees().isEmpty()) {
      return allTRs;
    }

    Collection<TimeRange> mandatoryTRs = queryHelper(mandatoryEvents, request);

    // If there are no avilable {@code TimeRange} objects for all attendees,
    // avilable TRs for mandatory attendees should be returned in case there
    // are avilable TRs for them.
    if (allTRs.isEmpty()) {
        return mandatoryTRs;
    }

    return allTRs;


  }

  /** 
   * This is a helper method for .query(). 
   * It works by finding all the availabe {@code TimeRange} objects that 
   * qualify for request.
   */
  public Collection<TimeRange> queryHelper(Collection<Event> separateEvents, MeetingRequest request) {
    // Map each {@code Event} to it's {@code TimeRange}.
    List<TimeRange> TimeRangeList = separateEvents.stream()
                     .map(a -> a.getWhen())
                     .collect(Collectors.toList());

    // Resulting Collection
    Collection<TimeRange> openTimeRanges = new ArrayList();

    int currentMinTime = 0;
    for(TimeRange tr : TimeRangeList) {
        
        // If the end time of the current {@code TimeRange} is less than 
        // {@code currentMinTime} then it should be disregarded 
        // as it will not change  the value of {@code currentMinTime}
        if(tr.end() <= currentMinTime) {
            continue;
        }

        // If the current {@code TimeRange} contains {@code currentMinTime},
        // it will change the value of {@code currentMinTime}, but will not
        // influence the start time of the current open time slot.
        else if(tr.contains(currentMinTime)) {
            currentMinTime = tr.end();
        } else {
          TimeRange newTR = TimeRange.fromStartEnd(currentMinTime, tr.start(), false);
          // the duration of the current time slot needs to be greater than the 
          // duration of {@code request} to be considered 
          if(newTR.duration() >= request.getDuration()) {
              openTimeRanges.add(newTR);
          }           
          currentMinTime = tr.end();
        } 
    }
    // Adding the final avaibale time slot if possible.
    TimeRange newTR = TimeRange.fromStartEnd(currentMinTime, TimeRange.END_OF_DAY, true);
    if(newTR.duration() >= request.getDuration()) {
      openTimeRanges.add(newTR);
    }
    return openTimeRanges;
  }


}
