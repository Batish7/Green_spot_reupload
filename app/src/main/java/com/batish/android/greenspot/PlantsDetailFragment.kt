package com.batish.android.greenspot

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.doOnLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.batish.android.greenspot.databinding.FragmentPlantsDetailBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import java.io.File
import java.util.Date
import java.util.UUID

private const val DATE_FORMAT = "EEE, MMM, dd"
class PlantsDetailFragment : Fragment() {

    private var _binding: FragmentPlantsDetailBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val args: PlantsDetailFragmentArgs by navArgs()

    private val plantDetailViewModel: PlantDetailViewModel by viewModels {
        PlantDetailViewModelFactory(args.plantId)
    }

    private val selectReporter = registerForActivityResult(
        ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        uri?.let { parseContactSelection(it) }
    }
    private val takePhoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { didTakePhoto: Boolean ->
        if (didTakePhoto && photoName != null) {
            plantDetailViewModel.updatePlant { oldPlant ->
                oldPlant.copy(photoFileName = photoName)
            }
        }
    }
    private var photoName: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlantsDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            plantsTitle.doOnTextChanged { text, _, _, _ ->
                plantDetailViewModel.updatePlant { oldPlant ->
                    oldPlant.copy(title = text.toString())
                }
            }
            plantsPlace.doOnTextChanged { text, _, _, _ ->
                plantDetailViewModel.updatePlant { oldPlant ->
                    oldPlant.copy(place = text.toString())
                }
            }
            plantFound.setOnCheckedChangeListener { _, isChecked ->
                plantDetailViewModel.updatePlant { oldPlant ->
                    oldPlant.copy(isFound = isChecked)
                }
            }
            plantReporter.setOnClickListener {
                selectReporter.launch(null)
            }
            val selectReporterIntent = selectReporter.contract.createIntent(
                requireContext(),
                null
            )
            plantReporter.isEnabled = canResolveIntent(selectReporterIntent)

            plantCamera.setOnClickListener {
                photoName = "IMG_${Date()}.JPG"
                val photoFile = File(requireContext().applicationContext.filesDir,
                    photoName)
                val photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.batish.android.greenspot",
                    photoFile
                )
                takePhoto.launch(photoUri)
            }
            val captureImageIntent = takePhoto.contract.createIntent(
                requireContext(),
                Uri.parse("")
            )
            plantCamera.isEnabled = canResolveIntent(captureImageIntent)


            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    plantDetailViewModel.plant.collect { plant ->
                        plant?.let { updateUi(it) }
                    }
                }
            }
            setFragmentResultListener(
                DatePickerFragment.REQUEST_KEY_DATE
            ) { _, bundle ->
                val newDate =
                    bundle.getSerializable(DatePickerFragment.BUNDLE_KEY_DATE) as Date
                plantDetailViewModel.updatePlant { it.copy(date = newDate) }
            }

            // Check if the record is existing before showing the delete button
            if (args.plantId != null && args.plantId != UUID.fromString("00000000-0000-0000-0000-000000000000")) {
                deleteButton.visibility = View.VISIBLE

                deleteButton.setOnClickListener {
                    plantDetailViewModel.deletePlant()
                    findNavController().navigateUp() // Navigate back after deletion
                }
            } else {
                // If the record is new, hide the delete button
                deleteButton.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUi(plant: Plant) {
        binding.apply {
            if (plantsTitle.text.toString() != plant.title) {
                plantsTitle.setText(plant.title)
            }
            plantsDate.text = DateFormat.format(DATE_FORMAT, plant.date).toString()

            plantsDate.setOnClickListener {
                findNavController().navigate(
                    PlantsDetailFragmentDirections.selectDate(plant.date)
                )
            }
            if (plantsPlace.text.toString() != plant.place) {
                plantsPlace.setText(plant.place)
            }
            plantFound.isChecked = plant.isFound

            plantReport.setOnClickListener {
                val reportIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getPlantReport(plant))
                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        getString(R.string.plant_report_subject)
                    )
                }
                plantReporter.text = plant.reporter.ifEmpty {
                    getString(R.string.plant_reporter_text)
                }

                startActivity(reportIntent)

                updatePhoto(plant.photoFileName)
            }
        }
    }



    private fun getPlantReport(plant: Plant): String {
        val solvedString = if (plant.isFound) {
            getString(R.string.plant_report_found)
        } else {
            getString(R.string.plant_report_not_found)
        }

        val dateString = DateFormat.format(DATE_FORMAT, plant.date).toString()
        val locationText = if (plant.place.isBlank()) {
            getString(R.string.plant_report_no_location)
        } else {
            getString(R.string.plant_report_location, plant.place)
        }

        return getString(
            R.string.plant_report,
            plant.title, dateString, solvedString, locationText
        )
    }
    private fun parseContactSelection(contactUri: Uri) {
        val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
        val queryCursor = requireActivity().contentResolver
            .query(contactUri, queryFields, null, null, null)
        queryCursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                val reporter = cursor.getString(0)
                plantDetailViewModel.updatePlant { oldPlant ->
                    oldPlant.copy(reporter = reporter)
                }
            }
        }
    }
    private fun canResolveIntent(intent: Intent): Boolean {

        val packageManager: PackageManager = requireActivity().packageManager
        val resolvedActivity: ResolveInfo? =
            packageManager.resolveActivity(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
        return resolvedActivity != null
    }
    private fun updatePhoto(photoFileName: String?) {
        if (binding.plantPhoto.tag != photoFileName) {
            val photoFile = photoFileName?.let {
                File(requireContext().applicationContext.filesDir, it)
            }
            if (photoFile?.exists() == true) {
                binding.plantPhoto.doOnLayout { measuredView ->
                    val scaledBitmap = getScaledBitmap(
                        photoFile.path,
                        measuredView.width,
                        measuredView.height
                    )
                    binding.plantPhoto.setImageBitmap(scaledBitmap)
                    binding.plantPhoto.tag = photoFileName
                }
            } else {
                binding.plantPhoto.setImageBitmap(null)
                binding.plantPhoto.tag = null
            }
        }
    }
}