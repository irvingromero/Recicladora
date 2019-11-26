package irvinc.example.com.inicioprincipal.Recicladora

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputEditText
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import irvinc.example.com.inicioprincipal.BD.BaseDeDatos
import irvinc.example.com.inicioprincipal.R

class ListaUnidades : AppCompatActivity() {

    private var rv : RecyclerView? = null
    private var listaUnidad : ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_unidades)
        supportActionBar?.hide()

        findViewById<ImageButton>(R.id.btnAtras_ListaUnidades).setOnClickListener {
            onBackPressed()
        }

        mostrarUnidades()
    }

    private fun mostrarUnidades() {
        rv = findViewById(R.id.rvListUnidades_ListaUnidades)
        rv?.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        listaUnidad = ArrayList()

        val bd =  BaseDeDatos(this, "material", null , 1)
        val basededatos = bd.readableDatabase
        val datos = basededatos.rawQuery("select unidad from ListaUnidades", null)

        if(datos.moveToFirst()) {
            do{
                val unidad = datos.getString(0)
                listaUnidad?.add(unidad)
            } while(datos.moveToNext())
        }
        datos.close()
        basededatos.close()

        listaUnidad?.sort()

        val adap = Adapter(listaUnidad!!, this)
        rv?.adapter = adap
    }

    fun agregarUnidadLista(view : View){
        val ventana = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        // CARGA EL LAYOUT PERSONALIZADO DEL DIALOG//
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.veagregar_material_unidad, null)
        ventana.setView(dialogView)

        ventana.setTitle("Unidad nueva")
        val nuevo = dialogView.findViewById<TextInputEditText>(R.id.tietNuevo_materialUnidad)
        nuevo.hint = getString(R.string.unidad_str)

        ventana.setPositiveButton(R.string.agregar_str){ _, _ ->
            val unidadNuevo = nuevo.text.toString()

            val objetobasededatos = BaseDeDatos(this, "material", null, 1)
            val flujodedatos = objetobasededatos.writableDatabase
            val consulta = flujodedatos.rawQuery("select unidad from ListaUnidades where unidad = '$unidadNuevo'", null)

            if(!consulta.moveToFirst()){
                val addUnidad = ContentValues()
                addUnidad.put("unidad", unidadNuevo)
                flujodedatos.insert("ListaUnidades", null, addUnidad)

                val toast = Toast(applicationContext)
                //// CARGA EL LAYOUT A UNA VISTA ////
                val view = layoutInflater.inflate(R.layout.usuario_registrado, null)
                toast.view = view
                toast.duration = Toast.LENGTH_LONG
                toast.setGravity(Gravity.BOTTOM,0, 30)
                view.findViewById<TextView>(R.id.tvToast_usuarioregistrado).text = getString(R.string.materialAgregado_str)
                toast.show()

                mostrarUnidades()
            } else {
                Snackbar.make(view, "Unidad ya existente", Snackbar.LENGTH_LONG).show()
            }
            flujodedatos.close()
            consulta.close()
        }
        ventana.setNeutralButton(R.string.cancelar_str){ _, _ ->
        }

        val dialog: AlertDialog = ventana.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

        nuevo.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = nuevo.length() > 1
            }
        })
    }

    ///////////////////////////////////////////////////
    class Adapter(var listaUnidades: ArrayList<String>, val contextoActividad : Activity) : RecyclerView.Adapter<Adapter.ViewHolder>(){

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            val v = LayoutInflater.from(p0.context).inflate(R.layout.materiales_unidades, p0, false)
            return ViewHolder(v)
        }

        override fun getItemCount(): Int {
            return listaUnidades.size
        }

        override fun onBindViewHolder(p0: ViewHolder, pos: Int) {
            p0.mostrarDatos(listaUnidades[pos])

            p0.itemView.setOnLongClickListener{
                val unidadSeleccionado = listaUnidades[pos]

                val ventana = AlertDialog.Builder(p0.itemView.context, R.style.CustomDialogTheme)
                // CARGA EL LAYOUT PERSONALIZADO//
                val inflater = LayoutInflater.from(p0.itemView.context)
                val dialogView = inflater.inflate(R.layout.veagregar_material_unidad, null)
                ventana.setView(dialogView)

                ventana.setTitle("Editar $unidadSeleccionado")
                val CampoNuevo = dialogView.findViewById<TextInputEditText>(R.id.tietNuevo_materialUnidad)
                CampoNuevo.hint = "Unidad"
                CampoNuevo.setText(unidadSeleccionado)

                ventana.setPositiveButton(R.string.agregar_str){ _, _ ->
                    val unidadNueva = CampoNuevo.text.toString()

                    val datos = ContentValues()
                    datos.put("unidad", unidadNueva)

                    val bd =  BaseDeDatos(p0.itemView.context, "material", null , 1)
                    val basededatos = bd.writableDatabase
                    basededatos.update("ListaUnidades", datos, "unidad=?", arrayOf(unidadSeleccionado))
                    basededatos.close()

                    Toast.makeText(p0.itemView.context, "Unidad modificada!", Toast.LENGTH_SHORT).show()

                    contextoActividad.finish()
                    val i = Intent(contextoActividad, ListaUnidades::class.java)
                    contextoActividad.startActivity(i)
                }

                ventana.setNeutralButton(R.string.cancelar_str){ _, _ ->  }

                val dialog: AlertDialog = ventana.create()
                dialog.show()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

                CampoNuevo.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(p0: Editable?) {}
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = CampoNuevo.length() > 1
                    }
                })
                true
            }
        }

        class ViewHolder(view : View) : RecyclerView.ViewHolder(view){

            fun mostrarDatos(texto : String){
                val tvUnidad : TextView = itemView.findViewById(R.id.tvTexto)
                tvUnidad.text = texto
            }
        }
    }
}