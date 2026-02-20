package com.apkextractor.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File

object DocumentFileHelper {

    /**
     * Save APK to a folder using DocumentFile.
     * Requires a tree URI from ACTION_OPEN_DOCUMENT_TREE.
     */
    fun saveApkToFolder(
        context: Context,
        folderUri: Uri,
        sourceApkPath: String,
        filename: String,
        onProgress: ((Int) -> Unit)? = null
    ): Result<Uri> {
        return try {
            val folder = DocumentFile.fromTreeUri(context, folderUri)
                ?: return Result.failure(Exception("Invalid folder URI"))

            if (!folder.exists() || !folder.isDirectory) {
                return Result.failure(Exception("Folder does not exist or is not a directory"))
            }

            // Delete existing file with same name
            folder.findFile(filename)?.delete()

            // Create new file
            val newFile = folder.createFile("application/vnd.android.package-archive", filename)
                ?: return Result.failure(Exception("Failed to create file"))

            val sourceFile = File(sourceApkPath)
            if (!sourceFile.exists() || !sourceFile.canRead()) {
                return Result.failure(Exception("Cannot read source APK"))
            }

            val totalBytes = sourceFile.length()
            var copiedBytes = 0L

            context.contentResolver.openOutputStream(newFile.uri)?.use { output ->
                sourceFile.inputStream().use { input ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        copiedBytes += read
                        onProgress?.invoke(((copiedBytes * 100) / totalBytes).toInt())
                    }
                }
            } ?: return Result.failure(Exception("Could not open output stream"))

            Result.success(newFile.uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get folder display name from tree URI.
     */
    fun getFolderName(context: Context, folderUri: Uri): String? {
        return try {
            val folder = DocumentFile.fromTreeUri(context, folderUri)
            folder?.name
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Create intent to open folder in file manager.
     * Returns null if not supported.
     */
    fun createOpenFolderIntent(folderUri: Uri): Intent? {
        return try {
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(folderUri, "vnd.android.document/directory")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Check if the folder URI is still valid and accessible.
     */
    fun isFolderAccessible(context: Context, folderUri: Uri): Boolean {
        return try {
            val folder = DocumentFile.fromTreeUri(context, folderUri)
            folder?.exists() == true && folder.isDirectory
        } catch (_: Exception) {
            false
        }
    }
}
