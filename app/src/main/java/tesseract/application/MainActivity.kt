package tesseract.application

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_main.*
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        mSelectedImage = null
        when (position) {
            0 -> mSelectedImage = getBitmapFromAsset(this, "20190612_161854.jpg")
            1 -> mSelectedImage = getBitmapFromAsset(this, "20190611_163816.jpg")
            2 -> mSelectedImage = getBitmapFromAsset(this, "20190611_163931.jpg")
            3 -> mSelectedImage = getBitmapFromAsset(this, "20190611_164039.jpg")
            4 -> mSelectedImage = getBitmapFromAsset(this, "20190611_164808.jpg")
            5 -> mSelectedImage = getBitmapFromAsset(this, "20190612_160053.jpg")
            6 -> mSelectedImage = getBitmapFromAsset(this, "20190612_160330.jpg")
            7 -> mSelectedImage = getBitmapFromAsset(this, "20190612_160550.jpg")
            8 -> mSelectedImage = getBitmapFromAsset(this, "20190612_160758.jpg")
            9 -> mSelectedImage = getBitmapFromAsset(this, "20190612_160845.jpg")
            10 -> mSelectedImage = getBitmapFromAsset(this, "20190612_160933.jpg")
            11 -> mSelectedImage = getBitmapFromAsset(this, "20190612_161019.jpg")
            12 -> mSelectedImage = getBitmapFromAsset(this, "20190612_161141.jpg")
            13 -> mSelectedImage = getBitmapFromAsset(this, "20190612_161550.jpg")
            14 -> mSelectedImage = getBitmapFromAsset(this, "20190612_161821.jpg")
        }
        imageView!!.setImageBitmap(mSelectedImage)
        DoOCRTask(text,this@MainActivity,mSelectedImage!!).execute()
    }


    private var mSelectedImage: Bitmap? = null
    private var mTessOCR: TessOCR? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        button_camera.setOnClickListener {
                            EasyImage.openCamera(this@MainActivity, 123)
                        }

                        button_gallery.setOnClickListener {
                            EasyImage.openGallery(this@MainActivity, 123)
                        }
                    }
                }).check()


        val dropdown = findViewById<Spinner>(R.id.spinner)
        val items = arrayOf(
                "Yudha (Normal)",
                "Sandi (Normal)",
                "Fawzi (With finger)",
                "Fawzi (Less finger)",
                "Suseno (Normal)",
                "Chandra (Low light)",
                "Johannes (Low Light)",
                "Fawzi (Low Light)",
                "Pauly (Glare)",
                "Fawzi (Glare)",
                "Wahyu (Low Light)",
                "Pauly (Low Light)",
                "Lucky (Tilted)",
                "Yudha (Low Light & Tilted)",
                "Yudha (Glare)")
        val adapter = ArrayAdapter(this, android.R.layout
                .simple_spinner_dropdown_item, items)
        dropdown.adapter = adapter
        dropdown.onItemSelectedListener = this
    }

    private fun doOCR(bitmap: Bitmap) {
        runOnUiThread {
            mTessOCR = TessOCR(this@MainActivity)
            if (mTessOCR != null) {
                val srcText = mTessOCR!!.getOCRResult(bitmap)
                text.text = handleResult(srcText)
                mTessOCR!!.onDestroy()
            } else
                Toast.makeText(this@MainActivity, "Thanos got tesseract!!!! Avengers assemble", Toast.LENGTH_LONG).show()
        }
    }

    class DoOCRTask(textView: TextView, context: Context, bitmap: Bitmap) : AsyncTask<Unit, String, String>() {

        val textView: TextView? = textView
        val context: Context? = context
        val bitmap: Bitmap? = bitmap
        private var mTessOCR: TessOCR? = null

        fun handleResult(result: String?): String {
            return Regex("""\d{16}""").find(result!!)?.value!!
        }

        override fun onPreExecute() {
            super.onPreExecute()
            textView!!.text = "Processing..result will appear soon"
        }

        override fun doInBackground(vararg params: Unit?): String? {
            try {
                mTessOCR = TessOCR(context!!)
                val srcText = mTessOCR!!.getOCRResult(bitmap!!)
                mTessOCR!!.onDestroy()
                return srcText
            }catch(e : Exception){
                return e.message
            }

        }


        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            textView!!.text = handleResult(result)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {
            override fun onImagePicked(imageFile: File?, source: EasyImage.ImageSource?, type: Int) {
                text.text = ""
                handleBitmap(imageFile)
            }

        })
    }

    fun handleBitmap(imageFile: File?) {
        try {
            var options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            var bitmap = BitmapFactory.decodeFile(imageFile?.absolutePath, options)
            imageView.setImageBitmap(bitmap)
            doOCR(bitmap)

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun handleResult(result: String?): String {
        return Regex("""\d{16}""").find(result!!)?.value!!
    }

    fun getBitmapFromAsset(context: Context, filePath: String): Bitmap? {
        val assetManager = context.assets
        val `is`: InputStream
        var bitmap: Bitmap? = null
        try {
            `is` = assetManager.open(filePath)
            bitmap = BitmapFactory.decodeStream(`is`)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return bitmap
    }

}
