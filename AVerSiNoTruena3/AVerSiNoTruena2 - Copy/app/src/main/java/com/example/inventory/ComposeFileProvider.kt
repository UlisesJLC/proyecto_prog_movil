package com.example.inventory

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File

class ComposeFileProvider : FileProvider(
    R.xml.filepaths
){
    companion object {
        fun getVideoUri(context: Context): Uri? {
            val contentResolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, "video_${System.currentTimeMillis()}.mp4") // Nombre del archivo
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4") // Tipo MIME para video MP4
                put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES) // Directorio de almacenamiento (Movies)
            }

            // Insertamos el valor en el ContentProvider para obtener el URI
            val videoUri: Uri? = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)

            return videoUri
        }

        fun getImageUri(context: Context): Uri {
            // 1
            val directory = File(context.cacheDir, "images")
            directory.mkdirs()
            // 2
            val file = File.createTempFile(
                "selected_image_",
                ".jpg",
                directory
            )
            // 3
            val authority = context.packageName + ".fileprovider"
            // 4
            return getUriForFile(
                context,
                authority,
                file,
            )
        }
    }
}