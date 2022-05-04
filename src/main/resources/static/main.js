const contents = document.getElementById("contents");

const fetchPoaStage = document.getElementById("fetch-poa-stage");
const fetchCertificateStage = document.getElementById(
  "fetch-certificate-stage"
);
const provideLocationStage = document.getElementById("provide-location-stage");
const providingLocationStage = document.getElementById(
  "providing-location-stage"
);

const progressBarContainer = document.getElementById("progress-bar-container");
const fetchPoaButton = document.getElementById("fetch-poa");
const ssid = document.getElementById("ssid");
const fetchCertificateButton = document.getElementById("fetch-certificate");
const provideLocationButton = document.getElementById("provide-location");
const displayJwtButton = document.getElementById("display-jwt");
const displayJsonButton = document.getElementById("display-json");
const tokenDisplay = document.getElementById("token");
const tokenTextArea = document.getElementById("token-text");

let token = "";

fetchPoaButton.onclick = () => {
  fetchPoaButton.disabled = true;
  fetch("/device/fetch-poa")
	.then(validateResponse)
    .then(() => runProgressBar())
    .then(() => fetch("/device/poa"))
    .then((response) => response.text())
    .then((aToken) => {
      token = aToken;
      ssid.innerHTML = parseJwt(token).destinationNetworkId;
      displayTokenJwt();
      hide(fetchPoaStage);
      show(fetchCertificateStage);
    })
    .catch(handleError);
};

fetchCertificateButton.onclick = () => {
  fetchCertificateButton.disabled = true;
  fetch("/device/fetch-certificate")
    .then(validateResponse)
    .then((response) => response.json())
    .then((certificateNames) => {
      for (let i = 0; i < certificateNames.length; i++) {
        const element = document.getElementById("cert-name-" + i);
        element.innerHTML = certificateNames[i];
      }
    })
    .then(() => runProgressBar())
    .then(() => {
      hide(fetchCertificateStage);
      show(provideLocationStage);
    })
    .catch(handleError);
};

provideLocationButton.onclick = () => {
  provideLocationButton.disabled = true;
  fetch("/device/provide-location")
    .then(validateResponse)
    .then(() => runProgressBar(3000))
    .then(() => {
      hide(provideLocationStage);
      show(providingLocationStage);
    })
    .catch(handleError);
};

displayJwtButton.onclick = displayTokenJwt;
displayJsonButton.onclick = displayTokenJson;

function parseJwt(token) {
  try {
    return JSON.parse(atob(token.split(".")[1]));
  } catch (e) {
    console.error(e);
  }
}

function displayTokenJwt() {
  tokenTextArea.innerHTML = token;
}

function displayTokenJson() {
  const parsedToken = parseJwt(token);
  const jsonString = JSON.stringify(parsedToken, null, 4);
  tokenTextArea.innerHTML = jsonString;
}

function show(element) {
  element.classList.remove("d-none");
}

function hide(element) {
  element.classList.add("d-none");
}

function validateResponse(response) {
  if (!response.ok) {
	  return response.json().then(obj => {
		throw Error(obj.message);
	  });
  }
  return response;
}

function handleError(e) {
	console.log(e);
	alert(e.message);
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
