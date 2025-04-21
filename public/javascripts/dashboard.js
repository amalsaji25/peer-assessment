document.addEventListener("DOMContentLoaded", function () {
    // Sidebar toggle functionality
    const sidebar = document.getElementById('sidebar');
    const content = document.getElementById('content');
    const sidebarToggle = document.getElementById('sidebarToggle');
    const overlay = document.querySelector('.overlay');

    // Function to toggle sidebar
    function toggleSidebar() {
        sidebar.classList.toggle('collapsed');
        content.classList.toggle('expanded');

        // Toggle overlay on mobile
        if (window.innerWidth <= 768) {
            overlay.classList.toggle('show');
            sidebar.classList.toggle('show');
        }

        // Update toggle button icon
        const icon = sidebarToggle.querySelector('i');
        if (sidebar.classList.contains('collapsed')) {
            icon.classList.remove('bi-list');
            icon.classList.add('bi-arrow-right');
        } else {
            icon.classList.remove('bi-arrow-right');
            icon.classList.add('bi-list');
        }

        // Store sidebar state in localStorage
        localStorage.setItem('sidebarCollapsed', sidebar.classList.contains('collapsed'));
    }

    // Event listeners for sidebar toggle
    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', toggleSidebar);
    }

    // Close sidebar when clicking on overlay (mobile)
    if (overlay) {
        overlay.addEventListener('click', function () {
            if (sidebar.classList.contains('show')) {
                toggleSidebar();
            }
        });
    }

    // Responsive behavior
    function handleResize() {
        if (window.innerWidth <= 768) {
            sidebar.classList.add('collapsed');
            content.classList.add('expanded');
            sidebar.classList.remove('show');
            overlay.classList.remove('show');
        } else {
            // Check localStorage for sidebar state
            const sidebarCollapsed = localStorage.getItem('sidebarCollapsed') === 'true';

            if (sidebarCollapsed) {
                sidebar.classList.add('collapsed');
                content.classList.add('expanded');
            } else {
                sidebar.classList.remove('collapsed');
                content.classList.remove('expanded');
            }

            sidebar.classList.remove('show');
            overlay.classList.remove('show');
        }
    }

    // Initial check on page load
    handleResize();

    // Check on window resize
    window.addEventListener('resize', handleResize);

    // Course filter functionality
    const courseFilter = document.getElementById('course-filter');
    if (courseFilter) {
        courseFilter.addEventListener('change', function () {
            const selectedCourse = this.value;
            const assignmentCards = document.querySelectorAll('.assignment-card');

            assignmentCards.forEach(card => {
                const courseCode = card.querySelector('.badge').textContent.trim();

                if (selectedCourse === 'all' || courseCode === selectedCourse.toUpperCase()) {
                    card.style.display = 'block';
                } else {
                    card.style.display = 'none';
                }
            });
        });
    }

    // Helper function to show toast notifications
    window.showToast = function (message, type = "info") {
        const toastContainer = document.createElement("div");
        toastContainer.className = "position-fixed top-0 end-0 p-3";
        toastContainer.style.zIndex = "1050";

        const toastEl = document.createElement("div");
        toastEl.className = `toast align-items-center text-white bg-${type === "success" ? "success" : type === "error" ? "danger" : type === "primary" ? "primary" : "info"}`;
        toastEl.setAttribute("role", "alert");
        toastEl.setAttribute("aria-live", "assertive");
        toastEl.setAttribute("aria-atomic", "true");

        toastEl.innerHTML = `
            <div class="d-flex">
                <div class="toast-body">
                    ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        `;

        toastContainer.appendChild(toastEl);
        document.body.appendChild(toastContainer);

        // Initialize Bootstrap's toast
        const bootstrap = window.bootstrap;
        const toast = new bootstrap.Toast(toastEl, {
            autohide: true, delay: 3000
        });

        toast.show();

        // Remove from DOM after hiding
        toastEl.addEventListener('hidden.bs.toast', () => {
            document.body.removeChild(toastContainer);
        });
    };
});


