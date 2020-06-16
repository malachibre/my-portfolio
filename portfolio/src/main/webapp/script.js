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
    
  document.getElementById("delete-comments")
      .addEventListener("click", deleteComments);

  // This condition should work every time after the first page load. 
  if (localStorage.getItem("commentAmount")) {
    document.getElementById("comment-amount").value =
        localStorage.getItem("commentAmount");
  }

  getComments();
  
});

// Displays the content title and text in the content container.
function showContent(element) {
  const contentContainer = document.getElementById('content-container');

  Array.from(contentContainer.children).forEach(child => {
      child.classList.toggle('hidden', true);
  });

  element.classList.toggle('hidden', false);
}

// Retrieves data from the /data page and displays it. 
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

// Clears the div container holding the comments. 
function clearComments() {
  commentContainer = document.getElementById("comments");
    while(commentContainer.firstChild) {
      commentContainer.removeChild(commentContainer.firstChild);
  }
}

/** 
 * This class creates a comment and it's associated popup so it can be added to the comments div.
 */
class PageComment extends HTMLElement {
  comment = null;

  constructor(comment) {
    super();
    this.comment = comment;
  }

  connectedCallback() {
    const {postedDateTime, email, text, title, imageUrl} = this.comment;
    if (imageUrl) {
      this.innerHTML = `
        <p id="comment">${text} posted on ${postedDateTime} by ${email}</p>
        <span id="popup" class="popup-content">
          <h5>${title}</h5>
          <p>${text}</p>
          <img src="${imageUrl}">
        </span>
      `;
    } else {
        this.innerHTML = `
        <p id="comment">${text} posted on ${postedDateTime} by ${email}</p>
        <span id="popup" class="popup-content">
          <h5>${title}</h5>
          <p>${text}</p>
        </span>
        `;
    }
    ['#comment', '#popup'].forEach(selector => {
      this.querySelector(selector).addEventListener('click', () => {
        this.querySelector('#popup').classList.toggle('show');
      });
    });
  }
}

customElements.define('my-comment', PageComment);

/**
 * Creates paragraph elements and sets the text to comments 
 * pulled from /data page.
 */
function displayComment(comment) {
    document.getElementById('comments').appendChild(new PageComment(comment));
}

// Removes the comments from the page after being cleared from the Datastore. 
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
  let userEmail = "";
  fetch(`/auth`).then(response => response.text())
      .then(text => {
          text = text.replace("\n", " ");
          /** 
           * email will always be at index 3 in this array
           * because of the way /auth is laid out.
           */
          userEmail = text.split(" ")[3];
          document.getElementById("user-display").innerText = `Hello ${userEmail}!`;r
      });
  document.getElementById("logout-container").classList.remove("hidden");
  
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
