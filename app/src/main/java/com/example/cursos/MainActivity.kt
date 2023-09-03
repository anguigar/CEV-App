package com.example.cursos

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import androidx.activity.ComponentActivity
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : ComponentActivity() {
    private lateinit var authService: LoginActivity.AuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        //Obtener datos para revisar si inicio sesion
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val savedEmail = sharedPreferences.getString("email", null)
        val savedPassword = sharedPreferences.getString("password", null)

        if (savedEmail != null && savedPassword != null){
            //Comprobar datos para iniciar sesion
            isValidCredentials(savedEmail,savedPassword);
        }else {
            //Ir al login si sesion no existe
            goToLogin()
        }
    }

    //Funcion para validar login
    private fun isValidCredentials(email: String, password: String) {
        val gson = GsonBuilder().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        authService = retrofit.create(LoginActivity.AuthService::class.java)

        val credentials = LoginActivity.Credentials(email, password)
        authService.login(credentials).enqueue(object : Callback<LoginActivity.LoginResponse> {
            override fun onResponse(call: Call<LoginActivity.LoginResponse>, response: Response<LoginActivity.LoginResponse>) {
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

                        val intent = Intent(this@MainActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else{
                        goToLogin();
                        Log.d("API Response", "Fail: ${loginResponse}")
                    }
                }else{
                    goToLogin();
                    Log.d("API Response", "Fail: ${loginResponse}")
                }
            }

            override fun onFailure(call: Call<LoginActivity.LoginResponse>, t: Throwable) {
                Log.e("API Response", "Failure: ${t.message}")
                goToLogin();
            }
        })
    }

    //Funcion para volver al login
    private fun goToLogin(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    //URL base de la API
    companion object {
        const val url: String = "http://192.168.1.33:8080/";
    }
}