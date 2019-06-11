package tesseract.application

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import com.googlecode.tesseract.android.TessBaseAPI
import com.snatik.storage.Storage
import android.content.res.AssetManager
import android.util.Log
import java.io.*


class TessOCR(context: Context){
    internal var mTess: TessBaseAPI? = TessBaseAPI()

    init{
        try {
            val path = Storage(context).externalStorageDirectory + "/tesseract/"
                mTess!!.pageSegMode = TessBaseAPI.PageSegMode.PSM_SINGLE_LINE
                mTess!!.init(path, "yud")
        } catch (e: IllegalArgumentException) {
            mTess = null
        }
    }


    fun getOCRResult(bitmap: Bitmap): String {
        mTess!!.setImage(bitmap)
        return mTess!!.utF8Text
    }

    fun onDestroy() {
        mTess?.end()
    }

    fun getAssetModelFilePath(context: Context) : String{
        val storage = Storage(context)
        val f = File(storage.internalCacheDirectory + "/tesseract/tessdata/yud.traineddata")
        return f.path
    }

    private fun copyAssets(context: Context) {
        val assetManager = context.assets
        var files: Array<String>? = null
        try {
            files = assetManager.list("")
        } catch (e: IOException) {
            Log.e("tag", "Failed to get asset file list.", e)
        }


        val storage = Storage(context)

        if (files != null)
            for (filename in files) {
                var `in`: InputStream? = null
                var out: OutputStream? = null
                try {
                    `in` = assetManager.open(filename)
                    val outFile = File(storage.internalCacheDirectory + "/tessdata/", filename)
                    out = FileOutputStream(outFile!!)
                    copyFile(`in`, out)
                } catch (e: IOException) {
                    Log.e("tag", "Failed to copy asset file: $filename", e)
                } finally {
                    if (`in` != null) {
                        try {
                            `in`.close()
                        } catch (e: IOException) {
                            // NOOP
                        }

                    }
                    if (out != null) {
                        try {
                            out.close()
                        } catch (e: IOException) {
                            // NOOP
                        }

                    }
                }
            }
    }


    @Throws(IOException::class)
    private fun copyFile(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        val read = `in`.read(buffer)
        while ((read) != -1) {
            out.write(buffer, 0, read)
        }
    }

}
