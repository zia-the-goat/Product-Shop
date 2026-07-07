# Implementation Plan - Enhanced Verification for Customer and Account Types

This plan addresses the need for manual verification of customer and account types during the fulfillment process, based on the specific business rules provided.

## Proposed Changes

### [Core Logic]

Updating the fulfillment logic to strictly enforce qualifying criteria.

#### [ProductViewModel.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/viewmodel/ProductViewModel.kt)

- Update `getQualifyingConstraints` to match the business criteria for all product types:
    - **Retail Insurance**: Only `INDIVIDUAL`.
    - **Commercial Insurance**: `SOLE PROP`, `NON-PROFIT`, `CIPC`.
    - **Device Contract**: `INDIVIDUAL`, `SOLE PROP`, `NON-PROFIT`, `CIPC`.
    - **Investment Products**: Varied criteria per product.
- Refine `completeFulfillment` to ensure these checks are performed before calling the take-up endpoint.

---

### [UI / UX]

Ensuring the error UI accurately reflects the verification requirements.

#### [FulfillmentFlowScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/FulfillmentFlowScreen.kt)

- Update `FulfillmentErrorUI` to use the same check name patterns as `ProductViewModel` for consistency.
- Ensure the "Requirements Checklist" displays "Customer Type" and "Account Type" correctly based on the updated criteria.

---

## Verification Plan

### Automated Tests
- Run existing tests: `gradlew :app:testDebugUnitTest --tests "com.example.productshop.ui.viewmodel.ProductViewModelTest"`
- Update `ProductViewModelTest.kt` to include scenarios for:
    - Failing "Customer Type" check.
    - Failing "Account Type" check.
    - Success when both customer and account types match.

### Manual Verification
1. **Login** as a user with a specific customer and account type (e.g., `INDIVIDUAL` with `Savings Account`).
2. Attempt to subscribe to a product that doesn't allow these types (e.g., `Commercial Short Term Insurance`).
3. Verify that the fulfillment process stops and shows a "Verification Incomplete" error.
4. Verify that the "Requirements Checklist" correctly identifies the mismatch for "Customer Type" and/or "Account Type".
5. Attempt to subscribe to a product that **does** allow these types (e.g., `Device Contract`).
6. Verify that the fulfillment process proceeds successfully.
