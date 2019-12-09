package irvinc.example.com.inicioprincipal.Recicladora

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.textfield.TextInputEditText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import irvinc.example.com.inicioprincipal.BD.BaseDeDatos
import irvinc.example.com.inicioprincipal.R
import kotlinx.android.synthetic.main.registro_venta_bs.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar

class VentasRecicladora : AppCompatActivity() {

    private var usuarioLogeado : String? = null
    private var bottomSheetBehavior : BottomSheetBehavior<LinearLayout>? = null
    private var rv : RecyclerView? = null
    private var listaVentas : ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ventas_recicladora)

        supportActionBar?.hide()
        usuarioLogeado = intent.extras?.getString("usuario")

        mostrarVentas()

        bottomSheetBehavior = BottomSheetBehavior.from<LinearLayout>(bsRegistroVenta)
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior?.skipCollapsed = true

            //// LISTENER PARA EL BOTTOMSHEET /////
        bottomSheetBehavior?.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {
                val campoNombre = findViewById<TextInputEditText>(R.id.tietNombreVenta_registroVenta)
                campoNombre.error = null
            }

            override fun onStateChanged(p0: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN ->{
                        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> { }
                }
            }
        })

        findViewById<MaterialButton>(R.id.btnAgregarVenta_VentasRecicladora).setOnClickListener {
            val titulo = findViewById<TextView>(R.id.tvTitulo_registroVenta)
            titulo?.text = getString(R.string.agregarVenta_str)
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            formularioVenta()
        }

        findViewById<ImageButton>(R.id.btnAtras_VentasRecicladora).setOnClickListener {
            onBackPressed()
        }
        setRecyclerViewItemTouchListener()
    }

    private fun mostrarVentas(){
        rv = findViewById(R.id.rvRegistrosVentas_ventasRecicladora)
        rv?.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        listaVentas = ArrayList()

        val bd =  BaseDeDatos(this, "Ventas", null , 1)
        val basededatos = bd.readableDatabase
        val datos = basededatos.rawQuery("select nombreCliente, material, id,cantidad, unidad, ganancia, fecha from Ventas where usuarioRecicladora= '$usuarioLogeado'", null)
        if(datos.moveToFirst())
        {
            do{
                val cliente = datos.getString(0)
                val material = datos.getString(1)
                val id = datos.getInt(2)
                val cantidad = datos.getDouble(3)
                val unidad = datos.getString(4)
                val ganancia = datos.getDouble(5)
                val fecha = datos.getString(6)

                listaVentas?.add("$cliente:$material:$id:$cantidad:$unidad:$ganancia:$fecha")
            } while(datos.moveToNext())
        }
        datos.close()
        basededatos.close()

        val contexto = this
        val adap = Adapter(listaVentas!!, usuarioLogeado!!, contexto)
        rv?.adapter = adap
    }

    private fun formularioVenta(){
        val campoNombre = findViewById<TextInputEditText>(R.id.tietNombreVenta_registroVenta)
        val material = findViewById<Spinner>(R.id.spMaterialVendido_registroVenta)
        val campoCantidad = findViewById<TextInputEditText>(R.id.tietCantidad_registroVenta)
        val unidad = findViewById<Spinner>(R.id.spUnidad_registroVenta)
        val campoGanancia = findViewById<TextInputEditText>(R.id.tietGanacia_registroVenta)
        val btnRegistro = findViewById<MaterialButton>(R.id.mbRegistroVenta_registroVenta)
        btnRegistro.text = getString(R.string.registrar_str)
        btnRegistro.isEnabled = false
        //// CARGA LA FECHA DEL DIA ////
        val fecha = SimpleDateFormat("d/MM/yyyy").format(Date())
        val campoFecha = findViewById<TextInputEditText>(R.id.tietFecha_registroVenta)
        campoFecha.setText(fecha.toString())

        campoNombre.setText("")
        campoCantidad.setText("")
        campoGanancia.setText("")
        material.setSelection(0)
        unidad.setSelection(0)

        var bnombre = false
        var bmaterial = false
        var bcantidad = false
        var bunidad = false
        var bganancia = false

        findViewById<ImageButton>(R.id.ibCalendario_registroVenta).setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val ventanaFecha = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, mes, dia ->
                val mesReal = mes+1
                val fechaCompleta = "$dia/$mesReal/$year"
                campoFecha.setText(fechaCompleta)
            }, year, month, day)
            ventanaFecha.show()
        }

        campoNombre.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(campoNombre.length() > 2){
                    bnombre = true

                    if(bmaterial && bcantidad && bunidad && bganancia){
                        btnRegistro.isEnabled = true
                    }

                } else {
                    bnombre = false

                    campoNombre.error = getString(R.string.mensajeUsuario_str)
                    requestFocus(campoNombre)
                    btnRegistro.isEnabled = false
                }
            }
        })

        material.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if(!material.selectedItem.toString().contains("Seleccionar material")){
                    bmaterial = true

                    if(bnombre && bcantidad && bunidad && bganancia){
                        btnRegistro.isEnabled = true
                    }

                } else {
                    bmaterial = false
                    btnRegistro.isEnabled = false
                }
            }
        }

        campoCantidad.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                if(!material.selectedItem.toString().contains("Seleccionar material") && !unidad.selectedItem.toString().contains("Seleccionar unidad")){
                    val mate = material.selectedItem.toString()
                    val uni = unidad.selectedItem.toString()

                    val bd =  BaseDeDatos(applicationContext, "Materiales", null , 1)
                    val basededatos = bd.readableDatabase
                    val datoPrecio = basededatos.rawQuery("select precio from Materiales where material='$mate' and unidad='$uni'", null)

                    if(datoPrecio.moveToFirst()){
                        if(campoCantidad.length() > 0) {
                            val cantidad = campoCantidad.text.toString().toDouble()
                            val total = datoPrecio.getDouble(0) * cantidad
                            campoGanancia.setText(total.toString())
                        } else {
                            campoGanancia.setText("")
                        }
                    }
                    basededatos.close()
                    datoPrecio.close()
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(campoCantidad.length() > 0){
                    bcantidad = true

                    if(bnombre && bmaterial && bunidad && bganancia){
                        btnRegistro.isEnabled = true
                    }
                } else {
                    bcantidad = false
                    btnRegistro.isEnabled = false
                }
            }
        })

        unidad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if(!unidad.selectedItem.toString().contains("Seleccionar unidad")){
                    bunidad = true

                    if(!material.selectedItem.toString().contains("Seleccionar material") && campoCantidad.length() > 0){
                        val mate = material.selectedItem.toString()
                        val uni = unidad.selectedItem.toString()

                        val bd =  BaseDeDatos(applicationContext, "Materiales", null , 1)
                        val basededatos = bd.readableDatabase
                        val datoPrecio = basededatos.rawQuery("select precio from Materiales where material='$mate' and unidad='$uni'", null)

                        if(datoPrecio.moveToFirst()){
                            if(campoCantidad.length() > 0) {
                                val cantidad = campoCantidad.text.toString().toDouble()
                                val total = datoPrecio.getDouble(0) * cantidad
                                campoGanancia.setText(total.toString())
                            } else {
                                campoGanancia.setText("")
                            }
                        }
                        basededatos.close()
                        datoPrecio.close()
                    }

                    if(bnombre && bcantidad && bcantidad && bganancia){
                        btnRegistro.isEnabled = true
                    }
                } else {
                    bunidad = false
                    btnRegistro.isEnabled = false
                }
            }
        }

        campoGanancia.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(campoGanancia.length() > 0){
                    bganancia = true

                    if(bnombre && bcantidad && bunidad && bunidad){
                        btnRegistro.isEnabled = true
                    }
                } else {
                    bganancia = false
                    btnRegistro.isEnabled = false
                }
            }
        })

        btnRegistro.setOnClickListener {
            registrarVenta(campoNombre.text.toString(),
                material.selectedItem.toString(),
                campoCantidad.text.toString().toDouble(),
                unidad.selectedItem.toString(),
                campoGanancia.text.toString().toDouble(),
                campoFecha.text.toString())

            //// VOLVER A LANZAR EL ACTIVITY PARA AHORRAR METODO PARA VOLVER A CARGAR EL RV
            /// CORRIGE BUG DEL BOTTOMSHEET CON EL TECLADO ABIERTO //////////////
            finish()
            val hilo = Handler(Looper.getMainLooper())
            hilo.post {
                val i = Intent(this, VentasRecicladora::class.java)
                i.putExtra("usuario", usuarioLogeado)
                startActivity(i)
            }
        }
    }

    private fun registrarVenta(cliente : String, material : String, cantidad : Double, unidad : String, ganancia: Double, fecha : String){
        val cv = ContentValues()
        cv.put("usuarioRecicladora", usuarioLogeado)
        cv.put("nombreCliente", cliente)
        cv.put("material", material)
        cv.put("cantidad", cantidad)
        cv.put("unidad", unidad)
        cv.put("ganancia", ganancia)
        cv.put("fecha", fecha)

        val cvClientesRecicladora = ContentValues()
        cvClientesRecicladora.put("nombre", cliente)

        val bd =  BaseDeDatos(this, "Ventas", null , 1)
        val basededatos = bd.writableDatabase
        basededatos.insert("Ventas", null, cv)

        basededatos.insert("ClientesRecicladora", null, cvClientesRecicladora)
        basededatos.close()

        val toast = Toast(applicationContext)
        //// CARGA EL LAYOUT A UNA VISTA ////
        val view = layoutInflater.inflate(R.layout.usuario_registrado, null)
        toast.view = view
        toast.duration = Toast.LENGTH_LONG
        toast.setGravity(Gravity.BOTTOM,0, 30)
        view.findViewById<TextView>(R.id.tvToast_usuarioregistrado).text = getString(R.string.registroAgregado_str)
        toast.show()
    }

    private fun eliminarVenta(){
        val tvId = findViewById<TextView>(R.id.tvId_ventasRecicladora)
        val id = tvId.text.toString()

        val bdM = BaseDeDatos(this, "Ventas", null, 1)
        val basededatosMateriales = bdM.writableDatabase

        basededatosMateriales.delete("Ventas", "id=?", arrayOf(id))
        basededatosMateriales.close()

        val toast = Toast(applicationContext)
        //// CARGA EL LAYOUT A UNA VISTA ////
        val view = layoutInflater.inflate(R.layout.usuario_registrado, null)
        toast.view = view
        toast.duration = Toast.LENGTH_LONG
        toast.setGravity(Gravity.BOTTOM,0, 30)
        view.findViewById<TextView>(R.id.tvToast_usuarioregistrado).text = getString(R.string.ventaEliminada_str)
        toast.show()
    }

    override fun onBackPressed() {
        if(bottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED){
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        } else {
            super.onBackPressed()
        }
    }

    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    private fun setRecyclerViewItemTouchListener() {
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, viewHolder1: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                     dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {

                val deleteIcon = ContextCompat.getDrawable(applicationContext, R.drawable.basura_icono)
                val itemView = viewHolder.itemView
                val itemHeight = itemView.bottom - itemView.top
                val intrinsicHeight = deleteIcon?.intrinsicHeight
                    // Draw the red delete background
                val background = ColorDrawable()
                val backgroundColor = Color.parseColor("#E90000")

                background.color = backgroundColor
                background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                background.draw(c)

                // Calculate position of delete icon
                val iconTop = itemView.top + (itemHeight - intrinsicHeight!!) / 2
                val iconMargin = (itemHeight - intrinsicHeight) / 2
                val iconLeft = itemView.right - iconMargin - intrinsicHeight
                val iconRight = itemView.right - iconMargin
                val iconBottom = iconTop + intrinsicHeight

                // Draw the delete icon
                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                deleteIcon.draw(c)

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                val position = viewHolder.adapterPosition
                listaVentas?.removeAt(position)
                rv?.adapter!!.notifyItemRemoved(position)

                eliminarVenta()
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(rv)
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class Adapter(var listaVenta: ArrayList<String>, val usuario : String, contextoActividad : Activity) : RecyclerView.Adapter<Adapter.ViewHolder>(){

        private var bottomSheetBehavior : BottomSheetBehavior<LinearLayout>? = null
        private var titulo : TextView? = null
        private var id : TextView? = null
        private var campoCliente : TextInputEditText? = null
        private var spMaterial : Spinner? = null
        private var campoCantidad : TextInputEditText? = null
        private var spUnidad : Spinner? = null
        private var campoGanancia : TextInputEditText? = null
        private var campoFecha : TextInputEditText? = null
        private var btnCalendar : ImageButton? = null
        private var btnModificar : MaterialButton? = null
            //// DATOS PARA EDITAR UN REGISTRO ////
        init {
            bottomSheetBehavior = BottomSheetBehavior.from<LinearLayout>(contextoActividad.findViewById(R.id.bsRegistroVenta))
            bottomSheetBehavior?.skipCollapsed = true

            id = contextoActividad.findViewById(R.id.tvId_registroVenta)
            titulo = contextoActividad.findViewById(R.id.tvTitulo_registroVenta)
            campoCliente = contextoActividad.findViewById(R.id.tietNombreVenta_registroVenta)
            spMaterial = contextoActividad.findViewById(R.id.spMaterialVendido_registroVenta)
            campoCantidad = contextoActividad.findViewById(R.id.tietCantidad_registroVenta)
            spUnidad = contextoActividad.findViewById(R.id.spUnidad_registroVenta)
            campoGanancia = contextoActividad.findViewById(R.id.tietGanacia_registroVenta)
            campoFecha = contextoActividad.findViewById(R.id.tietFecha_registroVenta)
            btnCalendar = contextoActividad.findViewById(R.id.ibCalendario_registroVenta)
            btnModificar = contextoActividad.findViewById(R.id.mbRegistroVenta_registroVenta)

            cargarListaMateriales(spMaterial!!, contextoActividad.applicationContext)
            cargarListaUnidad(spUnidad!!, contextoActividad.applicationContext)

            btnCalendar?.setOnClickListener {
                val c = Calendar.getInstance()
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)

                val ventanaFecha = DatePickerDialog(contextoActividad, DatePickerDialog.OnDateSetListener { _, year, mes, dia ->
                    val mesReal = mes+1
                    val fechaCompleta = "$dia/$mesReal/$year"
                    campoFecha?.setText(fechaCompleta)
                }, year, month, day)
                ventanaFecha.show()
            }

            btnModificar?.setOnClickListener {
                modificarRegistro(contextoActividad.applicationContext)
                Toast.makeText(contextoActividad.applicationContext, "Registro modificado", Toast.LENGTH_SHORT).show()
                    //// VOLVER A LANZAR EL ACTIVITY PARA AHORRAR METODO PARA VOLVER A CARGAR EL RV
                    /// CORRIGE BUG DEL BOTTOMSHEET CON EL TECLADO ABIERTO //////////////
                contextoActividad.finish()
                val hilo = Handler(Looper.getMainLooper())
                hilo.post {
                    val i = Intent(contextoActividad.applicationContext, VentasRecicladora::class.java)
                    i.putExtra("usuario", usuario)
                    contextoActividad.startActivity(i)
                }
            }

            validaCampos()
        }

        private fun validaCampos(){
            var bnombre = false
            var bmaterial = false
            var bcantidad = false
            var bunidad = false
            var bganancia = false

            campoCliente?.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {}
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if(campoCliente?.length()!! > 2){
                        bnombre = true

                       if(bmaterial && bcantidad && bunidad && bganancia){
                            btnModificar?.isEnabled = true
                        }
                    } else {
                        bnombre = false
                        btnModificar?.isEnabled = false
                    }
                }
            })

            spMaterial?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(p0: AdapterView<*>?) {}
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    if(!spMaterial?.selectedItem.toString().contains("Seleccionar material")){
                        bmaterial = true

                        if(bnombre && bcantidad && bunidad && bganancia){
                            btnModificar?.isEnabled = true
                        }

                    } else {
                        btnModificar?.isEnabled = false
                    }
                }
            }

            campoCantidad?.addTextChangedListener(object : TextWatcher{
                override fun afterTextChanged(p0: Editable?) {}
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if(campoCantidad?.length()!! > 0){
                        bcantidad = true

                        if(bnombre && bmaterial && bunidad && bganancia){
                            btnModificar?.isEnabled = true
                        }
                    } else {
                        btnModificar?.isEnabled = false
                    }
                }
            })

            spUnidad?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(p0: AdapterView<*>?) {}
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    if(!spUnidad?.selectedItem.toString().contains("Seleccionar unidad")){
                        bunidad = true

                        if(bnombre && bcantidad && bcantidad && bganancia){
                            btnModificar?.isEnabled = true
                        }
                    } else {
                        btnModificar?.isEnabled = false
                    }
                }
            }

            campoGanancia?.addTextChangedListener(object : TextWatcher{
                override fun afterTextChanged(p0: Editable?) {}
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if(campoGanancia?.length()!! > 0){
                        bganancia = true

                        if(bnombre && bcantidad && bunidad && bunidad){
                            btnModificar?.isEnabled = true
                        }
                    } else {
                        btnModificar?.isEnabled = false
                    }
                }
            })
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            val v = LayoutInflater.from(p0.context).inflate(R.layout.registros_ventas_compras, p0, false)
            return ViewHolder(v)
        }

        override fun getItemCount(): Int {
            return listaVenta.size
        }

        override fun onBindViewHolder(p0: ViewHolder, pos: Int) {
            p0.mostrarDatosRegistro(listaVenta[pos])

            p0.itemView.setOnLongClickListener{
                titulo?.text = "Editar venta"
                    //// OBTENER EL TEXTO DEL REGISTRO ////
                val tvId = p0.itemView.findViewById<TextView>(R.id.tvId_ventasRecicladora)
                val cliente = p0.itemView.findViewById<TextView>(R.id.tvCliente_ventasRecicladora)
                val tvMaterial = p0.itemView.findViewById<TextView>(R.id.tvMaterial_ventasRecicladora)
                val tvCantidad = p0.itemView.findViewById<TextView>(R.id.tvCantidad_ventasRecicladora)
                val tvUnidad = p0.itemView.findViewById<TextView>(R.id.tvUnidad_ventasRecicladora)
                val tvGanancia = p0.itemView.findViewById<TextView>(R.id.tvGanancia_ventaRecicladora)
                val tvFecha = p0.itemView.findViewById<TextView>(R.id.tvFecha_ventasRecicladora)

                materialPosicion(p0, tvMaterial.text.toString())
                unidadPosicion(p0, tvUnidad.text.toString())

                id?.text = tvId.text.toString()
                campoCliente?.setText(cliente.text.toString())
                campoCantidad?.setText(tvCantidad.text.toString())
                campoGanancia?.setText(tvGanancia.text.toString())
                campoFecha?.setText(tvFecha.text.toString())
                btnModificar?.text = "Modificar"

                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                true
            }
        }

        private fun materialPosicion(p0 : ViewHolder, material : String){
            val listaMateriales : ArrayList<String>? = ArrayList()
            val bd =  BaseDeDatos(p0.itemView.context, "material", null , 1)
            val basededatos = bd.readableDatabase
            val materiales = basededatos.rawQuery("select material from ListaMateriales", null)
            if(materiales.moveToFirst()){
                do{
                    listaMateriales?.add(materiales.getString(0))
                }while (materiales.moveToNext())

                listaMateriales!!.sort()/// ORDENA ALFABETICAMENTE ///
                listaMateriales.add(0, "Seleccionar material")

                val materialPos = listaMateriales.indexOf(material)
                spMaterial?.setSelection(materialPos)
            }
            basededatos.close()
            materiales.close()
        }
        private fun unidadPosicion(p0 : ViewHolder, unidad : String){
            val listaUnidades : ArrayList<String>? = ArrayList()
            val bd = BaseDeDatos(p0.itemView.context, "material", null , 1)
            val basededatos = bd.readableDatabase
            val unidadesc = basededatos.rawQuery("select unidad from ListaUnidades", null)
            if(unidadesc.moveToFirst()){
                do{
                    listaUnidades?.add(unidadesc.getString(0))
                }while (unidadesc.moveToNext())

                listaUnidades!!.sort()/// ORDENA ALFABETICAMENTE ///
                listaUnidades.add(0, "Seleccionar unidad")

                val unidadPos = listaUnidades.indexOf(unidad)
                spUnidad?.setSelection(unidadPos)
            }
            basededatos.close()
            unidadesc.close()
        }

        fun cargarListaMateriales(spmaterial : Spinner, contexto : Context){
            var listaMateriales : ArrayList<String>? = ArrayList()

            val bd =  BaseDeDatos(contexto, "material", null , 1)
            val basededatos = bd.readableDatabase
            val materiales = basededatos.rawQuery("select material from ListaMateriales", null)
            if(materiales.moveToFirst()){
                do{
                    listaMateriales?.add(materiales.getString(0))
                }while (materiales.moveToNext())

                listaMateriales!!.sort()/// ORDENA ALFABETICAMENTE ///
                listaMateriales.add(0, "Seleccionar material")

                val a = ArrayAdapter<String>(contexto, android.R.layout.simple_spinner_item, listaMateriales)
                a.setDropDownViewResource(android.R.layout.simple_list_item_activated_1)
                spmaterial.adapter = a
            }
            basededatos.close()
            materiales.close()
        }

        fun cargarListaUnidad(unidad : Spinner, contexto : Context){
            var listaMateriales : ArrayList<String>? = ArrayList()

            val bd =  BaseDeDatos(contexto, "material", null , 1)
            val basededatos = bd.readableDatabase
            val unidades = basededatos.rawQuery("select unidad from ListaUnidades", null)
            if(unidades.moveToFirst()){
                do{
                    listaMateriales?.add(unidades.getString(0))
                }while (unidades.moveToNext())

                listaMateriales!!.sort()/// ORDENA ALFABETICAMENTE ///
                listaMateriales?.add(0, "Seleccionar unidad")

                val a = ArrayAdapter<String>(contexto, android.R.layout.simple_spinner_item, listaMateriales)
                a.setDropDownViewResource(android.R.layout.simple_list_item_activated_1)
                unidad.adapter = a
            }
            basededatos.close()
            unidades.close()
        }

        fun modificarRegistro(contexto : Context){
            val id = id?.text.toString()

            val nuevosDatos = ContentValues()
            nuevosDatos.put("nombreCliente", campoCliente?.text.toString())
            nuevosDatos.put("material", spMaterial?.selectedItem.toString())
            nuevosDatos.put("cantidad", campoCantidad?.text.toString())
            nuevosDatos.put("unidad", spUnidad?.selectedItem.toString())
            nuevosDatos.put("ganancia", campoGanancia?.text.toString())
            nuevosDatos.put("fecha", campoFecha?.text.toString())

            val bd =  BaseDeDatos(contexto, "Ventas", null , 1)
            val basededatos = bd.writableDatabase
            basededatos.update("Ventas", nuevosDatos, "id=?", arrayOf(id))
            basededatos.close()
        }

        class ViewHolder(view : View) : RecyclerView.ViewHolder(view){

            fun mostrarDatosRegistro(texto : String){
                val tvCliente : TextView = itemView.findViewById(R.id.tvCliente_ventasRecicladora)
                val tvMaterial : TextView = itemView.findViewById(R.id.tvMaterial_ventasRecicladora)
                val tvId : TextView = itemView.findViewById(R.id.tvId_ventasRecicladora)
                val tvCantidad : TextView = itemView.findViewById(R.id.tvCantidad_ventasRecicladora)
                val tvUnidad : TextView = itemView.findViewById(R.id.tvUnidad_ventasRecicladora)
                val tvGanancia : TextView = itemView.findViewById(R.id.tvGanancia_ventaRecicladora)
                val tvFecha : TextView = itemView.findViewById(R.id.tvFecha_ventasRecicladora)
                    //// MUESTRA LOS DATOS EN EL RV ///
                tvCliente.text = cliente(texto)
                tvMaterial.text = material(texto)
                tvId.text = id(texto)
                tvCantidad.text = cantidad(texto)
                tvUnidad.text = unidad(texto)
                tvGanancia.text = ganancia(texto)
                tvFecha.text = fecha(texto)
            }

            private fun cliente(cadena : String) : String {
                val nombreCliente = cadena.split(":")
                return nombreCliente[0].trim()
            }
            private fun material(cadena : String) : String {
                val material = cadena.split(":")
                return material[1].trim()
            }
            private fun id(cadena : String) : String {
                val material = cadena.split(":")
                return material[2].trim()
            }
            private fun cantidad(cadena : String) : String {
                val c = cadena.split(":")
                return c[3].trim()
            }
            private fun unidad(cadena : String) : String {
                val u = cadena.split(":")
                return u[4].trim()
            }
            private fun ganancia(cadena : String) : String {
                val g = cadena.split(":")
                return g[5].trim()
            }
            private fun fecha(cadena : String) : String {
                val f = cadena.split(":")
                return f[6].trim()
            }
        }
    }
}