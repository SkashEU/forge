package com.skash.forge.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

internal actual val defaultDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO