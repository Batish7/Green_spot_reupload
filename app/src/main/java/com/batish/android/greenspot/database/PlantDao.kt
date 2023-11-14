package com.batish.android.greenspot.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.batish.android.greenspot.Plant
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface PlantDao {
        @Query("SELECT * FROM plant")
        fun getPlants(): Flow<List<Plant>>

        @Query("SELECT * FROM plant WHERE id=(:id)")
        suspend fun getPlant(id: UUID): Plant
        @Query("DELETE FROM plant WHERE id = :plantId")
        suspend fun deletePlant(plantId: UUID)
        @Update
        suspend fun updatePlant(plant: Plant)
    @Insert
    fun addPlant(plant: Plant)

}