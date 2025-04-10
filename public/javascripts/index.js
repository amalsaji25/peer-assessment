document.addEventListener("DOMContentLoaded", () => {
    const loginForm = document.getElementById("login-form")
    const continueButton = document.getElementById("continue-button")
    const continueButtonContainer = document.getElementById("continue-button-container")
    const passwordContainer = document.getElementById("password-container")
    const newPasswordContainer = document.getElementById("new-password-container")
    const submitButtonContainer = document.getElementById("submit-button-container")
    const userIdInput = document.getElementById("userId")
    const passwordInput = document.getElementById("password")
    const newPasswordInput = document.getElementById("newPassword")
    const confirmPasswordInput = document.getElementById("confirmPassword")

    let isFirstTimeUser = false

    console.log("Page loaded for sign in")

    // Handle the continue button click
    continueButton.addEventListener("click", () => {
        const userId = userIdInput.value.trim()

        if (!userId) {
            alert("Please enter a user ID")
            return
        }

        checkUserExists(userId)
    })

    // Function to check if user exists
    function checkUserExists(userId) {
        const csrfToken = document.querySelector("meta[name='csrf-token']").content

        fetch("/user-validation", {
            method: "POST", headers: {
                "Content-Type": "application/json", "Csrf-Token": csrfToken,
            }, body: JSON.stringify({userId: userId}),
        })
            .then((response) => {
                if (!response.ok) {
                    return response.json().then((errorData) => {
                        console.error("Server Error Response:", errorData)
                        throw errorData
                    })
                }
                return response.json()
            })
            .then((data) => {
                console.log("User check response:", data)

                // Hide continue button
                continueButtonContainer.style.display = "none"

                // Show submit button
                submitButtonContainer.style.display = "block"

                if (data.userExists) {
                    // Existing user - show password field
                    passwordContainer.style.display = "block"
                    passwordInput.required = true
                    isFirstTimeUser = data.firstTimeUser || false

                    if (isFirstTimeUser) {
                        // First time user - show new password fields
                        newPasswordContainer.style.display = "block"
                        newPasswordInput.required = true
                        confirmPasswordInput.required = true
                        passwordContainer.style.display = "none"
                        passwordInput.required = false
                    }
                } else {
                    // User doesn't exist
                    alert("Invalid user details. Please check and try again.")
                    continueButtonContainer.style.display = "block"
                    submitButtonContainer.style.display = "none"
                }
            })
            .catch((error) => {
                console.error("Error checking user:", error)
                alert(error.errors || "Server not reachable. Please try again later.")
                continueButtonContainer.style.display = "block"
            })
    }

    // Handle form submission
    loginForm.addEventListener("submit", (event) => {
        console.log("Form submitted")
        event.preventDefault() // Prevent full page reload

        if (isFirstTimeUser) {
            // Handle first time user password creation
            if (newPasswordInput.value !== confirmPasswordInput.value) {
                alert("Passwords do not match. Please try again.")
                return
            }

            createPassword(userIdInput.value, newPasswordInput.value)
        } else {
            // Handle regular login
            loginUser(userIdInput.value, passwordInput.value)
        }
    })

    // Function to create password for first time user
    function createPassword(userId, password) {
        const csrfToken = document.querySelector("meta[name='csrf-token']").content

        fetch("/create-password", {
            method: "POST", headers: {
                "Content-Type": "application/json", "Csrf-Token": csrfToken,
            }, body: JSON.stringify({
                userId: userId, password: password,
            }),
        })
            .then((response) => {
                if (!response.ok) {
                    return response.json().then((errorData) => {
                        console.error("Server Error Response:", errorData)
                        throw errorData
                    })
                }
                return response.json()
            })
            .then((data) => {
                console.log("Password creation successful")
                alert("Password created successfully. Please login with your credentials.")
                // Reset the form and reload the page
                window.location.reload()
            })
            .catch((error) => {
                console.error("Error creating password:", error)
                alert(error.errors || "Server not reachable. Please try again later.")
            })
    }

    // Function to login user
    function loginUser(userId, password) {
        const csrfToken = document.querySelector("meta[name='csrf-token']").content

        const formData = new FormData()
        formData.append("userId", userId)
        formData.append("password", password)

        fetch("/login", {
            method: "POST", body: formData, headers: {"Csrf-Token": csrfToken},
        })
            .then((response) => {
                if (!response.ok) {
                    return response.json().then((errorData) => {
                        console.error("Server Error Response:", errorData)
                        throw errorData
                    })
                }
                console.log("Response obtained from server:", response)
                return response.json()
            })
            .then((data) => {
                console.log("Authentication successful")
                window.location.href = data.redirectUrl
            })
            .catch((error) => {
                console.error("Error:", error)
                alert(error.errors || "Server not reachable. Please try again later.")
            })
    }
})

