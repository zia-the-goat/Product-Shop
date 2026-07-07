/**
 * ProductShop Website - App Detection & Download Logic
 */

const APP_SCHEME = "productshop://open";
const APK_URL = "ProductShop.apk";
const MODAL_ID = "warning-modal";
const PACKAGE_NAME = "com.example.productshop";

/**
 * Main entry point for the download button.
 * Uses only silent detection to avoid accidental app launches.
 */
async function handleDownloadClick() {
    console.log("Download clicked, checking for installed app (silent check)...");

    // Tier 1: Official navigator.getInstalledRelatedApps()
    // This is the ONLY silent way to check. If it fails or isn't supported,
    // we proceed to download rather than "testing" the URI scheme which causes the auto-open.
    const isInstalledOfficial = await checkAppInstalledOfficial();

    if (isInstalledOfficial) {
        console.log("App detected via official API. Showing warning modal.");
        showModal();
    } else {
        console.log("App not detected or API not supported. Proceeding to download.");
        initiateDownload();
    }
}

/**
 * Uses the official API to detect if the app is installed.
 * Requires Digital Asset Links (assetlinks.json) to be correctly configured.
 * This check is SILENT and does not trigger any app launch or browser prompts.
 */
async function checkAppInstalledOfficial() {
    if ('getInstalledRelatedApps' in navigator) {
        try {
            const relatedApps = await navigator.getInstalledRelatedApps();
            return relatedApps.some(app => app.id === PACKAGE_NAME);
        } catch (error) {
            console.warn("getInstalledRelatedApps failed:", error);
            return false;
        }
    }
    return false;
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
 * This is ONLY called if the user explicitly clicks "Open App" in the modal.
 */
function openApp() {
    console.log("User explicitly requested to open existing app.");
    closeModal();
    window.location.href = APP_SCHEME;
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
 * Proceeds with download from the modal.
 */
function proceedWithDownload() {
    console.log("User chose to download anyway.");
    closeModal();
    initiateDownload();
}
