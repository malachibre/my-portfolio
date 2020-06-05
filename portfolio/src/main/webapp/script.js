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

window.addEventListener('DOMContentLoaded', () => {
  const navBar = document.getElementById('nav-bar');

  Array.from(navBar.children).forEach(child => {
    child.addEventListener('click', () => {
      const targetId = child.getAttribute('data-target-id');
      const targetElement = document.getElementById(targetId);
      showContent(targetElement);
    });
  });
});

/* Displays the content title and text in the content container */
function showContent(element) {
  const contentContainer = document.getElementById('content-container');

  Array.from(contentContainer.children).forEach(child => {
    child.classList.toggle('hidden', true);
  });

  element.classList.toggle('hidden', false);
}

/** Retrieves data from the /data page and displays it. */
function getComments() {
  clearComments();
  localStorage.setItem("commentAmount", document.getElementById("comment-amount").value);
  commentAmount = localStorage.getItem("commentAmount");
  fetch('/data?comment-amount=' + commentAmount).then(response => response.json())
  .then((json) => {
    json.forEach(comment => displayComment(comment));
  });
  localStorage.setItem("commentAmount", document.getElementById("comment-amount").value);
}

/** Clears the div container holding the comments */
function clearComments() {
  commentContainer = document.getElementById("comments");
    while(commentContainer.firstChild){
      commentContainer.removeChild(commentContainer.firstChild);
  }
}

/** 
 * Creates paragraph elements and sets the text to comments 
 * pulled from /data page. 
 */
function displayComment(comment) {
  const commentElement = document.createElement("p");
  commentElement.innerText =
   `${comment.text} posted on: ${comment.month}/${comment.day}/${comment.year}`;
  document.getElementById("comments").appendChild(commentElement);
}

document.getElementById("comment-amount")
  .addEventListener("change", () => getComments());

function loadCanvas () {
  const c = document.getElementById("canvas");
  const ctx = c.getContext("2d");

}

window.addEventListener('DOMContentLoaded', getComments, false);


let pictureNumber = 0;

/** 
 * Cycles through 3 different pictures of myself.
 * TODO: Add the button to call this method.
 */
function cyclePictures() {
  const headerSelfie = document.getElementById("gallery-picture");
  const imageNames = ["campus", "suit", "main-selfie"];
  headerSelfie.src = `images/${imageNames[pictureNumber++ % imageNames.length]}.jpg`;
}
