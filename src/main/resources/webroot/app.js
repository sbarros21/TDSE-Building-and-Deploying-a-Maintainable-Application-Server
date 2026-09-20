function greet() {
    const name = document.getElementById("nameInput").value;
    const resultEl = document.getElementById("greetResult");

    resultEl.textContent = "Loading...";

    fetch("/hello?name=" + encodeURIComponent(name))
        .then(response => {
            if (!response.ok) {
                throw new Error("Request failed with status " + response.status);
            }
            return response.text();
        })
        .then(message => {
            resultEl.textContent = message;
        })
        .catch(error => {
            resultEl.textContent = "Error: " + error.message;
        });
}

function getPi() {
    const resultEl = document.getElementById("piResult");
    resultEl.textContent = "Loading...";

    fetch("/pi")
        .then(response => response.text())
        .then(value => {
            resultEl.textContent = "π = " + value;
        })
        .catch(error => {
            resultEl.textContent = "Error: " + error.message;
        });
}