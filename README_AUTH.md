# Authentication & Security Architecture

This document explains the technical implementation of the authentication flow, including secure storage and biometric integration.

## 1. Storage Strategy

We distinguish between **volatile** and **non-volatile** data to balance security and usability.

### Non-Volatile Storage (Credentials)
- **Data**: User Email and Password.
- **Implementation**: `EncryptedSharedPreferences` via `androidx.security:security-crypto`.
- **Security**: 
    - Uses **hardware-backed Keystore** (on supported devices).
    - Keys are encrypted with AES-256 SIV, and values with AES-256 GCM.
    - Persists across app restarts and device reboots.
- **Purpose**: Allows the app to remember the user and re-authenticate them using biometrics without asking for the password every time.

### Volatile Storage (Bearer Token)
- **Data**: `loginAccessKey` (Bearer Token).
- **Implementation**: In-memory `object` (`SessionManager`).
- **Security**: 
    - Data lives only in the app process RAM.
    - Automatically cleared when the app is killed or the process is recreated.
- **Purpose**: Used for high-frequency API calls during a single session. Since the server returns a unique key for every login, we don't store this key permanently to avoid "stale" or "compromised" token risks.

## 2. Biometric Authentication Flow

Biometrics act as a "secure gatekeeper" to the stored credentials.

1.  **Initial Setup**:
    - The user logs in manually with Email/Password.
    - Upon success, the app securely saves the credentials to `EncryptedSharedPreferences`.
2.  **Biometric Login**:
    - On the login screen, if the device supports biometrics and credentials exist, a **Fingerprint** icon is shown.
    - Tapping the icon triggers the `BiometricPrompt`.
    - **Success**: The app retrieves the encrypted Email/Password, sends them to the `/token` endpoint, and receives a **new, unique bearer token**.
    - **Fresh Session**: This ensures that even biometric logins generate a fresh, unique session on the backend.

## 3. Technical Implementation Details

### Components
- **`SecurityManager.kt`**: Wraps `EncryptedSharedPreferences` and handles biometric availability checks (`BiometricManager`).
- **`SessionManager.kt`**: A simple singleton to hold the volatile bearer token.
- **`AuthViewModel.kt`**: 
    - Orchestrates the login/signup calls.
    - Handles the callback from the biometric prompt.
    - Ensures the token is saved to the session manager.
- **`LoginScreen.kt`**:
    - Integrates the `BiometricPrompt` UI.
    - Displays the biometric action button based on availability.

### Dependencies
- `androidx.biometric:biometric:1.1.0`
- `androidx.security:security-crypto:1.1.0`

---
*Developed by the Product Shop Engineering Team.*
