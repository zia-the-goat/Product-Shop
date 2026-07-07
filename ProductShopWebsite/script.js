/**
 * ProductShop Website - App Detection & Download Logic
 */

const APP_SCHEME = "productshop://open";
const APK_URL = "ProductShop.apk";
const MODAL_ID = "warning-modal";
const PACKAGE_NAME = "com.example.productshop";

/**
 * Main entry point for the download button.
 * Uses a tiered approach for app detection.
 */
async function handleDownloadClick() {
    console.log("Download clicked, starting detection flow...");

    // 1. Tier 1: Official navigator.getInstalledRelatedApps()
    const isInstalledOfficial = await checkAppInstalledOfficial();

    if (isInstalledOfficial) {
        console.log("App detected via official API.");
        showModal();
        return;
    }

    // 2. Tier 2: Heuristic check (Custom Scheme attempt)
    runHeuristicCheck();
}

/**
 * Uses the official API to detect if the app is installed.
 * Requires Digital Asset Links (assetlinks.json) to be correctly configured.
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
 * Tries to open the app via its custom scheme.
 * If the browser loses focus, we suspect the app is installed.
 */
function runHeuristicCheck() {
    const start = Date.now();

    // Attempt to launch the app
    // We use a temporary iframe or window location to avoid hard redirects
    window.location.href = APP_SCHEME;

    setTimeout(() => {
        const elapsed = Date.now() - start;

        // If elapsed time is long or document lost focus, app likely opened
        if (document.hasFocus() && elapsed < 2500) {
            console.log("App not detected via heuristic. Starting download.");
            initiateDownload();
        } else {
            console.log("App suspected via heuristic. Showing modal.");
            showModal();
        }
    }, 1200);
}

/**
 * Triggers the APK download by creating a hidden anchor.
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
 * Opens the app using its custom URI scheme.
 */
function openApp() {
    console.log("Attempting to open existing app...");
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
    closeModal();
    initiateDownload();
}
