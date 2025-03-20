document.addEventListener("DOMContentLoaded", function () {
    const loginForm = document.querySelector("form");

    console.log("Page loaded for sign in");

    loginForm.addEventListener("submit", function (event) {
        console.log("Form submitted");
        event.preventDefault(); // Prevent full page reload

        const formData = new FormData(loginForm);
        const csrfToken = document.querySelector("meta[name='csrf-token']").content;

        fetch("/login", { method: "POST", body: formData, headers: { "Csrf-Token": csrfToken } })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(errorData => {
                        console.error("Server Error Response:", errorData);
                        throw errorData;
                    });
                }
                console.log("Response obtained from server:", response);
                return response.json();
            })
            .then(data => {
                console.log("Authentication successful");
                window.location.href = data.redirectUrl;
            })
            .catch(error => {
                console.error("Error:", error);
                alert(error.errors || "Server not reachable. Please try again later.");
            });
    });
});