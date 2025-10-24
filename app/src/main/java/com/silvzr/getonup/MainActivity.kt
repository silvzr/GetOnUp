package com.silvzr.getonup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.silvzr.getonup.ui.theme.GetOnUpTheme
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            GetOnUpTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    bottomBar = { GetOnUpToolbar() }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun GetOnUpToolbar(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                modifier = Modifier.align(Alignment.CenterStart),
                onClick = {}
            ) {
                Icon(
                    imageVector = Icons.Outlined.Timeline,
                    contentDescription = stringResource(id = R.string.nav_timeline),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            IconButton(
                modifier = Modifier.align(Alignment.Center),
                onClick = {}
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = stringResource(id = R.string.nav_calendar),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            IconButton(
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = {}
            ) {
                Icon(
                    imageVector = Icons.Outlined.FitnessCenter,
                    contentDescription = stringResource(id = R.string.nav_workouts),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ToolbarPreview() {
    GetOnUpTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            GetOnUpToolbar()
        }
    }
}
