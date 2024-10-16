package com.specure.connectivity.domain.mobile

enum class NrConnectionState(val stringValue: String) {

    NSA("NSA"),
    SA("SA"),
    AVAILABLE("AVAILABLE"),
    NOT_AVAILABLE("NOT_AVAILABLE");

    companion object {

        fun fromString(value: String): NrConnectionState {
            values().forEach {
                if (it.stringValue == value) return it
            }
            return NOT_AVAILABLE
        }
    }
}