package com.silvzr.getonup.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [WorkoutPlanEntity::class, ExerciseEntity::class],
    version = 1,
    exportSchema = false
)
internal abstract class WorkoutsDatabase : RoomDatabase() {
    abstract fun workoutsDao(): WorkoutsDao

    companion object {
        private const val DATABASE_NAME = "workouts.db"

        @Volatile
        private var instance: WorkoutsDatabase? = null

        fun getInstance(context: Context): WorkoutsDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context.applicationContext).also { instance = it }
            }
        }

        private fun buildDatabase(appContext: Context): WorkoutsDatabase {
            return Room.databaseBuilder(appContext, WorkoutsDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
