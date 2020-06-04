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

content = {
  "about": [
      "About Me", 
      "I was born on world on June 7, 2001 in Clevalnd, Ohio. My family " + 
      "moved away from Ohio when I was around 16 months old. Since we " + 
      "arrived in Georgia, we have also moved around several times. " +
      "Currently we are living in a neighboorhead around 45 miles " +
      "north of Atlanta." 
    ], 
    
  "education": [
    "Education",
    "Although I didn't always like school growing up, " +
    "I have also enjoyed learning new things about the world " +
    "and other people which pushed to start appreciating my " +
    "education more once I got to high school. The subjects I " +
    "liked the most were always math or science. I graduated " + 
    "high school in summer 2019 and am heading into my " +
    "second year as a copmuter science major at the " +
    "University of Georgia. "
    ],

  "professional": [
    "Professional",
    "I first started working when I was 17 years old at Banana Republic. " +
    "I stayed for a year until I left for college. At UGA, "+
    "I started a job working for the web team for the " +
    "College of Agricultre and Enviornmental Science. " +
    "I had to pause my work here to have time for my STEP internship over " +
    "the summer, and will resume working there in August. "    
    ],

  "facts": [
    "Random Facts",
    "Whenever someone asks for a fun fact, my go to response is how " +
    "I've broken both of my arms. Once when I was on a skateboard " +
    "and once when I was on a bike. One thing that others point " +
    "out is how I can remember the tiniest details about things. "
    ]
    
}

/* Displays the content title and text in the content container */
function showContent(contentName){
  document.getElementById("content-title").innerText = 
    content[contentName][0];
  if (contentName === "about") {
      loadIgIcon();
  } else if (contentName === "professional") {
      loadProIcons();
  } else if (contentName === "photo-gallery") {
      loadSelfies();
  }
  document.getElementById("content-text").innerText = 
    content[contentName][1];
}

/* Displays the instagra.svg */
function loadIgIcon() {
   /** instragram.svg was donloaded form flaticon.com and authored by Dave Gandy */
   const igLink = document.createElement('a');
   igLink.href = "https://www.instagram.com/malaxhib/";
   const igIcon =  document.createElement('img');
   igIcon.src = "images/instagram.svg";
   igIcon.classList.add("icon");
   igLink.appendChild(igIcon);
   document.getElementById("content-title").appendChild(igLink);
}

/** Loads the linknedin and github svg's */
function loadProIcons() {
   /** linkedin.svg was donloaded form flaticon.com and authored by Dave Gandy */
   const liLink = document.createElement('a');
   liLink.href = "https://www.linkedin.com/in/malachi-brewer-48957b15b";
   const liIcon =  document.createElement('img');
   liIcon.src = "images/linkedin.svg";
   liIcon.classList.add("icon");
   liLink.appendChild(liIcon);
   document.getElementById("content-title").appendChild(liLink); 

   /** github.svg was donloaded form flaticon.com and authored by Dave Gandy */
   const ghLink = document.createElement('a');
   ghLink.href = "https://github.com/malachibre";
   const ghIcon =  document.createElement('img');
   ghIcon.src = "images/github.svg";
   ghIcon.classList.add("icon");
   ghLink.appendChild(ghIcon);
   document.getElementById("content-title").appendChild(ghLink);
}
/**
 * Loads the first image found in the /images folder.
 */
function loadImages() {
  document.getElementById("content-title").innerText = "";  
  const selfie = document.createElement("img");
  selfie.src = "images/main-selfie.jpg";
  selfie.classList.add("selfies");
  document.getElementById("content-text").innerText = "";
  document.getElementById("content-text").appendChild(selfie);
}

document.getElementById("about-me")
  .addEventListener("click", () => showContent("about"));

document.getElementById("education")
  .addEventListener("click", () => showContent("education"));

document.getElementById("professional")
  .addEventListener("click", () => showContent("professional"));

document.getElementById("random-facts")
  .addEventListener("click", () => showContent("facts"));

document.getElementById("photo-gallery")
  .addEventListener("click", () => loadImages());
  
/** Retrieves data from the /data page and displays it. */
function getComments() {
  clearComments();
  commentAmount = document.getElementById("comment-amount").value;
  fetch('/data?comment-amount=' + commentAmount).then(response => response.json())
  .then((json) => {
    json.forEach(comment => displayComment(comment));
  });
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
 * pulled from \data page. 
 */
function displayComment(comment) {
  const commentElement = document.createElement("p");
  commentElement.innerText =
   `${comment.text} posted on: ${comment.month}/${comment.day}/${comment.year}`;
  document.getElementById("comments").appendChild(commentElement);
}

document.getElementById("submit-amount")
  .addEventListener("click", () => getComments());

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
