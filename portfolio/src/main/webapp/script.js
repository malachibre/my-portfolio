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

/** Retrieves data from the /data page and displays it. */
function getComments() {
  fetch('/data').then(response => response.json()).then((json) => {
    json.forEach(comment => displayComment(comment));
  });
}

/** 
 * Creates paragraph elements and sets the text to comments 
 * pulled from \data page. 
 */
function displayComment(comment) {
  const commentElement = document.createElement("p");
  commentElement.innerText =
   `${comment.text} posted on: ${comment.month}/${comment.day}/${comment.year}`;
  document.getElementById("comments-container").appendChild(commentElement);
}

window.addEventListener('DOMContentLoaded', getComments, false);

let pictureNumber = 0;

/** Cycles through 3 different pictures of myself. */
function cyclePictures() {
  const headerSelfie = document.getElementById("header-selfie");
  const imageNames = ["campus", "suit", "main-selfie"];
  headerSelfie.src = `images/${imageNames[pictureNumber++ % imageNames.length]}.jpg`;
}
