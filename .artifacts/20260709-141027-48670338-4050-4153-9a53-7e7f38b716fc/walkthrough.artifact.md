# Walkthrough - Profile Pic Cropping and Eligibility Validation

I have implemented profile picture cropping in the Account screen and added comprehensive eligibility validation in the Product Detail and Signup screens.

## Changes

### 1. Profile Picture Cropping (Account Screen)
Integrated the `Android-Image-Cropper` library to provide a circular cropping interface when updating the profile picture.
- **File**: [AccountScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/AccountScreen.kt)
- **Feature**: Clicking the profile picture now opens a picker followed by a square/circular cropper, ensuring consistent profile image ratios.

### 2. Product Eligibility Validation (Product Detail Screen)
Updated the "Apply" flow to perform an immediate eligibility check.
- **File**: [ProductDetailScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/ProductDetailScreen.kt)
- **Feature**: Clicking "Apply/Invest/Quote" triggers `viewModel.validateEligibility`. If the user's Customer Type or active Account Type doesn't match the product requirements, a detailed error dialog is shown.

### 3. Account vs Customer Type Validation (Signup Screen)
Added proactive validation during signup to prevent mismatched configurations.
- **File**: [SignupScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/SignupScreen.kt)
- **Feature**:
    - "Individual" customers are restricted to Retail accounts.
    - "Business" customers are restricted to Commercial accounts.
    - Real-time error messages are displayed if an incompatible combination is selected, and the "SEND OTP" button is disabled until corrected.

### 4. Build and Infrastructure
- **AndroidManifest.xml**: Registered `CropImageActivity`.
- **build.gradle.kts**: Added `Android-Image-Cropper` dependency.
- **settings.gradle.kts**: Added JitPack repository.
- **gradle.properties**: Increased heap size to 4GB to handle library dexing.

## Verification Results

### Automated Tests
- Successfully ran `gradle_build("app:assembleDevDebug")`.

### Manual Verification Steps
1. **Account Screen**:
   - Log in and go to the Account screen.
   - Click the profile picture and verify the circular cropper appears.
2. **Product Detail**:
   - Attempt to apply for a "Commercial Insurance" product as an "Individual" user.
   - Verify the "Verification Incomplete" dialog appears with the reason.
3. **Signup**:
   - Start signup as an "Individual".
   - Select "SME Checking" (Commercial account).
   - Verify the red error message: "Individuals can only select Retail accounts...".
