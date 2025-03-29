document.addEventListener("DOMContentLoaded", function () {
    const uploadForms = document.querySelectorAll(".uploadForm");

    uploadForms.forEach(function(uploadForm) {

    if (uploadForm) {
        uploadForm.addEventListener("submit", function (event) {
            event.preventDefault();

            let formData = new FormData(this); // Get form data

            const fileTypeInput = uploadForm.querySelector("input[name='fileType']");
            const fileType = fileTypeInput ? fileTypeInput.value : "";

            let actionUrl = uploadForm.action;
            if (fileType) {
                actionUrl += (actionUrl.includes("?") ? "&" : "?") + "fileType=" + encodeURIComponent(fileType);
            }

            const csrfTokenInput = uploadForm.querySelector("input[name='csrfToken']");
            const csrfToken = csrfTokenInput ? csrfTokenInput.value : "";

            fetch(actionUrl, {
                method: "POST",
                body: formData,
                headers: {
                    "Csrf-Token": csrfToken // Include CSRF token in headers
                },
                credentials: "same-origin"
            })
                .then(response => response.text()) // Read response as text
                .then(text => {
                    if (text.includes("File processing failed")) {
                        alert("File processing failed! Please check your file and try again.");
                    } else {
                        alert("File uploaded successfully!");
                    }
                    uploadForm.reset();
                })
                .catch(error => {
                    console.error("Error:", error);
                    alert("An error occurred while processing the file.");
                    uploadForm.reset();
                });
        });
    }
});
});