package com.cadrikmdev.iperf.presentation

import android.content.Context
import com.cadrikmdev.iperf.domain.IperfError
import com.cadrikmdev.iperf.domain.IperfOutputParser
import com.cadrikmdev.iperf.domain.IperfRunner
import com.cadrikmdev.iperf.domain.IperfTest
import com.cadrikmdev.iperf.domain.IperfTestDirection
import com.cadrikmdev.iperf.domain.IperfTestProgressDownload
import com.cadrikmdev.iperf.domain.IperfTestStatus
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
import kotlin.concurrent.thread


class IperfUploadRunner(
    private val applicationContext: Context,
    private val applicationScope: CoroutineScope,
    private val iperfParser: IperfOutputParser,
): IperfRunner {

        private var iperf = IPerf
        private val config: IPerfConfig
        get() {
            val hostname: String = BuildConfig.BASE_URL
            val stream = File(applicationContext.filesDir, "iperf3.UXXXXXX")
            return IPerfConfig(
                hostname = hostname,
                stream = stream.path,
                duration = 3600 * 8, // 8h
                interval = 1,
                port = 5202,
                download = false,
                useUDP = false,
                json = false,
                debug = false,
                maxBandwidthBitPerSecond = 2000000,
            )
        }
    private var testProgressDetails: IperfTest = IperfTest(
        status = IperfTestStatus.NOT_STARTED,
        direction = IperfTestDirection.UPLOAD,
    )

        private val _testProgressDetailsFlow = MutableStateFlow<IperfTest>(IperfTest())
        override val testProgressDetailsFlow: StateFlow<IperfTest> get() = _testProgressDetailsFlow

        override fun startTest() {
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
            Timber.d("IPERF Trying port ${config.port}")
            if (testProgressDetails.status in listOf(IperfTestStatus.NOT_STARTED, IperfTestStatus.STOPPED, IperfTestStatus.ERROR)) {
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
//        downloadResultBuilder.clear()

        override fun stopTest() {
            applicationScope.launch {
                testProgressDetails = testProgressDetails.copy(
                    status = IperfTestStatus.STOPPED,
                    direction = IperfTestDirection.UPLOAD,
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
                                    status = IperfTestStatus.ENDED
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
//                                _iPerfDownloadSpeed.postValue("-")
//                                _iPerfDownloadSpeedUnit.postValue("")
                            }
//                            _iPerfDownloadRequestResult.postValue("D ${downloadResultBuilder.toString()}")
//                            downloadResultBuilder.append(text)
                            applicationScope.launch {
                                _testProgressDetailsFlow.emit(testProgressDetails)
                            }

                        }
                        error { e ->
                            Timber.e("IPERF Upload: $e")
                            applicationScope.launch {
                                testProgressDetails = testProgressDetails.copy(
                                    status = IperfTestStatus.ERROR
                                )
                                _testProgressDetailsFlow.emit(testProgressDetails)
                            }
//                            downloadResultBuilder.append("\niPerf download request failed:\n error: $e")
//                            Timber.d("D $downloadResultBuilder")
//                            _iPerfDownloadTestRunning.postValue(false)
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
                                    status = IperfTestStatus.ENDED
                                )
                                _testProgressDetailsFlow.emit(testProgressDetails)
                            }
                        }

                        is IPerfResult.Error -> {
                            Timber.d("iPerf upload request failed-> ${result.error}")
                            applicationScope.launch {
                                testProgressDetails = testProgressDetails.copy(
                                    status = IperfTestStatus.ERROR,
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