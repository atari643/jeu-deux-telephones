package com.example.app.testing

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * Garde un StateFlow "chaud" pendant le test (necessaire avec SharingStarted.WhileSubscribed).
 * Ensuite on lit simplement `viewModel.uiState.value` apres chaque evenement.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T> TestScope.keepCollecting(flow: Flow<T>) {
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flow.collect {} }
}
