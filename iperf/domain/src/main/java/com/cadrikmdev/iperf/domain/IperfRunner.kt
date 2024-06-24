package com.cadrikmdev.iperf.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface IperfRunner {

    val testProgressDetailsFlow: StateFlow<IperfTest>

    fun startTest()

    fun stopTest()

}