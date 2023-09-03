package com.example.cursos

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


class DetalleActivity : AppCompatActivity() {

    private lateinit var authService: DetailService
    private lateinit var commentAdapter: CommentAdapter
    private val commentList = mutableListOf<String>()
    private val userList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Obtener el id del usuario logueado
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val usuario_id = sharedPreferences.getString("id", null)

        //Obtener los datos del detalle del curso que estas explorando
        val id = intent.getStringExtra("id")
        val curso_id = id;
        val imagen = intent.getStringExtra("imagen")
        val titulo = intent.getStringExtra("titulo")
        val descripcion = intent.getStringExtra("descripcion")
        val likes = intent.getStringExtra("likes")
        val url = intent.getStringExtra("url")

        val detalleLike = findViewById<Button>(R.id.detalleLike) //Boton me gusta
        val detalleNoLike = findViewById<Button>(R.id.detalleNoLike) //Boton no me gusta
        val tableLike = findViewById<TableRow>(R.id.tableLike) //Tabla donde esta el boton me gusta
        val tableNoLike = findViewById<TableRow>(R.id.tableNoLike) //Tabla donde esta el boton no me gusta

        tableLike.visibility = View.GONE;
        tableNoLike.visibility = View.GONE;

        //Obtiene detalle del curso
        dataCurso(curso_id.toString())
        //Obtiene si le ha dado me gusta
        checkLike(curso_id.toString(),usuario_id.toString());
        //Obtener los comentarios del curso
        dataComentario(curso_id.toString())

        val detailImageView = findViewById<ImageView>(R.id.detalleImagen) //Imagen del curso
        val detailTextView = findViewById<TextView>(R.id.detalleTitulo) //Titulo del curso
        val detailDescripcionTextView = findViewById<TextView>(R.id.detalleDescripcion) //Descripcion del curso
        val detailLikesTextView = findViewById<TextView>(R.id.detalleLikes) //Likes del curso
        val detalleInfoUrl = findViewById<TextView>(R.id.detalleInfoUrl) //Boton de ver mas informacion del curso

        //Establecer la imagen del detalle del curso
        Glide.with(this).load(imagen).into(detailImageView)

        //Mostrar informacion del curso
        detailTextView.text = titulo
        detailDescripcionTextView.text = descripcion
        detailLikesTextView.text = "${likes} Me Gusta"

        //Accion al boton de ver mas informacion
        detalleInfoUrl.setOnClickListener() {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }

        //Accion al boton de me gusta
        detalleLike.setOnClickListener() {
            tableLike.visibility = View.GONE;
            tableNoLike.visibility = View.VISIBLE;

            Log.e("Usuario:", usuario_id.toString());
            Log.e("Curso:", curso_id.toString());

            if (usuario_id != null && curso_id != null) {
                saveLike(usuario_id,curso_id)
            };
        }
        //Accion al boton de no me gusta
        detalleNoLike.setOnClickListener() {
            tableNoLike.visibility = View.GONE;
            tableLike.visibility = View.VISIBLE;

            if (usuario_id != null && curso_id != null) {
                deleteLike(usuario_id,curso_id)
            };
        }

        //Listar comentarios
        commentAdapter = CommentAdapter(commentList,userList)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = commentAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val btnSaveComment = findViewById<Button>(R.id.btnSaveComment) //Boton de guardar comentario
        val editComment = findViewById<EditText>(R.id.editComment) //Campo de escribir comentario
        //Accion del boton guardar comentario
        btnSaveComment.setOnClickListener {
            val newComment = editComment.text.toString()
            if (newComment.isNotEmpty()) {

                if (usuario_id != null && curso_id != null) {
                    saveComentario(newComment, curso_id, usuario_id);
                }

                commentList.add(newComment)
                userList.add("Yo")
                commentAdapter.notifyDataSetChanged()
                editComment.text.clear()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //Funcion que devuelve la informacion del curso
    fun dataCurso(curso_id: String){
        val gson = GsonBuilder().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(MainActivity.url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        authService = retrofit.create(DetailService::class.java)

        val credentials = Curso(curso_id)

        authService.data(credentials).enqueue(object : Callback<CursoResponse> {
            override fun onResponse(call: Call<CursoResponse>, response: Response<CursoResponse>) {
                val loginResponse = response.body()
                if (response.isSuccessful) {
                    if (loginResponse != null) {
                        Log.d("API Data", "Success: ${loginResponse}")

                        val detailLikesTextView = findViewById<TextView>(R.id.detalleLikes)
                        detailLikesTextView.text = "${loginResponse.likes} Me Gusta"
                    }
                }else{
                    Log.d("API Data", "Fail: ${loginResponse}")
                }
            }

            override fun onFailure(call: Call<CursoResponse>, t: Throwable) {
                Log.e("API Data", "Failure: ${t.message}")
            }
        })
    }

    //Funcion que devuelve el listado de comentarios
    fun dataComentario(curso_id: String){
        val gson = GsonBuilder().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(MainActivity.url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        authService = retrofit.create(DetailService::class.java)

        val curso = Curso(curso_id)

        val credentials = ComentarioData(curso)

        authService.dataComentario(credentials).enqueue(object : Callback<List<ComentarioItem>> {
            override fun onResponse(call: Call<List<ComentarioItem>>, response: Response<List<ComentarioItem>>) {
                if (response.isSuccessful) {
                    val comentarios = response.body()
                    if (comentarios != null) {
                        for (comentarioItem in comentarios) {
                            val usuario = comentarioItem.usuario
                            val curso = comentarioItem.curso
                            val comentario = comentarioItem.comentario

                            commentList.add(comentario)
                            userList.add(usuario.nombre)

                            commentAdapter.notifyDataSetChanged()
                        }
                    }
                } else {
                    Log.d("API Data Comentario", "Fail: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<ComentarioItem>>, t: Throwable) {
                Log.e("API Data Comentario", "Failure: ${t.message}")
            }
        })
    }

    //Funcion que comprueba el estado del me gusta
    fun checkLike(curso_id: String,usuario_id: String){
        val gson = GsonBuilder().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(MainActivity.url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        authService = retrofit.create(DetailService::class.java)

        val usuario = Usuario(usuario_id)
        val curso = Curso(curso_id)
        val credentials = Like(curso, usuario)
        val tableLike = findViewById<TableRow>(R.id.tableLike)
        val tableNoLike = findViewById<TableRow>(R.id.tableNoLike)

        authService.dataLike(credentials).enqueue(object : Callback<LikeDataResponse> {
            override fun onResponse(call: Call<LikeDataResponse>, response: Response<LikeDataResponse>) {
                val loginResponse = response.body()
                if (response.isSuccessful) {
                    if (loginResponse != null) {
                        Log.d("API Check Like", "Success: ${loginResponse}")


                        if (loginResponse.id != null){
                            tableLike.visibility = View.GONE;
                            tableNoLike.visibility = View.VISIBLE;
                        }else{
                            tableLike.visibility = View.VISIBLE;
                            tableNoLike.visibility = View.GONE;
                        }
                    }
                }else{
                    Log.d("API Check Like", "Fail: ${loginResponse}")
                    tableLike.visibility = View.VISIBLE;
                    tableNoLike.visibility = View.GONE;
                }
            }

            override fun onFailure(call: Call<LikeDataResponse>, t: Throwable) {
                Log.e("API Check Like", "Failure: ${t.message}")
                tableLike.visibility = View.VISIBLE;
                tableNoLike.visibility = View.GONE;
            }
        })
    }

    //Funcion que guarda el me gusta
    fun saveLike(usuario_id: String, curso_id: String){
        val gson = GsonBuilder().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(MainActivity.url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        authService = retrofit.create(DetailService::class.java)

        val usuario = Usuario(usuario_id)
        val curso = Curso(curso_id)

        val credentials = Like(curso, usuario)

        authService.like(credentials).enqueue(object : Callback<LikeResponse> {
            override fun onResponse(call: Call<LikeResponse>, response: Response<LikeResponse>) {
                val loginResponse = response.body()
                if (response.isSuccessful) {
                    if (loginResponse != null) {
                        Log.d("API Save Like", "Success: ${loginResponse.response}")
                    }
                    dataCurso(curso_id);
                }else{
                    Log.d("API Save Like", "Fail: ${loginResponse}")
                }
            }

            override fun onFailure(call: Call<LikeResponse>, t: Throwable) {
                Log.e("API Save Like", "Failure: ${t.message}")
            }
        })
    }

    //Funcion que elimina el me gusta
    fun deleteLike(usuario_id: String, curso_id: String){
        val gson = GsonBuilder().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(MainActivity.url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        authService = retrofit.create(DetailService::class.java)

        val usuario = Usuario(usuario_id)
        val curso = Curso(curso_id)

        val credentials = Like(curso, usuario)

        authService.deleteLike(credentials).enqueue(object : Callback<LikeResponse> {
            override fun onResponse(call: Call<LikeResponse>, response: Response<LikeResponse>) {
                val loginResponse = response.body()
                if (response.isSuccessful) {
                    if (loginResponse != null) {
                        Log.d("API No Like", "Success: ${loginResponse.response}")
                    }
                    dataCurso(curso_id);
                }else{
                    Log.d("API No Like", "Fail: ${loginResponse}")
                }
            }

            override fun onFailure(call: Call<LikeResponse>, t: Throwable) {
                Log.e("API No Like", "Failure: ${t.message}")
            }
        })
    }

    //Funcion que guarda el comentario
    fun saveComentario(comentario: String, curso_id: String, usuario_id: String){
        val gson = GsonBuilder().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(MainActivity.url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        authService = retrofit.create(DetailService::class.java)

        val usuario = Usuario(usuario_id)
        val curso = Curso(curso_id)

        val credentials = Comentario(curso, usuario, comentario)

        authService.comentario(credentials).enqueue(object : Callback<ComentarioResponse> {
            override fun onResponse(call: Call<ComentarioResponse>, response: Response<ComentarioResponse>) {
                val loginResponse = response.body()
                if (response.isSuccessful) {
                    if (loginResponse != null) {
                        Log.d("API Comentario", "Success: ${loginResponse.response}")
                    }
                    dataCurso(curso_id);
                }else{
                    Log.d("API Comentario", "Fail: ${loginResponse}")
                }
            }

            override fun onFailure(call: Call<ComentarioResponse>, t: Throwable) {
                Log.e("API Comentario", "Failure: ${t.message}")
            }
        })
    }

    //API de Data de Curso, Like y Comentarios
    interface DetailService {
        @POST("curso/data")
        fun data(@Body credentials: Curso): Call<CursoResponse>

        @POST("curso/like")
        fun like(@Body credentials: Like): Call<LikeResponse>

        @POST("curso/like/data")
        fun dataLike(@Body credentials: Like): Call<LikeDataResponse>

        @POST("curso/deleteLike")
        fun deleteLike(@Body credentials: Like): Call<LikeResponse>

        @POST("cursoComentario")
        fun comentario(@Body credentials: Comentario): Call<ComentarioResponse>

        @POST("cursoComentario/data")
        fun dataComentario(@Body credentials: ComentarioData): Call<List<ComentarioItem>>
    }

    //Clase de respuesta de Me gusta
    data class LikeResponse(
        val response: Any
    )
    //Clase de respuesta de la Data de Me gusta
    data class LikeDataResponse(
        val id: Any
    )
    //Clase de informacion dle curso
    data class CursoResponse(
        val likes: String,
        val imagen: String,
        val imagen_detalle: String,
        val descripcion: String,
        val titulo: String,
        val url: String,
        val id: String
    )
    //Clase de informacion del usuario en comentario
    data class UsuarioComentario(
        val id: Int,
        val nombre: String,
        val email: String,
        val telefono: String?,
        val password: String?,
        val imagen: String?
    )

    //Clase de informacion del curso en comentario
    data class CursoComentario(
        val id: Int,
        val titulo: String,
        val descripcion: String,
        val imagen: String,
        val imagen_detalle: String,
        val likes: Int,
        val comentarios: Int,
        val url: String
    )

    //Clase de general para comentario
    data class ComentarioItem(
        val usuario: UsuarioComentario,
        val curso: CursoComentario,
        val comentario: String
    )

    //Clase del comentario
    data class ComentarioResponse(
        val response: List<ComentarioItem>
    )

    //Clase del comentario
    data class Comentario(
        val curso: Curso,
        val usuario: Usuario,
        val comentario: String
    )

    //Clase de la data de comentarios
    data class ComentarioData(
        val curso: Curso
    )

    //Clase del comentario
    data class Like(
        val curso: Curso,
        val usuario: Usuario
    )

    //Clase del curso
    data class Curso(
        val id: String
    )

    //Clase de usuario
    data class Usuario(
        val id: String
    )
}