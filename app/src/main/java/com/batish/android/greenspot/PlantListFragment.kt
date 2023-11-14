package com.batish.android.greenspot

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.batish.android.greenspot.databinding.FragmentPlantListBinding
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class PlantListFragment : Fragment() {

    private var _binding: FragmentPlantListBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val plantsListViewModel: PlantsListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlantListBinding.inflate(inflater, container, false)
        binding.plantRecyclerView.layoutManager = LinearLayoutManager(context)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
         super.onViewCreated(view, savedInstanceState)
         viewLifecycleOwner.lifecycleScope.launch {
             viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                 plantsListViewModel.plants.collect { plants ->
                     binding.plantRecyclerView.adapter =
                         PlantListAdapter(plants) { plantId ->
                             findNavController().navigate(
                                 PlantListFragmentDirections.showPlantsDetail(plantId)
                             )
                         }
                 }
             }
         }
     }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_plant_list, menu)
        inflater.inflate(R.menu.fragment_help_plant_list, menu) // Add this line to include the Help menu

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_plant -> {
                showNewPlant()
                true
            }
            R.id.help -> {
                showHelp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun showHelp() {
        findNavController().navigate(R.id.action_plantListFragment_to_helpFragment)

    }

    private fun showNewPlant() {
        viewLifecycleOwner.lifecycleScope.launch {
            val newPlant = Plant(
                id = UUID.randomUUID(),
                title = "",
                date = Date(),
                place = "",
                isFound = false
            )
            plantsListViewModel.addPlant(newPlant)
            findNavController().navigate(
                PlantListFragmentDirections.showPlantsDetail(newPlant.id)
            )
        }
    }
}