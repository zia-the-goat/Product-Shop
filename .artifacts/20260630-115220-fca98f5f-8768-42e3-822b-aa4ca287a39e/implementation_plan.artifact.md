# Support for Multiple Accounts (Max 5) and Account Switching - Refined

This plan outlines the changes required to allow users to sign up for multiple account types (up to 5), track the active account in `SharedPreferences`, and switch between them in the profile with proper UI feedback and data refresh.

## Proposed Changes

### [Security/Storage]

#### [SecurityManager.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/security/SecurityManager.kt)
- [DONE] Add `saveActiveAccountTypeId(id: Long)` to store the currently active account type ID in `EncryptedSharedPreferences`.
- [DONE] Add `getActiveAccountTypeId(): Long` to retrieve the stored ID (defaulting to -1).

### [Auth & Session Management]

#### [AuthViewModel.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/viewmodel/AuthViewModel.kt)
- [DONE] Update `signup` to accept `List<Long>` for `accountTypeIds`.
- [DONE] In `signup`, iterate over the list and call `retrofitManager.customerService.addAccountToCustomer` for each ID.
- [DONE] In `login`, after fetching the profile, set the default `activeAccountTypeId` if not already set.
- [DONE] Add `switchAccount(accountTypeId: Long)` to update the active account.

### [Product Fulfillment]

#### [ProductViewModel.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/viewmodel/ProductViewModel.kt)
- [DONE] In `completeFulfillment`, retrieve `activeAccountTypeId` from `SecurityManager`.
- [DONE] Use this ID to find the active `AccountTypeDto` from `currentCustomer.customerAccounts`.

### [UI Screens]

#### [SignupScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/SignupScreen.kt)
- [DONE] Modify `accountTypeId` state to be a `Set<Long>` (e.g., `selectedAccountTypeIds`).
- [DONE] Update `FilterChip` logic to allow multiple selection, capped at 5.

#### [AccountScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/AccountScreen.kt)
- [DONE] Add an "My Accounts" section.
- [NEW] Handle `AuthUiState.Success` in a `LaunchedEffect` to trigger `kycViewModel.fetchProfileAndKyc()` and `authViewModel.resetState()` after switching, ensuring the UI reflects the new active account.

## Verification Plan

### Automated Tests
- Rely on manual verification and existing sanity checks.

### Manual Verification
1. **Signup**:
   - Verify selection of up to 5 accounts.
2. **Account Switching**:
   - Tap on a different account in the Profile screen.
   - Verify that a loading state appears.
   - **Verify that the "Currently Active" label and checkmark move to the new account after the switch completes.**
   - Verify that a notification appears confirming the switch.
3. **Persistence**:
   - Restart the app and verify the active account remains the one last switched to.
4. **Fulfillment Validation**:
   - Switch active account and verify product eligibility changes accordingly.
