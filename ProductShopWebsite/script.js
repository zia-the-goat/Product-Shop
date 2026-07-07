/**
 * ProductShop Website - App Detection & Download Logic
 */

const APP_SCHEME = "productshop://open";
const APK_URL = "ProductShop.apk";
const MODAL_ID = "warning-modal";

/**
 * Handles the "Download APK" button click.
 * Tries to detect if the app is installed first.
 */
function handleDownloadClick() {
    console.log("Download clicked, checking for app...");

    // Heuristic: Try to open the app via its custom scheme.
    // If the app is installed, the browser will likely lose focus.
    const start = Date.now();

    // Attempt to launch the app
    window.location.href = APP_SCHEME;

    // After a short delay, check if we are still on the page and focused.
    setTimeout(() => {
        const elapsed = Date.now() - start;

        // If elapsed time is short, it means the browser likely didn't switch away.
        // If document still has focus, the app probably didn't open.
        if (document.hasFocus() && elapsed < 3000) {
            console.log("App not detected or failed to open. Proceeding to download.");
            initiateDownload();
        } else {
            console.log("App might be installed (browser lost focus). Showing warning.");
            showModal();
        }
    }, 1500);
}

/**
 * Directly starts the APK download.
 */
function initiateDownload() {
    const link = document.createElement('a');
    link.href = APK_URL;
    link.download = APK_URL;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

/**
 * Shows the warning modal.
 */
function showModal() {
    document.getElementById(MODAL_ID).classList.add('active');
}

/**
 * Closes the warning modal.
 */
function closeModal() {
    document.getElementById(MODAL_ID).classList.remove('active');
}

/**
 * User confirmed they want to download despite detection.
 */
function proceedWithDownload() {
    closeModal();
    initiateDownload();
}
