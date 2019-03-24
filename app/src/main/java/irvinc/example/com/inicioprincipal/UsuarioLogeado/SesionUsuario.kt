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
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputEditText
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
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
import com.google.android.gms.maps.model.MarkerOptions
import irvinc.example.com.inicioprincipal.BD.BaseDeDatos
import irvinc.example.com.inicioprincipal.InicioSinSesion.MapsActivity
import irvinc.example.com.inicioprincipal.R

class SesionUsuario : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var miUbicacion : Location? = null
    private var drawerLayout : DrawerLayout? = null
    private var drawerOpen = false// Bandera para saber el estado del drawer

    private var usuarioLogeado : String? = null

    private var chipgroup : ChipGroup? = null
    private var contador = 0 //// Contador para los chips ////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sesion_usuario)
        supportActionBar?.hide()

        usuarioLogeado = intent.extras?.getString("usuario")

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapaUsuario) as SupportMapFragment
        mapFragment.getMapAsync(this)

        detectarSlide()
        findViewById<ImageButton>(R.id.btnMenu_sesionUsuario).setOnClickListener {
            drawerLayout?.openDrawer(Gravity.START)
            drawerOpen = true
        }

        chipgroup = findViewById(R.id.cg_sesionUsuario)
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
        listaview.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val dato = parent.getItemAtPosition(position)
            dialog.dismiss()
            chipMaterial(vista, dato.toString())
        }

        val values = arrayOf("Latas", "Chatarra","Vidrio","Carton","Alumino")
        val a = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values)
        listaview.adapter = a

        dialog.show()
    }

    private fun chipMaterial(vista : View ,material : String){
        val i = LayoutInflater.from(this@SesionUsuario)
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
                buscarMaterial_sesionUsuario(vista)
            } else {
                Snackbar.make(vista, R.string.maximoMateriales_str, Snackbar.LENGTH_LONG).show()
            }
        }

        chipItem.setOnCloseIconClickListener {
            chipgroup?.removeView(chipItem)
            contador --
        }
    }

    fun mejorPrecio_sesionUsuario(view : View){

    }

    fun masCercana_sesionUsuario(view : View){

    }

    fun cerrarSesion_sesionUsuario(view : View){
        val preferences = getSharedPreferences("user", Context.MODE_PRIVATE)
        preferences.edit().clear().apply()

        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun cerrarDrawer(){
        drawerLayout?.closeDrawer(Gravity.START)
        drawerOpen = false
    }

    private fun requestFocus(view: View) {
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
            super.onBackPressed()
        }
    }
}