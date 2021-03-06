package irvinc.example.com.inicioprincipal.Recicladora

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.view.GravityCompat
import irvinc.example.com.inicioprincipal.BD.BaseDeDatos
import irvinc.example.com.inicioprincipal.InicioSinSesion.MapsActivity
import irvinc.example.com.inicioprincipal.R
import java.math.RoundingMode
import java.util.ArrayList

class SesionRecicladora : AppCompatActivity() {

    private var usuarioLogeado : String? = null

    private var drawerLayout : DrawerLayout? = null
    private var drawerOpen = false// Bandera para saber el estado del drawer

    private var rv : RecyclerView? = null
    private var listaMateriales : ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sesion_recicladora)
        cerrarTeclado()
        supportActionBar?.hide()

        usuarioLogeado = intent.extras?.getString("usuario")
        findViewById<TextView>(R.id.tvUsuario_SesionRecicladora).text = usuarioLogeado.toString()

        rv = findViewById(R.id.rvMateriales_sesionRecicladora)

        cargarMateriales()
        cargarCalificacion()

        detectarSlide()
        findViewById<ImageButton>(R.id.btnMenu_sesionRecicladora).setOnClickListener {
            drawerLayout?.openDrawer(GravityCompat.START)
            drawerOpen = true
        }
    }

    private fun cargarMateriales(){
        rv?.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        listaMateriales = ArrayList()

        val bd =  BaseDeDatos(this, "Materiales", null , 1)
        val basededatos = bd.readableDatabase
        val datos = basededatos.rawQuery("select material, precio, unidad from Materiales where usuario = '$usuarioLogeado'", null)

        if(datos.moveToFirst())
        {
            do{
                var material = datos.getString(0)
                var precio = datos.getDouble(1)
                var unidad = datos.getString(2)

                listaMateriales!!.add("Material: "+material+"\nPrecio: "+precio+"\nUnidad: "+unidad)
            } while(datos.moveToNext())
        }
        datos.close()
        basededatos.close()
        val adap = Adapter(listaMateriales!!, usuarioLogeado!!)
        rv?.adapter = adap
    }

    private fun cargarCalificacion(){
        val tvCalificacion = findViewById<TextView>(R.id.tvMostrarCalificacion_sesionRecicladora)

        val objetobasededatos = BaseDeDatos(this, "Usuarios", null, 1)
        val flujodedatos = objetobasededatos.readableDatabase

        val cursorPuntuacion = flujodedatos.rawQuery("select calificacion from Calificacion where usuarioRecicladora = '$usuarioLogeado'", null)
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
            tvCalificacion.text = promedioCorto
        } else {///NUNCA A SIDO PUNTUADA LA RECICLADORA ///
            tvCalificacion.text = "0"
        }
        flujodedatos.close()
        cursorPuntuacion.close()
    }

    fun agregarMaterial(v : View){
        var materialOk = false
        var precioOk = false
        var unidadOk = false

        val ventana = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        // CARGA EL LAYOUT PERSONALIZADO DEL DIALOG//
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.agregar_material, null)
        ventana.setView(dialogView)

        val spMaterial = dialogView.findViewById<Spinner>(R.id.spMaterial_agregar_mateiral)
        val spUnidad = dialogView.findViewById<Spinner>(R.id.spUnidades_agregar_mateiral)
        val precio = dialogView.findViewById<TextInputEditText>(R.id.tietPrecio_agregarMaterialXML)

        cargarListaMateriales(spMaterial)
        cargarListaUnidad(spUnidad)

        ventana.setPositiveButton(R.string.agregar_str){ _, _ ->
            cerrarTeclado()

            if(!materialRepetido(spMaterial.selectedItem.toString(), precio.text.toString().toDouble(), spUnidad.selectedItem.toString())){
                val addMaterial = ContentValues()
                addMaterial.put("usuario", usuarioLogeado)
                addMaterial.put("material", spMaterial.selectedItem.toString())
                addMaterial.put("precio", precio.text.toString().toDouble())
                addMaterial.put("unidad", spUnidad.selectedItem.toString())

                val bd =  BaseDeDatos(this, "Materiales", null , 1)
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
            } else {
                Snackbar.make(v, "Material repetido", Snackbar.LENGTH_LONG).show()
            }
        }
        ventana.setNeutralButton(R.string.cancelar_str){ _, _ ->
            cerrarTeclado()
        }

        val dialog: AlertDialog = ventana.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false


        spMaterial.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if(!spMaterial.selectedItem.toString().contains("Seleccionar material")){
                    materialOk = true

                    if(materialOk && precioOk && unidadOk)
                    {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    }
                } else {
                    materialOk = false
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                }
            }
        }

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

        spUnidad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if(!spUnidad.selectedItem.toString().contains("Seleccionar unidad")){
                    unidadOk = true

                    if(materialOk && precioOk && unidadOk)
                    {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    }
                } else {
                    unidadOk = false
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                }
            }
        }
    }

    private fun cargarListaMateriales(spmaterial : Spinner){
        listaMateriales = ArrayList()

        val bd =  BaseDeDatos(this, "material", null , 1)
        val basededatos = bd.readableDatabase
        val materiales = basededatos.rawQuery("select material from ListaMateriales", null)
        if(materiales.moveToFirst()){
            do{
                listaMateriales?.add(materiales.getString(0))
            }while (materiales.moveToNext())

            listaMateriales!!.sort()/// ORDENA ALFABETICAMENTE ///
            listaMateriales?.add(0, "Seleccionar material")

            val a = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listaMateriales)
            a.setDropDownViewResource(android.R.layout.simple_list_item_activated_1)
            spmaterial.adapter = a
        }
        basededatos.close()
        materiales.close()
    }

    private fun cargarListaUnidad(unidad : Spinner){
        listaMateriales = ArrayList()

        val bd =  BaseDeDatos(this, "material", null , 1)
        val basededatos = bd.readableDatabase
        val unidades = basededatos.rawQuery("select unidad from ListaUnidades", null)
        if(unidades.moveToFirst()){
            do{
                listaMateriales?.add(unidades.getString(0))
            }while (unidades.moveToNext())

            listaMateriales!!.sort()/// ORDENA ALFABETICAMENTE ///
            listaMateriales?.add(0, "Seleccionar unidad")

            val a = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listaMateriales)
            a.setDropDownViewResource(android.R.layout.simple_list_item_activated_1)
            unidad.adapter = a
        }
        basededatos.close()
        unidades.close()
    }

    private fun materialRepetido(material : String, precio: Double, unidad : String) : Boolean {
        val bd =  BaseDeDatos(this, "Materiales", null , 1)
        val basededatos = bd.readableDatabase
        val repetido = basededatos.rawQuery("select material, precio, unidad from Materiales where usuario = '$usuarioLogeado' and material='$material' and precio='$precio' and unidad='$unidad'", null)
        if(repetido.moveToFirst()){
            basededatos.close()
            repetido.close()
            return true
        }
        basededatos.close()
        repetido.close()
        return false
    }

        //// MENU ////
    fun misDatos(vista : View){
        val hilo = Handler(Looper.getMainLooper())
        hilo.post {
            val i = Intent(this, DatosRecicladora::class.java)
            i.putExtra("usuario", usuarioLogeado)
            startActivity(i)

            cerrarDrawer()
        }
    }

    fun fotos(vista : View){

    }

    fun ubicacion(vista : View){
        val h = Handler(Looper.getMainLooper())
        h.post{
            val intent = Intent(this, UbicacionRecicladora::class.java)
            intent.putExtra("usuario", usuarioLogeado)
            startActivity(intent)

            cerrarDrawer()
        }
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

    fun regristroVenta(vista: View){
        val hilo = Handler(Looper.getMainLooper())
        hilo.post {
            val i = Intent(this, VentasRecicladora::class.java)
            i.putExtra("usuario", usuarioLogeado)
            startActivity(i)

            cerrarDrawer()
        }
    }

    fun registroCompra(view: View){
        val hilo = Handler(Looper.getMainLooper())
        hilo.post {
            val i = Intent(this, ComprasRecicladora::class.java)
            i.putExtra("usuario", usuarioLogeado)
            startActivity(i)

            cerrarDrawer()
        }
    }

    fun generarReporte(view: View){
        val reporte = Intent(this, ReporteRecicladora::class.java)
        reporte.putExtra("usuario", usuarioLogeado)
        startActivity(reporte)
        cerrarDrawer()
    }

    fun AgregarMaterialLista(view: View){
        val listamateriales = Intent(this, ListaMateriales::class.java)
        startActivity(listamateriales)
        cerrarDrawer()
    }

    fun AgregarUnidadLista(view: View){
        val unidades = Intent(this, ListaUnidades::class.java)
        startActivity(unidades)
        cerrarDrawer()
    }
        ////    FIN MENU ////
    override fun onBackPressed() {
        if (drawerOpen)
        {
            cerrarDrawer()
        } else {
            super.onBackPressed()
        }
    }

    private fun cerrarDrawer(){
        drawerLayout?.closeDrawer(GravityCompat.START)
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
    class Adapter(var list: ArrayList<String>, val usuario : String) : RecyclerView.Adapter<Adapter.ViewHolder>(){

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            val v = LayoutInflater.from(p0.context).inflate(R.layout.datos_material, p0, false)
            return ViewHolder(v)
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
            p0.mostrarDatos(list[p1])

            p0.itemView.findViewById<MaterialButton>(R.id.mbModificar_datosmaterial).setOnClickListener {
                var materialOk = true
                var precioOk = true
                var unidadOk = true

                val ventana = AlertDialog.Builder(p0.itemView.context, R.style.CustomDialogTheme)
                // CARGA EL LAYOUT PERSONALIZADO//
                val inflater = LayoutInflater.from(p0.itemView.context)
                val dialogView = inflater.inflate(R.layout.agregar_material, null)
                ventana.setView(dialogView)

                val materialViejo = p0.nombreMaterial(list[p1])
                val precioViejo = p0.cantidadPrecio(list[p1])
                val unidadViejo = p0.nombreUnidad(list[p1])

                val titulo = dialogView.findViewById<TextView>(R.id.tvTitulo_agregarMaterial)
                titulo.text = "Editar: "+materialViejo

                val material = dialogView.findViewById<Spinner>(R.id.spMaterial_agregar_mateiral)
                val unidad = dialogView.findViewById<Spinner>(R.id.spUnidades_agregar_mateiral)
                val campoPrecio = dialogView.findViewById<TextInputEditText>(R.id.tietPrecio_agregarMaterialXML)

                cargarListaMateriales(p0, material)
                cargarListaUnidades(p0, unidad)

//                campoMaterial.setText(materialViejo)
                campoPrecio.setText(precioViejo)
//                campoUnidad.setText(unidadViejo)

                ventana.setPositiveButton(R.string.modificar_str){ _, _ ->
//                    val materialNuevo = campoMaterial.text.toString()
                    val precioNuevo = campoPrecio.text.toString()
//                    val unidadNuevo = campoUnidad.text.toString()

                    val datos = ContentValues()
                    datos.put("material", material.selectedItem.toString())
                    datos.put("precio", precioNuevo)
                    datos.put("unidad", unidad.selectedItem.toString())

                    val bd =  BaseDeDatos(p0.itemView.context, "Materiales", null , 1)
                    val basededatos = bd.writableDatabase
                    basededatos.update("Materiales", datos, "material=? AND precio=? AND unidad=?", arrayOf(materialViejo, precioViejo,unidadViejo))
                    basededatos.close()

                    //// ELIMINA EL ELEMENTO DE LA VISTA ////
                    list.removeAt(p1)
                    notifyItemRemoved(p1)
                    notifyItemChanged(p1, list.size)

                    list.add("Material: "+material.selectedItem.toString()+"\nPrecio: "+precioNuevo+"\nUnidad: "+unidad.selectedItem.toString())
                }
                ventana.setNeutralButton(R.string.cancelar_str){ _, _ ->
                }

                val dialog: AlertDialog = ventana.create()
                dialog.show()

                material.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                    override fun onNothingSelected(p0: AdapterView<*>?) {}
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        if(!material.selectedItem.toString().contains("Seleccionar material")){
                            materialOk = true

                            if(materialOk && precioOk && unidadOk)
                            {
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                            }
                        } else {
                            unidadOk = false
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                        }
                    }
                }

                campoPrecio.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {}
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if(campoPrecio.length() < 1) //// Valida que se tenga un caracter ////
                        {
                            precioOk = false
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                        } else {
                            precioOk = true

                            if(campoPrecio.length() == 8){
                                precioOk = false
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                            }

                            if(materialOk && precioOk && unidadOk)
                            {
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                            }
                        }
                    }
                })

                unidad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                    override fun onNothingSelected(p0: AdapterView<*>?) {}
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        if(!unidad.selectedItem.toString().contains("Seleccionar unidad")){
                            unidadOk = true

                            if(materialOk && precioOk && unidadOk)
                            {
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                            }
                        } else {
                            unidadOk = false
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                        }
                    }
                }
            }

            p0.itemView.findViewById<MaterialButton>(R.id.mbEliminar_datosmaterial).setOnClickListener {
                val material = p0.nombreMaterial(list[p1])
                val precio = p0.cantidadPrecio(list[p1])
                val unidad = p0.nombreUnidad(list[p1])

                val bdM = BaseDeDatos(p0.itemView.context, "Materiales", null, 1)
                val basededatosMateriales = bdM.writableDatabase

                basededatosMateriales.delete("Materiales", "usuario=? AND material=? AND precio=? AND unidad=?", arrayOf(usuario, material, precio, unidad))
                basededatosMateriales.close()

                    //// ELIMINA EL ELEMENTO DE LA VISTA ////
                list.removeAt(p1)
                notifyItemRemoved(p1)
                notifyItemChanged(p1, list.size)

                Snackbar.make(p0.itemView, "$material eliminado", Snackbar.LENGTH_LONG).show()
            }
        }

        private fun cargarListaMateriales(c : ViewHolder, spmaterial : Spinner){
            val listaMateriales = ArrayList<String>()

            val bd =  BaseDeDatos(c.itemView.context, "material", null , 1)
            val basededatos = bd.readableDatabase
            val materiales = basededatos.rawQuery("select material from ListaMateriales", null)
            if(materiales.moveToFirst()){
                do{
                    listaMateriales?.add(materiales.getString(0))
                }while (materiales.moveToNext())

                listaMateriales!!.sort()/// ORDENA ALFABETICAMENTE ///
                listaMateriales?.add(0, "Seleccionar material")

                val a = ArrayAdapter<String>(c.itemView.context, android.R.layout.simple_spinner_item, listaMateriales)
                a.setDropDownViewResource(android.R.layout.simple_list_item_activated_1)
                spmaterial.adapter = a
            }
            basededatos.close()
            materiales.close()
        }

        private fun cargarListaUnidades(c : ViewHolder, unidad : Spinner){
            val listaUnidades = ArrayList<String>()

            val bd =  BaseDeDatos(c.itemView.context, "material", null , 1)
            val basededatos = bd.readableDatabase
            val unidades = basededatos.rawQuery("select unidad from ListaUnidades", null)
            if(unidades.moveToFirst()){
                do{
                    listaUnidades?.add(unidades.getString(0))
                }while (unidades.moveToNext())

                listaUnidades!!.sort()/// ORDENA ALFABETICAMENTE ///
                listaUnidades?.add(0, "Seleccionar material")

                val a = ArrayAdapter<String>(c.itemView.context, android.R.layout.simple_spinner_item, listaUnidades)
                a.setDropDownViewResource(android.R.layout.simple_list_item_activated_1)
                unidad.adapter = a
            }
            basededatos.close()
            unidades.close()
        }

        class ViewHolder(view : View) : RecyclerView.ViewHolder(view){

            fun mostrarDatos(texto : String){
                val t : TextView = itemView.findViewById(R.id.tvDatos_datosMaterial)
                t.text = texto
            }

            fun nombreMaterial(cadena : String) : String {
                val datos = cadena.split("\n") //// separa los tres renglones ////
                val materialVector = datos[0].split(":")
                return materialVector[1].trim()
            }

            fun cantidadPrecio (cadena : String) : String {
                val datos = cadena.split("\n") //// separa los tres renglones ////
                val materialVector = datos[0].split(":")
                val m = materialVector[1].trim()
                ///////
                val precioVector = datos[1].split(":")
                return precioVector[1].trim()
            }

            fun nombreUnidad(cadena : String) : String {
                val datos = cadena.split("\n") //// separa los tres renglones ////
                val materialVector = datos[0].split(":")
                val m = materialVector[1].trim()
                ///////
                val precioVector = datos[1].split(":")
                val p = precioVector[1].trim()
                ////
                val unidadVector = datos[2].split(":")
                return unidadVector[1].trim()
            }
        }// ViewHolder
    }//Adapter
}