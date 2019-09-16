package irvinc.example.com.inicioprincipal.Recicladora

import android.content.ContentValues
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.view.Gravity
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import irvinc.example.com.inicioprincipal.BD.BaseDeDatos
import irvinc.example.com.inicioprincipal.R

class UbicacionRecicladora : AppCompatActivity(), OnMapReadyCallback {

    private var usuarioLogeado : String? = null
    private lateinit var mapita: GoogleMap
    private var fab : FloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ubicacion_recicladora)
        supportActionBar?.hide()

        usuarioLogeado = intent.extras?.getString("usuario")

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapa_ubicacionRecicladora) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fab = findViewById(R.id.fabAceptar_ubicacionRecicladora)
        findViewById<ImageButton>(R.id.btnAtras_ubicacionRecicla).setOnClickListener {
            onBackPressed()
        }
    }

    override fun onMapReady(gm: GoogleMap?) {
        mapita = gm!!
        mapita.uiSettings.isZoomControlsEnabled = true
        mapita.uiSettings.isCompassEnabled = false
        mapita.uiSettings.isMapToolbarEnabled = false

        var yaregistado = false

        val bd =  BaseDeDatos(this, "Ubicacion", null , 1)
        val basededatos = bd.readableDatabase
        val consultaUbicacion = basededatos.rawQuery("select usuario from Ubicacion where usuario = '$usuarioLogeado'", null)
        if(consultaUbicacion.moveToFirst()){
            yaregistado = true
            ubicacionGuardada()
        }

        mapita.setOnMapClickListener { latLng ->
            mapita.clear()
            val marker = MarkerOptions().position(LatLng(latLng!!.latitude, latLng!!.longitude))
            mapita.addMarker(marker)

            fab?.show()
            fab?.setOnClickListener {
                // VALIDAR SI ES LA PRIMERA VEZ QUE SE GUARDA LA UBICACION O SE ESTA MODIFICANDO ///
                if(yaregistado){
                    ubicacionGuardada()
                } else {
                    registrarUbicacion(latLng.latitude, latLng.longitude)
                }
            }
        }
    }

    private fun registrarUbicacion(lat : Double, lon : Double){ // DA DE ALTA POR PRIMERA VEZ ///
        val bd =  BaseDeDatos(this, "Ubicacion", null , 1)
        val datosUbicacion = ContentValues()
        datosUbicacion.put("usuario", usuarioLogeado)
        datosUbicacion.put("latitud", lat)
        datosUbicacion.put("longitud", lon)

        val basededatos = bd.writableDatabase
        basededatos.insert("Ubicacion", null, datosUbicacion)
        basededatos.close()
        bd.close()

        onBackPressed()

        val toast = Toast(applicationContext)
        //// CARGA EL LAYOUT A UNA VISTA ////
        val view = layoutInflater.inflate(R.layout.usuario_registrado, null)
        toast.view = view
        toast.duration = Toast.LENGTH_LONG
        toast.setGravity(Gravity.BOTTOM,0, 30)
        view.findViewById<TextView>(R.id.tvToast_usuarioregistrado).text = getString(R.string.ubicacionAgregada_str)
        toast.show()
    }

    private fun ubicacionGuardada(){
        val bd =  BaseDeDatos(this, "Ubicacion", null , 1)
        val basededatos = bd.readableDatabase
        val datos = basededatos.rawQuery("select latitud, longitud from Ubicacion where usuario = '$usuarioLogeado'", null)
        if(datos.moveToFirst()){
            val marker = MarkerOptions().position(LatLng(datos.getDouble(0), datos.getDouble(1)))
            mapita.addMarker(marker)
        }
    }
}