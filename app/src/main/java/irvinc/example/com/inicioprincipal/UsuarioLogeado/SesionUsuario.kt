package irvinc.example.com.inicioprincipal.UsuarioLogeado

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.chip.Chip
import android.support.design.chip.ChipGroup
import android.support.design.widget.*
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.*
import android.widget.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import irvinc.example.com.inicioprincipal.BD.BaseDeDatos
import irvinc.example.com.inicioprincipal.InicioSinSesion.MapsActivity
import irvinc.example.com.inicioprincipal.R
import kotlinx.android.synthetic.main.datos_recicladora.*
import java.util.*
import kotlin.collections.HashSet

class SesionUsuario : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private var miUbicacion : Location? = null
    private var drawerLayout : DrawerLayout? = null
    private var drawerOpen = false// Bandera para saber el estado del drawer

    private var usuarioLogeado : String? = null

    private var chipgroup : ChipGroup? = null
    private var contador = 0 //// Contador para los chips ////

    private var bottomSheetBehavior : BottomSheetBehavior<LinearLayout>? = null

    private var rv : RecyclerView? = null
    private var listaMateriales : ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sesion_usuario)
        supportActionBar?.hide()

        bottomSheetBehavior  = BottomSheetBehavior.from<LinearLayout>(bottomSheet)
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN

        usuarioLogeado = intent.extras?.getString("usuario")
        rv = findViewById(R.id.rvMateriales_datosRecicla)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapaUsuario) as SupportMapFragment
        mapFragment.getMapAsync(this)

        detectarSlide()
        findViewById<ImageButton>(R.id.btnMenu_sesionUsuario).setOnClickListener {
            drawerLayout?.openDrawer(Gravity.START)
            drawerOpen = true
        }

        chipgroup = findViewById(R.id.cg_sesionUsuario)
            //// LISTENER PARA ABRIR EL BOTTOM SHEET CON UN TOUCH /////
        findViewById<LinearLayout>(R.id.ly_datosRecicladora).setOnClickListener {
            if (bottomSheetBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED){
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            }
            if(bottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
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
        rv?.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
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
        val adap = MapsActivity.Adapter(listaMateriales!!)
        rv?.adapter = adap
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        findViewById<TextView>(R.id.tvNombre_datosRecicladora).text = p0?.title

        val bottomSize = bottomSheetBehavior?.peekHeight
        mMap.setPadding(0,0,0,bottomSize!!)

        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        //// BLOQUEA Y DESBLOQUEA EL MENU LATERAL CUANDO EL BOTTOMSHEET ESTA EXPANDIDO /////
        bottomSheetBehavior?.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN ->{
                        mMap.setPadding(0,0,0,0)
                    }
                    BottomSheetBehavior.STATE_EXPANDED ->{
                        drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

                        cargarDatosRecicladora(p0!!.title)
                        cargarMaterialesRecicladora(p0!!.title)
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
        /// MENU ///
    fun modificarDatos_sesionUsuario(view : View){
        var contraOk = false
        var confirmContra = false

        val ventana = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        ventana.setCancelable(false) // EVITA QUE SE CIERRE EL DIALOG CON UN CLICK AFUERA DE EL //
        // CARGA EL LAYOUT PERSONALIZADO//
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.ventana_registro_usuario, null)
        ventana.setView(dialogView)
        ventana.setTitle("Ingresa los nuevos datos")

        val etUsuario = dialogView.findViewById<TextInputEditText>(R.id.etUsuario_registroUsuario)
        val etCorreo = dialogView.findViewById<TextInputEditText>(R.id.etCorreo_registroUsuario)
        val etContra = dialogView.findViewById<TextInputEditText>(R.id.etContra_registroUsuario)
        val etConfirmContra = dialogView.findViewById<TextInputEditText>(R.id.etConfirmarContra_registroUsuario)

            //// TRANSFORMA EL FORMATO TEXT A TEXTPASSWORD ////
        etContra.transformationMethod =  PasswordTransformationMethod()
        etConfirmContra.transformationMethod =  PasswordTransformationMethod()

        etUsuario.setText(usuarioLogeado)
        etUsuario.isEnabled = false
        etConfirmContra.isEnabled = false

        ventana.setPositiveButton(R.string.modificar_str){_, _ ->
            modificarDatos(view, etCorreo.text.toString(), etContra.text.toString())
        }
        ventana.setNeutralButton(R.string.cancelar_str){_,_ -> }

        val dialog: AlertDialog = ventana.create()
        //// CIERRA EL DIALOG CON EL BOTON HACIA ATRAS ////
        dialog.setOnKeyListener(object : DialogInterface.OnKeyListener {
            override fun onKey(
                arg0: DialogInterface, keyCode: Int, event: KeyEvent
            ): Boolean {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss()
                }
                return true
            }
        })
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

        etContra.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(etContra.length() < 3)
                {
                    etContra.error = getString(R.string.mensajeUsuario_str)
                    requestFocus(etContra)
                    etConfirmContra.isEnabled = false
                    contraOk = false
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                } else {
                    etConfirmContra.isEnabled = true
                    contraOk = true
                    if(confirmContra){
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    }
                }
            }
        })

        etConfirmContra.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(etConfirmContra.text.toString().equals(etContra.text.toString())){
                    confirmContra = true
                    if(contraOk){
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    }
                } else {
                    etConfirmContra.error = getString(R.string.mensajeContras_str)
                    requestFocus(etConfirmContra)
                    confirmContra = false
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                }
            }
        })
    }

    private fun modificarDatos(view : View, correo : String, contra : String){
        val datos = ContentValues()
        datos.put("correo", correo)
        datos.put("contra", contra)

        val bd =  BaseDeDatos(this, "Usuarios", null , 1)
        val basededatos = bd.writableDatabase
        basededatos.update("Usuarios", datos, "usuario='$usuarioLogeado'", null)
        basededatos.close()

        Snackbar.make(view, R.string.datosModificados_str, Snackbar.LENGTH_LONG).show()
    }

    fun buscarMaterial_sesionUsuario(vista : View){
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

        val odb = BaseDeDatos(this, "Materiales", null, 1)
        val fdb = odb.readableDatabase
        val cursorMaterial = fdb.rawQuery("select material from Materiales", null)

        if(cursorMaterial.moveToFirst()){
            listaMateriales = ArrayList()

            do{
                listaMateriales?.add(cursorMaterial.getString(0))
            }while (cursorMaterial.moveToNext())

            /// ELIMINA LOS ELEMENTOS REPETIDOS ////
            val hs = HashSet<String>()
            hs.addAll(listaMateriales!!)
            listaMateriales!!.clear()
            listaMateriales!!.addAll(hs)
        }
        fdb.close()
        cursorMaterial.close()
        val a = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listaMateriales)
        listaview.adapter = a

        dialog.show()

        listaview.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val dato = parent.getItemAtPosition(position)
            dialog.dismiss()
            chipMaterial(vista, dato.toString())

            mMap.clear()
            recicladorasMaterialBuscado(dato.toString())
        }
    }

    fun cerrarSesion_sesionUsuario(view : View){
        val preferences = getSharedPreferences("user", Context.MODE_PRIVATE)
        preferences.edit().clear().apply()

        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
        finish()
    }
        /// FIN MENU ///

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

    private fun mejorPrecio(material : String){
        val bd = BaseDeDatos(this, "Materiales", null, 1)
        val flujoBdMateriales = bd.readableDatabase
        val consultaMateriales = flujoBdMateriales.rawQuery("select precio from Materiales where material = '$material'", null)

        var mayorPrecio : Double
        mayorPrecio = 0.0

        if(consultaMateriales.moveToFirst()){
            var listaPrecios: ArrayList<Double> = ArrayList()

            do {
                listaPrecios.add(consultaMateriales.getDouble(0))
            }while (consultaMateriales.moveToNext())

            mayorPrecio = Collections.max(listaPrecios) /// DA EL NUMERO MAYOR EN LA LISTA ///
        }
        consultaMateriales.close()

        val materialUsuario = flujoBdMateriales.rawQuery("select usuario from Materiales where precio='$mayorPrecio' and material='$material'", null)
        if(materialUsuario.moveToFirst()){
            val usuariorecicladora = materialUsuario.getString(0)

            val bdUbicacion = BaseDeDatos(this, "Ubicacion", null, 1)
            val flujoBdUbicacion = bdUbicacion.readableDatabase
            val consultaUbicacion = flujoBdUbicacion.rawQuery("select latitud, longitud from Ubicacion where usuario = '$usuariorecicladora'", null)

            if(consultaUbicacion.moveToFirst()){
                val latlog = LatLng(consultaUbicacion.getDouble(0), consultaUbicacion.getDouble(1))
                val camara = CameraUpdateFactory.newLatLngZoom(latlog, 16.5F)
                mMap.animateCamera(camara)    /// MUEVE LA CAMARA HASTA EL MARCADOR ///
            }
            flujoBdUbicacion.close()
            consultaUbicacion.close()
        }
        flujoBdMateriales.close()
        materialUsuario.close()
    }

    private fun chipMaterial(vista : View ,material : String){
        val i = LayoutInflater.from(this@SesionUsuario)
        val chipItem = i.inflate(R.layout.chip, null , false) as Chip
        val fab = findViewById<FloatingActionButton>(R.id.fabMejorPrecio_sesionUsuario)
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
                buscarMaterial_sesionUsuario(vista)
            } else {
                Snackbar.make(vista, R.string.maximoMateriales_str, Snackbar.LENGTH_LONG).show()
            }
        }

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
            mejorPrecio(material)
        }
    }

    private fun cerrarDrawer(){
        drawerLayout?.closeDrawer(Gravity.START)
        drawerOpen = false
    }

    private fun requestFocus(view: View) { ////  MUESTRA EL ICONO EN EL EDITTEXT /////
        if (view.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
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
}