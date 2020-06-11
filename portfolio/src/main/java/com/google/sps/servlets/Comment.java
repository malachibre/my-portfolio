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

package com.google.sps.servlets;

/** This class sets the text and date data for a {@code Comment} */
public final class Comment {

  /** postedDateTIme should appear like this: Jun 11, 2020 11:18 AM */  
  private final String postedDateTime;
  private final String title;
  private final String text;
  private final String imageUrl;

  public Comment(String postedDateTime, String title, String text, String imageUrl) {
    this.postedDateTime = postedDateTime;
    this.title = title;
    this.text = text;
    this.imageUrl = imageUrl;
    
  }
}
