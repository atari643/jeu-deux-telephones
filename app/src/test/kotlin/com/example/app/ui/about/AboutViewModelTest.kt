package com.example.app.ui.about

import com.example.app.testing.MainDispatcherRule
import com.example.app.testing.keepCollecting
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Pattern : MainDispatcherRule + keepCollecting(uiState) + assert sur uiState.value.
 * Le ViewModel est cree dans @Before (APRES MainDispatcherRule), jamais en initialiseur de champ.
 */
class AboutViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: AboutViewModel

    @Before
    fun setUp() {
        viewModel = AboutViewModel()
    }

    @Test
    fun `when started then initial state has app version`() = runTest {
        keepCollecting(viewModel.uiState)

        assertEquals("1.0", viewModel.uiState.value.version)
    }
}
