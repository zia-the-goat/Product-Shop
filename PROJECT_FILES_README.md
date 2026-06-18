# Product Shop Android Project Documentation

This document provides a detailed explanation of each file in the project, including its purpose, logic, structure, error handling, and alignment with the project's UI/UX research.

---

## 1. Core Configuration Files

### [build.gradle.kts (Root)](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/build.gradle.kts)
*   **Purpose:** Defines top-level build configurations and shared plugins for the entire project.
*   **Logic:** It uses the `alias` syntax for version catalogs (libs) to manage dependencies centrally.
*   **Structure:** Standard Gradle Kotlin Script. It applies common plugins like Android Application, Kotlin Compose, and Google Services but with `apply false` to allow sub-modules to configure them specifically.
*   **Error Handling:** Gradle handles build-time configuration errors.

### [settings.gradle.kts](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/settings.gradle.kts)
*   **Purpose:** Configures the project structure and plugin/dependency repositories.
*   **Logic:** It defines where to find plugins and libraries (Google, MavenCentral, Gradle Plugin Portal).
*   **Structure:** Uses `dependencyResolutionManagement` to enforce repository settings across all modules (`FAIL_ON_PROJECT_REPOS`).
*   **Error Handling:** Gradle handles sync errors if repositories are unreachable or plugins are missing.

### [app/build.gradle.kts](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/build.gradle.kts)
*   **Purpose:** The main build configuration for the Android application module.
*   **Logic:** 
    *   Defines `applicationId`, `minSdk` (24), and `targetSdk` (36).
    *   Includes logic for `ndk` filters to support specific architectures (`arm64-v8a`, `armeabi-v7a`).
    *   Uses `resolutionStrategy` to force specific versions of libraries (DataStore, CameraX, Graphics) to resolve dependency conflicts, specifically targeting stability in JNI library loading.
*   **Structure:** Standard module-level Gradle script. Includes a comprehensive list of dependencies including Jetpack Compose, Retrofit, Firebase, CameraX, ML Kit, and TensorFlow Lite.
*   **Error Handling:** Build-time dependency resolution and compilation error reporting.
*   **UI/UX Alignment:** Includes `libs.plugins.kotlin.compose`, enabling the Material You (Material 3) UI framework as recommended in the research document.

---

## 2. Data Layer (Models & Services)

### [data/model/AuthDto.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/data/model/AuthDto.kt)
*   **Purpose:** Data Transfer Objects (DTOs) for authentication-related network requests and responses.
*   **Logic:** Simple data classes mapping JSON fields for Signup, Customer creation, and Login results.
*   **Structure:** Contains `SignupRequest`, `CreateCustomerDto`, and `LoginResult`.
*   **Error Handling:** `LoginResult` explicitly contains an `errorMessage` field to handle server-side errors gracefully in the UI.

### [data/model/KycDto.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/data/model/KycDto.kt)
*   **Purpose:** Models for KYC (Know Your Customer) status, document uploads, and customer profile details.
*   **Logic:**
    *   `KycDto`: Uses traffic light indicators (`red`, `amber`, `green`) for tax compliance.
    *   `DocumentDto`: Handles Base64 strings for images/PDFs.
    *   `CustomerDto`: A complex model representing the user profile, including account types and subscription info.
*   **Structure:** Grouped DTOs for KYC, Documents, and Subscriptions.
*   **Error Handling:** Nullable types (e.g., `id: Long?`) allow for flexible state representation before objects are persisted or when optional data is missing.

### [data/model/ProductDto.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/data/model/ProductDto.kt)
*   **Purpose:** Model representing a product in the shop.
*   **Logic:** Contains basic product information: ID, name, description, price, and image URL.
*   **Structure:** Simple data class.
*   **Empty States:** Absence of products in a list would be represented by an empty list of these DTOs.

### [data/remote/AuthService.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/data/remote/AuthService.kt)
*   **Purpose:** Retrofit interface for authentication API endpoints.
*   **Logic:** Defines `POST` requests for token generation (`login`) and token validation.
*   **Structure:** Standard Retrofit interface using `suspend` functions for asynchronous execution.
*   **Error Handling:** Uses HTTP status codes and `LoginResult.errorMessage`.

### [data/remote/CustomerService.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/data/remote/CustomerService.kt)
*   **Purpose:** API interface for customer registration and retrieval.
*   **Logic:** Handles customer creation and lookup by email.
*   **Structure:** Retrofit interface.
*   **Error Handling:** Relies on Retrofit's exception handling for network/parsing errors.

### [data/remote/KycService.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/data/remote/KycService.kt)
*   **Purpose:** API interface for managing KYC status.
*   **Logic:** `GET` and `POST` methods for fetching and updating KYC info.
*   **Structure:** Retrofit interface.
*   **Error Handling:** Uses `@Header("Authorization")` for secure access, failing if the token is invalid.

### [data/remote/ProductService.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/data/remote/ProductService.kt)
*   **Purpose:** API interface for products and subscriptions.
*   **Logic:** Fetches a general product list and customer-specific subscriptions.
*   **Structure:** Retrofit interface.
*   **UI/UX Alignment:** Supports the "Discover" and "Subscriptions" screens identified in the research document.

### [data/remote/ProfileService.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/data/remote/ProfileService.kt)
*   **Purpose:** API interface for profile management and document uploads.
*   **Logic:** Handles fetching the full profile and adding/getting KYC documents.
*   **Structure:** Retrofit interface.
*   **Error Handling:** Asynchronous calls return DTOs or throw exceptions handled by ViewModels.

### [data/remote/RetrofitManager.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/data/remote/RetrofitManager.kt)
*   **Purpose:** Centralized configuration and initialization for all Retrofit services.
*   **Logic:**
    *   Builds a single `Retrofit` instance with a base URL and `GsonConverterFactory`.
    *   Exposes instances of all service interfaces (`authService`, `productService`, etc.).
*   **Structure:** Singleton-like manager class.
*   **Error Handling:** Base URL configuration is centralized, making it easier to switch between environments (e.g., dev, prod).

---

## 3. Security Layer

### [security/SecurityManager.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/security/SecurityManager.kt)
*   **Purpose:** Manages encrypted storage for user credentials and biometric/face data.
*   **Logic:**
    *   Uses `EncryptedSharedPreferences` for secure data persistence.
    *   Handles saving/retrieving email and password.
    *   Saves Face embeddings (FloatArray) as JSON using `Gson`.
    *   Checks for biometric availability using `BiometricManager`.
*   **Structure:** A context-aware manager class using AES256 encryption.
*   **Error Handling:** Provides methods like `isFaceAuthSetup()` and `isBiometricAvailable()` to check states before performing operations, preventing crashes on unsupported devices.
*   **UI/UX Alignment:** Implements "Trust & Security" guidelines from the research document by using industry-standard encryption for sensitive data.

### [security/SessionManager.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/security/SessionManager.kt)
*   **Purpose:** In-memory storage for the active user session token.
*   **Logic:** A simple singleton holding the `bearerToken`.
*   **Structure:** Kotlin `object` for global access.
*   **Error Handling:** Token can be null, indicating no active session. `clearSession()` is used for logout.

### [security/facerecog/FaceAnalyzer.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/security/facerecog/FaceAnalyzer.kt)
*   **Purpose:** Processes camera frames to detect and crop faces using ML Kit.
*   **Logic:**
    *   Implements `ImageAnalysis.Analyzer` from CameraX.
    *   Uses ML Kit `FaceDetection` to find face bounds.
    *   Converts `ImageProxy` to `Bitmap`, rotates it correctly, and crops the detected face region.
*   **Structure:** Callback-based analyzer.
*   **Error Handling:** `addOnFailureListener` triggers an `onError` callback. Closes `imageProxy` in `onCompleteListener` to prevent memory leaks.
*   **UI/UX Alignment:** Part of the "Mobile UX & Gestures" and "Security" best practices, providing a modern biometric login experience.

### [security/facerecog/FaceNetModel.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/security/facerecog/FaceNetModel.kt)
*   **Purpose:** Generates unique facial embeddings using a TensorFlow Lite model.
*   **Logic:**
    *   Loads `mobilefacenet.tflite` from assets.
    *   Preprocesses Bitmaps: Resizes to 112x112, normalizes pixel values to [-1, 1], and converts to `ByteBuffer`.
    *   Runs inference to produce a 128-dimensional `FloatArray`.
*   **Structure:** Wrapper around TFLite `Interpreter`.
*   **Error Handling:** If the model fails to load, it returns a random "mock" embedding to allow the app to continue functioning (though login will fail), and logs the error.
*   **UI/UX Alignment:** High-performance ML implementation ensuring a smooth, low-latency user experience.

### [security/facerecog/FaceRecognitionUtils.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/security/facerecog/FaceRecognitionUtils.kt)
*   **Purpose:** Mathematical utilities for comparing face embeddings.
*   **Logic:** Calculates **Cosine Similarity** between two vectors.
*   **Structure:** Utility object.
*   **Functionality:** `isMatch` checks if similarity exceeds a threshold (default 0.75).
*   **Error Handling:** Returns `0f` (no match) if vectors have mismatched sizes.

---

## 4. Utility Layer

### [util/EmailService.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/util/EmailService.kt)
*   **Purpose:** Sends OTP verification emails using SMTP.
*   **Logic:** Uses `javax.mail` with Gmail's SMTP server.
*   **Structure:** Asynchronous service using `Dispatchers.IO`.
*   **Error Handling:** Catches `MessagingException` and rethrows for caller handling.
*   **UI/UX Alignment:** Supports secure onboarding and account verification flows.

### [util/OtpManager.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/util/OtpManager.kt)
*   **Purpose:** Generates and manages One-Time Passwords.
*   **Logic:**
    *   `generateOtp()` currently returns a static "000000" for easier testing/development.
    *   `saveOtpToEnv()` writes the OTP to a `.env` file for backend/external tool synchronization.
*   **Structure:** Utility object.
*   **Error Handling:** Try-catch blocks for file I/O operations.

---

## 5. Business Logic Layer (ViewModels)

### [ui/viewmodel/AuthViewModel.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/viewmodel/AuthViewModel.kt)
*   **Purpose:** Orchestrates authentication flows, including Email OTP, Signup, and Login (Biometric/Face/Password).
*   **Logic:**
    *   Manages `AuthUiState` (Idle, Loading, OtpSent, Success, Error) to drive the UI.
    *   `sendOtp`: Generates and sends OTP via `EmailService`, saving it to `.env` via `OtpManager`.
    *   `signup`: Performs a two-step registration (get token as 'signup' user, then register customer).
    *   `login`: Handles Basic Auth, saves session tokens, and fetches user profiles.
    *   Integrates `SecurityManager` for Biometric/Face matching logic.
*   **Structure:** `AndroidViewModel` to access application context for `SecurityManager`.
*   **Error Handling:** Extensive `HttpException` and `IOException` handling. Specific user-friendly error messages are provided for 400 (already registered), 401 (invalid credentials), and network failures.
*   **UI/UX Alignment:** Uses "Success States" (confetti/welcome messages) and "Error States" (friendly language) as per the research document. Supports "Biometrics" for trust.

### [ui/viewmodel/KycViewModel.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/viewmodel/KycViewModel.kt)
*   **Purpose:** Manages the KYC onboarding process and document uploads.
*   **Logic:**
    *   Fetches KYC status; handles 404 as "new user" state.
    *   `uploadKyc`: Uploads selfie and residence proof, then simulates a verification delay before updating the backend status.
    *   Includes a robust `uriToDataUriBase64` utility for image conversion.
*   **Structure:** Uses `viewModelScope` for long-running network tasks and `delay` for simulated UX.
*   **Error Handling:** Catches conversion and network errors, surfacing them via `KycUiState.Error`.
*   **UI/UX Alignment:** Implements "Skeleton Screens" (Loading state) and "Success States" after verification.

### [ui/viewmodel/ProductViewModel.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/viewmodel/ProductViewModel.kt)
*   **Purpose:** Handles fetching and processing the product catalog.
*   **Logic:**
    *   Fetches products from `ProductService`.
    *   `fixImageUrl`: A critical utility that replaces `localhost` or relative paths with the live `ngrok` base URL, ensuring images load correctly in the mobile environment.
*   **Structure:** Simple `ViewModel` with `mutableStateOf` properties.
*   **Empty States:** Defaults to `emptyList()`, which the UI can use to show empty state messages.
*   **UI/UX Alignment:** Powers the "Discover" screen; supports "Loading States" via `isLoading`.

### [ui/viewmodel/SubscriptionViewModel.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/viewmodel/SubscriptionViewModel.kt)
*   **Purpose:** Manages customer-specific product subscriptions.
*   **Logic:** Fetches subscriptions using the `profileId` stored in `KycViewModel`.
*   **Structure:** Uses a `sealed interface` for UI states to ensure exhaustive state handling.
*   **Error Handling:** Surfacing backend errors to the user.
*   **UI/UX Alignment:** Implements requirements for the "Subscriptions" screen from the core research document.

---

## 6. UI Components

### [ui/components/ShimmerEffect.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/components/ShimmerEffect.kt)
*   **Purpose:** Provides a "Skeleton Screen" loading animation.
*   **Logic:** 
    *   Uses `rememberInfiniteTransition` to animate a linear gradient.
    *   The gradient shifts from left to right, creating a "shimmering" light effect over grey placeholders.
*   **Structure:** Composable function `ShimmerItem` representing a generic card layout.
*   **UI/UX Alignment:** Directly implements "Loading States: Skeleton Screens (Shimmer)" from Section 4 of the `ANDROID_UI_UX_RESEARCH.md` to improve perceived performance.

---

## 7. App Entry & Navigation

### [MainActivity.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/MainActivity.kt)
*   **Purpose:** The single activity of the application, serving as the entry point and hosting the navigation logic.
*   **Logic:**
    *   Defines a custom Material 3 theme (`AppBlueTheme`) using the color palette from the research document.
    *   Uses a simple state-based navigation system (`ProductApp` composable with a `currentScreen` enum).
    *   Initializes and provides ViewModels to the entire screen hierarchy.
*   **Structure:** `FragmentActivity` (required for BiometricPrompt) containing the `ProductApp` navigation controller.
*   **Error Handling:** Navigation is centralized, ensuring that invalid states (like a missing product ID on the Detail screen) are handled by state checks.
*   **UI/UX Alignment:** Implements Section 13 (Visual Design System) by applying a consistent 8dp-based spacing and a modern Material 3 color system.

---

## 8. UI Screens

### [ui/screens/SplashScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/SplashScreen.kt)
*   **Purpose:** Initial branding screen with an interactive security-themed gate.
*   **Logic:**
    *   Features a "Bubble Background" where bubbles move, pop on tap, and spawn on drag.
    *   Requires a long-press on a Fingerprint icon to progress, simulating a biometric check.
*   **Structure:** Uses `rememberInfiniteTransition` and `Canvas` for performance-optimized animations.
*   **Error Handling:** Uses `semantics` for accessibility so screen readers understand the interaction requirement.
*   **UI/UX Alignment:** Implements Section 1 ("Value proposition") and Section 10 ("Gestures") by providing a delightful, interactive entry point.

### [ui/screens/LandingScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/LandingScreen.kt)
*   **Purpose:** Welcomes the user and offers entry paths (Login, Signup, or Guest).
*   **Logic:** Simple layout with clear Call-to-Action (CTA) buttons.
*   **Structure:** Uses a vertical gradient and prominent branding.
*   **UI/UX Alignment:** Follows Section 1 (Landing Screen) by prioritizing prominent login/signup actions and a low-friction "Guest" mode.

### [ui/screens/DiscoverScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/DiscoverScreen.kt)
*   **Purpose:** The central hub for product discovery, categories, and account navigation.
*   **Logic:**
    *   Implements "Search-as-you-type" via category filtering.
    *   Features a `DynamicHeroCarousel` with auto-scrolling banners.
    *   Supports sorting by price (Low-to-High/High-to-Low).
*   **Structure:** Uses `Scaffold` with a `NavigationBar` for bottom navigation (Home, Subscriptions, Account).
*   **Error Handling:** Integrates `DiscoverLoadingState` (Shimmer) and `DiscoverErrorState` (Retry button) for network robustness.
*   **UI/UX Alignment:** Adheres to Section 2 (Bottom Navigation), Section 4 (Loading States), and Section 7 (Sorting/Discovery).

### [ui/screens/FaceLoginScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/FaceLoginScreen.kt)
*   **Purpose:** Secure login screen using real-time facial recognition.
*   **Logic:**
    *   Requests `CAMERA` permission with a custom rationale dialog.
    *   Integrates `FaceAnalyzer` and `FaceNetModel` for on-device inference.
*   **Structure:** Uses `AndroidView` to host the CameraX `PreviewView`.
*   **Error Handling:** Handles permission denials gracefully and provides real-time feedback (e.g., "Face does not match").
*   **UI/UX Alignment:** Follows Section 12 (Trust & Security) by transparently explaining permission needs before the system prompt.

### [ui/screens/FaceSetupScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/FaceSetupScreen.kt)
*   **Purpose:** Enrolls the user's face for future biometric logins.
*   **Logic:** 
    *   Uses a circular mask overlay to guide face positioning.
    *   Captures a frame only when the user triggers "START SCAN".
*   **Structure:** Visual feedback via a custom `Canvas` overlay and status labels.
*   **UI/UX Alignment:** Follows Section 6 (Onboarding) by guiding the user through a technical setup process with clear instructions.

### [ui/screens/AccountScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/AccountScreen.kt)
*   **Purpose:** Displays the user's profile information, KYC status, and security settings.
*   **Logic:**
    *   Fetches the latest profile and KYC data on launch.
    *   Dynamically styles the KYC Status Card (Green for verified, Orange for unverified).
    *   Provides a conditional "Complete KYC" button if verification is missing.
*   **Structure:** Vertical list layout using Material 3 `Card` and `Surface` for menu items.
*   **UI/UX Alignment:** Adheres to Section 1 (Profile) by showing clear identity representation and stats (KYC status).

### [ui/screens/ProductDetailScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/ProductDetailScreen.kt)
*   **Purpose:** Provides an immersive, detailed view of a single product.
*   **Logic:**
    *   Features an "expandable description" (Read more/less) to manage text-heavy content.
    *   Generates product-specific benefits and requirements lists based on the `ProductType` (Insurance, Investment, or Contract).
    *   Includes a "You might also like" section with related products.
*   **Structure:** Uses a hierarchical structure with a persistent bottom bar for the primary "Add to cart" action.
*   **UI/UX Alignment:** Implements Section 1 (Detail View) with collapsible headers and primary actions in a bottom bar.

### [ui/screens/KycScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/KycScreen.kt)
*   **Purpose:** The interface for uploading identity documents.
*   **Logic:**
    *   Uses a `cameraLauncher` with `FileProvider` to capture secure, high-resolution photos.
    *   `DocumentCaptureCard` provides real-time feedback (thumbnail preview and checkmarks) once a document is captured.
*   **Error Handling:** Displays specific upload or compliance check errors at the bottom of the screen.
*   **UI/UX Alignment:** Follows Section 4 (States & Feedback) by using success dialogs with iconography and haptic-like visual cues.

### [ui/screens/SubscriptionsScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/SubscriptionsScreen.kt)
*   **Purpose:** Manages the user's active services.
*   **Logic:** Lists subscriptions in a clear, card-based `LazyColumn`.
*   **Empty States:** If no subscriptions are found, it provides a "Discover Products" CTA.
*   **UI/UX Alignment:** Directly implements Section 4 (Empty States) by explaining *why* it's empty and providing a path to discovery.

### [ui/screens/LoginScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/LoginScreen.kt)
*   **Purpose:** Secure password-based and biometric login interface.
*   **Logic:**
    *   Integrates `BiometricPrompt` for Fingerprint/FaceID login if configured.
    *   Implements real-time input validation (Dirty states).
*   **Error Handling:** Provides specific supporting text for "Username is required" and "Incorrect password" (API-driven).
*   **UI/UX Alignment:** Section 3 (Input Fields) optimization with floating labels and appropriate keyboard types.

### [ui/screens/SignupScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/SignupScreen.kt)
*   **Purpose:** Comprehensive user registration with OTP verification.
*   **Logic:**
    *   Uses a "Password Strength Monitor" to provide immediate feedback on security requirements.
    *   Features a `SingleChoiceSegmentedButtonRow` for selecting Customer Type.
*   **Structure:** Scrollable column to accommodate multiple input fields without clutter.
*   **UI/UX Alignment:** Section 3 (Dropdowns/Segmented Buttons) for faster selection of customer types.

---

## 9. Visual Theme & Styling

### [ui/theme/Color.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/theme/Color.kt)
*   **Purpose:** Defines the base color palette for the app.
*   **Logic:** Contains hex-code constants for M3 color roles.
*   **Note:** While this file exists, `MainActivity.kt` overrides these with a custom `AppBlueTheme` to match the branding requirements.

### [ui/theme/Theme.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/theme/Theme.kt)
*   **Purpose:** Configures the Material 3 Theme wrapper.
*   **Logic:** Supports "Dynamic Color" (Monet) on Android 12+ and handles Dark/Light mode switching.
*   **UI/UX Alignment:** Directly implements Section 13 (Visual Design System) for color harmony.

### [ui/theme/Type.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/theme/Type.kt)
*   **Purpose:** Defines the typography scale (Headline, Title, Body, Label).
*   **Logic:** Sets default font families, weights, and sizes.
*   **UI/UX Alignment:** Ensures consistent text rendering across the app, adhering to the project's type scale guidelines.
