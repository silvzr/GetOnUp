package com.silvzr.getonup.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
internal interface WorkoutsDao {
    @Query("SELECT * FROM workout_plans ORDER BY rowid")
    fun observePlans(): Flow<List<WorkoutPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlan(plan: WorkoutPlanEntity)

    @Query("DELETE FROM workout_plans WHERE id = :planId")
    suspend fun deletePlan(planId: String)

    @Query("UPDATE workout_plans SET isCurrent = CASE WHEN id = :planId THEN 1 ELSE 0 END")
    suspend fun setCurrentPlan(planId: String)

    @Query("SELECT * FROM exercises ORDER BY id")
    fun observeExercises(): Flow<List<ExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExercise(exercise: ExerciseEntity)

    @Query("DELETE FROM exercises WHERE id = :exerciseId")
    suspend fun deleteExercise(exerciseId: Int)

    @Query("SELECT MAX(id) FROM exercises")
    suspend fun getMaxExerciseId(): Int?

}
