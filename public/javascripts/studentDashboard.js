class ApiService {
    static async fetchStudentCourses() {
        const response = await fetch(`/api/student/courses`);
        if (!response.ok) throw new Error("Failed to fetch student courses");
        return response.json();
    }

    static async fetchStudentDashboard(courseCode) {
        const response = await fetch(`/dashboard?courseFilter=${courseCode}`, {
            method: "GET", headers: {"courseFilter": courseCode}
        });
        if (!response.ok) throw new Error("Failed to fetch dashboard");
        return response.text();
    }
}

class UiService {
    static populateCourseDropdown(courses) {
        const courseSelect = document.getElementById("course-filter");
        if (!courseSelect) return;

        const allOption = document.createElement("option");
        allOption.value = "all";
        allOption.textContent = "All Courses";
        courseSelect.appendChild(allOption);

        courses.forEach((course) => {
            const option = document.createElement("option");
            option.value = course.courseCode;
            option.textContent = `${course.courseCode}: ${course.courseName}`;
            courseSelect.appendChild(option);
        });
    }

    static updateDashboard(html) {
        const parser = new DOMParser();
        const newDoc = parser.parseFromString(html, "text/html");

        const fields = [{id: "assignment-count"}, {id: "pending-reviews"}, {id: "completed-reviews"}];

        fields.forEach(({id}) => {
            const newEl = newDoc.getElementById(id);
            const currEl = document.getElementById(id);
            if (newEl && currEl) currEl.textContent = newEl.textContent;
        });

        const assignmentList = document.querySelector(".student-assignment-list");
        const peerList = document.querySelector(".student-peer-review-list");
        const feedbackList = document.querySelector(".feedback-list");

        const newAssignmentList = newDoc.querySelector(".student-assignment-list");
        const newPeerList = newDoc.querySelector(".student-peer-review-list");
        const newFeedbackList = newDoc.querySelector(".feedback-list");

        if (assignmentList && newAssignmentList) assignmentList.innerHTML = newAssignmentList.innerHTML;

        if (peerList && newPeerList) peerList.innerHTML = newPeerList.innerHTML;

        if (feedbackList && newFeedbackList) feedbackList.innerHTML = newFeedbackList.innerHTML;
    }

    static showNotification(message, type = "success") {
        const alert = document.createElement("div");
        alert.className = `alert alert-${type}`;
        alert.role = "alert";
        alert.textContent = message;

        const container = document.getElementById("notification-container");
        container.innerHTML = "";
        container.appendChild(alert);

        setTimeout(() => alert.remove(), 3000);
    }
}

class EventHandlers {
    static async initializePage() {
        try {
            const courses = await ApiService.fetchStudentCourses();
            UiService.populateCourseDropdown(courses);

            const select = document.getElementById("course-filter");
            if (select) {
                select.addEventListener("change", async (e) => {
                    const selectedCourse = e.target.value;
                    const html = await ApiService.fetchStudentDashboard(selectedCourse);
                    UiService.updateDashboard(html);
                });
            }

            new ReviewFormHandler();

        } catch (error) {
            console.error(error);
            UiService.showNotification("Error loading dashboard", "danger");
        }
    }

}

class ReviewService {

    static async saveOrSubmitReview(reviewTaskId, formData, status) {
        const csrfToken = document.querySelector("input[name='csrfToken']")?.value || "";

        const response = await fetch(`/api/review-tasks/save-submit/${reviewTaskId}`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json', "Csrf-Token": csrfToken},
            body: JSON.stringify({...formData, status})
        });
        if (!response.ok) throw new Error("Failed to save/submit review");
    }
}

class ReviewFormHandler {
    constructor() {
        this.modal = document.getElementById('reviewModal');
        this.form = document.getElementById('review-form');
        this.loadingElement = document.getElementById('review-loading');
        this.questionsContainer = document.getElementById('review-questions-container');
        this.reviewTaskIdInput = document.getElementById('review-task-id');
        this.saveDraftBtn = document.getElementById('save-draft-btn');
        this.submitReviewBtn = document.getElementById('submit-review-btn');

        this.currentReviewTaskId = null;
        this.bsModal = null;

        this.initEventListeners();
    }

    initEventListeners() {
        document.addEventListener('click', async (e) => {
            if (e.target.classList.contains('btn') && ['Start Review', 'View Submission'].includes(e.target.textContent.trim())) {
                e.preventDefault();
                const reviewCard = e.target.closest('.card');
                if (!reviewCard) return;
                const reviewTaskId = reviewCard.dataset.reviewTaskId;
                await this.openReviewModal(reviewTaskId);
            }
        });

        this.saveDraftBtn.addEventListener('click', async () => {
            if (this.validateForm(true)) {
                try {
                    const formData = this.collectFormData();
                    await ReviewService.saveOrSubmitReview(this.currentReviewTaskId, formData, 'PENDING');
                    UiService.showNotification("Review saved as draft successfully", "success");

                    this.bsModal?.hide();

                    const html = await ApiService.fetchStudentDashboard(document.getElementById('course-filter').value);
                    UiService.updateDashboard(html);
                } catch (error) {
                    console.error(error);
                    UiService.showNotification("Error saving review: " + error.message, "danger");
                }
            }
        });

        this.submitReviewBtn.addEventListener('click', async () => {
            if (this.validateForm(false)) {
                try {
                    const formData = this.collectFormData();
                    await ReviewService.saveOrSubmitReview(this.currentReviewTaskId, formData, 'COMPLETED');
                    UiService.showNotification("Review submitted successfully", "success");

                    this.bsModal?.hide();

                    const html = await ApiService.fetchStudentDashboard(document.getElementById('course-filter').value);
                    UiService.updateDashboard(html);
                } catch (error) {
                    console.error(error);
                    UiService.showNotification("Error submitting review: " + error.message, "danger");
                }
            }
        });
    }

    async openReviewModal(reviewTaskId) {
        this.currentReviewTaskId = reviewTaskId;
        this.reviewTaskIdInput.value = reviewTaskId;

        this.questionsContainer.innerHTML = '';
        this.form.classList.add('d-none');
        this.loadingElement.classList.remove('d-none');

        this.bsModal = bootstrap.Modal.getOrCreateInstance(this.modal);
        this.bsModal.show();

        try {

            const reviewCard = document.querySelector(`.card[data-review-task-id="${reviewTaskId}"]`);
            const reviewStatus = reviewCard?.dataset?.reviewStatus || "PENDING";

            // Disable Save Draft button if already submitted
            this.saveDraftBtn.disabled = (reviewStatus === "COMPLETED");
            this.submitReviewBtn.disabled = (reviewStatus === "COMPLETED");


            const hiddenFormContent = document.getElementById(`review-form-content-${reviewTaskId}`);
            if (!hiddenFormContent) throw new Error("Form content not found for review task");

            this.questionsContainer.innerHTML = hiddenFormContent.innerHTML;

            // Hide loading, show form
            this.loadingElement.classList.add('d-none');
            this.form.classList.remove('d-none');
        } catch (error) {
            console.error(error);
            UiService.showNotification("Error loading review form", "danger");
            bsModal.hide();
        }
    }

    validateMarksInput(input) {
        const value = input.value.trim();
        const maxMarks = parseFloat(input.max);
        if (value === '') {
            input.classList.remove('is-invalid');
            return true;
        }
        const numValue = parseFloat(value);
        const isValid = !isNaN(numValue) && numValue >= 0 && numValue <= maxMarks;
        input.classList.toggle('is-invalid', !isValid);
        return isValid;
    }

    validateForm(isDraft) {
        let isValid = true;

        const marksInputs = this.form.querySelectorAll('.question-marks');
        marksInputs.forEach(input => {
            if (isDraft && input.value.trim() === '') {
                input.classList.remove('is-invalid');
                return;
            }
            if (!this.validateMarksInput(input)) isValid = false;
        });

        return isValid;
    }

    collectFormData() {
        const formData = {
            reviewTaskId: this.currentReviewTaskId, feedbacks: []
        };

        const feedbackTextareas = this.form.querySelectorAll('.question-feedback');
        feedbackTextareas.forEach(textarea => {
            const feedbackId = textarea.name.split('_')[1];
            const marksInput = this.form.querySelector(`input[name="marks_${feedbackId}"]`);
            const score = marksInput ? parseFloat(marksInput.value) : 0;

            formData.feedbacks.push({
                feedbackId: parseInt(feedbackId), marks: isNaN(score) ? 0 : score, feedback: textarea.value.trim()
            });
        });

        return formData;
    }
}

document.addEventListener("DOMContentLoaded", () => {
    EventHandlers.initializePage();
});