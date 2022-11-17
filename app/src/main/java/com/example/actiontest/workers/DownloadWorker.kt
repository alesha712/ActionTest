package com.example.actiontest.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.actiontest.FILE_IMAGE
import com.example.actiontest.NOTIFICATION_ID
import com.example.actiontest.NOTIFICATION_TITLE
import com.example.actiontest.api.DogApi
import com.example.actiontest.utils.clearNotification
import com.example.actiontest.utils.downloadAndSaveImage
import com.example.actiontest.utils.generateFileName
import com.example.actiontest.utils.showStatusNotification
import org.koin.java.KoinJavaComponent.get

class DownloadWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private val dogApi = get<DogApi>(DogApi::class.java)

    override fun doWork(): Result {
        val appContext = applicationContext
        //Show notification when work begins
        showStatusNotification(NOTIFICATION_TITLE, appContext)

        //Getting data from server (array of URLs) and saving them as images to the device
        try {
            val dogData = dogApi.callApi().execute()
            val dogsArr = dogData.body()?.message
            dogsArr?.forEach { url ->
                run {
                    val filename = generateFileName()
                    downloadAndSaveImage(appContext, url, FILE_IMAGE, filename)
                }
            }

            //Clear notification when work completes
            clearNotification(NOTIFICATION_ID, appContext)
            return Result.success()
        } catch (err: Exception) {
            println(err)
            clearNotification(NOTIFICATION_ID, appContext)
            return Result.failure()
        }
    }
}