package com.example.cursos

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity

import com.google.gson.GsonBuilder

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.POST

class RegisterActivity : ComponentActivity() {
    private lateinit var authService: AuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        var registerInit = findViewById(R.id.registerInit) as Button //Boton de registrar
        var registerLogin = findViewById(R.id.registerLogin) as Button //Boton de ir al login
        var registerNombre = findViewById(R.id.registerNombre) as EditText //Campo de nombre
        var registerEmail = findViewById(R.id.registerEmail) as EditText //Campo de email
        var registerPassword = findViewById(R.id.registerPassword) as EditText //Campo de contraseña
        var registerTelefono = findViewById(R.id.registerTelefono) as EditText //Campo de telefono

        //Accion para el boton de registro
        registerInit.setOnClickListener() {
            val nombre = registerNombre.text.toString()
            val email = registerEmail.text.toString()
            val password = registerPassword.text.toString()
            val telefono = registerTelefono.text.toString()
            isValidRegisterForm(nombre,email, password,telefono, registerInit);
            registerInit.visibility = View.INVISIBLE;
        }

        //Accion para el boton de ir al login
        registerLogin.setOnClickListener() {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    //Funcion que realiza el registro del usuario
    private fun isValidRegisterForm(nombre: String,email: String, password: String, telefono: String, registerInit: Button) {
        val gson = GsonBuilder().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(MainActivity.url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        authService = retrofit.create(AuthService::class.java)

        val credentials = RegisterForm(nombre, email, password, telefono)
        authService.register(credentials).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                val registerResponse = response.body()
                if (response.isSuccessful) {
                    if (registerResponse != null) {
                        Log.d("API Response", "Success: ${registerResponse.response}")
                    }
                    if (response.body()?.response == true) {
                        showAlertDialog("Registro Correcto", "Ya puede iniciar sesión")

                        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("email", null)
                        editor.putString("password", null)
                        editor.apply()
                    } else {
                        showAlertDialog(
                            response.body()?.response.toString(),
                            ""
                        )
                    }
                }else{
                    Log.d("API Response", "Fail: ${registerResponse}")
                }
                registerInit.visibility = View.VISIBLE;
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Log.e("API Response", "Failure: ${t.message}")
                showAlertDialog("Registro Incorrecto", "Credenciales inválidas")
                registerInit.visibility = View.VISIBLE;
            }
        })
    }


    //API de registro
    interface AuthService {
        @POST("auth/register")
        fun register(@Body credentials: RegisterForm): Call<RegisterResponse>
    }

    //Clase para registro
    data class RegisterForm(
        val nombre: String,
        val email: String,
        val password: String,
        val telefono: String
    )

    //Clase para respuesta del registro
    data class RegisterResponse(
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
                goToLogin()
            }
            setCancelable(false)
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    //Funcion que devuelve al login
    private fun goToLogin(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}