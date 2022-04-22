const contents = document.getElementById("contents");
const progressBarContainer = document.getElementById("progress-bar-container");
const fetchPoaButton = document.getElementById("fetch-poa");
const certificateButton = document.getElementById("fetch-certificate");
const provideLocationButton = document.getElementById("provide-location");
const displayJwtButton = document.getElementById("display-jwt");
const displayJsonButton = document.getElementById("display-json");
const tokenDisplay = document.getElementById("token");
const tokenTextArea = document.getElementById("token-text");
const providingLocationMessage = document.getElementById("providing-location");
let token = "";

fetchPoaButton.onclick = () => {
  fetchPoaButton.disabled = true;
  fetch("/device/fetch-poa")
    .then(() => runProgressBar())
    .then(() => fetch("/device/poa"))
    .then((response) => response.text())
    .then((aToken) => {
      token = aToken;
      certificateFetchingStage();
    })
    .catch(handleError);
};

certificateButton.onclick = () => {
  certificateButton.disabled = true;
  fetch("/device/fetch-certificate")
    .then(() => runProgressBar())
    .then(readyToProvideLocationStage)
    .catch(handleError);
};

provideLocationButton.onclick = () => {
  certificateButton.disabled = true;
  fetch("/device/provide-location")
    .then(() => runProgressBar(3000))
    .then(providingLocationStage)
    .catch(handleError);
};

displayJwtButton.onclick = displayJwt;

displayJsonButton.onclick = displayJson;

function parseJwt(token) {
  try {
    return JSON.parse(atob(token.split(".")[1]));
  } catch (e) {
    console.error(e);
  }
}

function certificateFetchingStage() {
  displayJwt(token);
  show(tokenDisplay);
  hide(fetchPoaButton);
  show(certificateButton);
}

function readyToProvideLocationStage() {
  hide(certificateButton);
  hide(tokenDisplay);
  show(provideLocationButton);
}

function displayToken() {
  show(tokenDisplay);
}

function displayJwt() {
  tokenTextArea.innerHTML = token;
}

function displayJson() {
  const parsedToken = parseJwt(token);
  const jsonString = JSON.stringify(parsedToken, null, 4);
  tokenTextArea.innerHTML = jsonString;
}

function providingLocationStage() {
  hide(provideLocationButton);
  show(providingLocationMessage);
}

function show(element) {
  element.classList.remove("d-none");
}

function hide(element) {
  element.classList.add("d-none");
}

function handleError(e) {
  console.log(e);
  alert("Something went wrong");
}

function runProgressBar(time = 1500) {
  const progressBar = $(".progress-bar");
  progressBar.css("width", "0%");
  hide(contents);
  show(progressBarContainer);
  return progressBar.animate({ width: "100%" }, time, () => {
    hide(progressBarContainer);
    show(contents);
  });
}
