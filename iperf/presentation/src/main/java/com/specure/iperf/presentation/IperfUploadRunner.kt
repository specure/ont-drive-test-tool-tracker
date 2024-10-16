package com.specure.iperf.presentation

import android.content.Context
import com.specure.core.domain.config.Config
import com.specure.iperf.domain.IperfError
import com.specure.iperf.domain.IperfOutputParser
import com.specure.iperf.domain.IperfRunner
import com.specure.iperf.domain.IperfTest
import com.specure.iperf.domain.IperfTestDirection
import com.specure.iperf.domain.IperfTestProgressDownload
import com.specure.iperf.domain.IperfTestStatus
import com.synaptictools.iperf.IPerf
import com.synaptictools.iperf.IPerfConfig
import com.synaptictools.iperf.IPerfResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File


class IperfUploadRunner(
    private val applicationContext: Context,
    private val applicationScope: CoroutineScope,
    private val iperfParser: IperfOutputParser,
    private val appConfig: Config,
): IperfRunner {

        private var iperf = IPerf
        private val config: IPerfConfig
        get() {
            val hostname: String = appConfig.getUploadSpeedTestServerAddress()
                ?: appConfig.getUploadSpeedTestServerAddressDefault()
            val stream = File(applicationContext.filesDir, "iperf3.UXXXXXX")
            return IPerfConfig(
                hostname = hostname,
                stream = stream.path,
                duration = appConfig.getSpeedTestDurationSeconds()
                    ?: appConfig.getSpeedTestDurationSecondsDefault(),
                interval = 1,
                port = appConfig.getUploadSpeedTestServerPort()
                    ?: appConfig.getUploadSpeedTestServerPortDefault(),
                download = false,
                useUDP = false,
                json = false,
                debug = false,
                maxBandwidthBitPerSecond = appConfig.getUploadSpeedTestMaxBandwidthBitsPerSecond()
                    ?: appConfig.getUploadSpeedTestMaxBandwidthBitsPerSecondDefault(),
            )
        }
    private var testProgressDetails: IperfTest = IperfTest(
        status = IperfTestStatus.NOT_STARTED,
        direction = IperfTestDirection.UPLOAD,
    )

        private val _testProgressDetailsFlow = MutableStateFlow<IperfTest>(IperfTest())
        override val testProgressDetailsFlow: StateFlow<IperfTest> get() = _testProgressDetailsFlow

        override fun startTest() {
            iperf = IPerf
            iperf.init(
                config.hostname,
                config.port,
                config.stream,
                config.duration,
                config.interval,
                config.download,
                config.useUDP,
                config.json,
                config.maxBandwidthBitPerSecond
            )
            Timber.d("IPERF Trying port ${config.port} status: ${testProgressDetails.status}")
            if (testProgressDetails.status in listOf(
                    IperfTestStatus.ENDED,
                    IperfTestStatus.NOT_STARTED,
                    IperfTestStatus.STOPPED,
                    IperfTestStatus.ERROR
                )
            ) {
                applicationScope.launch {
                    testProgressDetails = IperfTest()
                    testProgressDetails = testProgressDetails.copy(
                        startTimestamp = System.currentTimeMillis(),
                        status = IperfTestStatus.INITIALIZING,
                        direction = IperfTestDirection.UPLOAD,
                    )
                    _testProgressDetailsFlow.emit(testProgressDetails)
                    val isAsync = true
                    doStartUploadRequest(config, isAsync)
                }
            }
        }

        override fun stopTest() {
            applicationScope.launch {
                iperf.stopTest()
                testProgressDetails = testProgressDetails.copy(
                    status = IperfTestStatus.STOPPED,
                    direction = IperfTestDirection.UPLOAD,
                    testProgress = testProgressDetails.testProgress.plus(
                        zeroUploadSpeedProgress
                    ),
                )
                _testProgressDetailsFlow.emit(testProgressDetails)
            }
        }

    private suspend fun doStartUploadRequest(config: IPerfConfig, isAsync: Boolean) {
        withContext(Dispatchers.IO) {
            try {
                if (isAsync) {
                    iperf.seCallBack {
                        success {
                            Timber.d("iPerf upload request done")
                            applicationScope.launch {
                                testProgressDetails = testProgressDetails.copy(
                                    status = if (testProgressDetails.status == IperfTestStatus.ERROR) {
                                        IperfTestStatus.ERROR
                                    } else IperfTestStatus.ENDED,
                                    testProgress = testProgressDetails.testProgress.plus(
                                        zeroUploadSpeedProgress
                                    ),
                                )
                                _testProgressDetailsFlow.emit(testProgressDetails)
                            }
                        }
                        update { text ->
                            val progress = iperfParser.parseTestProgress(text.toString())
                            if (progress != null) {
                                val currentProgress = IperfTestProgressDownload(
                                    timestampMillis = System.currentTimeMillis(),
                                    relativeTestStartIntervalStart = progress.relativeTestStartIntervalStart,
                                    relativeTestStartIntervalUnit = "sec",
                                    relativeTestStartIntervalEnd = progress.relativeTestStartIntervalEnd,
                                    transferred = progress.transferred,
                                    transferredUnit = progress.transferredUnit,
                                    bandwidth = progress.bandwidth,
                                    bandwidthUnit = progress.bandwidthUnit,
                                )
                                testProgressDetails = testProgressDetails.copy(
                                    testProgress = testProgressDetails.testProgress.plus(
                                        currentProgress
                                    ),
                                    status = IperfTestStatus.RUNNING
                                )
                            } else {
                                // TODO: parse other states
                            }
                            applicationScope.launch {
                                _testProgressDetailsFlow.emit(testProgressDetails)
                            }
                        }
                        error { e ->
                            Timber.e("IPERF Upload: $e")
                            applicationScope.launch {
                                testProgressDetails = testProgressDetails.copy(
                                    status = IperfTestStatus.ERROR,
                                    testProgress = testProgressDetails.testProgress.plus(
                                        zeroUploadSpeedProgress
                                    ),
                                )
                                _testProgressDetailsFlow.emit(testProgressDetails)
                            }
                        }
                    }
                }
                Timber.d("IPERF UPLOAD: $iperf")
                val result = iperf.request(config)
                if (!isAsync) {
                    when (result) {
                        is IPerfResult.Success -> {
                            Timber.d("iPerf upload request done")
                            applicationScope.launch {
                                testProgressDetails = testProgressDetails.copy(
                                    status = if (testProgressDetails.status == IperfTestStatus.ERROR) {
                                        IperfTestStatus.ERROR
                                    } else IperfTestStatus.ENDED,
                                    testProgress = testProgressDetails.testProgress.plus(
                                        zeroUploadSpeedProgress
                                    ),
                                )
                                _testProgressDetailsFlow.emit(testProgressDetails)
                            }
                        }

                        is IPerfResult.Error -> {
                            Timber.d("iPerf upload request failed-> ${result.error}")
                            applicationScope.launch {
                                testProgressDetails = testProgressDetails.copy(
                                    status = IperfTestStatus.ERROR,
                                    testProgress = testProgressDetails.testProgress.plus(
                                        zeroUploadSpeedProgress
                                    ),
                                    error = testProgressDetails.error + IperfError(
                                        timestamp = System.currentTimeMillis(),
                                        error = result.error.toString()
                                    )
                                )
                                _testProgressDetailsFlow.emit(testProgressDetails)
                            }
                        }
                    }
                } else {

                }
            } catch (e: Exception) {
                Timber.d("error on upload doStartRequest() -> ${e.message}")
                applicationScope.launch {
                    testProgressDetails = testProgressDetails.copy(
                        status = IperfTestStatus.ERROR,
                        testProgress = testProgressDetails.testProgress.plus(
                            zeroUploadSpeedProgress
                        ),
                        error = testProgressDetails.error + IperfError(
                            timestamp = System.currentTimeMillis(),
                            error = "Error - unable to start iperf request"
                        )
                    )
                    _testProgressDetailsFlow.emit(testProgressDetails)
                }
            }
        }
    }
}