package irvinc.example.com.inicioprincipal.Recicladora

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
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
    private var listaGanancias : ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reporte_recicladora)

        supportActionBar?.hide()
        usuarioLogeado = intent.extras?.getString("usuario")

        rv = findViewById(R.id.rvClientesVentas_reporteRecicladora)
        val fechaInicio = findViewById<TextInputEditText>(R.id.tietFechaInicio_reporteRecicladora)
        val fechaCorte = findViewById<TextInputEditText>(R.id.tietFechaCorte_reporteRecicladora)

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

                datosVentas()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

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
                val datosCliente = conexion.rawQuery("select material from Ventas where nombreCliente = '$dato' and fecha='$fecha'", null)

                if(datosCliente.moveToFirst()){
                    do{
                        listaMateriales.add(datosCliente.getString(0)+"\n")
                        copiaMateriales.add(datosCliente.getString(0))
                    }while (datosCliente.moveToNext())
                }
            }

            while (copiaMateriales.size > 0) {  /// CALCULA LA CANTIDAD DE VECES QUE HAY DE UN MATERIAL ///
                val totalMaterial = Collections.frequency(copiaMateriales, copiaMateriales[0])
                contadorMaterial.add(totalMaterial.toString() + "\n")
                copiaMateriales.removeAll(Collections.singleton(copiaMateriales[0]))
            }

            datosCliente.add(listaClientes[index]+":"+listaMateriales+":"+contadorMaterial)
            listaMateriales.clear()
            contadorMaterial.clear()
        }
        conexion.close()

        rv?.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        val adap = Adapter(datosCliente)
        rv?.adapter = adap
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
        val fechas = flujodedatos.rawQuery("select fecha from Ventas", null)

        if(fechas.moveToFirst()){
            do{
                listaFechas.add(SimpleDateFormat("d/MM/yyyy").parse(fechas.getString(0)))
            }while (fechas.moveToNext())
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

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    class Adapter(var listaDatos: ArrayList<String>) : RecyclerView.Adapter<Adapter.ViewHolder>(){

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
                val g = cadena.split(":")
                return g[2].trim().substring(1, g[2].length -1)
            }
            private fun ganancia(cadena : String) : String {
                val g = cadena.split(":")
                return g[3].trim()
            }
        }
    }
}