const fetchPoaButton = document.getElementById("fetch-poa");
const onboardButton = document.getElementById("onboard");
const tokenDisplay = document.getElementById("token");

fetchPoaButton.onclick = () => {
  fetchPoaButton.disabled = true;
  fetch("/device/fetch-poa")
    .then(() => {
      console.log("Fetched PoA!");
    })
    .then(() => fetch("/device/poa"))
    .then((response) => response.text())
    .then((token) => {
      const parsedToken = parseJwt(token);
      const jsonString = JSON.stringify(parsedToken, null, 4);
      tokenDisplay.innerHTML = jsonString;
      onboardButton.disabled = false;
    })
    .catch((e) => {
      console.log(e);
      fetchPoaButton.disabled = true;
    });
};

function parseJwt(token) {
  try {
    return JSON.parse(atob(token.split(".")[1]));
  } catch (e) {
    console.error(e);
  }
}

onboardButton.onclick = () => {
  onboardButton.disabled = true;

  fetch("/device/onboard")
    .then(() => {
      console.log("Onboarded!");
    })
    .catch((e) => {
      console.log(e);
      onboardButton.disabled = true;
    });
};
