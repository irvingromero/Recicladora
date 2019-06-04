package irvinc.example.com.inicioprincipal.Recicladora

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.design.button.MaterialButton
import android.support.design.widget.TextInputEditText
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import irvinc.example.com.inicioprincipal.BD.BaseDeDatos
import irvinc.example.com.inicioprincipal.InicioSinSesion.MapsActivity
import irvinc.example.com.inicioprincipal.R
import java.util.ArrayList

class SesionRecicladora : AppCompatActivity() {

    private val bd =  BaseDeDatos(this, "Materiales", null , 1)

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

        cargarMateriales()

        detectarSlide()
        findViewById<ImageButton>(R.id.btnMenu_sesionRecicladora).setOnClickListener {
            drawerLayout?.openDrawer(Gravity.START)
            drawerOpen = true
        }
    }

    private fun cargarMateriales(){
        val rv = findViewById<RecyclerView>(R.id.rvMateriales_sesionRecicladora)
        rv.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        val listaMateriales = ArrayList<String>()

        val basededatos = bd.readableDatabase
        val datos = basededatos.rawQuery("select material, precio, unidad from Materiales where usuario = '$usuarioLogeado'", null)

        if(datos.moveToFirst())
        {
            do{
                var material = datos.getString(0)
                var precio = datos.getDouble(1)
                var unidad = datos.getString(2)

                listaMateriales.add("Material: "+material+"\nPrecio: "+precio+"\nUnidad: "+unidad)
            } while(datos.moveToNext())
        }

        basededatos.close()
        val adap = Adapter(listaMateriales)
        rv.adapter = adap
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

            val basededatos = bd.writableDatabase
            basededatos.insert("Materiales", null, addMaterial)
            basededatos.close()

            val toast = Toast(applicationContext)
            //// CARGA EL LAYOUT A UNA VISTA ////
            val view = layoutInflater.inflate(R.layout.usuario_registrado, null)
            toast.view = view
            toast.duration = Toast.LENGTH_LONG
            toast.setGravity(Gravity.BOTTOM,0, 30)
            view.findViewById<TextView>(R.id.tvToast_usuarioregistrado).text = getString(R.string.materialAgregado_str)
            toast.show()

            cargarMateriales()
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

    private fun modificarMaterial(c : Context, material : String, precio : String, unidad : String){
/*
        val ventana = AlertDialog.Builder(, R.style.CustomDialogTheme)
        // CARGA EL LAYOUT PERSONALIZADO//
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.agregar_material, null)
        ventana.setView(dialogView)
        ventana.setTitle("Modificar "+material)

        ventana.setPositiveButton(R.string.modificar_str){ _, _ ->
            cerrarTeclado()

        }
        ventana.setNeutralButton(R.string.cancelar_str){ _, _ ->
            cerrarTeclado()
        }

        val dialog: AlertDialog = ventana.create()
        dialog.show()
 */
    }

    fun misDatos(vista : View){
        val hilo = Handler(Looper.getMainLooper())
        hilo.post {
            val i = Intent(this, DatosRecicladora::class.java)
            startActivity(i)
        }
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

    ////////////////////////////////////////////////////////////////////////////////////////////
    class Adapter(var list: ArrayList<String>) : RecyclerView.Adapter<Adapter.ViewHolder>(){

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            val v = LayoutInflater.from(p0.context).inflate(R.layout.datos_material, p0, false)
            return ViewHolder(v)
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
            p0.mostrarDatos(list[p1])
        }

        class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
            fun mostrarDatos(texto : String){
                val t : TextView = itemView.findViewById(R.id.tvDatos_datosMaterial)
                t.text = texto

                itemView.setOnClickListener {
 //                   Toast.makeText(itemView.context, p, Toast.LENGTH_SHORT).show()

                }


                itemView.findViewById<MaterialButton>(R.id.mbModificar_datosmaterial).setOnClickListener {
                        ////  OBTENER MATERIAL, PRECIO Y UNIDAD /////
                    val datos = texto.split("\n") //// separa los tres renglones ////
                    val materialVector = datos[0].split(":")
                    val m = materialVector[1].trim()
                    ///////
                    val precioVector = datos[1].split(":")
                    val p = precioVector[1].trim()
                    ////
                    val unidadVector = datos[2].split(":")
                    val u = unidadVector[1].trim()

                    val s = SesionRecicladora()
                    s.modificarMaterial(itemView.context, m, p , u)
                }

                itemView.findViewById<MaterialButton>(R.id.mbEliminar_datosmaterial).setOnClickListener {
                }
            }
        }
    }
}