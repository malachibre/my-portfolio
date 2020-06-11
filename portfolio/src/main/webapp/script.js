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

  showCommentForm();

  const navBar = document.getElementById('nav-bar');

  Array.from(navBar.children).forEach(child => {
      child.addEventListener('click', () => {
          const targetId = child.getAttribute('data-target-id');
          const targetElement = document.getElementById(targetId);
          showContent(targetElement);
        });
      });

  document.getElementById("comment-amount")
      .addEventListener("change", getComments);

  document.getElementById("to-comment-page")
      .addEventListener("click", () => window.location.replace("/comment-constructor.jsp"))
    
  document.getElementById("delete-comments")
      .addEventListener("click", deleteComments);

  /** This condition should work every time after the first page load. */
  if (localStorage.getItem("commentAmount")) {
    document.getElementById("comment-amount").value =
        localStorage.getItem("commentAmount");
  }

  getComments();
  
});

/** Displays the content title and text in the content container. */
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

  localStorage.setItem("commentAmount",
      document.getElementById("comment-amount").value);

  commentAmount = localStorage.getItem("commentAmount");
  fetch(`/data?comment-amount=${commentAmount}`)
      .then(response => response.json())
      .then(json => {
          json.forEach(displayComment);
        });
}

/** Clears the div container holding the comments. */
function clearComments() {
  commentContainer = document.getElementById("comments");
    while(commentContainer.firstChild) {
      commentContainer.removeChild(commentContainer.firstChild);
  }
}

/** 
 * Creates paragraph elements and sets the text to comments 
 * pulled from /data page.
 * 
 * TODO: Sperate this method into 2. 
 * 1) Create the p element.
 * 2) Create the popupElement.
 */
function displayComment(comment) {
  const commentElement = document.createElement("p");
  commentElement.innerText =
      `${comment.title} posted on: ${comment.postedDateTime}`;

  const popupElement = document.createElement("span")
  popupElement.classList.add("popup-content");
  const commentTitle = document.createElement("h5");
  commentTitle.innerText = comment.title;
  const commentText = document.createElement("p");
  commentText.innerText = comment.text;
  const commentImg = document.createElement("img");
  commentImg.src = comment.imageUrl;

  popupElement.appendChild(commentTitle);
  popupElement.appendChild(commentText);

  if (comment.imageUrl) { 
    popupElement.appendChild(commentImg);
  }

  commentElement.addEventListener("click", () => {
      popupElement.classList.toggle("show");
  });

  popupElement.addEventListener("click", () => {
      popupElement.classList.toggle("show");
  });

  document.getElementById("comments").appendChild(commentElement);
  document.body.appendChild(popupElement);
}

/** Removes the comments from the page after being cleared from the Datastore. */
function deleteComments() {
  fetch("/data", {method: "DELETE"}).then(clearComments);
}

function loadCanvas() {
  const c = document.getElementById("canvas");
  const ctx = c.getContext("2d");
}


/** 
 * This method checks the login status. If the user is logged in, 
 * then the post comment button is displayed, the login buttons are hidden,
 * and the logout button is displayed.
 */
function showCommentForm() {
  fetch("/auth").then(response => response.text())
                .then(text => {
                    if (text.includes("logged in")) {
                        document.getElementById("header-login").classList.add("hidden");
                        document.getElementById("login").classList.add("hidden");
                        document.getElementById("to-comment-page").classList.remove("hidden");
                        showUser();
                    }
                });
}

function hideCommentForm() {
fetch("/auth").then(response => response.text())
              .then(text => {
                  if (text.replace("\n", "").includes("logged in")) {
                      document.getElementById("header-login").classList.add("hidden");
                      document.getElementById("login").classList.add("hidden");
                      document.getElementById("to-comment-page").classList.remove("hidden");
                      document.getElementById("logout-container").classList.add("hidden");
                  }
                });
}



/**
 * Displays logout button and the username of logged in user.
 */
function showUser() {
  document.getElementById("logout-container").classList.remove("hidden");
  document.getElementById("user-display").innerText = "Hello!"
}



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
