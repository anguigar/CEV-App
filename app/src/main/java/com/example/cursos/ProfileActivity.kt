package com.example.cursos

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

import com.google.gson.GsonBuilder

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.ByteArrayOutputStream
import java.io.IOException

class ProfileActivity : ComponentActivity() {
    private lateinit var authService: AuthService
    private lateinit var authServiceUser: HomeActivity.DataUserService
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_profile)

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val savedEmail = sharedPreferences.getString("email", null)
        if (savedEmail != null){
            checkUser(savedEmail);
        }else {
            goToHome()
        }

        var profileInit = findViewById(R.id.profileInit) as Button //Boton de actualizar perfil
        var profileHome = findViewById(R.id.profileHome) as Button //Boton de volver a Inicio

        var profileId = findViewById(R.id.profileId) as EditText //Campo Id
        var profileImagen = findViewById(R.id.profileImagen) as EditText //Campo Imagen
        var profileNombre = findViewById(R.id.profileNombre) as EditText //Campo nombre
        var profileEmail = findViewById(R.id.profileEmail) as EditText //Campo email
        var profileTelefono = findViewById(R.id.profileTelefono) as EditText //Campo telefono
        var profilePassword = findViewById(R.id.profilePassword) as EditText //Campo contraseña


        //Accion para el boton actualizar perfil
        profileInit.setOnClickListener() {
            val id = profileId.text.toString()
            val imagen = profileImagen.text.toString()
            val nombre = profileNombre.text.toString()
            val email = profileEmail.text.toString()
            val telefono = profileTelefono.text.toString()
            val password = profilePassword.text.toString()
            isValidProfileForm(id,nombre,email,telefono,password,imagen);
        }

        //Accion para el boton volver al inicio
        profileHome.setOnClickListener() {
            goToHome()
        }

        //Funcion que permite seleccionar una imagen de tus archivos
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val imageUri = result.data?.data
                val inputStream = contentResolver.openInputStream(imageUri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()

                val base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT)

                if (base64Image != ""){
                    val imageView = findViewById<ImageView>(R.id.logoCEV)
                    val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
                    val decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    imageView.setImageBitmap(decodedBitmap)

                    var profileImagen = findViewById(R.id.profileImagen) as EditText
                    profileImagen.setText(base64Image)
                }
            }
        }

        //Acción al darle el logo, q abra el explorador de archivos
        val logoCEV = findViewById<ImageView>(R.id.logoCEV)
        logoCEV.setOnClickListener { view ->
            openGallery()
        }

        //Acción al darle el titulo, q abra el explorador de archivos
        var textChangePhoto = findViewById<TextView>(R.id.textChangePhoto)
        textChangePhoto.setOnClickListener { view ->
            openGallery()
        }
    }

    //Funcion de abrir explorador de archivos
    private fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        pickImageLauncher.launch(intent)
    }

    //Funcion de volver al inicio
    fun goToHome(){
        val intent = Intent(this@ProfileActivity, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    //Obtiene la información del usuario
    private fun checkUser(email: String) {
        val gson = GsonBuilder().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(MainActivity.url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        authServiceUser = retrofit.create(HomeActivity.DataUserService::class.java)

        val credentials = HomeActivity.Credentials(email)
        authServiceUser.dataUser(credentials).enqueue(object : Callback<HomeActivity.DataUserResponse> {
            override fun onResponse(call: Call<HomeActivity.DataUserResponse>, response: Response<HomeActivity.DataUserResponse>) {
                val dataResponse = response.body()
                if (dataResponse != null) {
                    if (response.isSuccessful && dataResponse.success) {
                        if (dataResponse != null) {
                            var profileId = findViewById(R.id.profileId) as EditText
                            var profileImagen = findViewById(R.id.profileImagen) as EditText
                            var profileNombre = findViewById(R.id.profileNombre) as EditText
                            var profileEmail = findViewById(R.id.profileEmail) as EditText
                            var profileTelefono = findViewById(R.id.profileTelefono) as EditText

                            profileId.setText(dataResponse.response.id)
                            profileNombre.setText(dataResponse.response.nombre)
                            profileEmail.setText(dataResponse.response.email)
                            profileTelefono.setText(dataResponse.response.telefono)

                            if (dataResponse.response.imagen != null){
                                try {
                                    val imageView = findViewById<ImageView>(R.id.logoCEV)
                                    val decodedBytes = Base64.decode(dataResponse.response.imagen, Base64.DEFAULT)
                                    val decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                    imageView.setImageBitmap(decodedBitmap)
                                    profileImagen.setText(dataResponse.response.imagen)
                                } catch (e: IOException) {
                                }
                            }

                            Log.d("API Response", "Data: ${dataResponse.response}")
                        }
                    }else{
                        Log.d("API Response", "Fail: ${dataResponse}")
                    }
                }else{
                    Log.d("API Response", "Fail: ${dataResponse}")
                }
            }

            override fun onFailure(call: Call<HomeActivity.DataUserResponse>, t: Throwable) {
                Log.e("API Response", "Failure: ${t.message}")
            }
        })
    }

    //Funcion que valida la actualizacion de la informacion del perfil
    private fun isValidProfileForm(id: String, nombre: String,email: String, telefono: String, password: String,imagen:String) {
        val gson = GsonBuilder().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(MainActivity.url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        authService = retrofit.create(AuthService::class.java)

        val credentials = ProfileForm(id, nombre, email, telefono, password,imagen)
        authService.profile(credentials).enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                val profileResponse = response.body()
                if (response.isSuccessful) {
                    if (profileResponse != null) {
                        Log.d("API Response", "Success: ${profileResponse.response}")
                    }
                    if (response.body()?.response == true) {

                        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("email", email)
                        if (password != ""){
                            editor.putString("password", password)
                        }
                        editor.apply()

                        showAlertDialog("Perfil Actualizado", "")
                    } else {
                        showAlertDialog(
                            response.body()?.response.toString(),
                            ""
                        )
                    }
                }else{
                    Log.d("API Response", "Fail: ${profileResponse}")
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                Log.e("API Response", "Failure: ${t.message}")
                showAlertDialog("Profile Incorrecto", "Credenciales inválidas")
            }
        })
    }

    //API de editar perfil
    interface AuthService {
        @POST("auth/editProfile")
        fun profile(@Body credentials: ProfileForm): Call<ProfileResponse>
    }

    //Clase para perfil
    data class ProfileForm(
        val id: String,
        val nombre: String,
        val email: String,
        val telefono: String,
        val password: String,
        val imagen: String
    )

    //Clase para respuesta del perfil
    data class ProfileResponse(
        val response: Any
    )

    //Funcion que muestra la alerta
    private fun showAlertDialog(title: String, message: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}