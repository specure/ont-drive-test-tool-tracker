package com.cadrikmdev.track.presentation.active_track.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadrikmdev.connectivity.domain.mobile.MobileNetworkInfo
import com.cadrikmdev.core.domain.Temperature
import com.cadrikmdev.core.presentation.designsystem.SignalTrackerTheme
import com.cadrikmdev.core.presentation.ui.formatted
import com.cadrikmdev.iperf.domain.IperfTest
import com.cadrikmdev.iperf.domain.IperfTestProgressDownload
import com.cadrikmdev.iperf.domain.IperfTestProgressUpload
import com.cadrikmdev.track.domain.TrackData
import com.cadrikmdev.track.presentation.R
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun TrackDataCard(
    elapsedTime: Duration,
    trackData: TrackData,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            if (trackData.temperature != null) {
                TrackDataItem(
                    title = stringResource(id = R.string.temperature),
                    value = "${trackData.temperature?.temperatureCelsius.toString()} °C" ,
                    valueFontSize = 10.sp
                )
            } else {
                TrackDataItem(
                    title = stringResource(id = R.string.temperature),
                    value = "- °C" ,
                    valueFontSize = 10.sp
                )
            }
            TrackDataItem(
                title = stringResource(id = R.string.duration),
                value = elapsedTime.formatted(),
                valueFontSize = 32.sp
            )
            if (trackData.networkInfo != null && trackData.networkInfo is MobileNetworkInfo) {
                val mobileNetworkInfo = (trackData.networkInfo as MobileNetworkInfo)
                TrackDataItem(
                    title = stringResource(id = R.string.network),
                    value = "${mobileNetworkInfo.networkType} / ${trackData.networkInfo?.name.toString()} \n ${mobileNetworkInfo.primarySignalDbm}" ,
                    valueFontSize = 10.sp
                )
            } else {
                TrackDataItem(
                    title = stringResource(id = R.string.network),
                    value = "-" ,
                    valueFontSize = 10.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            TrackDataItem(
                title = stringResource(id = R.string.download),
                value = "${trackData.iperfTestDownload?.testProgress?.lastOrNull()?.bandwidth ?: "-"} ${trackData.iperfTestDownload?.testProgress?.lastOrNull()?.bandwidthUnit ?: ""}",
                modifier = Modifier
                    .defaultMinSize(minWidth = 75.dp)
            )
            TrackDataItem(
                title = stringResource(id = R.string.upload),
                value = "${trackData.iperfTestUpload?.testProgress?.lastOrNull()?.bandwidth ?: "-"} ${trackData.iperfTestUpload?.testProgress?.lastOrNull()?.bandwidthUnit ?: ""}",
                modifier = Modifier
                    .defaultMinSize(minWidth = 75.dp)
            )
        }

    }
}


@Composable
private fun TrackDataItem(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueFontSize: TextUnit = 16.sp
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (title.isNotBlank())
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = valueFontSize
        )
    }

}

@Preview
@Composable
private fun TrackDataCardPreview() {
    SignalTrackerTheme {
        TrackDataCard(
            elapsedTime = 35818.seconds,
            trackData = TrackData(
               iperfTestDownload =  IperfTest(
                    testProgress = listOf(
                        IperfTestProgressDownload(
                            1718713960422,
                            relativeTestStartIntervalStart = 0.00,
                            relativeTestStartIntervalEnd = 1.00,
                            relativeTestStartIntervalUnit = "sec",
                            transferred = 230.0,
                            transferredUnit = "KBytes",
                            bandwidth = 20.0,
                            bandwidthUnit = "Mbits",
                        ),
                    )
                ),
                iperfTestUpload =  IperfTest(
                    testProgress = listOf(
                        IperfTestProgressUpload(
                            retransmissions = 3,
                            congestionWindow = 20,
                            congestionWindowUnit = "KBytes",
                            timestampMillis = 1718713960422,
                            relativeTestStartIntervalStart = 0.00,
                            relativeTestStartIntervalEnd = 1.00,
                            relativeTestStartIntervalUnit = "sec",
                            transferred = 234.0,
                            transferredUnit = "KBytes",
                            bandwidth = 1.9,
                            bandwidthUnit = "Mbits",
                        ),
                    )
                ),
                temperature = Temperature(
                    temperatureCelsius = 22.3f,
                    timestampMillis = 554684684346
                )
            )
        )
    }
}