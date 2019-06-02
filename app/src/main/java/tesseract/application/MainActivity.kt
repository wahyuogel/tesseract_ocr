package tesseract.application

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
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

class MainActivity : AppCompatActivity() {

    private var mTessOCR: TessOCR? = null
    private var isPermitted: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

            Dexter.withActivity(this)
                    .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(object : MultiplePermissionsListener{
                        override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            EasyImage.openGallery(this@MainActivity, 123)
                        }
                    }).check()



    }

    private fun doOCR(bitmap: Bitmap) {
        runOnUiThread {
            mTessOCR = TessOCR(this@MainActivity)
            if (mTessOCR != null) {
                val srcText = mTessOCR!!.getOCRResult(bitmap)
                text.setText(srcText)
                mTessOCR!!.onDestroy()
            }
            else
                Toast.makeText(this@MainActivity, "Thanos got tesseract!!!! Avengers assemble", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {
            override fun onImagePicked(imageFile: File?, source: EasyImage.ImageSource?, type: Int) {
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

        });
    }


}
