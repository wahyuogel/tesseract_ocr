package tesseract.application

import android.content.Context
import android.graphics.Bitmap
import com.googlecode.tesseract.android.TessBaseAPI
import com.snatik.storage.Storage
import java.io.File
import java.io.FileOutputStream


class TessOCR(context: Context){
    internal var mTess: TessBaseAPI? = TessBaseAPI()

    init{
        try {
            val path = getAssetModelFilePath(context)
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
        val f = File(storage.externalStorageDirectory + "/tessdata/yud.traineddata")
        if (!f.exists())
            try {

                val `is` = context.assets.open("yud.traineddata")
                val size = `is`.available()
                val buffer = ByteArray(size)
                `is`.read(buffer)
                `is`.close()


                val fos = FileOutputStream(f)
                fos.write(buffer)
                fos.close()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }

        return f.path
    }
}
