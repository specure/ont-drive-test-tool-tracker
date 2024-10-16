package com.specure.connectivity.domain

class SignalStrengthInfoCommon(
    override val transport: TransportType,
    override val value: Int?,
    override val rsrq: Int?,
    override val signalLevel: Int,
    override val min: Int,
    override val max: Int,
    override val timestampNanos: Long,
) : SignalStrengthInfo()