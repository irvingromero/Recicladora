package irvinc.example.com.inicioprincipal.UsuarioLogeado

import android.content.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputEditText
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
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
import com.google.android.gms.maps.model.MarkerOptions
import irvinc.example.com.inicioprincipal.BD.BaseDeDatos
import irvinc.example.com.inicioprincipal.MapsActivity
import irvinc.example.com.inicioprincipal.R

class SesionUsuario : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var drawerLayout : DrawerLayout? = null
    private var drawerOpen = false// Bandera para saber el estado del drawer

    private var usuarioLogeado : String? = null

    var listaDatos : ArrayList<String>? = null
    var lista : RecyclerView? = null

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

        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
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

    fun buscarMaterial_sesionUsuario(view : View){
        cerrarDrawer()

        val ventana = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        // CARGA EL LAYOUT PERSONALIZADO//
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.ventana_buscar_material, null)
        ventana.setView(dialogView)
        ventana.setTitle("Materiales disponibles")

        val dialog: AlertDialog = ventana.create()

        val listaview = dialogView.findViewById<ListView>(R.id.lvBuscar_material)
        listaview.isClickable = true
        listaview.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            dialog.dismiss()
        }


        val values = arrayOf("irv", "awfaf","egeg","asfef","klanfkwla","alksef","anck","ekjfn","xdxd")
        val a = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values)
        listaview.adapter = a

        dialog.show()
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