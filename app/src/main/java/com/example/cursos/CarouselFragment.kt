package com.example.cursos

import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.collection.ArrayMap
import androidx.fragment.app.Fragment
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.io.InputStream
import java.net.URL
import java.util.ArrayList


//Clase que genera la galeria de cursos
class CarouselFragment : Fragment() {

    private var position: Int = 0
    private lateinit var authService: DataCursoService

    companion object {
        private const val ARG_POSITION = "position"

        fun newInstance(position: Int): CarouselFragment {
            val fragment = CarouselFragment()
            val args = Bundle()
            args.putInt(ARG_POSITION, position)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            position = it.getInt(ARG_POSITION)
        }
    }

    override fun onResume() {
        super.onResume()
        val rootView = view
        if (rootView != null) {
            val imageView = rootView.findViewById<ImageView>(R.id.imageView)
            val titleTextView = rootView.findViewById<TextView>(R.id.titleTextView)
            val likeTextView = rootView.findViewById<TextView>(R.id.likeTextView)

            dataCursos(imageView, titleTextView, likeTextView)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.carousel_item, container, false)

        val imageView = rootView.findViewById<ImageView>(R.id.imageView) //Imagen que se muestra en galeria
        val titleTextView = rootView.findViewById<TextView>(R.id.titleTextView) //Titulo que se muestra en galeria
        val likeTextView = rootView.findViewById<TextView>(R.id.likeTextView) //Likes que se muestran en galeria

        //Obtener el listado de Cursos
        dataCursos(imageView,titleTextView, likeTextView);

        return rootView
    }

    //Funcion que obtiene el listado de Cursos
    private fun dataCursos(imageView: ImageView,titleTextView:TextView,likeTextView:TextView) {
        val gson = GsonBuilder().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(MainActivity.url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        authService = retrofit.create(DataCursoService::class.java)

        authService.dataCurso().enqueue(object : Callback<DataCursoResponse> {
            override fun onResponse(call: Call<DataCursoResponse>, response: Response<DataCursoResponse>) {
                val dataResponse = response.body()
                if (dataResponse != null) {
                    if (response.isSuccessful && dataResponse.success) {
                        if (dataResponse != null) {
                            Log.d("API Curso Response", "Data: ${dataResponse.response}")

                            if (position < dataResponse.response.size) {
                                val cursoItem = dataResponse.response[position]

                                val myUrl = URL(cursoItem.imagen)
                                val inputStream = myUrl.content as InputStream
                                val drawable = Drawable.createFromStream(inputStream, null)

                                imageView.setImageDrawable(drawable)
                                titleTextView.text = cursoItem.titulo
                                likeTextView.text = "${cursoItem.likes} Me Gusta"


                                imageView.setOnClickListener() {
                                    goToDetalle(cursoItem)
                                }

                                titleTextView.setOnClickListener() {
                                    goToDetalle(cursoItem)
                                }
                            }
                        }
                    }else{
                        Log.d("API Curso Response", "Fail: ${dataResponse}")
                    }
                }else{
                    Log.d("API Curso Response", "Fail: ${dataResponse}")
                }
            }

            override fun onFailure(call: Call<DataCursoResponse>, t: Throwable) {
                Log.e("API Curso Response", "Failure: ${t.message}")
            }

            //Devuelve la informacion detallada del Usuario
            fun goToDetalle(cursoItem: CursoItem){
                val intent = Intent(activity, DetalleActivity::class.java)
                intent.putExtra("id", cursoItem.id)
                intent.putExtra("imagen", cursoItem.imagen_detalle)
                intent.putExtra("titulo", cursoItem.titulo)
                intent.putExtra("descripcion", cursoItem.descripcion)
                intent.putExtra("likes", cursoItem.likes)
                intent.putExtra("url", cursoItem.url)
                startActivity(intent)
            }
        })
    }
    //API del Curso
    interface DataCursoService {
        @GET("curso")
        fun dataCurso(): Call<DataCursoResponse>
    }

    //Clase para respuesta del curso
    data class DataCursoResponse(
        val response: ArrayList<CursoItem>,
        val success: Boolean
    )

    //Clase para respuesta del detalle curso
    data class CursoItem(
        val likes: String,
        val imagen: String,
        val imagen_detalle: String,
        val descripcion: String,
        val titulo: String,
        val url: String,
        val id: String
    )
}