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

/**
 * Adds a random greeting to the page.
 */

let greetingContainer = document.getElementById("greetingContainer");
let headerNav = document.getElementById("headerNav");


greetingContainer.addEventListener("mouseover", () => {
					headerNav.style.opacity = 1;
				} );

greetingContainer.addEventListener("mouseleave", () => {
					hideElement(headerNav);
				});
// Gradually hides an element.
function fadeAway(element) {
	let fadeInterval = setInterval( ()  => {
  		if (element.style.opacity > 0) {
 			element.style.opacity -= .1;
		} else {
			clearInterval(fadeInterval);
		}
	}, 150);
}

//Element starts to fade away after 5 seconds.
function hideElement(element) {
	setTimeout(() => {fadeAway(headerNav); }, 5000);
}

// Cycles through 3 different pictures of myself.
let pictureNumber = 0;
function cyclePictures() {
  	let headerSelfie = document.getElementById("headerSelfie");
	const imageNames = ["campus", "suit", "mainSelfie"];
  	headerSelfie.src = "images/" + imageNames[pictureNumber] + ".jpg";
  	pictureNumber++;
	if (pictureNumber === 3) pictureNumber = 0;
}
