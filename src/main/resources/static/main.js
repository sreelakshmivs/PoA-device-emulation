const fetchPoaButton = document.getElementById("fetch-poa");
const certificateButton = document.getElementById("fetch-certificate");
const provideLocationButton = document.getElementById("provide-location");
const tokenDisplay = document.getElementById("token");
const jwtDisplay = document.getElementById("jwt");
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
  hide(fetchPoaButton);
  show(certificateButton);
}

function readyToProvideLocationStage() {
  hide(certificateButton);
  hide(tokenDisplay);
  show(provideLocationButton);
}

function displayToken(token) {
  const parsedToken = parseJwt(token);
  const jsonString = JSON.stringify(parsedToken, null, 4);
  jwtDisplay.innerHTML = jsonString;
  show(tokenDisplay);
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
