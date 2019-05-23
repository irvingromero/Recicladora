package irvinc.example.com.inicioprincipal.Recicladora

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputEditText
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.TextView
import irvinc.example.com.inicioprincipal.BD.BaseDeDatos
import irvinc.example.com.inicioprincipal.InicioSinSesion.MapsActivity
import irvinc.example.com.inicioprincipal.R

class SesionRecicladora : AppCompatActivity() {

    private var usuarioLogeado : String? = null

    private var drawerLayout : DrawerLayout? = null
    private var drawerOpen = false// Bandera para saber el estado del drawer

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sesion_recicladora)
        cerrarTeclado()
        supportActionBar?.hide()

        usuarioLogeado = intent.extras?.getString("usuario")
        findViewById<TextView>(R.id.tvUsuario_SesionRecicladora).text = usuarioLogeado.toString()

        detectarSlide()
        findViewById<ImageButton>(R.id.btnMenu_sesionRecicladora).setOnClickListener {
            drawerLayout?.openDrawer(Gravity.START)
            drawerOpen = true
        }
    }

    fun agregarMaterial(v : View){
        var materialOk = false
        var precioOk = false
        var unidadOk = false

        val ventana = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        // CARGA EL LAYOUT PERSONALIZADO//
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.agregar_material, null)
        ventana.setView(dialogView)

        val material = dialogView.findViewById<TextInputEditText>(R.id.tietMaterial_agregarMaterialXML)
        val precio = dialogView.findViewById<TextInputEditText>(R.id.tietPrecio_agregarMaterialXML)
        val unidad = dialogView.findViewById<TextInputEditText>(R.id.tietUnidad_agregarMaterialXML)

        ventana.setPositiveButton(R.string.agregar_str){ _, _ ->
            cerrarTeclado()

            val addMaterial = ContentValues()
            addMaterial.put("usuario", usuarioLogeado)
            addMaterial.put("material", material.text.toString())
            addMaterial.put("precio", precio.text.toString().toDouble())
            addMaterial.put("unidad", unidad.text.toString())

            val bd =  BaseDeDatos(this, "Materiales", null , 1)
            val basededatos = bd.writableDatabase
            basededatos.insert("Materiales", null, addMaterial)
            basededatos.close()

            Snackbar.make(v, "Material agregado", Snackbar.LENGTH_LONG).show()
        }
        ventana.setNeutralButton(R.string.cancelar_str){ _, _ ->
            cerrarTeclado()
        }

        val dialog: AlertDialog = ventana.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

        material.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(material.length() < 1) //// Valida que se tenga un caracter ////
                {
                    materialOk = false
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                } else {
                    materialOk = true

                    if(material.length() == 26){
                        materialOk = false
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                        material.error = getString(R.string.letras30_str)
                        requestFocus(material)
                    }

                    if(materialOk && precioOk && unidadOk)
                    {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    }
                }
            }
        })

        precio.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(precio.length() < 1) //// Valida que se tenga un caracter ////
                {
                    precioOk = false
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                } else {
                        precioOk = true

                    if(precio.length() == 8){
                        precioOk = false
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                        precio.error = getString(R.string.caracteres7_str)
                        requestFocus(precio)
                    }

                    if(materialOk && precioOk && unidadOk)
                    {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    }
                }
            }
        })

        unidad.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(unidad.length() < 1) //// Valida que se tenga un caracter ////
                {
                    unidadOk = false
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    unidadOk = true

                    if(unidad.length() == 16){
                        unidadOk = false
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                        unidad.error = getString(R.string.caracteres15_str)
                        requestFocus(unidad)
                    }

                    if(materialOk && precioOk && unidadOk)
                    {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    }
                }
            }
        })
    }

    fun misDatos(vista : View){

    }

    fun fotos(vista : View){

    }

    fun ubicacion(vista : View){

    }

    fun horario(vista : View){

    }

    fun cerrarSesion(v : View){
        val preferences = getSharedPreferences("user", Context.MODE_PRIVATE)
        preferences.edit().clear().apply()

        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        if (drawerOpen)
        {
            cerrarDrawer()
        } else {
            super.onBackPressed()
        }
    }

    private fun cerrarDrawer(){
        drawerLayout?.closeDrawer(Gravity.START)
        drawerOpen = false
    }

    private fun cerrarTeclado(){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    private fun detectarSlide(){
        //// DETECTA CUANDO AL MENU SE LE HACE SLIDE ////
        drawerLayout = findViewById(R.id.drawer_layout_sesionRecicladora) //// Aqui va xd
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

    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }
}