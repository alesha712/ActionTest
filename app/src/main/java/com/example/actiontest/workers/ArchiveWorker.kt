package com.example.actiontest.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.actiontest.utils.deleteItem
import com.example.actiontest.utils.queryImages

class ArchiveWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        val items = queryImages(applicationContext)
        items.forEach { singleItem ->
            run {
                deleteItem(applicationContext, singleItem)
            }
        }

        return Result.success()
    }
}