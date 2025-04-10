// API Service - Centralized API calls
class ApiService {
    static async fetchTerm() {
        const response = await fetch("/api/terms")
        if (!response.ok) {
            throw new Error(`Failed to fetch terms: ${response.status}`)
        }
        return response.json()
    }

    static async fetchCourses(term = "all") {
        const response = await fetch(`/api/courses/${term}`)
        if (!response.ok) {
            throw new Error(`Failed to fetch courses: ${response.status}`)
        }
        return response.json()
    }

    static async fetchAssignment(assignmentId) {
        const response = await fetch(`/api/assignments/${assignmentId}`)
        if (!response.ok) {
            throw new Error(`Failed to fetch assignment: ${response.status}`)
        }
        return response.json()
    }

    static async fetchAssignmentsForCourse(courseCode) {
        const response = await fetch(`/api/courses/${courseCode}/assignments`)
        if (!response.ok) {
            throw new Error(`Failed to fetch assignments: ${response.status}`)
        }
        return response.json()
    }

    static async fetchSubmissionsOverview(assignmentId) {
        const response = await fetch(`/api/reviewTasks/overview/${assignmentId}`)
        if (!response.ok) {
            throw new Error(`Failed to fetch submissions: ${response.status}`)
        }
        return response.json()
    }

    static async createAssignment(formData) {
        const csrfToken = document.querySelector("input[name='csrfToken']")?.value || ""

        const response = await fetch("/api/assignments/create", {
            method: "POST",
            headers: {
                "Csrf-Token": csrfToken,
            },
            body: formData,
            credentials: "same-origin",
        })

        if (!response.ok) {
            throw new Error(`Failed to create assignment: ${response.status}`)
        }

        return response.json()
    }

    static async updateAssignment(assignmentId, formData) {
        const csrfToken = document.querySelector("input[name='csrfToken']")?.value || ""

        const response = await fetch(`/api/assignments/edit/${assignmentId}`, {
            method: "POST",
            headers: {
                "Csrf-Token": csrfToken,
            },
            body: formData,
            credentials: "same-origin",
        })

        if (!response.ok) {
            throw new Error(`Failed to update assignment: ${response.status}`)
        }

        return response.json()
    }

    static async deleteAssignment(assignmentId, courseCode) {
        const csrfToken = document.querySelector("input[name='csrfToken']")?.value || ""

        const response = await fetch(`/api/assignments/delete/${assignmentId}`, {
            method: "DELETE",
            headers: {
                "Content-Type": "application/json",
                "Csrf-Token": csrfToken,
            },
            body: JSON.stringify({ courseCode }),
            credentials: "same-origin",
        })

        if (!response.ok) {
            const error = await response.json().catch(() => ({
                message: `Failed with status ${response.status}`,
            }))
            throw new Error(error.message || `Failed to delete assignment: ${response.status}`)
        }

        return response.json()
    }

    static async downloadAssignmentReport(courseCode, assignmentId) {
        const url = `/api/download/report/${courseCode}/${assignmentId}`
        window.location.href = url
    }

    static async downloadStudentFeedbackReport(studentData) {
        const csrfToken = document.querySelector("input[name='csrfToken']")?.value || ""

        const response = await fetch("/api/download/studentFeedback", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Csrf-Token": csrfToken,
            },
            body: JSON.stringify(studentData),
        })

        if (!response.ok) {
            const error = await response.text()
            throw new Error(error || `Failed to download student report: ${response.status}`)
        }

        // Convert response to blob and trigger download
        const blob = await response.blob()
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement("a")
        a.href = url
        a.download = `${studentData.studentName.replace(" ", "_")}_feedback.xlsx`
        document.body.appendChild(a)
        a.click()
        a.remove()
        window.URL.revokeObjectURL(url)
    }
}

// UI Service - DOM manipulation and UI updates
class UiService {
    static populateTermDropdowns(terms, selectedTerm = null) {
        const termSelect = document.querySelectorAll("#term-filter")

        termSelect.forEach((select) => {
            // Preserve first option if it's a placeholder
            const firstOption = select.querySelector("option:first-child")
            select.innerHTML = ""

            if (firstOption && firstOption.value === "") {
                select.appendChild(firstOption)
            }

            // Add term options
            terms.forEach((term) => {
                const option = document.createElement("option")
                option.value = term
                option.textContent = `${term} `
                select.appendChild(option)
            })

            // Set selected value if applicable
            if (select.id === "term-filter") {
                const storedCourseCode = sessionStorage.getItem("selectedTerm")
                if (storedCourseCode && storedCourseCode !== "all") {
                    select.value = storedCourseCode
                }
            }

            if (selectedTerm && select.id === "edit-term-select") {
                select.value = selectedTerm
            }
        })
    }

    static populateCourseDropdowns(courses, selectedCourseCode = null) {
        const courseSelects = document.querySelectorAll(
            "#course-select, #course-filter, #edit-course-select, #export-course-select, #delete-course-select ,#template-course-filter"
        )

        courseSelects.forEach((select) => {
            // Preserve first option if it's a placeholder
            const firstOption = select.querySelector("option:first-child")
            select.innerHTML = ""

            if (firstOption && firstOption.value === "") {
                select.appendChild(firstOption)
            }

            // Add "All Courses" option for filter dropdown
            if (select.id === "course-filter") {
                const allOption = document.createElement("option")
                allOption.value = "all"
                allOption.textContent = "All Courses"
                select.appendChild(allOption)
            }

            // Add course options
            courses.forEach((course) => {
                const option = document.createElement("option")
                option.value = `${course.code} (${course.section}): ${course.name}`
                option.textContent = `${course.code} (${course.section}): ${course.name}`
                select.appendChild(option)
            })

            // Set selected value if applicable
            if (select.id === "course-filter") {
                const storedCourseCode = sessionStorage.getItem("selectedCourseCode")
                if (storedCourseCode && storedCourseCode !== "all") {
                    select.value = storedCourseCode
                }
            }

            if (selectedCourseCode && select.id === "edit-course-select") {
                select.value = selectedCourseCode
            }
        })
    }

    static createQuestionElement(question = "", marks = 0, questionId = null) {
        const questionContainer = document.createElement("div")
        questionContainer.className = "review-question mb-3"

        let questionIdHtml = ""
        if (questionId) {
            questionIdHtml = `<input type="hidden" name="questionIds[]" value="${questionId}">`
        }

        questionContainer.innerHTML = `
      ${questionIdHtml}
      <div class="d-flex align-items-center mb-2">
        <input type="text" class="form-control form-control-sm" name="reviewQuestions[]" value="${question}" placeholder="Enter a review question" required>
        <button type="button" class="btn btn-sm btn-outline-danger ms-2 rounded-circle remove-question">
          <i class="bi bi-x"></i>
        </button>
      </div>
      <div class="d-flex align-items-center">
        <label class="form-label me-2 mb-0 small">Assigned Marks:</label>
        <input type="number" class="form-control form-control-sm" name="questionMarks[]" min="0" max="100" value="${marks}" style="width: 80px;">
        <small class="text-muted ms-2">(0-100)</small>
      </div>
    `

        // Add event listeners
        const marksInput = questionContainer.querySelector("input[name='questionMarks[]']")
        marksInput.addEventListener("input", this.validateMarksInput)

        const removeBtn = questionContainer.querySelector(".remove-question")
        removeBtn.addEventListener("click", () => questionContainer.remove())

        return questionContainer
    }

    static validateMarksInput(event) {
        const input = event.target
        const value = Number.parseInt(input.value, 10)

        if (value > 100) {
            input.value = 100
        } else if (value < 0 || isNaN(value)) {
            input.value = 0
        }
    }

    static setMinimumDateForInputs() {
        const dateInputs = document.querySelectorAll('input[type="date"]')
        const today = new Date().toLocaleDateString("en-CA") // YYYY-MM-DD format

        dateInputs.forEach((input) => {
            input.min = today
        })
    }

    static showNotification(message, type = "info") {
        // Create toast container if it doesn't exist
        let toastContainer = document.querySelector(".toast-container")
        if (!toastContainer) {
            toastContainer = document.createElement("div")
            toastContainer.className = "toast-container position-fixed top-0 end-0 p-3"
            document.body.appendChild(toastContainer)
        }

        // Create notification element
        const notification = document.createElement("div")
        notification.className = `toast align-items-center text-white bg-${type === "success" ? "success" : type === "error" ? "danger" : "primary"} border-0`
        notification.setAttribute("role", "alert")
        notification.setAttribute("aria-live", "assertive")
        notification.setAttribute("aria-atomic", "true")

        notification.innerHTML = `
      <div class="d-flex">
        <div class="toast-body">${message}</div>
        <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
      </div>
    `

        toastContainer.appendChild(notification)

        // Initialize and show toast
        const toast = new bootstrap.Toast(notification, { delay: 3000 })
        toast.show()

        // Remove after hiding
        notification.addEventListener("hidden.bs.toast", () => notification.remove())
    }

    static async populateAssignmentForm(assignment, formPrefix) {
        const editModal = document.getElementById("edit-assignment-modal")
        const mode = editModal.getAttribute("data-mode")

        const modalTitle = editModal.querySelector(".modal-title")
        const termWrapper = document.getElementById("edit-term-wrapper")

        // Template mode adjustments
        if (mode === "template") {
            // Show term select
            termWrapper.classList.remove("d-none")


            // Hide the read-only course display
            document.getElementById("edit-course-display").classList.add("d-none")

            // Show course select
            document.getElementById("edit-course-select").classList.remove("d-none")
            document.getElementById("edit-course-select").disabled = true
            document.getElementById("edit-course-select").innerHTML = '<option value="">Select Course</option>'

            // Update modal heading
            modalTitle.textContent = "Create New Assignment"

            const termSelect = document.querySelector(".edit-term-select")
            const courseSelect = document.getElementById("edit-course-select")

            // Clear previous selection
            termSelect.value = ""
            courseSelect.innerHTML = '<option value="">Select Course</option>'
            courseSelect.disabled = true

            // Attach fresh event listener (ensure no duplicates)
            termSelect.addEventListener("change", async function () {
                const selectedTerm = this.value
                if (!selectedTerm) {
                    courseSelect.innerHTML = '<option value="">Select Course</option>'
                    courseSelect.disabled = true
                    return
                }

                await UiService.handleTermSelect(this, courseSelect)
            })
        } else {
            document.getElementById("edit-assignment-id").value = assignment.assignmentId
            modalTitle.textContent = "Edit Assignment"
            termWrapper.classList.add("d-none")
            termWrapper.classList.add("d-none")
            const termSelect = document.querySelector(".edit-term-select")

            termSelect.removeAttribute("required")
            termSelect.removeAttribute("name")

            document.querySelector("#edit-assignment-form button[type='submit']").textContent = "Update Assignment"
            document.getElementById("edit-assignment-id").value = assignment.assignmentId
        }

        // Populate fields
        document.getElementById(`${formPrefix}-assignment-title`).value = assignment.title
        document.getElementById(`${formPrefix}-start-date`).value = mode === "template" ? "" : assignment.startDate
        document.getElementById(`${formPrefix}-due-date`).value = mode === "template" ? "" : assignment.dueDate
        document.getElementById(`${formPrefix}-assignment-description`).value = assignment.description
        document.getElementById("edit-course-display").value =
            `${assignment.courseCode} (${assignment.courseSection}): ${assignment.title}`

        const courseSelect = document.getElementById("edit-course-select");
        const fullCourseValue = `${assignment.courseCode}:::${assignment.courseSection}:::${assignment.term}`;

        const option = document.createElement("option");
        option.value = fullCourseValue;
        option.textContent = `${assignment.courseCode} (${assignment.courseSection}): ${assignment.title}`;
        courseSelect.appendChild(option);

        courseSelect.value = fullCourseValue;

        // Clear review questions and repopulate
        const reviewContainer = document.getElementById(`${formPrefix}-review-questions`)
        reviewContainer.innerHTML = ""

        assignment.reviewQuestions.forEach((q) => {
            const questionElement = this.createQuestionElement(q.question, q.marks, formPrefix === "edit" ? q.questionId : null)
            if (q.question === "Private Comment for Professor") {
                questionElement.style.display = "none";
            }
            reviewContainer.appendChild(questionElement)
        })
    }

    static populateSubmissionsView(submissionData) {
        if (!submissionData) return

        const submissionsOverview = document.getElementById("submissions-overview")
        const groupDetails = document.getElementById("group-details")
        const groupsTableBody = document.getElementById("groups-table-body")

        // Reset view
        submissionsOverview.style.display = "block"
        groupDetails.style.display = "none"

        // Update stats
        document.getElementById("total-submissions-count").textContent = submissionData.totalSubmissions
        document.getElementById("reviews-completed-percent").textContent = submissionData.reviewsCompleted + "%"
        document.getElementById("groups-count").textContent = submissionData.groups.length

        // Clear existing groups
        groupsTableBody.innerHTML = ""

        // Add groups to table
        submissionData.groups.forEach((group) => {
            const row = document.createElement("tr")
            row.innerHTML = `
        <td>${group.groupName}</td>
        <td>${group.members.length}</td>
        <td>${group.reviewsCompleted}/${group.totalReviewTasks}</td>
        <td class="text-end">
          <button class="btn btn-sm btn-outline-primary view-group-btn" data-group-id="${group.groupId}">
            View Details
          </button>
        </td>
      `
            groupsTableBody.appendChild(row)

            // Add event listener to view group button
            const viewGroupBtn = row.querySelector(".view-group-btn")
            viewGroupBtn.addEventListener("click", () => {
                this.viewGroupDetails(group.groupId, submissionData)
            })
        })
    }

    static viewGroupDetails(groupId, submissionData) {
        if (!submissionData) return

        const group = submissionData.groups.find((g) => g.groupId === Number(groupId))
        if (!group) return

        const submissionsOverview = document.getElementById("submissions-overview")
        const groupDetails = document.getElementById("group-details")
        const membersTableBody = document.getElementById("members-table-body")

        // Update view
        submissionsOverview.style.display = "none"
        groupDetails.style.display = "block"

        // Update group name
        document.getElementById("group-name").textContent = group.userName

        // Clear existing members
        membersTableBody.innerHTML = ""

        // Add members to table
        group.members.forEach((member) => {
            const row = document.createElement("tr")
            row.innerHTML = `
        <td>${member.userName}</td>
        <td>${member.email}</td>
        <td>${member.averageFeedbackScore}</td>
        <td><span class="badge ${member.status === "COMPLETED" ? "bg-success" : "bg-danger"}">${member.status}</span></td>
        <td class="text-end">
          <button class="btn btn-sm btn-outline-secondary download-submission" data-submission-id="${member.submissionId}">
            <i class="bi bi-download"></i> Download
          </button>
        </td>
      `
            membersTableBody.appendChild(row)

            // Add download event listener
            const downloadBtn = row.querySelector(".download-submission")
            downloadBtn.addEventListener("click", async () => {
                const studentData = {
                    userId: member.userId,
                    studentName: member.userName,
                    email: member.email,
                    status: member.status,
                    averageFeedbackScore: member.averageFeedbackScore,
                    feedbacks: member.feedbacks,
                }

                try {
                    await ApiService.downloadStudentFeedbackReport(studentData)
                } catch (error) {
                    console.error("Error downloading student feedback report:", error)
                    UiService.showNotification("Download failed: " + error.message, "error")
                }
            })
        })
    }

    static async handleTermSelect(termSelect, targetCourseSelect) {
        if (!termSelect || !targetCourseSelect) return

        const term = termSelect.value
        targetCourseSelect.innerHTML = '<option value="">Select Course</option>'
        targetCourseSelect.disabled = true

        if (term) {
            try {
                // Fetch courses for the selected term
                const courses = await ApiService.fetchCourses(term)

                if (courses.length > 0) {
                    courses.forEach((course) => {
                        const option = document.createElement("option")
                        option.value = `${course.code} ::: ${course.section} ::: ${course.term}`
                        option.textContent = `${course.code} (${course.section}): ${course.name}`
                        targetCourseSelect.appendChild(option)
                    })
                    targetCourseSelect.disabled = false
                } else {
                    const option = document.createElement("option")
                    option.value = ""
                    option.textContent = "No courses found"
                    targetCourseSelect.appendChild(option)
                }
            } catch (error) {
                console.error("Error loading courses:", error)
                UiService.showNotification("Failed to load courses", "error")
            }
        }
    }

    static async handleCourseSelect(courseSelect, targetAssignmentSelect, disableButton = null) {
        if (!courseSelect || !targetAssignmentSelect) return

        const courseCode = courseSelect.value
        targetAssignmentSelect.innerHTML = '<option value="">Select Assignment</option>'
        targetAssignmentSelect.disabled = true
        if (disableButton) disableButton.disabled = true

        if (courseCode) {
            try {
                const assignments = await ApiService.fetchAssignmentsForCourse(courseCode)

                if (assignments.length > 0) {
                    assignments.forEach((assignment) => {
                        const option = document.createElement("option")
                        option.value = assignment.assignmentId
                        option.textContent = assignment.title
                        targetAssignmentSelect.appendChild(option)
                    })
                    targetAssignmentSelect.disabled = false
                } else {
                    const option = document.createElement("option")
                    option.value = ""
                    option.textContent = "No assignments found"
                    targetAssignmentSelect.appendChild(option)
                }
            } catch (error) {
                console.error("Error loading assignments:", error)
                UiService.showNotification("Failed to load assignments", "error")
            }
        }
    }
}

// Event Handlers - Centralized event handling
class EventHandlers {
    static async initializePage() {
        try {
            // Fetch courses and populate dropdowns
            const terms = await ApiService.fetchTerm()
            UiService.populateTermDropdowns(terms)
            const courses = await ApiService.fetchCourses()
            UiService.populateCourseDropdowns(courses)

            // Setup UI components
            this.setupQuestionManagement()
            this.setupFormSubmissions()
            this.setupCourseFilter()
            this.setupAssignmentManagement()
            this.setupUploadAndDownloadAndDeleteForms()
            this.setupAssignmentTypeSelection()

            const toggleBtn = document.getElementById("toggle-filter-btn");
            const filterControls = document.getElementById("filter-controls");

            toggleBtn.addEventListener("click", async function () {
                filterControls.classList.toggle("d-none");
            });

            // Set minimum date for date inputs when modals are shown
            const createAssignmentModal = document.getElementById("create-assignment-modal")
            if (createAssignmentModal) {
                createAssignmentModal.addEventListener("shown.bs.modal", () => {
                    UiService.setMinimumDateForInputs()
                })
            }

            const editAssignmentModal = document.getElementById("edit-assignment-modal")
            if (editAssignmentModal) {
                editAssignmentModal.addEventListener("shown.bs.modal", () => {
                    UiService.setMinimumDateForInputs()
                })
            }

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
                                    UiService.showNotification(data.success, "success")

                                    const parentModal = uploadForm.closest(".modal")
                                    if (parentModal) {
                                        const modalInstance = bootstrap.Modal.getInstance(parentModal)
                                        modalInstance?.hide()
                                    }

                                    setTimeout(() => {
                                        window.location.reload()
                                    }, 1000)
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

        } catch (error) {
            console.error("Error initializing page:", error)
            UiService.showNotification("Failed to initialize page: " + error.message, "error")
        }
    }

    static setupQuestionManagement() {
        // Add question button for create form
        const addQuestionBtn = document.getElementById("add-question-btn")
        const reviewQuestionsContainer = document.getElementById("review-questions")

        if (addQuestionBtn && reviewQuestionsContainer) {
            addQuestionBtn.addEventListener("click", () => {
                const questionElement = UiService.createQuestionElement()
                reviewQuestionsContainer.appendChild(questionElement)
            })
        }

        // Add question button for edit form
        const editAddQuestionBtn = document.getElementById("edit-add-question-btn")
        const editReviewQuestions = document.getElementById("edit-review-questions")

        if (editAddQuestionBtn && editReviewQuestions) {
            editAddQuestionBtn.addEventListener("click", () => {
                const questionElement = UiService.createQuestionElement()
                editReviewQuestions.appendChild(questionElement)
            })
        }

        // Setup existing marks inputs for validation
        const marksInputs = document.querySelectorAll('input[name="questionMarks[]"]')
        marksInputs.forEach((input) => {
            input.addEventListener("input", UiService.validateMarksInput)
        })
    }

    static setupFormSubmissions() {
        // Create assignment form
        const createForm = document.getElementById("create-assignment-form")

        if (createForm) {
            createForm.addEventListener("submit", async (e) => {
                e.preventDefault()

                try {
                    const formData = new FormData(createForm)
                    await ApiService.createAssignment(formData)

                    UiService.showNotification("Assignment created successfully!", "success")

                    // Close modal and trigger reload after it's fully hidden
                    const modalElement = document.getElementById("create-assignment-modal")
                    const modalInstance = bootstrap.Modal.getInstance(modalElement)

                    modalElement.addEventListener("hidden.bs.modal", () => {
                        setTimeout(() => {
                            window.location.reload()
                        }, 1000)
                    }, { once: true })
                    modalInstance.hide()
                } catch (error) {
                    console.error("Error creating assignment:", error)
                    UiService.showNotification("Failed to create assignment: " + error.message, "error")
                }
            })
        }

        // Edit assignment form
        const editForm = document.getElementById("edit-assignment-form")
        if (editForm) {
            editForm.addEventListener("submit", async (e) => {
                e.preventDefault()

                try {
                    const formData = new FormData(editForm)
                    const editModal = document.getElementById("edit-assignment-modal")
                    const mode = editModal.getAttribute("data-mode") || "edit"
                    const assignmentId = document.getElementById("edit-assignment-id").value

                    if (mode === "template") {
                        await ApiService.createAssignment(formData)

                        editModal.setAttribute("data-mode", "edit")
                        UiService.showNotification("Assignment created from template!", "success")

                        const modalElement = document.getElementById("edit-assignment-modal")
                        modalElement.addEventListener(
                            "hidden.bs.modal",
                            () => {
                                setTimeout(() => {
                                    window.location.reload()
                                }, 1000)
                            },
                            { once: true }
                        )

                        const modalInstance = bootstrap.Modal.getInstance(modalElement)
                        modalInstance.hide()
                    } else {
                        await ApiService.updateAssignment(assignmentId, formData)
                        UiService.showNotification("Assignment updated successfully!", "success")
                    }

                    // Close modal
                    const modalElement = document.getElementById("edit-assignment-modal")
                    const modal = bootstrap.Modal.getInstance(modalElement)
                    modal.hide()

                    UiService.showNotification("Assignment updated successfully!", "success")
                    window.location.reload()
                } catch (error) {
                    console.error("Error updating assignment:", error)
                    UiService.showNotification("Failed to update assignment: " + error.message, "error")
                }
            })
        }
    }

    static setupCourseFilter() {
        const courseFilter = document.getElementById("dashboard-course-filter")
        const dashboardTermSelect = document.querySelector(".dashboard-term-filter")
        if (dashboardTermSelect && courseFilter) {
            dashboardTermSelect.addEventListener("change", function () {
                const selectedTerm = this.value;
                const selectedCourse = courseFilter.value;

                if (!selectedTerm) {
                    courseFilter.innerHTML = '<option value="">Select Course</option>';
                    courseFilter.disabled = true;

                    if (!selectedCourse || selectedCourse === "") {
                        window.location.reload();
                        return;
                    }
                    return;
                }

                UiService.handleTermSelect(this, courseFilter);
            });

            courseFilter.addEventListener("change", function () {
                const selectedCourse = this.value;
                const selectedTerm = dashboardTermSelect.value;

                if (!selectedTerm || !selectedCourse) {
                    return;
                }

                // Store selection in session storage
                if (selectedCourse === "all") {
                    sessionStorage.removeItem("selectedCourseCode")
                } else {
                    sessionStorage.setItem("selectedCourseCode", selectedCourse)
                }

                // Reload dashboard with filter
                const headers = selectedCourse === "all" ? {} : { termFilter: selectedTerm, courseFilter: selectedCourse }

                fetch("/dashboard", {
                    method: "GET",
                    headers: headers,
                })
                    .then((response) => response.text())
                    .then((html) => {
                        console.log("Fetched HTML snippet:", html)
                        const parser = new DOMParser()
                        const newDoc = parser.parseFromString(html, "text/html")

                        // Replace specific parts of the DOM
                        const newAssignmentList = newDoc.querySelector(".assignment-list")
                        const newReviewList = newDoc.querySelector(".peer-assignment-list")

                        document.querySelector(".assignment-list").innerHTML = newAssignmentList.innerHTML
                        document.querySelector(".peer-assignment-list").innerHTML = newReviewList.innerHTML

                        // Update counts
                        document.getElementById("student-count").textContent = newDoc.getElementById("student-count").textContent

                        document.getElementById("assignment-count").textContent =
                            newDoc.getElementById("assignment-count").textContent

                        document.getElementById("course-count").textContent = newDoc.getElementById("course-count").textContent

                        // Re-run setup
                        EventHandlers.setupAssignmentManagement()
                        EventHandlers.setupUploadAndDownloadAndDeleteForms()
                    })
                    .catch((err) => {
                        console.error("Dashboard update failed:", err)
                        UiService.showNotification("Failed to update dashboard", "error")
                    })
            })
        }
    }

    static setupAssignmentManagement() {
        // Edit assignment buttons
        const editButtons = document.querySelectorAll(".edit-assignment-btn")
        const editAssignmentModal = new bootstrap.Modal(document.getElementById("edit-assignment-modal"))

        editButtons.forEach((button) => {
            button.addEventListener("click", async function () {
                try {
                    const assignmentId = this.dataset.assignmentId
                    const assignment = await ApiService.fetchAssignment(assignmentId)
                    const courses = await ApiService.fetchCourses()

                    UiService.populateCourseDropdowns(courses, assignment.courseCode)
                    UiService.populateAssignmentForm(assignment,"edit")
                    editAssignmentModal.show()
                } catch (error) {
                    console.error("Error loading assignment:", error)
                    UiService.showNotification("Failed to load assignment details", "error")
                }
            })
        })

        // View submissions buttons
        const viewSubmissionsButtons = document.querySelectorAll(".view-submissions-btn")
        const viewSubmissionsModal = new bootstrap.Modal(document.getElementById("view-submissions-modal"))

        viewSubmissionsButtons.forEach((button) => {
            button.addEventListener("click", async function () {
                try {
                    const assignmentId = this.dataset.assignmentId
                    const submissionData = await ApiService.fetchSubmissionsOverview(assignmentId)

                    UiService.populateSubmissionsView(submissionData)
                    viewSubmissionsModal.show()
                } catch (error) {
                    console.error("Error loading submissions:", error)
                    UiService.showNotification("Failed to load submission data", "error")
                }
            })
        })

        // Back to groups button
        const backToGroupsBtn = document.getElementById("back-to-groups-btn")
        if (backToGroupsBtn) {
            backToGroupsBtn.addEventListener("click", () => {
                document.getElementById("group-details").style.display = "none"
                document.getElementById("submissions-overview").style.display = "block"
            })
        }
    }

    static setupUploadAndDownloadAndDeleteForms() {
        // Export form setup
        const exportCourseSelect = document.getElementById("export-course-select")
        const exportAssignmentSelect = document.getElementById("export-assignment-select")
        const exportDownloadBtn = document.getElementById("export-download-btn")

        if (exportCourseSelect) {
            exportCourseSelect.addEventListener("change", async function () {
                if (this.value) {
                    try {
                        exportAssignmentSelect.disabled = false
                        exportAssignmentSelect.innerHTML = '<option value="">Select Assignment</option>'

                        const assignments = await ApiService.fetchAssignmentsForCourse(this.value)
                        assignments.forEach((assignment) => {
                            const option = document.createElement("option")
                            option.value = assignment.assignmentId
                            option.textContent = assignment.title
                            exportAssignmentSelect.appendChild(option)
                        })
                    } catch (error) {
                        console.error("Error loading assignments:", error)
                        UiService.showNotification("Failed to load assignments", "error")
                    }
                } else {
                    exportAssignmentSelect.disabled = true
                    exportAssignmentSelect.innerHTML = '<option value="">Select Assignment</option>'
                    exportDownloadBtn.disabled = true
                }
            })
        }

        if (exportAssignmentSelect) {
            exportAssignmentSelect.addEventListener("change", function () {
                exportDownloadBtn.disabled = !this.value
            })
        }

        // Delete form setup
        const deleteCourseSelect = document.getElementById("delete-course-select")
        const deleteAssignmentSelect = document.getElementById("delete-assignment-select")
        const deleteConfirmBtn = document.getElementById("delete-confirm-btn")

        if (deleteCourseSelect) {
            deleteCourseSelect.addEventListener("change", async function () {
                if (this.value) {
                    try {
                        deleteAssignmentSelect.disabled = false
                        deleteAssignmentSelect.innerHTML = '<option value="">Select Assignment</option>'

                        const assignments = await ApiService.fetchAssignmentsForCourse(this.value)
                        assignments.forEach((assignment) => {
                            const option = document.createElement("option")
                            option.value = assignment.assignmentId
                            option.textContent = assignment.title
                            deleteAssignmentSelect.appendChild(option)
                        })
                    } catch (error) {
                        console.error("Error loading assignments:", error)
                        UiService.showNotification("Failed to load assignments", "error")
                    }
                } else {
                    deleteAssignmentSelect.disabled = true
                    deleteAssignmentSelect.innerHTML = '<option value="">Select Assignment</option>'
                    deleteConfirmBtn.disabled = true
                }
            })
        }

        if (deleteAssignmentSelect) {
            deleteAssignmentSelect.addEventListener("change", function () {
                deleteConfirmBtn.disabled = !this.value
            })
        }

        // Export form submission
        const exportForm = document.getElementById("export-data-form")
        if (exportForm) {
            exportForm.addEventListener("submit", (e) => {
                e.preventDefault()
                const courseCode = exportCourseSelect.value
                const assignmentId = exportAssignmentSelect.value

                ApiService.downloadAssignmentReport(courseCode, assignmentId)
            })
        }

        // Delete form submission
        const deleteForm = document.getElementById("delete-assignment-form")
        if (deleteForm) {
            deleteForm.addEventListener("submit", async (e) => {
                e.preventDefault()
                const courseCode = deleteCourseSelect.value
                const assignmentId = deleteAssignmentSelect.value

                if (confirm("Are you sure you want to delete this assignment? This action cannot be undone.")) {
                    try {
                        await ApiService.deleteAssignment(assignmentId, courseCode)

                        // Close modal and refresh page
                        const modalElement = document.getElementById("delete-assignment-modal")
                        const modal = bootstrap.Modal.getInstance(modalElement)
                        modal.hide()

                        UiService.showNotification("Assignment deleted successfully!", "success")
                        window.location.reload()
                    } catch (error) {
                        console.error("Error deleting assignment:", error)
                        UiService.showNotification("Failed to delete assignment: " + error.message, "error")
                    }
                }
            })
        }

    }

    static setupAssignmentTypeSelection() {
        // Add styles for assignment type options

        const assignmentTypeOptions = document.querySelectorAll(".assignment-type-option")
        const assignmentTypeSelection = document.getElementById("assignment-type-selection")
        const existingAssignmentSelection = document.getElementById("existing-assignment-selection")
        const createAssignmentForm = document.getElementById("create-assignment-form")
        const backToOptionsBtn = document.getElementById("back-to-options-btn")
        const courseDropDownSelect = document.querySelectorAll(".course-filter")
        const termDropDownSelect = document.querySelectorAll(".term-filter")
        const assignmentDropDownSelect = document.querySelectorAll(".assignment-filter")
        const templateAssignmentSelect = document.getElementById("template-assignment-select")
        const useTemplateBtn = document.getElementById("use-template-btn")

        // Handle assignment type selection
        if (assignmentTypeOptions && assignmentTypeOptions.length > 0) {
            assignmentTypeOptions.forEach((option) => {
                option.addEventListener("click", function () {
                    const type = this.getAttribute("data-type")

                    if (type === "new") {
                        assignmentTypeSelection.style.display = "none"
                        createAssignmentForm.style.display = "block"
                    } else if (type === "existing") {
                        assignmentTypeSelection.style.display = "none"
                        existingAssignmentSelection.style.display = "block"
                    }
                })
            })
        }

        // Back to options button
        if (backToOptionsBtn) {
            backToOptionsBtn.addEventListener("click", () => {
                existingAssignmentSelection.style.display = "none"
                assignmentTypeSelection.style.display = "block"
            })
        }

        // Handle term selection for template
        if (termDropDownSelect && termDropDownSelect.length > 0) {
            termDropDownSelect.forEach((termSelect, index) => {
                const courseSelected = courseDropDownSelect[index]
                termSelect.addEventListener("change", function () {
                    UiService.handleTermSelect(this, courseSelected)
                })
            })
        }

        // Handle course selection for template
        if (courseDropDownSelect && courseDropDownSelect.length > 0) {
            courseDropDownSelect.forEach((courseSelect, index) => {
                const assignmentSelect = assignmentDropDownSelect[index]
                courseSelect.addEventListener("change", function () {
                    UiService.handleCourseSelect(this, assignmentSelect)
                })
            })
        }

        // Handle assignment selection for template
        if (templateAssignmentSelect) {
            templateAssignmentSelect.addEventListener("change", function () {
                useTemplateBtn.disabled = !this.value
            })
        }

        // Handle use template button click
        if (useTemplateBtn) {
            useTemplateBtn.addEventListener("click", async () => {
                console.log("Use Template button clicked!")
                const assignmentId = templateAssignmentSelect.value

                if (assignmentId) {
                    try {
                        // Fetch assignment details
                        const assignment = await ApiService.fetchAssignment(assignmentId)

                        const courses = await ApiService.fetchCourses()
                        UiService.populateCourseDropdowns(courses, assignment.courseCode)

                        const editModal = document.getElementById("edit-assignment-modal")

                        editModal.setAttribute("data-mode", "template")

                        // Populate the create assignment form with the template data
                        UiService.populateAssignmentForm(assignment, "edit")

                        // Show the create assignment form
                        existingAssignmentSelection.style.display = "none"

                        const createAssignmentModal = document.getElementById("create-assignment-modal")
                        const createModalInstance = bootstrap.Modal.getInstance(createAssignmentModal)
                        createModalInstance?.hide()

                        document.querySelector("#edit-assignment-form button[type='submit']").textContent = "Save as New Assignment"


                        const editModalInstance = bootstrap.Modal.getOrCreateInstance(editModal)
                        editModalInstance.show()
                    } catch (error) {
                        console.error("Error loading assignment details:", error)
                        UiService.showNotification("Failed to load assignment template", "error")
                    }
                }
            })
        }


        // Reset modal when it's closed
        const createAssignmentModal = document.getElementById("create-assignment-modal")
        if (createAssignmentModal) {
            createAssignmentModal.addEventListener("hidden.bs.modal", () => {
                if (assignmentTypeSelection) assignmentTypeSelection.style.display = "block"
                if (existingAssignmentSelection) existingAssignmentSelection.style.display = "none"
                if (createAssignmentForm) {
                    createAssignmentForm.style.display = "none"
                    createAssignmentForm.reset()
                }


                // Clear review questions except the first one
                const reviewQuestionsContainer = document.getElementById("review-questions")
                if (reviewQuestionsContainer) {
                    const reviewQuestions = reviewQuestionsContainer.querySelectorAll(".review-question")
                    const marksInputs = reviewQuestionsContainer.querySelectorAll(".d-flex.align-items-center:not(:first-child)")

                    for (let i = 1; i < reviewQuestions.length; i++) {
                        reviewQuestions[i].remove()
                    }

                    marksInputs.forEach((div) => div.remove())

                    // Reset the first question and marks
                    if (reviewQuestions.length > 0) {
                        reviewQuestions[0].querySelector("input").value = ""
                    }

                    const firstMarksInput = reviewQuestionsContainer.querySelector('input[name="questionMarks[]"]')
                    if (firstMarksInput) {
                        firstMarksInput.value = "0"
                    }
                }
            })
        }
    }
}

// Initialize the application when the DOM is loaded
document.addEventListener("DOMContentLoaded", () => {
    EventHandlers.initializePage()
})

// For demonstration purposes, log that the refactored code is loaded
console.log("Refactored course management code loaded successfully!")

