package com.cadrikmdev.permissions.presentation.screen.permissions.components

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cadrikmdev.permissions.domain.model.Permission
import com.cadrikmdev.permissions.presentation.R
import com.cadrikmdev.permissions.presentation.model.AndroidPermission
import com.cadrikmdev.permissions.presentation.screen.permissions.mappers.permissionToUiName

@Composable
fun PermissionRationale(
    permission: Permission,
    onResolveClick: (permission: Permission) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Text(text = stringResource(id = permission.name.permissionToUiName()))
        Spacer(modifier = Modifier.height(8.dp))
        if (permission.isGranted) {
            Text(text = stringResource(id = R.string.granted))
        } else {
            if (!permission.shouldShowRationale && permission.isAsked) {
                ErrorTextView(text = stringResource(id = R.string.not_granted))
            } else {
                ErrorTextView(text = stringResource(id = R.string.not_granted))
            }
        }
        if (!permission.isGranted) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = { onResolveClick(permission) }) {
                    Text(
                        text = stringResource(id = R.string.resolve),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }

}


@Preview
@Composable
fun previewPermissionRationale() {
    PermissionRationale(
        permission = AndroidPermission(
            Manifest.permission.READ_PHONE_STATE,
        ),
        onResolveClick = { permission -> },
        modifier = Modifier.fillMaxWidth()
    )
}