package com.example.actiontest.utils

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.net.toUri
import com.example.actiontest.*
import com.example.actiontest.models.MediaStoreImage
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit

fun downloadAndSaveImage(context: Context, url: String, filetType: String, filename: String): Uri? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        //Specifying the file properties
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, filetType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        return if (uri != null) {
            URL(url).openStream().use { input ->
                //Setting the file data
                resolver.openOutputStream(uri).use { output ->
                    input.copyTo(output!!, DEFAULT_BUFFER_SIZE)
                }
            }
            uri
        } else {
            null
        }

    } else {

        val target = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            filename
        )
        URL(url).openStream().use { input ->
            FileOutputStream(target).use { output ->
                input.copyTo(output)
            }
        }

        return target.toUri()
    }
}

//This function will
fun queryImages(context: Context): List<MediaStoreImage> {
    val images = mutableListOf<MediaStoreImage>()

    //the list of columns to request from the provider
    //can be null to receive the whole data
    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.DATE_ADDED
    )

    //it is the "WHERE ..." clause of a SQL statement
    val selection = null

    /**
     * The `selectionArgs` is a list of values that will be filled in for each `?`
     * in the `selection`.
     */
    val selectionArgs = null

    /**
     * Sort order to use. This can also be null, which will use the default sort
     * order. For [MediaStore.Images], the default sort order is ascending by date taken.
     */
    val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

    context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        sortOrder
    )?.use { cursor ->

        /**
         * In order to retrieve the data from the [Cursor] that's returned, we need to
         * find which index matches each column that we're interested in.
         *
         * There are two ways to do this. The first is to use the method
         * [Cursor.getColumnIndex] which returns -1 if the column ID isn't found. This
         * is useful if the code is programmatically choosing which columns to request,
         * but would like to use a single method to parse them into objects.
         *
         * In our case, since we know exactly which columns we'd like, and we know
         * that they must be included (since they're all supported from API 1), we'll
         * use [Cursor.getColumnIndexOrThrow]. This method will throw an
         * [IllegalArgumentException] if the column named isn't found.
         *
         * In either case, while this method isn't slow, we'll want to cache the results
         * to avoid having to look them up for each row.
         */
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val dateModifiedColumn =
            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
        val displayNameColumn =
            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

        while (cursor.moveToNext()) {

            // Here we'll use the column indexs that we found above.
            val id = cursor.getLong(idColumn)
            val dateModified =
                Date(TimeUnit.SECONDS.toMillis(cursor.getLong(dateModifiedColumn)))
            val displayName = cursor.getString(displayNameColumn)


            /**
             * Since we're accessing images (using
             * [MediaStore.Images.Media.EXTERNAL_CONTENT_URI], we'll use that
             * as the base URI and append the ID of the image to it.
             */
            val contentUri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id
            )

            val image = MediaStoreImage(id, displayName, dateModified, contentUri)
            images += image
        }
    }
    return images
}

//Delete files
fun deleteItem(context: Context, item: MediaStoreImage) {
    context.contentResolver.delete(
        item.contentUri,
        "${MediaStore.Images.Media._ID} = ?",
        arrayOf(item.id.toString())
    )
}

//This functions generates a file name by appending a timestamp to a FILE_PREFIX constant
fun generateFileName(): String {
    val unixTime = System.currentTimeMillis()

    return FILE_PREFIX + "_" + unixTime.toString()
}