package com.example.actiontest.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.example.actiontest.FILE_IMAGE
import com.example.actiontest.PERIODIC_WORK_ARCHIVE
import com.example.actiontest.PERIODIC_WORK_GET_IMAGES
import com.example.actiontest.models.MediaStoreImage
import com.example.actiontest.repo.DogRepo
import com.example.actiontest.utils.deleteItem
import com.example.actiontest.utils.downloadAndSaveImage
import com.example.actiontest.utils.generateFileName
import com.example.actiontest.utils.queryImages
import com.example.actiontest.workers.ArchiveWorker
import com.example.actiontest.workers.DownloadWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.get
import java.util.concurrent.TimeUnit

class DogViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = get<DogRepo>(DogRepo::class.java)

    private val workManager: WorkManager = WorkManager.getInstance(application)
    val files = MutableLiveData<MutableList<MediaStoreImage>>()

    //Starting the worker
    fun getImagesWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresCharging(true)
            .build()

        val myWork =
            PeriodicWorkRequestBuilder<DownloadWorker>(15, TimeUnit.MINUTES).setConstraints(
                constraints
            ).build()
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_GET_IMAGES,
            ExistingPeriodicWorkPolicy.KEEP,
            myWork
        )


        val archiveWork =
            PeriodicWorkRequestBuilder<ArchiveWorker>(2, TimeUnit.DAYS).setConstraints(constraints)
                .build()
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_ARCHIVE,
            ExistingPeriodicWorkPolicy.KEEP,
            archiveWork
        )
    }

    //Getting all images saved by the app
    suspend fun getAvailableImages() {
        withContext(Dispatchers.IO) {
            val items = queryImages(getApplication())
            files.postValue(items.toMutableList())
        }
    }

    //Fetching new images from server
    suspend fun fetchNewImages() {
        //"dogItem" holds an array of images urls
        val dogItem = repo.performCall()
        if (dogItem !== null) {
            val dogsArr = dogItem.message //the array of image urls'
            withContext(Dispatchers.IO) {
                dogsArr.forEach { url ->
                    run {
                        //Save each url as image on the device
                        val filename = generateFileName()
                        downloadAndSaveImage(getApplication(), url, FILE_IMAGE, filename)
                    }
                }
            }
            //Run getAvailableImages to update the "files" value
            getAvailableImages()
        }
    }

    //Delete all existing files downloaded by the app
    suspend fun deleteAllImages() {
        withContext(Dispatchers.IO) {
            files.value?.forEach { item ->
                run {
                    deleteItem(getApplication<Application>(), item)
                }
            }
            getAvailableImages()
        }
    }

}