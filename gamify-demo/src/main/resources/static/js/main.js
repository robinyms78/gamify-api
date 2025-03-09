/**
 * Main JavaScript file for Gamify API
 */

document.addEventListener("DOMContentLoaded", function () {
  console.log("Gamify API frontend loaded successfully");

  // Initialize any components
  initializeComponents();

  // Add event listeners
  setupEventListeners();
});

/**
 * Initialize UI components
 */
function initializeComponents() {
  // Mobile navigation toggle
  const navToggle = document.querySelector(".nav-toggle");
  if (navToggle) {
    const navMenu = document.querySelector("nav ul");
    navToggle.addEventListener("click", function () {
      navMenu.classList.toggle("show");
    });
  }

  // Initialize tooltips
  const tooltips = document.querySelectorAll("[data-tooltip]");
  tooltips.forEach((tooltip) => {
    tooltip.addEventListener("mouseenter", showTooltip);
    tooltip.addEventListener("mouseleave", hideTooltip);
  });
}

/**
 * Set up event listeners for interactive elements
 */
function setupEventListeners() {
  // Form submission handling
  const forms = document.querySelectorAll("form");
  forms.forEach((form) => {
    form.addEventListener("submit", function (event) {
      // Prevent default form submission for AJAX handling if needed
      // event.preventDefault();

      // Form validation can be added here
      if (!validateForm(form)) {
        event.preventDefault();
        return false;
      }
    });
  });

  // Button click handlers
  const actionButtons = document.querySelectorAll("[data-action]");
  actionButtons.forEach((button) => {
    button.addEventListener("click", handleActionButton);
  });
}

/**
 * Validate form inputs
 * @param {HTMLFormElement} form - The form to validate
 * @returns {boolean} - Whether the form is valid
 */
function validateForm(form) {
  let isValid = true;

  // Get all required inputs
  const requiredInputs = form.querySelectorAll("[required]");

  // Check each required input
  requiredInputs.forEach((input) => {
    if (!input.value.trim()) {
      isValid = false;
      showError(input, "This field is required");
    } else {
      clearError(input);
    }
  });

  // Email validation
  const emailInputs = form.querySelectorAll('input[type="email"]');
  emailInputs.forEach((input) => {
    if (input.value && !isValidEmail(input.value)) {
      isValid = false;
      showError(input, "Please enter a valid email address");
    }
  });

  return isValid;
}

/**
 * Show error message for an input
 * @param {HTMLElement} input - The input with an error
 * @param {string} message - The error message
 */
function showError(input, message) {
  // Clear any existing error
  clearError(input);

  // Create error message element
  const errorElement = document.createElement("div");
  errorElement.className = "error-message";
  errorElement.textContent = message;

  // Add error class to input
  input.classList.add("error");

  // Insert error message after input
  input.parentNode.insertBefore(errorElement, input.nextSibling);
}

/**
 * Clear error message for an input
 * @param {HTMLElement} input - The input to clear errors for
 */
function clearError(input) {
  // Remove error class
  input.classList.remove("error");

  // Remove any existing error message
  const errorElement = input.parentNode.querySelector(".error-message");
  if (errorElement) {
    errorElement.remove();
  }
}

/**
 * Validate email format
 * @param {string} email - The email to validate
 * @returns {boolean} - Whether the email is valid
 */
function isValidEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

/**
 * Handle action button clicks
 * @param {Event} event - The click event
 */
function handleActionButton(event) {
  const button = event.currentTarget;
  const action = button.getAttribute("data-action");

  switch (action) {
    case "delete":
      if (!confirm("Are you sure you want to delete this item?")) {
        event.preventDefault();
      }
      break;
    // Add more action handlers as needed
  }
}

/**
 * Show tooltip
 * @param {Event} event - The mouseenter event
 */
function showTooltip(event) {
  const element = event.currentTarget;
  const tooltipText = element.getAttribute("data-tooltip");

  const tooltip = document.createElement("div");
  tooltip.className = "tooltip";
  tooltip.textContent = tooltipText;

  document.body.appendChild(tooltip);

  const rect = element.getBoundingClientRect();
  tooltip.style.top = `${rect.bottom + 10}px`;
  tooltip.style.left = `${
    rect.left + rect.width / 2 - tooltip.offsetWidth / 2
  }px`;
}

/**
 * Hide tooltip
 * @param {Event} event - The mouseleave event
 */
function hideTooltip() {
  const tooltip = document.querySelector(".tooltip");
  if (tooltip) {
    tooltip.remove();
  }
}
