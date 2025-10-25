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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.silvzr.getonup.ui.theme.GetOnUpTheme
import java.util.UUID
import kotlin.math.max

/** Represents a workout plan rendered inside the carousel. */
@Immutable
data class WorkoutPlan(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val subtitle: String? = null,
    val description: String? = null,
    val isCurrent: Boolean = false
)

/** Strongly-typed identifier for exercises following a zero-padded numeric sequence. */
@JvmInline
value class ExerciseId internal constructor(val value: String) {
    val number: Int get() = value.toInt()

    override fun toString(): String = value

    companion object {
        private const val MIN_VALUE = 1
        internal const val MAX_VALUE = 9999

        fun fromNumber(number: Int): ExerciseId {
            require(number in MIN_VALUE..MAX_VALUE) {
                "Exercise id must stay within 0001..%04d".format(MAX_VALUE)
            }
            return ExerciseId(number.toString().padStart(4, '0'))
        }
    }
}

/** Lightweight representation of an exercise. */
@Immutable
data class Exercise(
    val id: ExerciseId,
    val name: String,
    val summary: String? = null
)

/** UI contract consumed by [WorkoutsScreen]. */
@Immutable
data class WorkoutsUiState(
    val plans: List<WorkoutPlan> = emptyList(),
    val selectedPlanId: String? = null,
    val currentPlanId: String? = null,
    val exercises: List<Exercise> = emptyList()
)

/** Mutable holder for workouts-related state, responsible for id generation and user actions. */
@Stable
class WorkoutsState(
    initialPlans: List<WorkoutPlan> = emptyList(),
    initialExercises: List<Exercise> = emptyList()
) {
    private var nextExerciseNumber: Int = nextExerciseSeed(initialExercises)

    private var _uiState by mutableStateOf(
        WorkoutsUiState(
            plans = initialPlans,
            selectedPlanId = resolveInitialSelection(initialPlans),
            currentPlanId = initialPlans.firstOrNull { it.isCurrent }?.id,
            exercises = initialExercises
        )
    )
    val uiState: WorkoutsUiState get() = _uiState

    var isSettingsRequested by mutableStateOf(false)
        private set
    var planEditRequest: String? by mutableStateOf(null)
        private set
    var isExerciseManagementRequested by mutableStateOf(false)
        private set

    fun updatePlans(plans: List<WorkoutPlan>) {
        val selected = _uiState.selectedPlanId
        refreshState(
            plans = plans,
            selectedPlanId = plans.takeIf { it.any { plan -> plan.id == selected } }?.let { selected }
                ?: resolveInitialSelection(plans)
        )
    }

    fun upsertPlan(plan: WorkoutPlan) {
        val updated = _uiState.plans.toMutableList().apply {
            val index = indexOfFirst { it.id == plan.id }
            if (index >= 0) {
                this[index] = plan
            } else {
                add(plan)
            }
        }
        refreshState(
            plans = updated,
            selectedPlanId = plan.id
        )
    }

    fun removePlan(planId: String) {
        refreshState(plans = _uiState.plans.filterNot { it.id == planId })
    }

    fun selectPlan(planId: String) {
        if (_uiState.plans.any { it.id == planId }) {
            refreshState(selectedPlanId = planId)
        }
    }

    fun setCurrentPlan(planId: String) {
        if (_uiState.plans.none { it.id == planId }) return

        val updatedPlans = _uiState.plans.map {
            if (it.id == planId) it.copy(isCurrent = true) else it.copy(isCurrent = false)
        }
        refreshState(
            plans = updatedPlans,
            selectedPlanId = planId
        )
    }

    fun requestSettings() {
        isSettingsRequested = true
    }

    fun consumeSettingsRequest() {
        isSettingsRequested = false
    }

    fun requestPlanEdit(planId: String) {
        planEditRequest = planId
    }

    fun consumePlanEditRequest() {
        planEditRequest = null
    }

    fun requestExerciseManagement() {
        isExerciseManagementRequested = true
    }

    fun consumeExerciseManagementRequest() {
        isExerciseManagementRequested = false
    }

    fun createEmptyPlan(): WorkoutPlan {
        val newPlan = WorkoutPlan(
            name = "Untitled plan",
            subtitle = null,
            description = null,
            isCurrent = _uiState.plans.isEmpty()
        )
        upsertPlan(newPlan)
        return newPlan
    }

    fun updateExercises(exercises: List<Exercise>) {
        nextExerciseNumber = max(nextExerciseSeed(exercises), nextExerciseNumber)
        refreshState(exercises = exercises)
    }

    fun addExercise(name: String, summary: String? = null): Exercise {
        val id = generateExerciseId()
        val exercise = Exercise(id = id, name = name, summary = summary)
        refreshState(exercises = _uiState.exercises + exercise)
        return exercise
    }

    fun deleteExercise(id: ExerciseId) {
        refreshState(exercises = _uiState.exercises.filterNot { it.id == id })
    }

    fun generateExerciseId(): ExerciseId {
        check(nextExerciseNumber <= ExerciseId.MAX_VALUE) {
            "Maximum number of exercises (%d) reached".format(ExerciseId.MAX_VALUE)
        }
        return ExerciseId.fromNumber(nextExerciseNumber++)
    }

    private fun refreshState(
        plans: List<WorkoutPlan> = _uiState.plans,
        selectedPlanId: String? = _uiState.selectedPlanId,
        exercises: List<Exercise> = _uiState.exercises
    ) {
        val normalizedPlans = sanitizePlans(plans)
        val currentPlanId = normalizedPlans.firstOrNull { it.isCurrent }?.id
        val resolvedSelection = selectedPlanId
            ?.takeIf { id -> normalizedPlans.any { it.id == id } }
            ?: currentPlanId
            ?: normalizedPlans.firstOrNull()?.id

        _uiState = WorkoutsUiState(
            plans = normalizedPlans,
            selectedPlanId = resolvedSelection,
            currentPlanId = currentPlanId,
            exercises = exercises
        )
    }

    private fun sanitizePlans(plans: List<WorkoutPlan>): List<WorkoutPlan> {
        var currentAssigned = false
        return plans.map { plan ->
            when {
                plan.isCurrent && !currentAssigned -> {
                    currentAssigned = true
                    plan
                }
                plan.isCurrent -> plan.copy(isCurrent = false)
                else -> plan
            }
        }
    }

    private fun nextExerciseSeed(exercises: List<Exercise>): Int {
        val next = (exercises.maxOfOrNull { it.id.number } ?: 0) + 1
        return next.coerceAtLeast(1)
    }

    private fun resolveInitialSelection(plans: List<WorkoutPlan>): String? {
        return plans.firstOrNull { it.isCurrent }?.id ?: plans.firstOrNull()?.id
    }
}

@Composable
fun rememberWorkoutsState(
    initialPlans: List<WorkoutPlan> = emptyList(),
    initialExercises: List<Exercise> = emptyList()
): WorkoutsState = remember { WorkoutsState(initialPlans, initialExercises) }

@Composable
fun WorkoutsScreen(
    state: WorkoutsUiState,
    onSettingsClick: () -> Unit,
    onSelectPlan: (String) -> Unit,
    onEditPlan: (String) -> Unit,
    onSetCurrent: (String) -> Unit,
    onCreatePlan: () -> Unit,
    onExercisesManage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 12.dp, bottom = 32.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        WorkoutsHeader(onSettingsClick = onSettingsClick)

        if (state.plans.isEmpty()) {
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
            WorkoutCarousel(
                plans = state.plans,
                selectedPlanId = state.selectedPlanId,
                onSelected = onSelectPlan,
                onEditPlan = onEditPlan,
                onSetCurrent = onSetCurrent
            )
        }

        CreatePlanButton(onCreatePlan = onCreatePlan)

        ExercisesSection(
            exercises = state.exercises,
            onManageExercises = onExercisesManage
        )
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
    selectedPlanId: String?,
    onSelected: (String) -> Unit,
    onEditPlan: (String) -> Unit,
    onSetCurrent: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        itemsIndexed(plans, key = { _, plan -> plan.id }) { index, plan ->
            val isSelected = plan.id == selectedPlanId || (selectedPlanId == null && index == 0)
            val targetWidth by animateDpAsState(targetValue = if (isSelected) 296.dp else 148.dp, label = "cardWidth")
            val targetHeight by animateDpAsState(targetValue = if (isSelected) 256.dp else 188.dp, label = "cardHeight")

            ElevatedCard(
                modifier = Modifier
                    .width(targetWidth)
                    .height(targetHeight)
                    .clickable { onSelected(plan.id) },
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
    onEditPlan: (String) -> Unit,
    onSetCurrent: (String) -> Unit,
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

            OutlinedButton(onClick = { onEditPlan(plan.id) }) {
                Text(text = stringResource(id = R.string.workouts_edit))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = { onSetCurrent(plan.id) },
                enabled = !plan.isCurrent
            ) {
                Text(
                    text = if (plan.isCurrent) {
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
private fun ExercisesSection(
    exercises: List<Exercise>,
    onManageExercises: () -> Unit,
    modifier: Modifier = Modifier
) {
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

                if (exercises.isEmpty()) {
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
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        exercises.take(3).forEach { exercise ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.large,
                                tonalElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = exercise.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = exercise.id.value,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    exercise.summary?.takeIf { it.isNotBlank() }?.let {
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        if (exercises.size > 3) {
                            Text(
                                text = stringResource(
                                    id = R.string.workouts_exercises_more,
                                    exercises.size - 3
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
    val previewState = WorkoutsUiState(
        plans = listOf(
            WorkoutPlan(
                id = "plan-1",
                name = "Strength Foundation",
                subtitle = "4-week progressive plan",
                description = "Build consistency with three focused strength days and optional mobility add-ons.",
                isCurrent = true
            ),
            WorkoutPlan(
                id = "plan-2",
                name = "Mobility Reset",
                subtitle = "Gentle daily flow"
            ),
            WorkoutPlan(
                id = "plan-3",
                name = "Endurance Builder",
                subtitle = "Cardio-first approach"
            )
        ),
        selectedPlanId = "plan-1",
        currentPlanId = "plan-1",
        exercises = listOf(
            Exercise(ExerciseId.fromNumber(1), "Goblet Squat", "3 sets of 12"),
            Exercise(ExerciseId.fromNumber(2), "Tempo Push-up", "3 sets of 8"),
            Exercise(ExerciseId.fromNumber(3), "Dead Bug Hold", "45 seconds")
        )
    )

    GetOnUpTheme {
        WorkoutsScreen(
            state = previewState,
            onSettingsClick = {},
            onSelectPlan = {},
            onEditPlan = {},
            onSetCurrent = {},
            onCreatePlan = {},
            onExercisesManage = {}
        )
    }
}
