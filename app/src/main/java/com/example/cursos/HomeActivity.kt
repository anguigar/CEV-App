package com.example.cursos

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract.Profile
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.navigation.NavigationView
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.InputStream
import java.net.URL

class HomeActivity : AppCompatActivity() {
    private lateinit var authService: DataUserService
    private lateinit var authServiceCurso: CarouselFragment.DataCursoService

    private val carouselAdapter = CarouselAdapter(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        //Obtiene email del usuario logueado
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val savedEmail = sharedPreferences.getString("email", null)

        //Habilita la galeria de cursos
        var viewPager = findViewById(R.id.viewPager) as ViewPager2
        viewPager.adapter = carouselAdapter
        loadData()

        if (savedEmail != null){
            //Chequea si existe usuario para mantener la sesion
            checkUser(savedEmail);
        }else {
            //Devuelve al login si no esta logueado
            goToLogin()
        }

        //Habilita el menu
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)

        //Opciones del menu
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                //Ir a editar perfil
                R.id.nav_edit_profile -> {
                    goToProfile()
                }
                //Ir a contacto
                R.id.nav_contact -> {
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse("https://www.cev.com/contacto/")
                    startActivity(i)
                }
                //Ir a cerrar sesion
                R.id.nav_logout -> {
                    val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("email", null)
                    editor.putString("password", null)
                    editor.apply()

                    goToLogin()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e("Entre a Home", "Ahora");
        reloadData();
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_menu -> {
                val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //Funcion para obtener la informacion del usuario
    private fun checkUser(email: String) {
        val gson = GsonBuilder().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(MainActivity.url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        authService = retrofit.create(DataUserService::class.java)

        val credentials = Credentials(email)
        authService.dataUser(credentials).enqueue(object : Callback<DataUserResponse> {
            override fun onResponse(call: Call<DataUserResponse>, response: Response<DataUserResponse>) {
                val dataResponse = response.body()
                if (dataResponse != null) {
                    if (response.isSuccessful && dataResponse.success) {
                        if (dataResponse != null) {
                            var textWelcome = findViewById(R.id.textWelcome) as TextView
                            val welcomeText = "Bienvenido ${dataResponse.response.nombre}"
                            //textWelcome.text = welcomeText

                            val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("id", dataResponse.response.id)
                            editor.apply()

                            setTitle(welcomeText);

                            Log.d("API Response", "Data: ${dataResponse.response}")
                        }
                    }else{
                        goToLogin();
                        Log.d("API Response", "Fail: ${dataResponse}")
                    }
                }else{
                    goToLogin();
                    Log.d("API Response", "Fail: ${dataResponse}")
                }
            }

            override fun onFailure(call: Call<DataUserResponse>, t: Throwable) {
                Log.e("API Response", "Failure: ${t.message}")
                goToLogin();
            }
        })
    }

    //API para la data de usuariuo
    interface DataUserService {
        @POST("auth/dataUser")
        fun dataUser(@Body credentials: Credentials): Call<DataUserResponse>
    }


    //Valores a enviar para obtener la informacion de usuario
    data class Credentials(
        val email: String
    )

    //Clase de respuesta de la informacion de usuario
    data class DataUserResponse(
        val response: ResponseData,
        val success: Boolean
    )

    //Clase de respuesta de la informacion de usuario
    data class ResponseData(
        val id: String,
        val nombre: String,
        val email: String,
        val telefono: String,
        val imagen: String,
    )

    //Funcion que lleva a editar el perfil
    private fun goToProfile(){
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
        finish()
    }

    //Funcion que llev al login
    private fun goToLogin(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    //Carga la informacion para la galeria de cursos
    private fun loadData() {
        val gson = GsonBuilder().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(MainActivity.url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        authServiceCurso = retrofit.create(CarouselFragment.DataCursoService::class.java)

        authServiceCurso.dataCurso().enqueue(object : Callback<CarouselFragment.DataCursoResponse> {
            override fun onResponse(call: Call<CarouselFragment.DataCursoResponse>, response: Response<CarouselFragment.DataCursoResponse>) {
                val dataResponse = response.body()
                if (dataResponse != null) {
                    if (response.isSuccessful && dataResponse.success) {
                        if (dataResponse != null) {
                            carouselAdapter.setItemCount(dataResponse.response.size)
                        }
                    }else{
                        carouselAdapter.setItemCount(0)
                        Log.d("API Curso Response", "Fail: ${dataResponse}")
                    }
                }else{
                    carouselAdapter.setItemCount(0)
                    Log.d("API Curso Response", "Fail: ${dataResponse}")
                }
            }

            override fun onFailure(call: Call<CarouselFragment.DataCursoResponse>, t: Throwable) {
                carouselAdapter.setItemCount(0)
                Log.e("API Curso Response", "Failure: ${t.message}")
            }
        })
    }


    //Funcion para recargar la información de galeria de cursos
    fun reloadData(){
        loadData();
        carouselAdapter.setItemCount(0)
        carouselAdapter.notifyDataSetChanged();
    }

    //Habilita la galeria de cursos
    class CarouselAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
        private var itemCount = 0

        fun setItemCount(count: Int) {
            this.itemCount = count
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = itemCount

        override fun createFragment(position: Int): Fragment {
            return CarouselFragment.newInstance(position)
        }
    }

}