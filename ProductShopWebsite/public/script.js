/**
 * ProductShop Website - App Selection & Download Logic
 */

const APP_SCHEME = "productshop://open";
const APK_URL = "ProductShop.apk";
const MODAL_ID = "warning-modal";

/**
 * Main entry point for the "Get the App" button.
 * Always shows the modal to let the user choose their action.
 */
function handleDownloadClick() {
    console.log("Get the App clicked. Showing options modal.");
    showModal();
}

/**
 * Triggers the APK download by creating a hidden anchor.
 */
function initiateDownload() {
    console.log("Starting APK download...");
    const link = document.createElement('a');
    link.href = APK_URL;
    link.download = APK_URL;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

/**
 * Opens the app using its custom URI scheme.
 * Only called if the user explicitly clicks "Open App" in the modal.
 */
function openApp() {
    console.log("User requested to open existing app.");
    closeModal();
    window.location.href = APP_SCHEME;

    // Optional: Fallback if app doesn't open after a delay
    setTimeout(() => {
        if (document.hasFocus()) {
            console.log("App likely not installed, offering download.");
            // We could show a toast or message here if needed.
        }
    }, 2500);
}

/**
 * Shows the action modal.
 */
function showModal() {
    document.getElementById(MODAL_ID).classList.add('active');
}

/**
 * Closes the modal.
 */
function closeModal() {
    document.getElementById(MODAL_ID).classList.remove('active');
}

/**
 * User chose to download the APK from the modal.
 */
function proceedWithDownload() {
    console.log("User chose to download APK.");
    closeModal();
    initiateDownload();
}
