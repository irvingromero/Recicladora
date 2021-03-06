package irvinc.example.com.inicioprincipal.InicioSinSesion
/*
validar tener la ubicacion prendida
Al eliminar un material queda desfasado el conntador del array en sesion recicladora
 */
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle
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
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.GravityCompat
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import irvinc.example.com.inicioprincipal.BD.BaseDeDatos
import irvinc.example.com.inicioprincipal.R
import irvinc.example.com.inicioprincipal.Recicladora.SesionRecicladora
import irvinc.example.com.inicioprincipal.UsuarioLogeado.SesionUsuario
import kotlinx.android.synthetic.main.datos_recicladora.*
import java.math.RoundingMode
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
            drawerLayout?.openDrawer(GravityCompat.START)
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

        val botonCalificar = findViewById<MaterialButton>(R.id.btnCalificar_datos_recicladora)
        botonCalificar.setOnClickListener {
            mensaje()
            val rb = findViewById<RatingBar>(R.id.rbPuntuar_datosRecicladora)
            rb.rating = 0.0f
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

        permiso()
        cargarRecicladoras()

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
                    }

                    BottomSheetBehavior.STATE_EXPANDED ->
                    {
                        mMap.setPadding(0,0,0, bottomSize)
                        drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

                        cargarDatosRecicladora(p0!!.title)
                        cargarMaterialesRecicladora(p0!!.title)
                        cargarCalificacionRecicladora(p0!!.title)
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

    private fun cargarRecicladoras(){
        val bdClase =  BaseDeDatos(this, "Ubicacion", null , 1)
        val bdConexion = bdClase.readableDatabase

        val ubicacionReci = bdConexion.rawQuery("select usuario, latitud, longitud from Ubicacion",null)
        if(ubicacionReci.moveToFirst()){
            var usuarioRecicladora : String  /// GUARDA EL USUARIO DE RECICLADORA EN CADA ITERACION ///
            do {
                usuarioRecicladora = ubicacionReci.getString(0)
                    /// NO MOVER CONFIGURACION DE LA BASE DE DATOS, PROBLEMAS DE CONEXION u.u///
                val wwww = BaseDeDatos(applicationContext, "Usuarios", null , 1)
                val zzzz = wwww.readableDatabase
                val nombreReci = zzzz.rawQuery("select nombre from Recicladoras where usuario = '$usuarioRecicladora'",null)

                val bandera = nombreReci.moveToFirst()
                val nombre = nombreReci.getString(0)

                mMap.addMarker(MarkerOptions().position(LatLng(ubicacionReci.getDouble(1), ubicacionReci.getDouble(2))).title(nombre))
            }while (ubicacionReci.moveToNext())
        }
        ubicacionReci.close()
        bdConexion.close()
    }

    private fun cargarDatosRecicladora(nombreRecicladora : String){
        val campoCorreo = findViewById<TextInputEditText>(R.id.tietMostrarCorreo_datosRecicladora)
        val campoTelefono = findViewById<TextInputEditText>(R.id.etMostrarTelefono_datosRecicladora)
        val campoCalle = findViewById<TextInputEditText>(R.id.etMostrarCalle1_datosRecicladora)
        val campoColonia = findViewById<TextInputEditText>(R.id.etMostrarColonia_datosRecicla)
        val campoNumeroInt = findViewById<TextInputEditText>(R.id.etMostrarNumueroInt_datosRecicla)

        val objetobasededatos = BaseDeDatos(this, "Usuarios", null, 1)
        val flujodedatos = objetobasededatos.readableDatabase
        val variableCursor = flujodedatos.rawQuery("select correo, telefono, calle, colonia, numeroInt from Recicladoras where nombre = '$nombreRecicladora'", null)
        val bandera = variableCursor.moveToFirst()

        if(variableCursor.moveToFirst()){
            campoCorreo.setText(variableCursor.getString(0))
            campoTelefono.setText(variableCursor.getString(1))
            campoCalle.setText(variableCursor.getString(2))
            campoColonia.setText(variableCursor.getString(3))
            campoNumeroInt.setText(variableCursor.getString(4))
        }
        variableCursor.close()
        flujodedatos.close()
    }

    private fun cargarMaterialesRecicladora(nombreRecicladora : String){
        rv?.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.VERTICAL,
            false
        )
        listaMateriales = ArrayList()

        val objetobasededatos = BaseDeDatos(applicationContext, "Usuarios", null, 1)
        val flujodedatos = objetobasededatos.readableDatabase
        val variableCursor = flujodedatos.rawQuery("select usuario from Recicladoras where nombre = '$nombreRecicladora'", null)
        val bandera = variableCursor.moveToFirst()
        val usuarioRecicladora = variableCursor.getString(0)
        variableCursor.close()
        flujodedatos.close()

        val bd =  BaseDeDatos(applicationContext, "Materiales", null , 1)
        val basededatos = bd.readableDatabase
        val datos = basededatos.rawQuery("select material, precio, unidad from Materiales where usuario = '$usuarioRecicladora'", null)

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
        datos.close()
        val adap = Adapter(listaMateriales!!)
        rv?.adapter = adap
    }

    private fun cargarCalificacionRecicladora(nombreRecicladora: String){
        val objetobasededatos = BaseDeDatos(this, "Usuarios", null, 1)
        val flujodedatos = objetobasededatos.readableDatabase
        val variableCursor = flujodedatos.rawQuery("select usuario from Recicladoras where nombre = '$nombreRecicladora'", null)

        if(variableCursor.moveToFirst()){
            val usuarioReci = variableCursor.getString(0)
            variableCursor.close()

            val cursorPuntuacion = flujodedatos.rawQuery("select calificacion from Calificacion where usuarioRecicladora = '$usuarioReci'", null)
            if(cursorPuntuacion.moveToFirst()){
                var sumaPuntuaje = 0.0f
                var temp: Float

                do{
                    temp = cursorPuntuacion.getFloat(0)
                    var aux = temp + sumaPuntuaje
                    sumaPuntuaje = aux
                }while (cursorPuntuacion.moveToNext())

                val promedio = sumaPuntuaje / cursorPuntuacion.count///NUMERO DE USUARIOS QUE HAN PUNTUADO///
                val promedioCorto = promedio.toBigDecimal().setScale(1, RoundingMode.HALF_UP).toString()/// RECORTA LOS DECIMALES A 1 ////
                tvPuntuacion_datosRecicla.text = promedioCorto
            } else {///NUNCA A SIDO PUNTUADA LA RECICLADORA ///
                tvPuntuacion_datosRecicla.text = "0"
            }
            flujodedatos.close()
            cursorPuntuacion.close()
        }
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
        ///////// OPCIONES DEL MENU //////////////////
    fun iniciarSesion(view : View){
            cerrarDrawer()
            onBackPressed()
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
        listaMateriales = ArrayList()

        val odb = BaseDeDatos(this, "Materiales", null, 1)
        val fdb = odb.readableDatabase
        val cursorMaterial = fdb.rawQuery("select material from Materiales", null)

        if(cursorMaterial.moveToFirst()){
            listaview.isEnabled = true

            do{
                listaMateriales?.add(cursorMaterial.getString(0))
            }while (cursorMaterial.moveToNext())

                /// ELIMINA LOS ELEMENTOS REPETIDOS ////
            val hs = HashSet<String>()
            hs.addAll(listaMateriales!!)
            listaMateriales!!.clear()
            listaMateriales!!.addAll(hs)
        } else {
            listaMateriales!!.add("No materiales disponibles")
            listaview.isEnabled = false
        }
        fdb.close()
        cursorMaterial.close()
        val a = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listaMateriales)
        listaview.adapter = a

        dialog.show()

        listaview.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val material = parent.getItemAtPosition(position)
            dialog.dismiss()
            chipMaterial(v, material.toString())

            mMap.clear()
            recicladorasMaterialBuscado(material.toString())
        }
    }
        //////////// TERMINA EL MENU ///////////////
    private fun recicladorasMaterialBuscado(materialBuscado : String){
        val objetoparabasededatos = BaseDeDatos(this, "Materiales", null,1)
        val conexionBd = objetoparabasededatos.readableDatabase

        val usuarioMaterial = conexionBd.rawQuery("select usuario from Materiales where material ='$materialBuscado'", null)
        if(usuarioMaterial.moveToFirst()){
            conexionBd.close()
            do{
                val usuarioRecicladora = usuarioMaterial.getString(0)

                val objetoparabasededatos2 = BaseDeDatos(this, "Ubicacion", null,1)
                val conexionBd2 = objetoparabasededatos2.readableDatabase

                val consultaUbicacion = conexionBd2.rawQuery("select latitud, longitud from Ubicacion where usuario='$usuarioRecicladora'", null)
                if(consultaUbicacion.moveToFirst()){

                    val objetoparabasededatos3 = BaseDeDatos(this, "Usuarios", null,1)
                    val conexionBd3 = objetoparabasededatos3.readableDatabase

                    val consultaRecicladora = conexionBd3.rawQuery("select nombre from Recicladoras where usuario ='$usuarioRecicladora'", null)
                    if(consultaRecicladora.moveToFirst()){
                        mMap.addMarker(MarkerOptions().position(LatLng(consultaUbicacion.getDouble(0), consultaUbicacion.getDouble(1))).title(consultaRecicladora.getString(0)))
                    }
                }
            }while (usuarioMaterial.moveToNext())
        }
        usuarioMaterial.close()
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
//            Snackbar.make(vista, R.string.maximoMateriales_str, Snackbar.LENGTH_LONG).show()
        }
/*
        chipItem.setOnClickListener {
            if(contador < 1) {
                buscarMaterial(vista)
            } else {
                Snackbar.make(vista, R.string.maximoMateriales_str, Snackbar.LENGTH_LONG).show()
            }
        }
*/
        chipItem.setOnCloseIconClickListener {
            cargarRecicladoras()

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
        drawerLayout?.closeDrawer(GravityCompat.START)
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