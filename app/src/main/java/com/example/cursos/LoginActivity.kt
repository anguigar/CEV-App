package com.example.cursos

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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

class LoginActivity : ComponentActivity() {
    private lateinit var authService: AuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        var loginInit = findViewById(R.id.loginInit) as Button //Boton de login
        var loginRegister = findViewById(R.id.loginRegister) as Button //Boton de registro
        var recoverLogin = findViewById(R.id.recoverLogin) as TextView //Boton de recuperar contraseña
        var loginEmail = findViewById(R.id.loginEmail) as EditText //Campo de email
        var loginPassword = findViewById(R.id.loginPassword) as EditText //Campo de contraseña

        //Accion para el boton de login
        loginInit.setOnClickListener() {
            val email = loginEmail.text.toString()
            val password = loginPassword.text.toString()
            isValidCredentials(email, password);
        }

        //Accion para el boton de registro
        loginRegister.setOnClickListener() {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        //Accion para el boton de recuperar contraseña
        recoverLogin.setOnClickListener() {
            val intent = Intent(this@LoginActivity, RecoverActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    //Funcion para validar login
    private fun isValidCredentials(email: String, password: String) {
        val gson = GsonBuilder().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(MainActivity.url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        authService = retrofit.create(AuthService::class.java)

        val credentials = Credentials(email, password)
        authService.login(credentials).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                val loginResponse = response.body()
                if (response.isSuccessful) {
                    if (loginResponse != null) {
                        Log.d("API Response", "Success: ${loginResponse.response}")
                    }
                    if (response.body()?.response == true) {

                        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("email", email)
                        editor.putString("password", password)
                        editor.apply()

                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        if (response.body()?.response == false) {
                            showAlertDialog("Login Incorrecto", "Credenciales inválidas")
                        } else {
                            showAlertDialog(
                                response.body()?.response.toString(),
                                ""
                            )
                        }
                    }
                }else{
                    Log.d("API Response", "Fail: ${loginResponse}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("API Response", "Failure: ${t.message}")
                showAlertDialog("Login Incorrecto", "Credenciales inválidas")
            }
        })
    }

    //API de login
    interface AuthService {
        @POST("auth/login")
        fun login(@Body credentials: Credentials): Call<LoginResponse>
    }

    //Clase para login
    data class Credentials(
        val email: String,
        val password: String
    )

    //Clase para respuesta del login
    data class LoginResponse(
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