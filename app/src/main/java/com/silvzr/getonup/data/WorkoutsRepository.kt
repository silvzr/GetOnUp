package com.silvzr.getonup.data

import com.silvzr.getonup.Exercise
import com.silvzr.getonup.ExerciseId
import com.silvzr.getonup.WorkoutPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class WorkoutsRepository(private val dao: WorkoutsDao) {
    val plans: Flow<List<WorkoutPlan>> = dao.observePlans().map { entities ->
        entities.map(WorkoutPlanEntity::toDomain)
    }

    val exercises: Flow<List<Exercise>> = dao.observeExercises().map { entities ->
        entities.map(ExerciseEntity::toDomain)
    }

    suspend fun upsertPlan(plan: WorkoutPlan) {
        dao.upsertPlan(plan.toEntity())
        if (plan.isCurrent) {
            dao.setCurrentPlan(plan.id)
        }
    }

    suspend fun deletePlan(planId: String) {
        dao.deletePlan(planId)
    }

    suspend fun setCurrentPlan(planId: String) {
        dao.setCurrentPlan(planId)
    }

    suspend fun addExercise(name: String, summary: String?): Exercise {
        val nextId = ((dao.getMaxExerciseId() ?: 0) + 1).also { next ->
            require(next <= ExerciseId.MAX_VALUE) {
                "Maximum number of exercises (${ExerciseId.MAX_VALUE}) reached"
            }
        }
        val entity = ExerciseEntity(id = nextId, name = name, summary = summary)
        dao.insertExercise(entity)
        return entity.toDomain()
    }

    suspend fun deleteExercise(id: ExerciseId) {
        dao.deleteExercise(id.number)
    }
}
