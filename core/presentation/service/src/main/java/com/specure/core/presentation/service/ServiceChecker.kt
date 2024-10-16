package com.specure.core.presentation.service

interface ServiceChecker {

    fun isServiceEnabled(): Boolean

    fun isServiceAvailable(): Boolean

    fun resolve()
}