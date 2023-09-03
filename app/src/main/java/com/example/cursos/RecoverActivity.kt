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

class RecoverActivity : ComponentActivity() {
    private lateinit var authService: AuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recover)

        var recoverInit = findViewById(R.id.recoverInit) as Button //Boton de enviar correo de recuperacion
        var recoverLogin = findViewById(R.id.recoverLogin) as Button //Boton de ir al login
        var recoverEmail = findViewById(R.id.recoverEmail) as EditText //Campo de email

        //Accion para el boton de recuperar contraseña
        recoverInit.setOnClickListener() {
            val email = recoverEmail.text.toString()
            recoverInit.visibility = View.INVISIBLE;
            isValidRecoverForm(email,recoverInit);
        }

        //Accion para volver al login
        recoverLogin.setOnClickListener() {
            val intent = Intent(this@RecoverActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    //Funcion para validar recuperar contraseña
    private fun isValidRecoverForm(email: String, recoverInit: Button) {
        val gson = GsonBuilder().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(MainActivity.url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        authService = retrofit.create(AuthService::class.java)

        val credentials = RecoverForm(email)
        authService.recover(credentials).enqueue(object : Callback<RecoverResponse> {
            override fun onResponse(call: Call<RecoverResponse>, response: Response<RecoverResponse>) {
                val recoverResponse = response.body()
                if (response.isSuccessful) {
                    if (recoverResponse != null) {
                        Log.d("API Response", "Success: ${recoverResponse.response}")
                    }
                    if (response.body()?.response == true) {
                        showAlertDialog("Email de recuperación enviado", "")
                    } else {
                        showAlertDialog(
                            response.body()?.response.toString(),
                            ""
                        )
                    }
                }else{
                    Log.d("API Response", "Fail: ${recoverResponse}")
                }
                recoverInit.visibility = View.VISIBLE;
            }

            override fun onFailure(call: Call<RecoverResponse>, t: Throwable) {
                Log.e("API Response", "Failure: ${t.message}")
                showAlertDialog("Email Incorrecto", "Credenciales inválidas")
                recoverInit.visibility = View.VISIBLE;
            }
        })
    }

    //API de Recuperar Contraseña
    interface AuthService {
        @POST("auth/recover")
        fun recover(@Body credentials: RecoverForm): Call<RecoverResponse>
    }

    //Clase para recuperar contraseña
    data class RecoverForm(
        val email: String
    )

    //Clase para respuesta de recuperar contraseña
    data class RecoverResponse(
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