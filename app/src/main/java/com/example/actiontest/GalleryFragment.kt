package com.example.actiontest

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.actiontest.adapters.GalleryAdapter
import com.example.actiontest.models.MediaStoreImage
import com.example.actiontest.viewModels.DogViewModel
import kotlinx.coroutines.launch

class GalleryFragment : Fragment() {

    companion object {
        fun newInstance() = GalleryFragment()
    }

    private lateinit var viewModel: DogViewModel
    private lateinit var getImagesBtn: Button
    private lateinit var deleteImagesBtn: Button
    private lateinit var recyclerView: RecyclerView
    lateinit var galleryAdapter: GalleryAdapter

    private lateinit var activityResultLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_gallery, container, false)

        //Init fragment views
        initViews(view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Permission Check - relevant if targeting lower that API 29
        initActivityResultLauncher()
        checkForPermissions()
        //init of recycler view and its' adapter
        setRecyclerView()
        //Buttons logic
        initButtons()
        //Init of the viewmodel
        setViewModel()
        //setting the viewmodel observers
        initObservers()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.galleryRV)
        getImagesBtn = view.findViewById(R.id.getImagesBtn)
        deleteImagesBtn = view.findViewById(R.id.deleteImagesBtn)
    }

    private fun setViewModel() {
        viewModel = ViewModelProvider(this)[DogViewModel::class.java]
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getImagesWork()
            viewModel.getAvailableImages()
        }
    }

    private fun initObservers() {
        // Create the observer which updates the UI.
        val listObserver = Observer<MutableList<MediaStoreImage>> { list ->
            // Update the recycler view.
            galleryAdapter.updateItems(list)
        }

        viewModel.files.observe(viewLifecycleOwner, listObserver)
    }

    private fun setRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        galleryAdapter = GalleryAdapter()
        recyclerView.adapter = galleryAdapter
    }

    private fun initButtons() {
        getImagesBtn.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.fetchNewImages()
            }
        }

        deleteImagesBtn.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.deleteAllImages()
            }
        }
    }

    private fun initActivityResultLauncher() {
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            var allAreGranted = true
            for (b in result.values) {
                allAreGranted = allAreGranted && b
            }
        }
    }

    private fun checkForPermissions() {
        val appPerms = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        activityResultLauncher.launch(appPerms)

    }
}