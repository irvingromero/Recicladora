package irvinc.example.com.inicioprincipal.InicioSinSesion
/*
validar tener la ubicacion prendida
VALIDAR QUE NO SE SELECCIONE EL MISMO MATERIAL////
 */
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Gravity
import android.view.View

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import android.widget.ImageButton
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v4.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.support.design.chip.Chip
import android.support.design.chip.ChipGroup
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import irvinc.example.com.inicioprincipal.BD.BaseDeDatos
import irvinc.example.com.inicioprincipal.R
import irvinc.example.com.inicioprincipal.UsuarioLogeado.SesionUsuario

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var drawerLayout : DrawerLayout? = null
    private var drawerOpen = false// Bandera para saber el estado del drawer
    private var miUbicacion : Location? = null

    private var chipgroup : ChipGroup? = null
    private var contador = 0 //// Contador para los chips ////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        supportActionBar?.hide()

        sesionGuardada()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        detectarSlide()
        findViewById<ImageButton>(R.id.btnMenu).setOnClickListener {
            drawerLayout?.openDrawer(Gravity.START)
            drawerOpen = true
        }

        chipgroup = findViewById(R.id.cg_MapsActivity)
    }

    private fun sesionGuardada(){
        val preferences = getSharedPreferences("user", Context.MODE_PRIVATE)
        val usuario = preferences.getString("usuario", "nada")
        if(!usuario.equals("nada"))
        {
            /// Busqueda en la bd, para saber si el usuario existe aun ////
            val bd =  BaseDeDatos(this, "Usuarios", null , 1)
            val basededatos = bd.readableDatabase
            val consultaUsuario = basededatos.rawQuery("select usuario from Usuarios where usuario ='$usuario'",null)

            if(consultaUsuario.moveToFirst()){
                basededatos.close()
                val intent = Intent(this, SesionUsuario::class.java)
                intent.putExtra("usuario", usuario)
                startActivity(intent)
                finish()
            }
            basededatos.close()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = false

        permiso()
    }

    @SuppressLint("MissingPermission")
    private fun permiso() {
        val permisoActivado: Boolean

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permisoActivado = estadoPermisoUbicacion()

            if (permisoActivado == false) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
                {
                    //// MIUESTRA EL DIALOG PARA EL PERMISO ////
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 10)
                }
            } else { // PERMISO YA DADO
                mMap.isMyLocationEnabled = true
                camaraAubicacion()
            }
        } else {// VERSION MENOR A 6.0
            mMap.isMyLocationEnabled = true
            camaraAubicacion()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 10) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMap.isMyLocationEnabled = true
            }
        }
    }

    private fun estadoPermisoUbicacion(): Boolean {
        val resultado = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        return resultado == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun camaraAubicacion(){
        val locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        miUbicacion = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false))
        try {
            val latitude = miUbicacion?.latitude
            val longitud = miUbicacion?.longitude

            val latlog = LatLng(latitude!!, longitud!!)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlog))
            mMap.setMinZoomPreference(11.0f)
        } catch (e: Exception) { }
    }

    private fun detectarSlide(){
        //// DETECTA CUANDO AL MENU SE LE HACE SLIDE ////
        drawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout?.setDrawerListener(object : ActionBarDrawerToggle(this, drawerLayout,0,0)
        {
            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                drawerOpen = false//is Closed
            }
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                drawerOpen = true//is Opened
            }
        })
    }

    fun iniciarSesion(view : View){
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            val intent = Intent(this, IniciarSesion::class.java)
            startActivity(intent)

            cerrarDrawer()
        }
    }

    fun buscarMaterial(v : View){
        cerrarDrawer()

        val ventana = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        // CARGA EL LAYOUT PERSONALIZADO//
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.ventana_buscar_material, null)
        ventana.setView(dialogView)
        ventana.setTitle(R.string.materialesDisponibles_str)

        val dialog: AlertDialog = ventana.create()

        val listaview = dialogView.findViewById<ListView>(R.id.lvBuscar_material)
        listaview.isClickable = true
        listaview.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val dato = parent.getItemAtPosition(position)
            dialog.dismiss()
            chipMaterial(v, dato.toString())
        }

        val values = arrayOf("Latas", "Chatarra","Vidrio","Carton","Alumino")
        val a = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values)
        listaview.adapter = a

        dialog.show()
    }

    private fun chipMaterial(vista : View ,material : String){
        val i = LayoutInflater.from(this@MapsActivity)
        val chipItem = i.inflate(R.layout.chip, null , false) as Chip

        if(contador < 3){
            chipItem.text = material
            chipgroup?.addView(chipItem)
            chipgroup?.visibility = View.VISIBLE
            contador ++
        } else {
            Snackbar.make(vista, R.string.maximoMateriales_str, Snackbar.LENGTH_LONG).show()
        }

        chipItem.setOnClickListener {
            if(contador < 3) {
                buscarMaterial(vista)
            } else {
                Snackbar.make(vista, R.string.maximoMateriales_str, Snackbar.LENGTH_LONG).show()
            }
        }

        chipItem.setOnCloseIconClickListener {
            chipgroup?.removeView(chipItem)
            contador --
        }
    }

    fun mejorPrecio(view : View){
        mensaje()
    }

    fun masCercana(view : View){
        mensaje()
    }

    private fun mensaje(){
        val ventana = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        ventana.setView(layoutInflater.inflate(R.layout.ventana_mensaje_mapa, null))

        ventana.setPositiveButton(R.string.registrate_str){ _, _ ->
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                val intent = Intent(this, IniciarSesion::class.java)
                startActivity(intent)

                cerrarDrawer()
            }
        }

        ventana.setNeutralButton(R.string.aceptar_str){ _, _ -> }

        val dialog: AlertDialog = ventana.create()
        dialog.show()
    }

    private fun cerrarDrawer(){
        drawerLayout?.closeDrawer(Gravity.START)
        drawerOpen = false
    }

    override fun onBackPressed() {
        if (drawerOpen)
        {
            cerrarDrawer()
        }
        else {
            super.onBackPressed()
        }
    }
}