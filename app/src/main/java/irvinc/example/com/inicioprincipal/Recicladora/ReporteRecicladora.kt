package irvinc.example.com.inicioprincipal.Recicladora

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.design.widget.TextInputEditText
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import irvinc.example.com.inicioprincipal.BD.BaseDeDatos
import irvinc.example.com.inicioprincipal.Mail.EnviarCorreo
import irvinc.example.com.inicioprincipal.Pdf.Pdf
import irvinc.example.com.inicioprincipal.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Collections
import java.util.Date
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class ReporteRecicladora : AppCompatActivity() {

    private var usuarioLogeado : String? = null
    private var rv : RecyclerView? = null

    private var datosCliente : ArrayList<String> = ArrayList()
    private val listaFechas : ArrayList<Date> = ArrayList()
    private var listaClientes : ArrayList<String> = ArrayList()
    private var listaMateriales : ArrayList<String> = ArrayList()
    private var listaGanancias : ArrayList<Double> = ArrayList()

    private var btnCorreo : MaterialButton? = null

    private var listaFechasCompra : ArrayList<Date> = ArrayList()
    private var listaClientesCompra : ArrayList<String> = ArrayList()
    private var listaMaterialCompra : ArrayList<String> = ArrayList()
    private var listaGasto : ArrayList<Double> = ArrayList()
    private var datosCompra : ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reporte_recicladora)

        supportActionBar?.hide()
        usuarioLogeado = intent.extras?.getString("usuario")

        rv = findViewById(R.id.rvClientesVentas_reporteRecicladora)
        val fechaInicio = findViewById<TextInputEditText>(R.id.tietFechaInicio_reporteRecicladora)
        val fechaCorte = findViewById<TextInputEditText>(R.id.tietFechaCorte_reporteRecicladora)
        btnCorreo = findViewById(R.id.mbCorreo_ReporteRecicladora)

        fechaInicio?.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val ventanaFecha = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, mes, dia ->
                val mesReal = mes+1
                val fechaCompleta = "$dia/$mesReal/$year"
                fechaInicio?.setText(fechaCompleta)
                fechaCorte?.isEnabled = true
            }, year, month, day)
            ventanaFecha.show()
        }

        fechaCorte?.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val ventanaFecha = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, mes, dia ->
                val mesReal = mes+1
                val fechaCompleta = "$dia/$mesReal/$year"
                    //// VALIDA QUE LA FECHA DE CORTE SEA DESPUES DE LA DE INICIO /////
                val ok = validaFecha(fechaInicio?.text.toString(), fechaCompleta)
                if(ok){
                    fechaCorte?.setText(fechaCompleta)
                } else {
                    Toast.makeText(applicationContext, "Debe ser despues de Fecha de inicio", Toast.LENGTH_SHORT).show()
                }
            }, year, month, day)
            ventanaFecha.show()
        }

        fechaCorte?.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                // SE LIMPIAN LAS LISTAS POR SI SE CAMBIA FECHA DE CORTE VARIAS VECES NO REPITA DATOS ////
                listaClientes.clear()
                datosCliente.clear()
                listaMateriales.clear()
                listaFechas.clear()

                datosCompra.clear()
                listaFechasCompra.clear()
                listaClientesCompra.clear()
                listaMaterialCompra.clear()
                listaGasto.clear()

                datosVentas()
                datosCompras()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        btnCorreo?.setOnClickListener {
            correo()
        }

        findViewById<ImageButton>(R.id.btnAtras_ReporteRecicladora).setOnClickListener {
            onBackPressed()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun validaFecha(fechaInicio : String, fechaCorte : String) : Boolean{
        val fi = SimpleDateFormat("d/MM/yyyy").parse(fechaInicio)
        val fc = SimpleDateFormat("d/MM/yyyy").parse(fechaCorte)

        if(fi.before(fc)){
            return true
        }
        return false
    }

    @SuppressLint("SimpleDateFormat")
    private fun datosVentas() {
        fecha()
        listaClientes = clientes(listaFechas)

        val contadorMaterial : ArrayList<String> = ArrayList()
        val copiaMateriales : ArrayList<String> = ArrayList()
        val base = BaseDeDatos(this, "Ventas", null, 1)
        val conexion = base.readableDatabase

        for((index, dato) in listaClientes.withIndex()){
            for((index, datoFe) in listaFechas.withIndex()) {

                val fecha = SimpleDateFormat("d/MM/yyyy").format(datoFe)
                val datosCliente = conexion.rawQuery("select material, ganancia from Ventas where nombreCliente = '$dato' and fecha='$fecha'", null)

                if(datosCliente.moveToFirst()){
                    do{
                        listaMateriales.add(datosCliente.getString(0)+"\n")
                        copiaMateriales.add(datosCliente.getString(0))
                        listaGanancias.add(datosCliente.getDouble(1))
                    }while (datosCliente.moveToNext())
                }
            }

            while (copiaMateriales.size > 0) {  /// CALCULA LA CANTIDAD DE VECES QUE HAY DE UN MATERIAL ///
                val totalMaterial = Collections.frequency(copiaMateriales, copiaMateriales[0])
                contadorMaterial.add(totalMaterial.toString() + "\n")
                copiaMateriales.removeAll(Collections.singleton(copiaMateriales[0]))
            }

            /// ELIMINA MATERIALES REPETIDOS ///
            val hs = HashSet<String>()
            hs.addAll(listaMateriales)
            listaMateriales.clear()
            listaMateriales.addAll(hs)
            listaMateriales.sort()//ORDENA ALFABETIC

            datosCliente.add(listaClientes[index]+":"+listaMateriales+":"+contadorMaterial+":"+listaGanancias.sum())
            listaMateriales.clear()
            contadorMaterial.clear()
            listaGanancias.clear()
        }
        conexion.close()

        rv?.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        val adap = Adapter(datosCliente)
        rv?.adapter = adap

        if(adap.itemCount > 0){
            btnCorreo?.isEnabled = true
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun datosCompras(){
        val rvCompras = findViewById<RecyclerView>(R.id.rvCompras_reporteRecicladora)

        fechaCompras()
        listaClientesCompra = clientesCompras(listaFechasCompra)

        val contadorMaterial : ArrayList<String> = ArrayList()
        val copiaMateriales : ArrayList<String> = ArrayList()
        val base = BaseDeDatos(this, "Compras", null, 1)
        val conexion = base.readableDatabase

        for((index, dato) in listaClientesCompra.withIndex()){
            for((index, datoFe) in listaFechasCompra.withIndex()) {

                val fecha = SimpleDateFormat("d/MM/yyyy").format(datoFe)
                val datosCliente = conexion.rawQuery("select material, gasto from Compras where nombreCliente = '$dato' and fecha='$fecha'", null)

                if(datosCliente.moveToFirst()){
                    do{
                        listaMaterialCompra.add(datosCliente.getString(0)+"\n")
                        copiaMateriales.add(datosCliente.getString(0))
                        listaGasto.add(datosCliente.getDouble(1))
                    }while (datosCliente.moveToNext())
                }
            }

            while (copiaMateriales.size > 0) {  /// CALCULA LA CANTIDAD DE VECES QUE HAY DE UN MATERIAL ///
                val totalMaterial = Collections.frequency(copiaMateriales, copiaMateriales[0])
                contadorMaterial.add(totalMaterial.toString() + "\n")
                copiaMateriales.removeAll(Collections.singleton(copiaMateriales[0]))
            }

            /// ELIMINA MATERIALES REPETIDOS ///
            val hs = HashSet<String>()
            hs.addAll(listaMaterialCompra)
            listaMaterialCompra.clear()
            listaMaterialCompra.addAll(hs)
            listaMaterialCompra.sort()//ORDENA ALFABETIC

            datosCompra.add(listaClientesCompra[index]+":"+listaMaterialCompra+":"+contadorMaterial+":"+listaGasto.sum())
            listaMaterialCompra.clear()
            contadorMaterial.clear()
            listaGasto.clear()
        }
        conexion.close()

        rvCompras?.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        val adapCompra = Adapter(datosCompra)
        rvCompras?.adapter = adapCompra

        if(adapCompra.itemCount > 0){
            btnCorreo?.isEnabled = true
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun fecha(){
        val campoFechaInicio = findViewById<TextInputEditText>(R.id.tietFechaInicio_reporteRecicladora)
        val campoFechaCorte = findViewById<TextInputEditText>(R.id.tietFechaCorte_reporteRecicladora)

        val fechainicio = SimpleDateFormat("d/MM/yyyy").parse(campoFechaInicio?.text.toString())
        val fechaCorte = SimpleDateFormat("d/MM/yyyy").parse(campoFechaCorte?.text.toString())
        val indices : ArrayList<Int> = ArrayList()

        val db = BaseDeDatos(this, "Ventas", null, 1)
        val flujodedatos = db.readableDatabase
        val fechas = flujodedatos.rawQuery("select fecha from Ventas where usuarioRecicladora = '$usuarioLogeado'", null)

        if(fechas.moveToFirst()){
            val tvTitulo = findViewById<TextView>(R.id.tvTituloVentas_ReporteRcicladora)
            val tvTituloCompra = findViewById<TextView>(R.id.tvTituloCompras_ReporteRcicladora)

            do{
                listaFechas.add(SimpleDateFormat("d/MM/yyyy").parse(fechas.getString(0)))
            }while (fechas.moveToNext())

            tvTitulo.text = getString(R.string.registrarVenta_str).toString()
            tvTituloCompra.text = getString(R.string.registrarCompra_str).toString()
        }
        fechas.close()
        flujodedatos.close()

        for ((indice, item) in listaFechas.withIndex()) {
            if(item.before(fechainicio) || item.after(fechaCorte)){
                // SACA LAS POCIONES DE LAS FECHAS QUE NO SE OCUPAN //
                indices.add(indice)
            }
        }
        val comparador = Collections.reverseOrder<Int>()
        Collections.sort(indices, comparador)
        /// ELIMINA LA POSICION CON EL NUMERO MAYOR HACIA 0////
        indices.forEach {
            listaFechas.removeAt(it)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun clientes(listaFechas: ArrayList<Date>) : ArrayList<String> {
        val base = BaseDeDatos(this, "Ventas", null, 1)
        val conexion = base.readableDatabase

        for((index, dato) in listaFechas.withIndex()){
            val fecha = SimpleDateFormat("d/MM/yyyy").format(dato)

            val datosCliente = conexion.rawQuery("select nombreCliente from Ventas where fecha = '$fecha'", null)
            if(datosCliente.moveToFirst()){
                listaClientes.add(datosCliente.getString(0))
            }
        }
        conexion.close()
        /// ELIMINA CLIENTES REPETIDOS ///
        val hs = HashSet<String>()
        hs.addAll(listaClientes)
        listaClientes.clear()
        listaClientes.addAll(hs)

        listaClientes.sort()//ORDENA ALFABE
        return listaClientes
    }

    @SuppressLint("SimpleDateFormat")
    private fun fechaCompras(){
        val campoFechaInicio = findViewById<TextInputEditText>(R.id.tietFechaInicio_reporteRecicladora)
        val campoFechaCorte = findViewById<TextInputEditText>(R.id.tietFechaCorte_reporteRecicladora)

        val fechainicio = SimpleDateFormat("d/MM/yyyy").parse(campoFechaInicio?.text.toString())
        val fechaCorte = SimpleDateFormat("d/MM/yyyy").parse(campoFechaCorte?.text.toString())
        val indices : ArrayList<Int> = ArrayList()

        val db = BaseDeDatos(this, "Compras", null, 1)
        val flujodedatos = db.readableDatabase
        val fechasCompra = flujodedatos.rawQuery("select fecha from Compras where usuarioRecicladora = '$usuarioLogeado'", null)

        if(fechasCompra.moveToFirst()){
            val tvTitulo = findViewById<TextView>(R.id.tvTituloVentas_ReporteRcicladora)
            val tvTituloCompra = findViewById<TextView>(R.id.tvTituloCompras_ReporteRcicladora)

            do{
                listaFechasCompra.add(SimpleDateFormat("d/MM/yyyy").parse(fechasCompra.getString(0)))
            }while (fechasCompra.moveToNext())

            tvTitulo.text = getString(R.string.registrarVenta_str).toString()
            tvTituloCompra.text = getString(R.string.registrarCompra_str).toString()
        }
        fechasCompra.close()
        flujodedatos.close()

        for ((indice, item) in listaFechasCompra.withIndex()) {
            if(item.before(fechainicio) || item.after(fechaCorte)){
                // SACA LAS POCIONES DE LAS FECHAS QUE NO SE OCUPAN //
                indices.add(indice)
            }
        }
        val comparador = Collections.reverseOrder<Int>()
        Collections.sort(indices, comparador)
        /// ELIMINA LA POSICION CON EL NUMERO MAYOR HACIA 0////
        indices.forEach {
            listaFechasCompra.removeAt(it)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun clientesCompras(listaFechas: ArrayList<Date>) : ArrayList<String> {
        val base = BaseDeDatos(this, "Compras", null, 1)
        val conexion = base.readableDatabase

        for((index, dato) in listaFechasCompra.withIndex()){
            val fecha = SimpleDateFormat("d/MM/yyyy").format(dato)

            val datosCliente = conexion.rawQuery("select nombreCliente from Compras where fecha = '$fecha'", null)
            if(datosCliente.moveToFirst()){
                listaClientesCompra.add(datosCliente.getString(0))
            }
        }
        conexion.close()
        /// ELIMINA CLIENTES REPETIDOS ///
        val hs = HashSet<String>()
        hs.addAll(listaClientesCompra)
        listaClientesCompra.clear()
        listaClientesCompra.addAll(hs)

        listaClientesCompra.sort()//ORDENA ALFABE
        return listaClientesCompra
    }

    private fun correo(){
        val ventana = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        // CARGA EL LAYOUT PERSONALIZADO//
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.ventcorreo_reportes, null)
        ventana.setView(dialogView)

        val tvCorreo = dialogView.findViewById<TextView>(R.id.tvCorreoRegistrado_ReporteRecicladora)
        val tvOtroCorreo = dialogView.findViewById<TextView>(R.id.tvOtrocorreo_ReporteRecicladora)

        val bd = BaseDeDatos(this, "Usuarios", null, 1)
        val conexion = bd.readableDatabase
        val consultaCorreo = conexion.rawQuery("select correo from Recicladoras where usuario = '$usuarioLogeado'", null)

        if(consultaCorreo.moveToFirst()) {
            val correo = consultaCorreo.getString(0).contains("@")
            if (correo) {
                tvCorreo.text = consultaCorreo.getString(0)
            } else {
                tvCorreo.text = getString(R.string.sinCorreo_str)
                tvCorreo.isEnabled = false
            }
        }
        conexion.close()
        consultaCorreo.close()

        val dialog: AlertDialog = ventana.create()
        dialog.show()

        tvCorreo.setOnClickListener {
            dialog.dismiss()

            cargarDatosReporte()

            val sender = EnviarCorreo()
            sender.enviar(tvCorreo.text.toString())
        }

        tvOtroCorreo.setOnClickListener {
            dialog.dismiss()
            otroCorreo()
        }
    }

    private fun otroCorreo(){
        val ventana = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        // CARGA EL LAYOUT PERSONALIZADO//
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.ventana_recuperar_contra, null)
        ventana.setView(dialogView)

        val etCorreo = dialogView.findViewById<TextInputEditText>(R.id.etCorreo_IniciarSesion)

        ventana.setPositiveButton(R.string.enviar_str) { _,_ ->
            cargarDatosReporte()

            val sender = EnviarCorreo()
            sender.enviar(etCorreo.text.toString())
        }
        ventana.setNegativeButton(R.string.cancelar_str){_,_ -> }

        val dialog: AlertDialog = ventana.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

        etCorreo.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(etCorreo!!.length() > 2){
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = etCorreo?.text.toString().contains("@")
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                }
            }
        })
    }

    private fun cargarDatosReporte(){
        val campoFechaInicio = findViewById<TextInputEditText>(R.id.tietFechaInicio_reporteRecicladora)
        val campoFechaCorte = findViewById<TextInputEditText>(R.id.tietFechaCorte_reporteRecicladora)
        val fechainicio = campoFechaInicio.text.toString()
        val fechaCorte = campoFechaCorte.text.toString()

        val tituloTabla = arrayOf("Cliente", "Material", "Cantidad material", "Total")
        val pdf = Pdf(applicationContext)
        pdf.crearArchivo()
        pdf.abrirDocumento()
        pdf.agregarParrafo("Reporte generado desde $fechainicio a $fechaCorte")
        pdf.agregarParrafo("Datos de ventas:")
        pdf.crearTabla(tituloTabla, getClientsVentas())
        pdf.agregarParrafo("Datos de compras:")
        pdf.crearTabla(tituloTabla, getClientsCompras())
        pdf.cerrarDocumento()
    }

    private fun getClientsVentas() : ArrayList<Array<String>> {
        val row : ArrayList<Array<String>> = ArrayList()

        for((index, dato) in datosCliente.withIndex()){
            row.add(arrayOf(cliente(datosCliente[index]), material(datosCliente[index]), cantidadMaterial(datosCliente[index]), ganancia(datosCliente[index])))
        }
        return row
    }

    private fun getClientsCompras() : ArrayList<Array<String>> {
        val row : ArrayList<Array<String>> = ArrayList()

        for((index, dato) in datosCompra.withIndex()){
            row.add(arrayOf(cliente(datosCompra[index]), material(datosCompra[index]), cantidadMaterial(datosCompra[index]), ganancia(datosCompra[index])))
        }
        return row
    }

    private fun cliente(cadena : String) : String {
        val nombreCliente = cadena.split(":")
        return nombreCliente[0].trim()
    }
    private fun material(cadena : String) : String {
        val material = cadena.split(":")
        return material[1].trim().substring(1, material[1].length -1) // ELIMINA EL "[" "]" DEL MATERIAL //
    }
    private fun cantidadMaterial(cadena : String) : String {
        val can = cadena.split(":")
        return can[2].trim().substring(1, can[2].length -1)
    }
    private fun ganancia(cadena : String) : String {
        val g = cadena.split(":")
        return g[3].trim()
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    class Adapter(private var listaDatos: ArrayList<String>) : RecyclerView.Adapter<Adapter.ViewHolder>(){

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            val v = LayoutInflater.from(p0.context).inflate(R.layout.datos_reporte, p0, false)
            return ViewHolder(v)
        }

        override fun getItemCount(): Int {
            return listaDatos.size
        }

        override fun onBindViewHolder(p0: ViewHolder, pos: Int) {
            p0.mostrarDatosRegistro(listaDatos[pos])
        }

        class ViewHolder(view : View) : RecyclerView.ViewHolder(view){

            fun mostrarDatosRegistro(texto : String){
                val tvCliente : TextView = itemView.findViewById(R.id.tvCliente_ReportesRecicladora)
                val tvMaterial : TextView = itemView.findViewById(R.id.tvMaterial_ReportesRecicladora)
                val tvCantidad : TextView = itemView.findViewById(R.id.tvCantidad_ReporteRecicladora)
                val tvGanancia : TextView = itemView.findViewById(R.id.tvGanancia_ReporteRecicladora)
                //// MUESTRA LOS DATOS EN EL RV ///
                tvCliente.text = cliente(texto)
                tvMaterial.text = material(texto)
                tvCantidad.text = cantidadMaterial(texto)
                tvGanancia.text = ganancia(texto)
            }

            private fun cliente(cadena : String) : String {
                val nombreCliente = cadena.split(":")
                return nombreCliente[0].trim()
            }
            private fun material(cadena : String) : String {
                val material = cadena.split(":")
                return material[1].trim().substring(1, material[1].length -1) // ELIMINA EL "[" "]" DEL MATERIAL //
            }
            private fun cantidadMaterial(cadena : String) : String {
                val can = cadena.split(":")
                return can[2].trim().substring(1, can[2].length -1)
            }
            private fun ganancia(cadena : String) : String {
                val g = cadena.split(":")
                return g[3].trim()
            }
        }
    }
}