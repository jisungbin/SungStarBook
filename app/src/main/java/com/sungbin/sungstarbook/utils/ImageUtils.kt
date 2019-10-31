package com.sungbin.sungstarbook.utils


import android.os.AsyncTask
import android.os.Environment
import com.facebook.FacebookSdk
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object ImageUtils {
    fun checkExist(path: String): Boolean{
        val imageFile = File(
            "${Environment.getExternalStorageDirectory().absolutePath}/$path")
        return imageFile.exists()
    }

    fun download(path: String, url: String) {
        val parentPath = path.replaceLast("/", "*").split("*")[0]
        val parentFile = File(
            "${Environment.getExternalStorageDirectory().absolutePath}/$parentPath")
        if(!parentFile.exists()) parentFile.mkdirs()
        ImageDownloadTask().execute(path, url)
    }

    private fun String.replaceLast(regex: String, replacement: String): String{
        val regexIndexOf = this.lastIndexOf(regex)
        return if(regexIndexOf == -1) this
        else {
            this.substring(0, regexIndexOf) + this.substring(regexIndexOf).replace(regex, replacement)
        }
    }

    private class ImageDownloadTask : AsyncTask<String?, Void?, Void?>() {
        override fun doInBackground(vararg params: String?): Void? {
            val imageFile = File(
                "${Environment.getExternalStorageDirectory().absolutePath}/${params[0]}")
            try {
                val imgUrl = URL(params[1])
                val conn = imgUrl.openConnection() as HttpURLConnection
                val len = conn.contentLength
                val tmpByte = ByteArray(len)
                val `is` = conn.inputStream
                val fos = FileOutputStream(imageFile)
                var read: Int

                while (true) {
                    read = `is`.read(tmpByte)
                    if (read <= 0) {
                        break
                    }
                    fos.write(tmpByte, 0, read)
                }

                `is`.close()
                fos.close()
                conn.disconnect()
            } catch (e: Exception) {
                Utils.error(FacebookSdk.getApplicationContext(), e.toString())
            }

            return null
        }

        override fun onPostExecute(result: Void?) {
            return
        }

    }
}