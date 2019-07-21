package irvinc.example.com.inicioprincipal.InicioSinSesion
/*
validar tener la ubicacion prendida
VALIDAR QUE NO SE SELECCIONE EL MISMO MATERIAL////

Al eliminar un material queda desfasado el conntador del array
 */
import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
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
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import irvinc.example.com.inicioprincipal.BD.BaseDeDatos
import irvinc.example.com.inicioprincipal.R
import irvinc.example.com.inicioprincipal.Recicladora.SesionRecicladora
import irvinc.example.com.inicioprincipal.UsuarioLogeado.SesionUsuario
import kotlinx.android.synthetic.main.datos_recicladora.*
import java.util.ArrayList

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private var drawerLayout : DrawerLayout? = null
    private var drawerOpen = false// Bandera para saber el estado del drawer
    private var miUbicacion : Location? = null

    private var chipgroup : ChipGroup? = null
    private var contador = 0 //// Contador para los chips ////

    private var bottomSheetBehavior : BottomSheetBehavior<LinearLayout>? = null

    private var rv : RecyclerView? = null
    private var listaMateriales : ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        supportActionBar?.hide()

        bottomSheetBehavior = BottomSheetBehavior.from<LinearLayout>(bottomSheet)
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN

        sesionGuardada()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        detectarSlide()
        findViewById<ImageButton>(R.id.btnMenu_maps).setOnClickListener {
            drawerLayout?.openDrawer(Gravity.START)
            drawerOpen = true
        }

        chipgroup = findViewById(R.id.cg_MapsActivity)

        rv = findViewById(R.id.rvMateriales_datosRecicla)

        //// LISTENER PARA ABRIR EL BOTTOM SHEET CON UN TOUCH /////
        findViewById<LinearLayout>(R.id.ly_datosRecicladora).setOnClickListener {
            if (bottomSheetBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            }
            if (bottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
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
            } else {
                val consultaRecicla = basededatos.rawQuery("select usuario from Recicladoras where usuario ='$usuario'",null)
                if(consultaRecicla.moveToFirst()) {
                    basededatos.close()
                    val intent = Intent(this, SesionRecicladora::class.java)
                    intent.putExtra("usuario", usuario)
                    startActivity(intent)
                    finish()
                }
            }
            basededatos.close()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = false
        mMap.setOnMarkerClickListener(this)

        mMap.addMarker(MarkerOptions().position(LatLng(32.6578,-115.584)).title("Recicladora 11"))
        mMap.addMarker(MarkerOptions().position(LatLng(32.6578,-115.484)).title("Recicladora asFnk"))
        mMap.addMarker(MarkerOptions().position(LatLng(32.6278,-115.584)).title("r"))

        permiso()

        mMap.setOnMapClickListener {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            mMap.setPadding(0,0,0,0)
        }

        mMap.setOnInfoWindowClickListener {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        findViewById<TextView>(R.id.tvNombre_datosRecicladora).text = p0?.title

        val bottomSize = bottomSheetBehavior?.peekHeight
        mMap.setPadding(0,0,0, bottomSize!!)

        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        //// BLOQUEA Y DESBLOQUEA EL MENU LATERAL CUANDO EL BOTTOMSHEET ESTA EXPANDIDO /////
        bottomSheetBehavior?.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN ->{
                        mMap.setPadding(0,0,0,0)

                        listaMateriales?.clear()
                    }

                    BottomSheetBehavior.STATE_EXPANDED ->{
                        mMap.setPadding(0,0,0, bottomSize)
                        drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

                        rv?.layoutManager = LinearLayoutManager(applicationContext, LinearLayout.VERTICAL, false)
                        listaMateriales = ArrayList()

                        val nombre = p0?.title
                                ////////////////// PARA EJEMPLO ///////////////////////
                        val addMaterial = ContentValues()
                        addMaterial.put("usuario", nombre)
                        addMaterial.put("material", "Papel")
                        addMaterial.put("precio", 1556.50)
                        addMaterial.put("unidad", "Kg")

                        val bd11 =  BaseDeDatos(applicationContext, "Recicladoras", null , 1)
                        val zzz = bd11.writableDatabase
                        zzz.insert("Materiales", null, addMaterial)
                        zzz.close()

                        val bd =  BaseDeDatos(applicationContext, "Recicladoras", null , 1)
                        val basededatos = bd.readableDatabase
                        val datos = basededatos.rawQuery("select material, precio, unidad from Materiales where usuario = '$nombre'", null)

                        if(datos.moveToFirst())
                        {
                            do{
                                var material = datos.getString(0)
                                var precio = datos.getDouble(1)
                                var unidad = datos.getString(2)

                                listaMateriales!!.add("Material: "+material+"\nPrecio: "+precio+"\nUnidad: "+unidad)
                            } while(datos.moveToNext())
                        }

                        basededatos.close()
                        val adap = Adapter(listaMateriales!!)
                        rv?.adapter = adap
                        ///////////////////////////////////////////////////////////////////////////////////////////////////////
/*
                        val bd =  BaseDeDatos(applicationContext, "Recicladoras", null , 1)
                        val basededatos = bd.readableDatabase
                        val datos = basededatos.rawQuery("select usuario from Recicladoras where nombre = '$nombre'", null)

                            //// SOLO SE TIENE EL NOMBRE DE LA RECI, ASI QUE SE BUSCARA EL USUARIO CON ESE NOMBRE PARA ACCEDER A SUS MATERIALES ////

                        if(datos.moveToFirst())
                        {
                            do{
                                var material = datos.getString(0)
                                var precio = datos.getDouble(1)
                                var unidad = datos.getString(2)

                                listaMateriales!!.add("Material: "+material+"\nPrecio: "+precio+"\nUnidad: "+unidad)
                            } while(datos.moveToNext())
                        }

                        basededatos.close()
                        val adap = Adapter(listaMateriales!!)
                        rv?.adapter = adap
*/
                    }

                    BottomSheetBehavior.STATE_COLLAPSED ->{
                        drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    }
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter{
            override fun getInfoContents(p0: Marker?): View {
                val vista = layoutInflater.inflate(R.layout.infowindow, null)
                return vista
            }
            override fun getInfoWindow(p0: Marker?): View? {
                val vista = layoutInflater.inflate(R.layout.infowindow, null)
                vista.findViewById<TextView>(R.id.tvNombreReci_infowindow).text = p0?.title.toString()
                return vista
            }
        })

        return false
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
        var locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
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
        val fab = findViewById<FloatingActionButton>(R.id.fabMejorPrecio_maps)
        val tv = findViewById<TextView>(R.id.tvMejorPrecio_maps)

        if(contador < 1){
            chipItem.text = material
            chipgroup?.addView(chipItem)
            chipgroup?.visibility = View.VISIBLE
            contador ++
        } else {
            Snackbar.make(vista, R.string.maximoMateriales_str, Snackbar.LENGTH_LONG).show()
        }

        chipItem.setOnClickListener {
            if(contador < 1) {
                buscarMaterial(vista)
            } else {
                Snackbar.make(vista, R.string.maximoMateriales_str, Snackbar.LENGTH_LONG).show()
            }
        }

        chipItem.setOnCloseIconClickListener {
            chipgroup?.removeView(chipItem)
            contador --

            if(contador == 0){
                fab.hide()
                tv.text = ""
            }
        }

        fab.show()
        tv.text = "Mejor precio"
        fab.setOnClickListener {
            mensaje()
        }
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
            if (bottomSheetBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED){
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
                mMap.setPadding(0,0,0,0)
            } else {
                if(bottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                } else {
                    super.onBackPressed()
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    class Adapter(var listita: ArrayList<String>) : RecyclerView.Adapter<Adapter.ViewHolder>(){

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            val v = LayoutInflater.from(p0.context).inflate(R.layout.materiales_datos_recicladora, p0, false)
            return ViewHolder(v)
        }

        override fun getItemCount(): Int {
            return listita.size
        }

        override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
            p0.mostrarDatos(listita[p1])
        }

        class ViewHolder(view : View) : RecyclerView.ViewHolder(view){

            fun mostrarDatos(texto : String){
                val t : TextView = itemView.findViewById(R.id.tvMostrar_materiales_datos_recicladora)
                t.text = texto
            }
        }// ViewHolder
    }//Adapter
}