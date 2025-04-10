// Initialize Bootstrap collapse functionality
document.addEventListener('DOMContentLoaded', function() {

    populateTermDropdown();

    // Add click event listeners for the collapsible sections
    document.getElementById('addDetailsHeader').addEventListener('click', function() {
        const icon = this.querySelector('.bi');
        icon.classList.toggle('bi-chevron-down');
        icon.classList.toggle('bi-chevron-up');
    });

    document.getElementById('dataManagementHeader').addEventListener('click', function() {
        const icon = this.querySelector('.bi');
        icon.classList.toggle('bi-chevron-down');
        icon.classList.toggle('bi-chevron-up');
    });

    // Add Professor form submission
    document.getElementById('saveUserBtn').addEventListener('click', function() {
        const form = document.getElementById('addUserForm');
        if (form.checkValidity()) {
            const formData = new FormData(form);
            const formObject = {};

            for (const [key, value] of formData.entries()) {
                formObject[key] = value;
            }

            const csrfToken = document.querySelector("input[name='csrfToken']").value;
            // Send data to backend API
            fetch('/api/create-user', {
                method: 'POST',
                headers: {
                    "Csrf-Token": csrfToken
                },
                body: formData,
                credentials: "same-origin"
            })
                .then((response) => response.json())
                .then((data) => {
                    if (data.message) {
                        alert(data.message);
                        bootstrap.Modal.getInstance(document.getElementById("addUserModal")).hide();
                        form.reset();
                    } else if (data.error) {
                        alert("Error: " + data.error);
                    }
                })
                .catch((error) => {
                    console.error("Error creating user:", error);
                    alert("Something went wrong while creating the user.");
                });
        } else {
            form.reportValidity();
        }
    });

    // Professor search functionality
    document.getElementById('searchProfessorBtn').addEventListener('click', function () {
        const professorIdInput = document.getElementById('professorId');
        const professorId = professorIdInput.value.trim();

        const infoContainer = document.getElementById('professorInfoContainer');
        const infoElement = document.getElementById('professorInfo');

        // Hide info container initially
        infoContainer.style.display = 'none';
        infoElement.innerHTML = '';

        if (professorId) {
            fetch(`/api/professor/validate/${professorId}`)
                .then(response => response.json())
                .then(data => {
                    if (data.isValid) {
                        infoElement.innerHTML = `Professor ID <strong>${data.professorId}</strong> is valid.`;
                        infoElement.classList.remove("text-danger");
                        infoElement.classList.add("text-success");
                        infoContainer.style.display = 'block';

                        // Mark input as valid
                        professorIdInput.setCustomValidity(""); // clear any previous error
                    } else {
                        infoElement.innerHTML = `Invalid Professor ID: ${data.professorId}.`;
                        infoElement.classList.remove("text-success");
                        infoElement.classList.add("text-danger");
                        infoContainer.style.display = 'block';

                        // Block form submission
                        professorIdInput.setCustomValidity("Invalid professor ID");
                    }
                })
                .catch(error => {
                    console.error("Validation error:", error);
                    infoElement.innerHTML = 'Error validating professor. Try again later.';
                    infoElement.classList.remove("text-success");
                    infoElement.classList.add("text-danger");
                    infoContainer.style.display = 'block';

                    // Block form submission
                    professorIdInput.setCustomValidity("Error validating professor");
                });
        }
    });

    // Add Course form submission
    document.getElementById('saveCourseBtn').addEventListener('click', function() {
        const form = document.getElementById('addCourseForm');
        if (form.checkValidity()) {
            const formData = new FormData(form);

            // Send data to backend API
            fetch('/api/create-course', {
                method: 'POST',
                body: formData
            })
                .then((response) => response.json())
                .then((data) => {
                    if (data.message) {
                        alert(data.message);
                        bootstrap.Modal.getInstance(document.getElementById("addCourseModal")).hide();
                        form.reset();
                    } else if (data.error) {
                        alert("Error: " + data.error);
                    }
                })
                .catch((error) => {
                    console.error("Error creating course:", error);
                    alert("Something went wrong while creating the course.");
                });
        } else {
            form.reportValidity();
        }
    });

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
                    .then(response => response.json())
                    .then(data => {
                        if (data.success) {
                            alert(data.success);
                        } else if (data.error) {
                            alert("Error: " + data.error);
                        } else {
                            alert("Unexpected response from server.");
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

    const termSelect = document.getElementById("term");

    if (termSelect) {
        const currentYear = new Date().getFullYear();
        const terms = ["Winter", "Summer", "Fall"];

        termSelect.innerHTML = terms.map(term => {
            const label = `${term} ${currentYear}`;
            return `<option value="${label}">${label}</option>`;
        }).join('');
    }

    document.getElementById("addCourseModal").addEventListener('hidden.bs.modal', function () {
        const infoContainer = document.getElementById('professorInfoContainer');
        const infoElement = document.getElementById('professorInfo');
        const professorIdInput = document.getElementById('professorId');

        // Reset professor validation UI and state
        infoContainer.style.display = 'none';
        infoElement.innerHTML = '';
        infoElement.classList.remove("text-success", "text-danger");
        professorIdInput.setCustomValidity("");
    });
});

function populateTermDropdown() {
    const termSelect = document.getElementById("term");

    if (termSelect) {
        const currentYear = new Date().getFullYear();
        const terms = ["Winter", "Summer", "Fall"];

        termSelect.innerHTML = terms.map(term => {
            const label = `${term} ${currentYear}`;
            return `<option value="${label}">${label}</option>`;
        }).join('');
    }
}
