package com.example.pocketml.ui.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.api.load
import com.example.pocketml.R
import com.example.pocketml.databinding.FragmentDatasetManagerDetailBinding
import com.example.pocketml.ui.viewmodel.DetailViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

const val REQUEST_IMAGE_OPEN = 1

class DetailFragment : Fragment() {

    private val viewModel: DetailViewModel by viewModel()
    private val args: DetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentDatasetManagerDetailBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_dataset_manager_detail, container, false
        )
        binding.lifecycleOwner = this

        viewModel.setId(args.id)

        val adapter = ArrayAdapter<String>(
            requireContext(),
            R.layout.list_item_dataset_manager_classes,
        )
        viewModel.dClasses.observe(viewLifecycleOwner) { dClasses ->
            dClasses?.let { classNames ->
                adapter.clear()
                adapter.addAll(classNames)
            }
        }
        // Set Adapter for classes suggestions
        (binding.dClassTextInput as? AutoCompleteTextView)?.setAdapter(adapter)

        viewModel.dImageUri.observe(viewLifecycleOwner) { uri ->
            Timber.d("Observed dImageImageUri: $uri")
            setImage(binding, uri)
        }

        viewModel.existingDImage.observe(viewLifecycleOwner) { dImage ->
            binding.dClassTextInput.setText(dImage?.dClass)
        }

        binding.dImageView.setOnClickListener {
            selectImage()
        }

        binding.saveButton.setOnClickListener {
            viewModel.onSave()
        }

        viewModel.navigateToOverview.observe(viewLifecycleOwner, {
            if (it) {
                findNavController().popBackStack()
                viewModel.doneNavigateToOverview()
            }
        })

        binding.cancelButton.setOnClickListener {
            viewModel.onNavigateToOverview()
        }

        binding.dClassTextInput.doAfterTextChanged {
            viewModel.setDClassInputText(it.toString())
        }

        viewModel.isDClassInputTextValid.observe(viewLifecycleOwner) {
            binding.dClassTextInput.error =
                if (it) null else getString(R.string.d_class_input_error)
        }

        viewModel.isDataValid.observe(viewLifecycleOwner) {
            binding.saveButton.isEnabled = it
        }

        viewModel.makeToast.observe(viewLifecycleOwner) {
            it?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.doneMakingToast()
            }
        }

        return binding.root
    }

    private fun setImage(binding: FragmentDatasetManagerDetailBinding, uri: Uri?) {
        val default = R.drawable.ic_baseline_image_240
        binding.dImageView.load(uri) {
            placeholder(default)
            fallback(default)
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, REQUEST_IMAGE_OPEN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_OPEN && resultCode == Activity.RESULT_OK) {
            data?.data?.let { path ->
                viewModel.setLocalDImageUri(path)
            }
        }
    }
}
