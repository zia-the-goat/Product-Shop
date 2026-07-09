package com.example.productshop.ui.viewmodel

import android.app.Application
import com.example.productshop.data.model.*
import com.example.productshop.data.remote.ProductService
import com.example.productshop.data.remote.RetrofitManager
import com.example.productshop.security.ProfileManager
import com.example.productshop.security.SecurityManager
import com.example.productshop.security.SessionManager
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductViewModelTest {

    private val application = mockk<Application>(relaxed = true)
    private val retrofitManager = mockk<RetrofitManager>()
    private val productService = mockk<ProductService>()
    
    private lateinit var viewModel: ProductViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        mockkConstructor(RetrofitManager::class)
        every { anyConstructed<RetrofitManager>().service } returns productService
        
        // Mock SecurityManager
        mockkConstructor(SecurityManager::class)
        every { anyConstructed<SecurityManager>().getActiveAccountTypeId() } returns 1L

        // Mock ProfileManager
        mockkConstructor(ProfileManager::class)
        every { anyConstructed<ProfileManager>().getSubscriptionProgress(any(), any(), any()) } returns 1

        // Mock SessionManager
        mockkObject(SessionManager)
        every { SessionManager.bearerToken } returns "fake_token"
        every { SessionManager.isDebugMode } returns false
        
        // Mock KycViewModel
        mockkObject(KycViewModel.Companion)
        KycViewModel.profileId = 123L

        // Mock AuthViewModel currentCustomer to avoid client-side validation failures
        mockkObject(AuthViewModel.Companion)
        AuthViewModel.currentCustomer = CustomerDto(
            id = 123L,
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            idNumber = "9202205000089",
            customerType = CustomerTypeDto(1, "INDIVIDUAL", "Desc"),
            customerAccounts = listOf(AccountTypeDto(1, "Gold Cheque Account", "Desc"))
        )

        viewModel = ProductViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `validateEligibility skips when already verified`() = runTest {
        val product = ProductDto(1, "Test Product", "Desc", 100.0, "")
        viewModel.startFulfillment(product)
        viewModel.fulfillmentUiState = FulfillmentUiState.Verified
        
        var called = false
        viewModel.validateEligibility {
            called = true
        }
        
        assertTrue(called)
        assertEquals(FulfillmentUiState.Verified, viewModel.fulfillmentUiState)
    }

    @Test
    fun `completeFulfillment fails when customer type is not eligible`() = runTest {
        val product = ProductDto(1, "Retail Short Term Insurance", "Desc", 100.0, "")
        viewModel.startFulfillment(product)
        
        // Mock AuthViewModel currentCustomer with wrong customer type
        mockkObject(AuthViewModel.Companion)
        AuthViewModel.currentCustomer = CustomerDto(
            id = 123L,
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            idNumber = "9202205000089",
            customerType = CustomerTypeDto(1, "SOLE PROP", "Desc"), // Retail needs INDIVIDUAL
            customerAccounts = listOf(AccountTypeDto(1, "Gold Cheque Account", "Desc"))
        )

        viewModel.completeFulfillment()
        
        advanceUntilIdle()
        
        assertTrue(viewModel.fulfillmentUiState is FulfillmentUiState.Error)
        val errorState = viewModel.fulfillmentUiState as FulfillmentUiState.Error
        assertEquals("Verification Incomplete", errorState.message)
        assertTrue(errorState.failedChecks.any { it.checkName == "Customer Type" })
    }

    @Test
    fun `completeFulfillment fails when account type is not eligible`() = runTest {
        val product = ProductDto(1, "Retail Short Term Insurance", "Desc", 100.0, "")
        viewModel.startFulfillment(product)
        
        // Mock AuthViewModel currentCustomer with wrong account type
        mockkObject(AuthViewModel.Companion)
        AuthViewModel.currentCustomer = CustomerDto(
            id = 123L,
            email = "test@example.com",
            firstName = "Test",
            lastName = "User",
            idNumber = "9202205000089",
            customerType = CustomerTypeDto(1, "INDIVIDUAL", "Desc"),
            customerAccounts = listOf(AccountTypeDto(1, "Savings Account", "Desc")) // Retail needs Cheque Accounts
        )

        viewModel.completeFulfillment()
        
        advanceUntilIdle()
        
        assertTrue(viewModel.fulfillmentUiState is FulfillmentUiState.Error)
        val errorState = viewModel.fulfillmentUiState as FulfillmentUiState.Error
        assertEquals("Verification Incomplete", errorState.message)
        assertTrue(errorState.failedChecks.any { it.checkName == "Account Type" })
    }

    @Test
    fun `completeFulfillment success scenario`() = runTest {
        val product = ProductDto(1, "Retail Short Term Insurance", "Desc", 100.0, "")
        viewModel.startFulfillment(product)
        
        val response = TakeUpResponse(
            subscriptionId = 999L,
            fulfilmentResultList = listOf(
                FulfilmentResultDto("KYC Check", true),
                FulfilmentResultDto("LIVING STATUS", true),
                FulfilmentResultDto("Duplicate ID Document", true),
                FulfilmentResultDto("fraud", true)
            )
        )
        
        coEvery { productService.productTakeUp(any(), any()) } returns response
        
        viewModel.completeFulfillment()
        
        advanceUntilIdle()
        
        assertTrue(viewModel.fulfillmentUiState is FulfillmentUiState.Success)
        assertEquals(999L, (viewModel.fulfillmentUiState as FulfillmentUiState.Success).subscriptionId)
    }

    @Test
    fun `completeFulfillment succeeds even when subscriptionId is null if checks pass`() = runTest {
        val product = ProductDto(1, "Retail Short Term Insurance", "Desc", 100.0, "")
        viewModel.startFulfillment(product)
        
        val response = TakeUpResponse(
            subscriptionId = null,
            fulfilmentResultList = listOf(
                FulfilmentResultDto("kyc", true),
                FulfilmentResultDto("living", true),
                FulfilmentResultDto("id", true),
                FulfilmentResultDto("fraud", true)
            )
        )
        
        coEvery { productService.productTakeUp(any(), any()) } returns response
        
        viewModel.completeFulfillment()
        
        advanceUntilIdle()
        
        // New behavior: fallback to a random ID if missing but checks passed
        assertTrue(viewModel.fulfillmentUiState is FulfillmentUiState.Success)
        val successState = viewModel.fulfillmentUiState as FulfillmentUiState.Success
        assertTrue(successState.subscriptionId in 1000L..9999L)
    }

    @Test
    fun `completeFulfillment fails when required checks fail`() = runTest {
        val product = ProductDto(1, "Retail Short Term Insurance", "Desc", 100.0, "")
        viewModel.startFulfillment(product)
        
        val response = TakeUpResponse(
            subscriptionId = null,
            fulfilmentResultList = listOf(
                FulfilmentResultDto("KYC", false, "Invalid ID"),
                FulfilmentResultDto("Living Status", true)
            )
        )
        
        coEvery { productService.productTakeUp(any(), any()) } returns response
        
        viewModel.completeFulfillment()
        
        advanceUntilIdle()
        
        assertTrue(viewModel.fulfillmentUiState is FulfillmentUiState.Error)
        val errorState = viewModel.fulfillmentUiState as FulfillmentUiState.Error
        assertEquals("Verification Incomplete", errorState.message)
        assertTrue(errorState.failedChecks.any { it.checkName == "KYC" })
    }
}
