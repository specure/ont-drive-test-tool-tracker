package com.cadrikmdev.iperf.domain

import kotlinx.coroutines.flow.Flow

interface IperfRunner {

    fun startTest()

    fun stopTest()

}