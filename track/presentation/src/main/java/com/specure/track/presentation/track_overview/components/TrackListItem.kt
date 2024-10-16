@file:OptIn(ExperimentalFoundationApi::class)

package com.specure.track.presentation.track_overview.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.specure.core.presentation.designsystem.CalendarIcon
import com.specure.core.presentation.designsystem.SignalTrackerTheme
import com.specure.core.presentation.designsystem.TrackOutlinedIcon
import com.specure.track.presentation.R
import com.specure.track.presentation.track_overview.model.TrackDataUi
import com.specure.track.presentation.track_overview.model.TrackUi

@Composable
fun TrackListItem(
    trackUi: TrackUi,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDropDown by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .background(MaterialTheme.colorScheme.surface)
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    showDropDown = true
                }
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TrackingTimeSection(
            duration = trackUi.durationMillis.toString(),
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        TrackingDateSection(
            dateTime = trackUi.timestamp
        )
    }
    DropdownMenu(
        expanded = showDropDown,
        onDismissRequest = {
            showDropDown = false
        },
    ) {
        DropdownMenuItem(
            text = {
                Text(text = stringResource(id = R.string.delete))
            },
            onClick = {
                showDropDown = false
                onDeleteClick()
            },
        )
    }
}

@Composable
private fun TrackingDateSection(
    dateTime: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = CalendarIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = dateTime,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}



@Composable
private fun TrackingTimeSection(
    duration: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = TrackOutlinedIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier
                .weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(id = R.string.total_tracking_time),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = duration,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


@Composable
private fun DataGridCell(
    trackData: TrackDataUi,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = trackData.name,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = trackData.value,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview
@Composable
private fun TrackListItemPreview() {
    SignalTrackerTheme {
        TrackListItem(
            trackUi = TrackUi(
                id = 1,
                durationMillis = 8504646086,
                timestamp = "May 13th, 2024 - 10:25AM",
                timestampRaw = 8504646086,
                temperatureCelsius = 12.3,
                temperatureTimestamp = "May 13th, 2024 - 10:27AM",
                temperatureTimestampRaw = 18716461181681,
                downloadSpeed = 12.2,
                downloadSpeedUnit = "Mbit/sec",
                downloadSpeedTestState = "RUNNING",
                downloadSpeedTestError = null,
                downloadSpeedTestTimestamp = "May 13th, 2024 - 10:27AM",
                downloadSpeedTestTimestampRaw = 18716461181681,
                uploadSpeed = 2.2,
                uploadSpeedUnit = "Mbit/sec",
                uploadSpeedTestState = "RUNNING",
                uploadSpeedTestError = null,
                uploadSpeedTestTimestamp = "May 13th, 2024 - 10:27AM",
                uploadSpeedTestTimestampRaw = 18716461181681,
                latitude = 48.3,
                longitude = 42.4,
                locationTimestamp = "May 13th, 2024 - 10:27AM",
                locationTimestampRaw = 18716461181681,
                networkType = "CELLULAR",
                mobileNetworkOperator = "O2 - SK",
                mobileNetworkType = "LTE",
                signalStrength = -110,
                networkInfoTimestamp = "May 13th, 2024 - 10:27AM",
                networkInfoTimestampRaw = 18716461181681,
                connectionStatus = "CONNECTED",
                exported = false,
            ),
            onDeleteClick = { }
        )
    }
}