package com.specure.permissions.presentation.screen.permissions.components.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.specure.permissions.presentation.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionDialog(
    permission: PermissionDialogTextProvider,
    isPermanentlyDeclined: Boolean,
    onDismiss: () -> Unit,
    onOkClick: () -> Unit,
    onGoToAppSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicAlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        content = {

            Surface(shape = ShapeDefaults.Large) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.permission_required),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = permission.getDescription(isPermanentlyDeclined),
                        textAlign = TextAlign.Justify,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isPermanentlyDeclined) {
                                stringResource(id = R.string.grant_permission)
                            } else {
                                stringResource(id = android.R.string.ok)
                            },
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isPermanentlyDeclined) {
                                        onGoToAppSettingsClick()
                                    } else {
                                        onOkClick()
                                    }
                                }
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun PermissionDialogOld(
    permission: PermissionDialogTextProvider,
    isPermanentlyDeclined: Boolean,
    onDismiss: () -> Unit,
    onOkClick: () -> Unit,
    onGoToAppSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = @Composable {
            Text(
                text = if (isPermanentlyDeclined) {
                    "Grant permission"
                } else {
                    "OK"
                },
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (isPermanentlyDeclined) {
                            onGoToAppSettingsClick()
                        } else {
                            onOkClick()
                        }
                    }
                    .padding(16.dp)
            )
        },
        title = {
            Text(text = "Permission required")
        },
        text = {
            Text(
                text = permission.getDescription(
                    isPermanentlyDeclined = isPermanentlyDeclined
                )
            )
        },
        modifier = modifier
    )

}

@PreviewScreenSizes
@Composable
fun BasicPermissionDialogPreview() {
    PermissionDialog(
        modifier = Modifier.padding(24.dp),
        permission = object : PermissionDialogTextProvider {
            override val permissionName: String
                get() = "Fine location"

            override fun getDescription(isPermanentlyDeclined: Boolean): String {
                return if (isPermanentlyDeclined) {
                    "It seems that you permanently declined fine location permission. You can go to app settings to grant it."
                } else {
                    "This app needs access to your fine position in order to log properly position for your measurements"
                }
            }

        },
        isPermanentlyDeclined = true,
        onDismiss = { },
        onOkClick = { },
        onGoToAppSettingsClick = { })
}

interface PermissionDialogTextProvider {
    val permissionName: String

    fun getDescription(isPermanentlyDeclined: Boolean): String
}

class UnknownPermissionProvider : PermissionDialogTextProvider {
    override val permissionName: String
        get() = "UNKNOWN"

    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return "This is unknown permission and you should have never see this unless new asked permission has been not correctly implemented"
    }
}