const fetchPoaButton = document.getElementById("fetch-poa");
const certificateButton = document.getElementById("fetch-certificate");
const provideLocationButton = document.getElementById("provide-location");
const tokenDisplay = document.getElementById("token");
const providingLocationMessage = document.getElementById("providing-location");

fetchPoaButton.onclick = () => {
  fetchPoaButton.disabled = true;
  fetch("/device/fetch-poa")
    .then(() => fetch("/device/poa"))
    .then((response) => response.text())
    .then((token) => {
      certificateFetchingStage(token);
    })
    .catch(handleError);
};

certificateButton.onclick = () => {
  certificateButton.disabled = true;
  fetch("/device/fetch-certificate")
  .then(readyToProvideLocationStage)
  .catch(handleError);
};

provideLocationButton.onclick = () => {
    certificateButton.disabled = true;
    fetch("/device/provide-location")
    .then(providingLocationStage)
    .catch(handleError);
  };


function parseJwt(token) {
  try {
    return JSON.parse(atob(token.split(".")[1]));
  } catch (e) {
    console.error(e);
  }
}

function certificateFetchingStage(token) {
  displayToken(token);
  fetchPoaButton.style.display = "none";
  certificateButton.style.display = "inline-block";
}

function readyToProvideLocationStage() {
  certificateButton.style.display = "none";
  provideLocationButton.style.display = "inline-block";
  tokenDisplay.style.display = "none";
}

function displayToken(token) {
  const parsedToken = parseJwt(token);
  const jsonString = JSON.stringify(parsedToken, null, 4);
  tokenDisplay.innerHTML = jsonString;
}

function providingLocationStage() {
    provideLocationButton.style.display = "none";
    providingLocationMessage.style.display = "inline-block";
}

function handleError(e) {
  console.log(e);
  alert("Something went wrong");
}
