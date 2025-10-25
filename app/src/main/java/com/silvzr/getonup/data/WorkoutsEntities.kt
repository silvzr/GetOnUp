package com.silvzr.getonup.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.silvzr.getonup.Exercise
import com.silvzr.getonup.ExerciseId
import com.silvzr.getonup.WorkoutPlan

@Entity(tableName = "workout_plans")
internal data class WorkoutPlanEntity(
    @PrimaryKey val id: String,
    val name: String,
    val subtitle: String?,
    val description: String?,
    val isCurrent: Boolean
)

@Entity(tableName = "exercises")
internal data class ExerciseEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val summary: String?
)

internal fun WorkoutPlanEntity.toDomain(): WorkoutPlan = WorkoutPlan(
    id = id,
    name = name,
    subtitle = subtitle,
    description = description,
    isCurrent = isCurrent
)

internal fun WorkoutPlan.toEntity(): WorkoutPlanEntity = WorkoutPlanEntity(
    id = id,
    name = name,
    subtitle = subtitle,
    description = description,
    isCurrent = isCurrent
)

internal fun ExerciseEntity.toDomain(): Exercise = Exercise(
    id = ExerciseId.fromNumber(id),
    name = name,
    summary = summary
)

internal fun Exercise.toEntity(): ExerciseEntity = ExerciseEntity(
    id = id.number,
    name = name,
    summary = summary
)
