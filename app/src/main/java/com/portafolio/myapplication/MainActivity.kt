package com.portafolio.myapplication

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {
    /*---------------------------------------------------------------------------*/
    //ELEMENTOS VISUALES Y VARIABLES GLOBALES
    /*
    *
    *
    * */
    private lateinit var cardView: CardView
    private lateinit var bitmap: Bitmap
    private lateinit var guardar: Button
    private lateinit var open_camera: Button
    private lateinit var text: Button
    private lateinit var file: File
    private lateinit var editex: EditText
    private lateinit var textView: TextView
    private lateinit var img: ImageView

    /*
    *
    *
    * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        InstanciarObjet()
    }

    /*
    *
    * */

    override fun onStart() {
        super.onStart()
        botones()
    }


    private fun InstanciarObjet() {
        text = findViewById(R.id.text)
        open_camera = findViewById(R.id.open_camera)
        guardar = findViewById(R.id.save_to_gallery)
        cardView = findViewById(R.id.card_view)
        editex = findViewById(R.id.editTextTextEmailAddress)
        textView = findViewById(R.id.textview_first)
        img = findViewById(R.id.img)
    }


    private fun botones() {

        /*BOTON PARA GUARDAR EL TEXTO ESCRITO*/
        text.setOnClickListener {
            /**/
            textView.setText(editex.getText())

        }
        /*BOTON PARA GUARDAR CONTENIDO EN IMAGEN PUBLICA*/
        guardar.setOnClickListener {

            Convertir_Guardar()
        }


    }

    private fun Convertir_Guardar() {
        bitmap = convertCardViewToBitmap(cardView)
        file = File(cacheDir, "voucher.jpeg")
        val outputStream = FileOutputStream(file)
        //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        //  var contentT = bitmapToContentValues(bitmap)
        val result = convertBitmapToFile(this, bitmap)
        Log.e("TAG", "onViewCreated-convertBitmapToFile: " + result.name)
        /*------------------------------------------------------------------------------------*/

        val contentTT = createContent(result)
        Log.e("TAG", "contentTT: " + contentTT)
        if (contentTT != null) {
            val uri = save(contentTT, result)
            Log.e("TAG", "saveT: " + uri)
            clearContents(contentTT, uri)

        }
        Log.e("saveToGallery", "saveToGallery uri: " + result)

        /*------------------------------------------------------------------------------------*/


        //  outputStream.flush()
        //  outputStream.close()

        val file = File(Environment.getExternalStorageDirectory(), "image.jpeg")
        val uri =
            FileProvider.getUriForFile(this, this.packageName + ".fileprovider", file)
        Log.e("TAG", "ubicacion imagen: " + uri)


        val bitmap = getBitmap(result)
        img.setImageBitmap(bitmap)

        /*
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "image/*"
                intent.putExtra(Intent.EXTRA_STREAM, uri)

                startActivity(Intent.createChooser(intent, "Compartir imagen"))
        */

         */

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 200)
    }

    /*
        private val openCamera =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    // val data = result.data!!
                    // val bitmap = data.extras!!.get("data") as Bitmap
                    val imageUri = data.data
                    Log.i("TAG", "imageUri :" + imageUri)

                }
            }
        */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_STREAM, imageUri)

            startActivity(Intent.createChooser(intent, "Compartir imagen"))

            Log.i("TAG", "imageUri :" + imageUri)
        }
    }

    /*METODOS DE PROCESAMIENTO*/

    private fun convertBitmapToFile(context: Context, bitmap: Bitmap): File {
        // Create a directory to save the image
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir("Images", Context.MODE_APPEND)
        // file = File(file, "${UUID.randomUUID()}.jpg")
        file = File(file, "Capture.jpeg")

        try {
            // Create an OutputStream to write the bitmap data
            val stream: OutputStream = FileOutputStream(file)
            // Compress the bitmap and save it as a JPEG file
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Return the URI of the saved file
        //Uri.parse(file.toString())
        return file
    }

    private fun clearContents(content: ContentValues, uri: Uri) {
        content.clear()
        content.put(MediaStore.MediaColumns.IS_PENDING, 0)
        contentResolver.update(uri, content, null, null)
    }

    private fun convertCardViewToBitmap(cardView: CardView): Bitmap {
        val bitmap = Bitmap.createBitmap(cardView.width, cardView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        cardView.draw(canvas)
        return bitmap
    }

    private fun createContent(file: File): ContentValues {
        Log.e("saveToGallery", "saveToGallery content : " + file)
        Log.e("saveToGallery", "saveToGallery content : " + file.name)
        val fileName = file.name
        val fileType = "image/jpeg"
        return ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.Files.FileColumns.MIME_TYPE, fileType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
    }

    private fun save(content: ContentValues, file: File): Uri {
        var outputStream: OutputStream?
        var uri: Uri?
        application.contentResolver.also { resolver ->
            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, content)
            outputStream = resolver.openOutputStream(uri!!)
        }
        outputStream.use { output ->
            if (output != null) {
                getBitmapT(file).compress(Bitmap.CompressFormat.JPEG, 100, output)
            }
        }
        return uri!!
    }

    private fun getBitmapT(file: File): Bitmap {
        return BitmapFactory.decodeFile(file.toString())
    }

    private fun getBitmap(file: File): Bitmap {
        return BitmapFactory.decodeFile(file.toString())
    }

    private fun shareImage(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/jpeg"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(intent, "Share image"))
    }


}