package com.silvzr.getonup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.silvzr.getonup.ui.theme.GetOnUpTheme
import androidx.core.view.WindowCompat
import androidx.annotation.StringRes
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
                    bottomBar = { GetOnUpNavigationBar() }
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
fun GetOnUpNavigationBar(modifier: Modifier = Modifier) {
    var selectedDestination by rememberSaveable { mutableStateOf(GetOnUpDestination.Timeline) }

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = NavigationBarDefaults.Elevation
    ) {
        GetOnUpDestination.entries.forEach { destination ->
            val label = stringResource(id = destination.labelRes)
            val isSelected = destination == selectedDestination
            val iconVector = if (isSelected) destination.selectedIcon ?: destination.unselectedIcon else destination.unselectedIcon
            val iconSize = if (isSelected) destination.selectedIconSize else destination.unselectedIconSize

            NavigationBarItem(
                selected = isSelected,
                onClick = { selectedDestination = destination },
                icon = {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = label,
                        modifier = Modifier.size(iconSize)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
    }
}

private enum class GetOnUpDestination(
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector?,
    val unselectedIcon: ImageVector,
    val selectedIconSize: Dp = 24.dp,
    val unselectedIconSize: Dp = 24.dp
) {
    Timeline(
        R.string.nav_performance,
        selectedIcon = null,
        unselectedIcon = Icons.Outlined.Timeline,
        selectedIconSize = 26.dp
    ),
    Calendar(
        R.string.nav_calendar,
        selectedIcon = Icons.Filled.CalendarToday,
        unselectedIcon = Icons.Outlined.CalendarToday
    ),
    Workouts(
        R.string.nav_workouts,
        selectedIcon = null,
        unselectedIcon = Icons.Outlined.FitnessCenter,
        selectedIconSize = 26.dp
    )
}

@Preview(showBackground = true)
@Composable
private fun NavigationBarPreview() {
    GetOnUpTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            GetOnUpNavigationBar()
        }
    }
}
