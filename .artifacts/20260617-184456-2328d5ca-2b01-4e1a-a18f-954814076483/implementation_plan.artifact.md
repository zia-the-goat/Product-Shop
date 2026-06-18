# Implementation Plan - UI/UX Adjustments

This plan outlines the changes to be made to the Product Shop application to align with the Android UI/UX Best Practices Research Report.

## Proposed Changes

### 1. Global Accessibility & Consistency
- Add `contentDescription` to all interactive and decorative elements that currently lack it.
- Ensure all screens follow the 8dp grid system for padding and margins.
- Standardize Material 3 corner radii (12dp-28dp) across all cards and buttons.

### 2. Screen-Specific Enhancements

#### [SignupScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/SignupScreen.kt)
- **Replace Dropdown**: Change the "Customer Type" dropdown (3 options) to `SegmentedButton` or `FilterChip` selection for faster input, as recommended for fewer than 5 options.

#### [DiscoverScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/DiscoverScreen.kt)
- **Pull to Refresh**: Implement `PullToRefresh` for the product grid to align with mobile UX standards.
- **Sorting**: Add a sorting chip/button near the top of the list for "Price: Low to High" or "Newest".

#### [SubscriptionsScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/SubscriptionsScreen.kt)
- **Actionable Empty State**: Update the "No active subscriptions" message with a Call to Action (CTA) button leading back to the Discover screen.

#### [KycScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/KycScreen.kt)
- **Success State**: Implement a success dialog or full-screen feedback upon successful KYC submission to celebrate the milestone.

#### [FaceLoginScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/FaceLoginScreen.kt) & [FaceSetupScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/FaceSetupScreen.kt)
- **Permission Transparency**: Add a rationale dialog explaining why Camera permission is required for face recognition *before* requesting the system permission.

#### [AccountScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/AccountScreen.kt)
- **Accessibility**: Add missing content descriptions for profile and menu icons.
- **Visuals**: Ensure consistent spacing and grouping of menu items.

## Verification Plan

### Automated Tests
- Run `gradle_build("app:assembleDebug")` to ensure no regressions in build.
- (Optional) Run UI tests if available.

### Manual Verification
- Use `render_compose_preview` for:
    - `SignupScreen` (to verify SegmentedButton)
    - `DiscoverScreen` (to verify pull-to-refresh and sorting UI)
    - `SubscriptionsScreen` (to verify empty state CTA)
    - `KycScreen` (to verify success state)
    - `FaceSetupScreen` (to verify permission rationale)
- Deploy the app to a device and manually navigate through each screen to verify the UX improvements.
