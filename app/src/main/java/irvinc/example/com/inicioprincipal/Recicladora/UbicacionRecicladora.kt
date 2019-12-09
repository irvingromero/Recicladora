package irvinc.example.com.inicioprincipal.Recicladora

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.view.Gravity
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import irvinc.example.com.inicioprincipal.BD.BaseDeDatos
import irvinc.example.com.inicioprincipal.R

class UbicacionRecicladora : AppCompatActivity(), OnMapReadyCallback {

    private var usuarioLogeado : String? = null
    private lateinit var mapita: GoogleMap
    private var fab : FloatingActionButton? = null
    private var latitudVieja : Double? = null
    private var longitudVieja : Double? = null

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
        mapita.uiSettings.isMapToolbarEnabled = false

        var yaregistado = false
        permiso()

        val bd =  BaseDeDatos(this, "Ubicacion", null , 1)
        val basededatos = bd.readableDatabase
        val consultaUbicacion = basededatos.rawQuery("select usuario from Ubicacion where usuario = '$usuarioLogeado'", null)
        if(consultaUbicacion.moveToFirst()){
            yaregistado = true
            ubicacionGuardada()
        }

        mapita.setOnMapClickListener { latLng ->
            if(yaregistado){
                Toast.makeText(this, "Modificar ubicacion", Toast.LENGTH_SHORT).show()

                mapita.clear()
                val guardado = MarkerOptions().position(LatLng(latitudVieja!!, longitudVieja!!))// VUELVE A PONER EN EL MAPA LA UBICACION//
                mapita.addMarker(guardado).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

                val markerNuevo = MarkerOptions().position(LatLng(latLng!!.latitude, latLng!!.longitude))// NUEVO MARCADOR //
                mapita.addMarker(markerNuevo)
            } else {
                mapita.clear()
                val markerNuevo = MarkerOptions().position(LatLng(latLng!!.latitude, latLng!!.longitude))// NUEVO MARCADOR //
                mapita.addMarker(markerNuevo)
            }

            fab?.show()
            fab?.setOnClickListener {
                if(yaregistado){ /// MODIFICAR UBICACION ////
                    actualizarUbicacion(latLng.latitude, latLng.longitude)
                    onBackPressed()
                    mostrarMensaje(getString(R.string.ubicacionActualizada_str).toString())

                } else { /// DAR DE ALTA POR PRIMERA VEZ ////
                    registrarUbicacion(latLng.latitude, latLng.longitude)
                    onBackPressed()
                    mostrarMensaje(getString(R.string.ubicacionAgregada_str).toString())
                }
            }
        }
    }

    private fun mostrarMensaje(mensaje : String){
        val toast = Toast(applicationContext)
        //// CARGA EL LAYOUT A UNA VISTA ////
        val view = layoutInflater.inflate(R.layout.usuario_registrado, null)
        toast.view = view
        toast.duration = Toast.LENGTH_LONG
        toast.setGravity(Gravity.BOTTOM,0, 30)
        view.findViewById<TextView>(R.id.tvToast_usuarioregistrado).text = mensaje
        toast.show()
    }

    private fun ubicacionGuardada(){
        val bd =  BaseDeDatos(this, "Ubicacion", null , 1)
        val basededatos = bd.readableDatabase
        val datos = basededatos.rawQuery("select latitud, longitud from Ubicacion where usuario = '$usuarioLogeado'", null)
        if(datos.moveToFirst()){
            latitudVieja = datos.getDouble(0)
            longitudVieja = datos.getDouble(1)
            val marker = MarkerOptions().position(LatLng(latitudVieja!!, datos.getDouble(1)))
            mapita.addMarker(marker).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

            val latlog = LatLng(latitudVieja!!, longitudVieja!!)
            val camara = CameraUpdateFactory.newLatLngZoom(latlog, 14.5F)
            mapita.animateCamera(camara)    /// MUEVE LA CAMARA HASTA EL MARCADOR ///
        }
        basededatos.close()
        datos.close()
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
    }

    private fun actualizarUbicacion(lat : Double, lon : Double){
        val ubicacionNueva = ContentValues()
        ubicacionNueva.put("latitud", lat)
        ubicacionNueva.put("longitud", lon)

        val bd =  BaseDeDatos(this, "Ubicacion", null , 1)
        val basededatos = bd.writableDatabase
        basededatos.update("Ubicacion", ubicacionNueva, "usuario=? AND latitud=? AND longitud=?", arrayOf(usuarioLogeado, latitudVieja.toString(),longitudVieja.toString()))
        basededatos.close()
    }

    @SuppressLint("MissingPermission")
    private fun permiso() {
        val permisoActivado: Boolean

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            permisoActivado = estadoPermisoUbicacion()

            if (permisoActivado == false)
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
                {
                    //// MIUESTRA EL DIALOG PARA EL PERMISO ////
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 10)
                }
            } else { // PERMISO YA DADO
                mapita.isMyLocationEnabled = true
                camaraAubicacion()
            }
        } else {// VERSION MENOR A 6.0
            mapita.isMyLocationEnabled = true
            camaraAubicacion()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 10) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mapita.isMyLocationEnabled = true
            }
        }
    }

    private fun estadoPermisoUbicacion(): Boolean {
        val resultado = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        return resultado == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun camaraAubicacion(){
        var locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        val miUbicacion = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false))
        try {
            val latitude = miUbicacion?.latitude
            val longitud = miUbicacion?.longitude

            val latlog = LatLng(latitude!!, longitud!!)
            mapita.moveCamera(CameraUpdateFactory.newLatLng(latlog))
            mapita.setMinZoomPreference(11.0f)
        } catch (e: Exception) { }
    }
}