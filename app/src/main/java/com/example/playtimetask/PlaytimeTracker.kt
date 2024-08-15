package com.example.playtimetask

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PlaytimeTracker {

    private lateinit var sharedPreferences: SharedPreferences

    private val listener = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> start()
            Lifecycle.Event.ON_STOP -> stop()
            else -> Unit
        }
    }

    private val _playtimeState = MutableStateFlow(0L)
    val playtimeState: Flow<Long> = _playtimeState.asStateFlow()

    private val bgScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var timeTrackerJob: Job? = null

    fun init(context: Context, lifecycleOwner: LifecycleOwner) {
        sharedPreferences = context.getSharedPreferences("PlaytimePrefs", Context.MODE_PRIVATE)
        lifecycleOwner.lifecycle.addObserver(listener)
        _playtimeState.value = sharedPreferences.getLong("playtime", 0L)
    }

    private fun start() {
        if (timeTrackerJob?.isActive != true) {
            timeTrackerJob = tickerFlow(
                period = ONE_SECOND_IN_MILLIS,
                initialDelay = ONE_SECOND_IN_MILLIS,
            ).onEach {
                _playtimeState.value = _playtimeState.value.inc()
            }.launchIn(bgScope)
        }
    }


    private fun stop() {
        timeTrackerJob?.cancel()
        with(sharedPreferences.edit()) {
            putLong("playtime", _playtimeState.value)
            apply()
        }
    }

    companion object {
        private const val ONE_SECOND_IN_MILLIS = 1_000L
    }
}


fun tickerFlow(period: Long, initialDelay: Long = 0) = flow {
    delay(initialDelay)
    while (true) {
        emit(Unit)
        delay(period)
    }
}