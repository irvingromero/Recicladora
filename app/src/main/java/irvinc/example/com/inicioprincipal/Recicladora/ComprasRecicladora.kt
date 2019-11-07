package irvinc.example.com.inicioprincipal.Recicladora

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.design.button.MaterialButton
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.TextInputEditText
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import irvinc.example.com.inicioprincipal.BD.BaseDeDatos
import irvinc.example.com.inicioprincipal.R
import kotlinx.android.synthetic.main.registro_compra_bs.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.collections.ArrayList

class ComprasRecicladora : AppCompatActivity() {

    private var usuarioLogeado : String?= null
    private var bottomSheetBehavior : BottomSheetBehavior<LinearLayout>? = null
    private var rv : RecyclerView? = null
    private var listaCompras : ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compras_recicladora)

        supportActionBar?.hide()
        usuarioLogeado = intent.extras?.getString("usuario")

        mostrarCompras()

        bottomSheetBehavior = BottomSheetBehavior.from<LinearLayout>(bsCompra)
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior?.skipCollapsed = true

        findViewById<MaterialButton>(R.id.btnAgregarCompra_ComprasRecicladora).setOnClickListener {
            val titulo = findViewById<TextView>(R.id.tvTitulo_registroCompra)
            titulo?.text = getString(R.string.agregarCompra_str)
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            validaCamposCompra()
        }

        findViewById<ImageButton>(R.id.btnAtras_ComprasRecicladora).setOnClickListener {
            onBackPressed()
        }

        setRecyclerViewItemTouchListener()
    }

    private fun mostrarCompras(){
        rv = findViewById(R.id.rvRegistroCompras_comprasRecicladora)
        rv?.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        listaCompras = ArrayList()

        val bdCompras =  BaseDeDatos(this, "Compras", null , 1)
        val con = bdCompras.readableDatabase
        val datosCompras = con.rawQuery("select nombreCliente, material, id, cantidad, unidad, gasto, fecha from Compras where usuarioRecicladora= '$usuarioLogeado'", null)

        if(datosCompras.moveToFirst())
        {
            do{
                val cliente = datosCompras.getString(0)
                val material = datosCompras.getString(1)
                val id = datosCompras.getInt(2)
                val cantidad = datosCompras.getDouble(3)
                val unidad = datosCompras.getString(4)
                val gasto = datosCompras.getDouble(5)
                val fecha = datosCompras.getString(6)

                listaCompras?.add("$cliente:$material:$id:$cantidad:$unidad:$gasto:$fecha")
            } while(datosCompras.moveToNext())
        }
        datosCompras.close()
        con.close()

        val contexto = this
        val adap = Adapter(listaCompras!!, usuarioLogeado!!, contexto)
        rv?.adapter = adap
    }

    private fun validaCamposCompra(){
        val campoNombre = findViewById<TextInputEditText>(R.id.tietNombre_registroCompra)
        val spMaterial = findViewById<Spinner>(R.id.spMaterial_registroCompra)
        val campoCantidad = findViewById<TextInputEditText>(R.id.tietCantidad_registroCompra)
        val spUnidad = findViewById<Spinner>(R.id.spUnidad_registroCompra)
        val campoGasto = findViewById<TextInputEditText>(R.id.tietGasto_registroCompra)
        val campoFecha = findViewById<TextInputEditText>(R.id.tietFecha_registroCompra)
        val btnRegistro = findViewById<MaterialButton>(R.id.mbRegistrar_registroCompra)
        btnRegistro.text = getString(R.string.registrar_str)
        btnRegistro.isEnabled = false

        //// CARGA LA FECHA DEL DIA ////
        val fecha = SimpleDateFormat("d/MM/yyyy").format(Date())
        campoFecha.setText(fecha.toString())
        campoNombre.setText("")
        campoCantidad.setText("")
        campoGasto.setText("")
        spMaterial.setSelection(0)
        spUnidad.setSelection(0)

        var bnombre = false
        var bmaterial = false
        var bcantidad = false
        var bunidad = false
        var bganancia = false

        findViewById<ImageButton>(R.id.ibCalendario_registroCompra).setOnClickListener {
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
                    btnRegistro.isEnabled = false
                }
            }
        })

        spMaterial.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if(!spMaterial.selectedItem.toString().contains("Seleccionar material")){
                    bmaterial = true

                    if(bnombre && bcantidad && bunidad && bganancia){
                        btnRegistro.isEnabled = true
                    }
                } else {
                    btnRegistro.isEnabled = false
                }
            }
        }

        campoCantidad.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if(!spMaterial.selectedItem.toString().contains("Seleccionar material") && !spUnidad.selectedItem.toString().contains("Seleccionar unidad")){
                    val mate = spMaterial.selectedItem.toString()
                    val uni = spUnidad.selectedItem.toString()

                    val bd =  BaseDeDatos(applicationContext, "Materiales", null , 1)
                    val basededatos = bd.readableDatabase
                    val datoPrecio = basededatos.rawQuery("select precio from Materiales where material='$mate' and unidad='$uni'", null)

                    if(datoPrecio.moveToFirst()){
                        if(campoCantidad.length() > 0) {
                            val cantidad = campoCantidad.text.toString().toDouble()
                            val total = datoPrecio.getDouble(0) * cantidad
                            campoGasto.setText(total.toString())
                        } else {
                            campoGasto.setText("")
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
                    btnRegistro.isEnabled = false
                }
            }
        })

        spUnidad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if(!spUnidad.selectedItem.toString().contains("Seleccionar unidad")){
                    bunidad = true

                    if(!spMaterial.selectedItem.toString().contains("Seleccionar material") && !spUnidad.selectedItem.toString().contains("Seleccionar unidad")){
                        val mate = spMaterial.selectedItem.toString()
                        val uni = spUnidad.selectedItem.toString()

                        val bd =  BaseDeDatos(applicationContext, "Materiales", null , 1)
                        val basededatos = bd.readableDatabase
                        val datoPrecio = basededatos.rawQuery("select precio from Materiales where material='$mate' and unidad='$uni'", null)

                        if(datoPrecio.moveToFirst()){
                            if(campoCantidad.length() > 0) {
                                val cantidad = campoCantidad.text.toString().toDouble()
                                val total = datoPrecio.getDouble(0) * cantidad
                                campoGasto.setText(total.toString())
                            } else {
                                campoGasto.setText("")
                            }
                        }
                        basededatos.close()
                        datoPrecio.close()
                    }

                    if(bnombre && bcantidad && bcantidad && bganancia){
                        btnRegistro.isEnabled = true
                    }
                } else {
                    btnRegistro.isEnabled = false
                }
            }
        }

        campoGasto.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(campoGasto.length() > 0){
                    bganancia = true

                    if(bnombre && bcantidad && bunidad && bunidad){
                        btnRegistro.isEnabled = true
                    }
                } else {
                    btnRegistro.isEnabled = false
                }
            }
        })

        btnRegistro.setOnClickListener {
            agregarCompra(campoNombre.text.toString(),
                spMaterial.selectedItem.toString(),
                campoCantidad.text.toString().toDouble(),
                spUnidad.selectedItem.toString(),
                campoGasto.text.toString().toDouble(),
                campoFecha.text.toString())

            //// VOLVER A LANZAR EL ACTIVITY PARA AHORRAR METODO PARA VOLVER A CARGAR EL RV
            /// CORRIGE BUG DEL BOTTOMSHEET CON EL TECLADO ABIERTO //////////////
            finish()
            val hilo = Handler(Looper.getMainLooper())
            hilo.post {
                val i = Intent(this, ComprasRecicladora::class.java)
                i.putExtra("usuario", usuarioLogeado)
                startActivity(i)
            }
        }
    }

    private fun agregarCompra(cliente : String, material : String, cantidad : Double, unidad : String, gasto: Double, fecha : String){
        val cv = ContentValues()
        cv.put("usuarioRecicladora", usuarioLogeado)
        cv.put("nombreCliente", cliente)
        cv.put("material", material)
        cv.put("cantidad", cantidad)
        cv.put("unidad", unidad)
        cv.put("gasto", gasto)
        cv.put("fecha", fecha)

        val bd =  BaseDeDatos(this, "Compras", null , 1)
        val basededatos = bd.writableDatabase
        basededatos.insert("Compras", null, cv)
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

    private fun eliminarCompra(){
        val tvId = findViewById<TextView>(R.id.tvId_ventasRecicladora)
        val id = tvId.text.toString()

        val bdM = BaseDeDatos(this, "Compras", null, 1)
        val basededatosMateriales = bdM.writableDatabase

        basededatosMateriales.delete("Compras", "id=?", arrayOf(id))
        basededatosMateriales.close()

        val toast = Toast(applicationContext)
        //// CARGA EL LAYOUT A UNA VISTA ////
        val view = layoutInflater.inflate(R.layout.usuario_registrado, null)
        toast.view = view
        toast.duration = Toast.LENGTH_LONG
        toast.setGravity(Gravity.BOTTOM,0, 30)
        view.findViewById<TextView>(R.id.tvToast_usuarioregistrado).text = getString(R.string.compraEliminada_str)
        toast.show()
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
                listaCompras?.removeAt(position)
                rv?.adapter!!.notifyItemRemoved(position)

                eliminarCompra()
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(rv)
    }

    override fun onBackPressed() {
        if(bottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED){
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        } else {
            super.onBackPressed()
        }
    }
        /////////////////////////////////////////////////////////////////////////////////////////////
    class Adapter(var listaComp: ArrayList<String>, val usuario : String, contextoActividad : Activity) : RecyclerView.Adapter<Adapter.ViewHolder>(){

        private var bottomSheetBehavior : BottomSheetBehavior<LinearLayout>? = null
        private var titulo : TextView? = null
        private var id : TextView? = null
        private var campoCliente : TextInputEditText? = null
        private var spMaterial : Spinner? = null
        private var campoCantidad : TextInputEditText? = null
        private var spUnidad : Spinner? = null
        private var campoGasto : TextInputEditText? = null
        private var campoFecha : TextInputEditText? = null
        private var btnCalendar : ImageButton? = null
        private var btnModificar : MaterialButton? = null

        init {
            bottomSheetBehavior = BottomSheetBehavior.from<LinearLayout>(contextoActividad.findViewById(R.id.bsCompra))
            bottomSheetBehavior?.skipCollapsed = true

            id = contextoActividad.findViewById(R.id.tvId_registroCompra)
            titulo = contextoActividad.findViewById(R.id.tvTitulo_registroCompra)
            campoCliente = contextoActividad.findViewById(R.id.tietNombre_registroCompra)
            spMaterial = contextoActividad.findViewById(R.id.spMaterial_registroCompra)
            campoCantidad = contextoActividad.findViewById(R.id.tietCantidad_registroCompra)
            spUnidad = contextoActividad.findViewById(R.id.spUnidad_registroCompra)
            campoGasto = contextoActividad.findViewById(R.id.tietGasto_registroCompra)
            campoFecha = contextoActividad.findViewById(R.id.tietFecha_registroCompra)
            btnCalendar = contextoActividad.findViewById(R.id.ibCalendario_registroCompra)
            btnModificar = contextoActividad.findViewById(R.id.mbRegistrar_registroCompra)

            cargarListaMateriales(spMaterial!!, contextoActividad.applicationContext)
            cargarListaUnidad(spUnidad!!, contextoActividad.applicationContext)

            btnCalendar?.setOnClickListener {
                val c = Calendar.getInstance()
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)

                val ventanaFecha = DatePickerDialog(
                    contextoActividad,
                    DatePickerDialog.OnDateSetListener { _, year, mes, dia ->
                        val mesReal = mes + 1
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
                    val i = Intent(contextoActividad.applicationContext, ComprasRecicladora::class.java)
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

            campoGasto?.addTextChangedListener(object : TextWatcher{
                override fun afterTextChanged(p0: Editable?) {}
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if(campoGasto?.length()!! > 0){
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
            return listaComp.size
        }

        override fun onBindViewHolder(p0: ViewHolder, pos: Int) {
            p0.mostrarDatosRegistro(listaComp[pos])

            p0.itemView.setOnLongClickListener{
                titulo?.text = "Editar compra"
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
                campoGasto?.setText(tvGanancia.text.toString())
                campoFecha?.setText(tvFecha.text.toString())
                btnModificar?.text = "Modificar"

                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                true
            }
        }

        fun cargarListaMateriales(spmaterial : Spinner, contexto : Context){
            val listaMateriales : ArrayList<String>? = ArrayList()

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
            val listaMateriales : ArrayList<String>? = ArrayList()

            val bd =  BaseDeDatos(contexto, "material", null , 1)
            val basededatos = bd.readableDatabase
            val unidades = basededatos.rawQuery("select unidad from ListaUnidades", null)
            if(unidades.moveToFirst()){
                do{
                    listaMateriales?.add(unidades.getString(0))
                }while (unidades.moveToNext())

                listaMateriales!!.sort()/// ORDENA ALFABETICAMENTE ///
                listaMateriales.add(0, "Seleccionar unidad")

                val a = ArrayAdapter<String>(contexto, android.R.layout.simple_spinner_item, listaMateriales)
                a.setDropDownViewResource(android.R.layout.simple_list_item_activated_1)
                unidad.adapter = a
            }
            basededatos.close()
            unidades.close()
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

        fun modificarRegistro(contexto : Context){
            val id = id?.text.toString()

            val nuevosDatos = ContentValues()
            nuevosDatos.put("nombreCliente", campoCliente?.text.toString())
            nuevosDatos.put("material", spMaterial?.selectedItem.toString())
            nuevosDatos.put("cantidad", campoCantidad?.text.toString())
            nuevosDatos.put("unidad", spUnidad?.selectedItem.toString())
            nuevosDatos.put("gasto", campoGasto?.text.toString())
            nuevosDatos.put("fecha", campoFecha?.text.toString())

            val bd =  BaseDeDatos(contexto, "Compras", null , 1)
            val basededatos = bd.writableDatabase
            basededatos.update("Compras", nuevosDatos, "id=?", arrayOf(id))
            basededatos.close()
        }

        class ViewHolder(view : View) : RecyclerView.ViewHolder(view){

            fun mostrarDatosRegistro(texto : String){
                val tvMostrarGasto : TextView = itemView.findViewById(R.id.tvGasto_ventasRecicladora)
                tvMostrarGasto.text = "Pago: "

                val tvCliente : TextView = itemView.findViewById(R.id.tvCliente_ventasRecicladora)
                val tvMaterial : TextView = itemView.findViewById(R.id.tvMaterial_ventasRecicladora)
                val tvId : TextView = itemView.findViewById(R.id.tvId_ventasRecicladora)
                val tvCantidad : TextView = itemView.findViewById(R.id.tvCantidad_ventasRecicladora)
                val tvUnidad : TextView = itemView.findViewById(R.id.tvUnidad_ventasRecicladora)
                val tvGasto : TextView = itemView.findViewById(R.id.tvGanancia_ventaRecicladora)
                val tvFecha : TextView = itemView.findViewById(R.id.tvFecha_ventasRecicladora)
                //// MUESTRA LOS DATOS EN EL RV ///
                tvCliente.text = cliente(texto)
                tvMaterial.text = material(texto)
                tvId.text = id(texto)
                tvCantidad.text = cantidad(texto)
                tvUnidad.text = unidad(texto)
                tvGasto.text = gasto(texto)
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
            private fun gasto(cadena : String) : String {
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