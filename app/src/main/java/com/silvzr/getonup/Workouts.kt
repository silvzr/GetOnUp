package com.silvzr.getonup

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.silvzr.getonup.ui.theme.GetOnUpTheme

/** Represents a workout plan item rendered in the carousel. */
data class WorkoutPlan(
    val id: String,
    val name: String,
    val subtitle: String? = null,
    val description: String? = null,
    val isCurrent: Boolean = false
)

@Composable
fun WorkoutsScreen(
    plans: List<WorkoutPlan>,
    onSettingsClick: () -> Unit,
    onCreatePlan: () -> Unit,
    onEditPlan: (WorkoutPlan) -> Unit,
    onSetCurrent: (WorkoutPlan) -> Unit,
    onExercisesManage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var selectedIndex by rememberSaveable { mutableStateOf(-1) }
    LaunchedEffect(plans) {
        selectedIndex = when {
            plans.isEmpty() -> -1
            else -> plans.indexOfFirst { it.isCurrent }.takeIf { it >= 0 } ?: 0
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        WorkoutsHeader(onSettingsClick = onSettingsClick)

        if (plans.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 6.dp,
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.workouts_no_plans),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val safeIndex = selectedIndex.coerceIn(plans.indices)
            if (safeIndex != selectedIndex) {
                selectedIndex = safeIndex
            }

            WorkoutCarousel(
                plans = plans,
                selectedIndex = safeIndex,
                onSelected = { index -> selectedIndex = index },
                onEditPlan = onEditPlan,
                onSetCurrent = onSetCurrent
            )
        }

        CreatePlanButton(onCreatePlan = onCreatePlan)

        ExercisesSection(onManageExercises = onExercisesManage)
    }
}

@Composable
private fun WorkoutsHeader(onSettingsClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(id = R.string.workouts_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = stringResource(id = R.string.workouts_settings_cd)
            )
        }
    }
}

@Composable
private fun WorkoutCarousel(
    plans: List<WorkoutPlan>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    onEditPlan: (WorkoutPlan) -> Unit,
    onSetCurrent: (WorkoutPlan) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        itemsIndexed(plans, key = { _, plan -> plan.id }) { index, plan ->
            val isSelected = index == selectedIndex
            val targetWidth by animateDpAsState(targetValue = if (isSelected) 296.dp else 148.dp, label = "cardWidth")
            val targetHeight by animateDpAsState(targetValue = if (isSelected) 256.dp else 188.dp, label = "cardHeight")

            ElevatedCard(
                modifier = Modifier
                    .width(targetWidth)
                    .height(targetHeight)
                    .clickable { onSelected(index) },
                shape = MaterialTheme.shapes.extraLarge
            ) {
                if (isSelected) {
                    WorkoutLargeCardContent(
                        plan = plan,
                        onEditPlan = onEditPlan,
                        onSetCurrent = onSetCurrent
                    )
                } else {
                    WorkoutSmallCardContent(plan = plan)
                }
            }
        }
    }
}

@Composable
private fun WorkoutLargeCardContent(
    plan: WorkoutPlan,
    onEditPlan: (WorkoutPlan) -> Unit,
    onSetCurrent: (WorkoutPlan) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = plan.name,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            plan.subtitle?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            plan.description?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(onClick = { onEditPlan(plan) }) {
                Text(text = stringResource(id = R.string.workouts_edit))
            }

            Spacer(modifier = Modifier.width(12.dp))

            val isCurrent = plan.isCurrent
            Button(
                onClick = { onSetCurrent(plan) },
                enabled = !isCurrent
            ) {
                Text(
                    text = if (isCurrent) {
                        stringResource(id = R.string.workouts_current)
                    } else {
                        stringResource(id = R.string.workouts_set_current)
                    }
                )
            }
        }
    }
}

@Composable
private fun WorkoutSmallCardContent(plan: WorkoutPlan, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = plan.name,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        plan.subtitle?.takeIf { it.isNotBlank() }?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CreatePlanButton(onCreatePlan: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onCreatePlan,
        modifier = modifier.fillMaxWidth(),
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(id = R.string.workouts_create_plan))
            Icon(
                imageVector = Icons.Outlined.ArrowForward,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun ExercisesSection(onManageExercises: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.workouts_exercises_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 6.dp,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.workouts_exercises_preview),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = MaterialTheme.shapes.large
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(id = R.string.workouts_exercises_empty_preview),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onManageExercises,
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.OpenInFull,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = stringResource(id = R.string.workouts_exercises_manage))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkoutsScreenPreview() {
    GetOnUpTheme {
        WorkoutsScreen(
            plans = listOf(
                WorkoutPlan(
                    id = "1",
                    name = "Strength Foundation",
                    subtitle = "4-week progressive plan",
                    description = "Build consistency with three focused strength days and optional mobility add-ons.",
                    isCurrent = true
                ),
                WorkoutPlan(
                    id = "2",
                    name = "Mobility Reset",
                    subtitle = "Gentle daily flow"
                ),
                WorkoutPlan(
                    id = "3",
                    name = "Endurance Builder",
                    subtitle = "Cardio first approach"
                )
            ),
            onSettingsClick = {},
            onCreatePlan = {},
            onEditPlan = {},
            onSetCurrent = {},
            onExercisesManage = {}
        )
    }
}
