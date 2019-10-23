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
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class ReporteRecicladora : AppCompatActivity() {

    private var usuarioLogeado : String? = null
    private var datosCliente : ArrayList<String> = ArrayList()
    private var rv : RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reporte_recicladora)

        supportActionBar?.hide()
        usuarioLogeado = intent.extras?.getString("usuario")

        rv = findViewById(R.id.rvClientesVentas_reporteRecicladora)
        val fechaInicio = findViewById<TextInputEditText>(R.id.tietFechaInicio_reporteRecicladora)
        val fechaCorte = findViewById<TextInputEditText>(R.id.tietFechaCorte_reporteRecicladora)

        fechaInicio.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val ventanaFecha = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, mes, dia ->
                val mesReal = mes+1
                val fechaCompleta = "$dia/$mesReal/$year"
                fechaInicio.setText(fechaCompleta)
                fechaCorte.isEnabled = true
            }, year, month, day)
            ventanaFecha.show()
        }

        fechaCorte.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val ventanaFecha = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, mes, dia ->
                val mesReal = mes+1
                val fechaCompleta = "$dia/$mesReal/$year"
                    //// VALIDA QUE LA FECHA DE CORTE SEA DESPUES DE LA DE INICIO /////
                val ok = validaFecha(fechaInicio.text.toString(), fechaCompleta)
                if(ok){
                    fechaCorte.setText(fechaCompleta)
                } else {
                    Toast.makeText(applicationContext, "Debe ser despues de Fecha de inicio", Toast.LENGTH_SHORT).show()
                }
            }, year, month, day)
            ventanaFecha.show()
        }

        fechaCorte.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
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

    private fun datosVentas(){
        rv?.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        val base = BaseDeDatos(this, "Ventas", null, 1)
        val conexion = base.readableDatabase

        val consulta = conexion.rawQuery("select nombreCliente from Ventas", null)
        if(consulta.moveToFirst()){
            var nombrec : String?
            var ganancia = ""
            val listaMateriales : ArrayList<String> = ArrayList()
            val contadorMaterial : ArrayList<String> = ArrayList()
            var copia : ArrayList<String> = ArrayList()

            do{
                nombrec = consulta.getString(0)

                val materialCliente = conexion.rawQuery("select material, ganancia from Ventas where nombreCliente='$nombrec'", null)
                if(materialCliente.moveToFirst()) {
                    do{
                        listaMateriales.add(materialCliente.getString(0)+"\n")
                        copia.add(materialCliente.getString(0)+"\n")
                    }while (materialCliente.moveToNext())
//                    ganancia = materialCliente.getString(1)
                }

                listaMateriales.sort()
                copia.sort()

                while (copia.size > 0) {  /// CALCULA LA CANTIDAD DE VECES QUE HAY DE UN MATERIAL ///
                    val totalMaterial = Collections.frequency(copia, copia[0])
                    contadorMaterial.add(totalMaterial.toString() + "\n")
                    copia.removeAll(Collections.singleton(copia[0]))
                }

                    /// ELIMINA MATERIALES REPETIDOS ///
                val hs = HashSet<String>()
                hs.addAll(listaMateriales)
                listaMateriales.clear()
                listaMateriales.addAll(hs)

                listaMateriales.sort()
                copia.sort()

                datosCliente.add(consulta.getString(0) +":"+listaMateriales+":"+contadorMaterial+":"+ ganancia)
                listaMateriales.clear()
                contadorMaterial.clear()
            }while (consulta.moveToNext())
        }
        consulta.close()
        conexion.close()
            /// ELIMINA CLIENTES REPETIDOS ///
        val hs = HashSet<String>()
        hs.addAll(datosCliente)
        datosCliente.clear()
        datosCliente.addAll(hs)

        val adap = Adapter(datosCliente)
        rv?.adapter = adap
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