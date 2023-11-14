package com.batish.android.greenspot

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class PlantsListViewModel : ViewModel() {
    private val plantRepository = PlantRepository.get()
    private val _plants: MutableStateFlow<List<Plant>> = MutableStateFlow(emptyList())
    val plants: StateFlow<List<Plant>>
        get() = _plants.asStateFlow()

    init {
        viewModelScope.launch {
            plantRepository.getPlants().collect {
                Log.d("PlantsListViewModel", "Received ${it.size} plants")
                _plants.value = it
            }
        }
    }

    suspend fun addPlant(plant: Plant) {
        plantRepository.addPlant(plant)
    }


        }

