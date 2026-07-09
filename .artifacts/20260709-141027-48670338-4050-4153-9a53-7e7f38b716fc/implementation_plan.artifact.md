# Implementation Plan - Profile Pic Cropping and Eligibility Validation

Add profile picture selection with cropping functionality to the account screen and implement eligibility validation in the product detail screen and signup screen.

## User Review Required

> [!NOTE]
> 1. **Product Eligibility Validation**: This is performed in `ProductDetailScreen` when the user clicks "Apply/Invest/Quote". It checks if the current Customer Type and active Account Type are eligible for the specific product.
> 2. **Account vs Customer Type Validation (Signup)**: This is a separate validation rule:
>    - Individual customers (ID 1) are restricted to Retail accounts (IDs 1-5).
>    - Business customers (IDs 2-4) are restricted to Commercial accounts (IDs 6-8).
>    - System type (ID 5) is unrestricted.

## Proposed Changes

### Build and Manifest

#### [AndroidManifest.xml](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/AndroidManifest.xml)
- Register `com.canhub.cropper.CropImageActivity` to enable the cropping library.

---

### UI Layer

#### [ProductDetailScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/ProductDetailScreen.kt)
- Update the action button logic to call `viewModel.validateEligibility` before proceeding to fulfillment.
- Handle `FulfillmentUiState.Error` from `ProductViewModel` to show a descriptive error dialog if the user is ineligible due to their customer or account type.

#### [AccountScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/AccountScreen.kt)
- Integrate `CropImageContract` launcher to allow cropping when updating the profile picture.
- Replace the simple image picker with the circular cropper.

#### [SignupScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/SignupScreen.kt)
- Implement validation logic to ensure the selected `customerTypeId` and `selectedAccountTypeIds` are compatible.
- Display a validation error and disable the "SEND OTP" button if an "Individual" selects a business account or a "Business" selects a retail account.

## Verification Plan

### Automated Tests
- Run `gradle_build("app:assembleDebug")` to ensure compilation is successful with the new library integration.

### Manual Verification
- **Profile Picture (Account Screen):**
    - Go to Account screen.
    - Click on the profile picture.
    - Select an image and crop it using the circular cropper.
    - Verify the cropped image is displayed and saved correctly.
- **Product Eligibility (Product Detail Screen):**
    - Log in as an Individual with a Retail account.
    - Try to apply for a "Commercial Insurance" product.
    - Verify an eligibility error dialog appears.
- **Account Type Validation (Signup Screen):**
    - Select "Individual" customer type.
    - Try to select a business account (e.g., SME Checking).
    - Verify a validation error appears and the button is disabled/shows error.
