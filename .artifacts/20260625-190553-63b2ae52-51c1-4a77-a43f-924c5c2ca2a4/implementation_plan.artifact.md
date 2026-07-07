# True Navigation Backstack, Support Integration, and Debug Removal

This plan fixes the navigation backstack issue by converting the `DiscoverScreen` internal tabs into true navigation destinations. It also connects the Help & Support button and removes developer/debug options.

## User Review Required

- **Navigation Architecture Change**: Currently, the tabs (Home, Subscriptions, Account) are managed by a single `selectedTab` state inside `DiscoverScreen`. This means the Android Backstack doesn't "see" tab changes. I will convert these into individual composable destinations in `NavGraph.kt` so that `navController` can manage them.
- **Backstack Behavior**: Using `navController` with `popUpTo` and `saveState`/`restoreState` will ensure that navigating from Home -> Subscriptions -> Details and hitting Back returns you to Subscriptions exactly as you left it.

## Proposed Changes

### [Navigation & Routing]

#### [NavGraph.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/navigation/NavGraph.kt)
- Add new routes: `discover_home`, `discover_subscriptions`, `discover_account`.
- Update `Discover` composable to accept a `startTab` or similar, OR split the `DiscoverScreen` into three distinct wrapper composables.
- Implement proper `NavigationItem` clicks using `popUpTo` to avoid stack bloat while preserving state.

#### [DiscoverScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/DiscoverScreen.kt)
- Remove `selectedTab` internal state.
- Accept `currentTab: Int` and `onTabSelected: (Int) -> Unit` as parameters.
- Remove internal `BackHandler` as `navController` will now handle the backstack naturally.
- Add `onNavigateToSupport: () -> Unit` parameter for the Account tab.

---

### [Account Screen Cleanup]

#### [AccountScreen.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/screens/AccountScreen.kt)
- Add `onNavigateToSupport: () -> Unit` parameter.
- Connect the "Help & Support" menu item to `onNavigateToSupport`.
- **DELETE**: Remove "Enable Debug Mode" menu item.
- **DELETE**: Remove "Developer Options" section and "Test Crash" button.

#### [KycViewModel.kt](file:///C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/ui/viewmodel/KycViewModel.kt)
- Remove `isDebugMode` state and `toggleDebugMode()` function.

## Verification Plan

### Manual Verification
1. **Navigation Flow**:
   - Start on Home -> Click "Subscriptions" tab -> Click a subscription item to see Details.
   - Press "Back". Verify you are on the "Subscriptions" tab.
   - Press "Back" again. Verify you are on the "Home" tab.
2. **Support**:
   - Go to "Account" tab -> Click "Help & Support". Verify it navigates to the Support form.
3. **Debug Cleanup**:
   - Verify the "Debug Mode" and "Developer Options" sections are gone from the Account tab.
