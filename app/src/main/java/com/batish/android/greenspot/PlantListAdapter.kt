package com.batish.android.greenspot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.batish.android.greenspot.databinding.ListItemPlantsBinding
import java.util.UUID

class PlantHolder(
    private val binding: ListItemPlantsBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(plant: Plant, onPlantClicked: (plantId: UUID) -> Unit) {
        binding.plantTitle.text = plant.title
        binding.plantDate.text = plant.date.toString()

        binding.root.setOnClickListener {
            onPlantClicked(plant. id)
        }
    }
}

class PlantListAdapter(
    private val plants: List<Plant>,
    private val onPlantClicked: (plantId : UUID) -> Unit
) : RecyclerView.Adapter<PlantHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlantHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemPlantsBinding.inflate(inflater, parent, false)
        return PlantHolder(binding)
    }

    override fun onBindViewHolder(holder: PlantHolder, position: Int) {
        val plant = plants[position]
        holder.bind(plant, onPlantClicked)
    }

    override fun getItemCount(): Int {
        return plants.size
    }


}