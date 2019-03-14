package irvinc.example.com.inicioprincipal.UsuarioLogeado

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import irvinc.example.com.inicioprincipal.R

class SesionUsuario : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sesion_usuario)
        supportActionBar?.hide()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapaUsuario) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    fun modificarDatos_sesionUsuario(view : View){

    }

    fun buscarMaterial_sesionUsuario(view : View){

    }

    fun mejorPrecio_sesionUsuario(view : View){

    }

    fun masCercana_sesionUsuario(view : View){

    }

    fun cerrarSesion_sesionUsuario(view : View){

    }
}