package irvinc.example.com.inicioprincipal.Recicladora

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.textfield.TextInputEditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import android.widget.TextView
import irvinc.example.com.inicioprincipal.BD.BaseDeDatos
import irvinc.example.com.inicioprincipal.R

class ListaMateriales : AppCompatActivity() {

    private var rv : RecyclerView? = null
    private var listaMaterial : ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_materiales)
        supportActionBar?.hide()

        findViewById<ImageButton>(R.id.btnAtras_ListaMateriales).setOnClickListener {
            onBackPressed()
        }

        mostrarLista()
    }

    private fun mostrarLista(){
        rv = findViewById(R.id.rvListaMateriales_ListaMateriales)
        rv?.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        listaMaterial = ArrayList()

        val bd =  BaseDeDatos(this, "material", null , 1)
        val basededatos = bd.readableDatabase
        val datos = basededatos.rawQuery("select material from ListaMateriales", null)
        if(datos.moveToFirst()) {
            do{
                val material = datos.getString(0)
                listaMaterial?.add(material)
            } while(datos.moveToNext())
        }
        datos.close()
        basededatos.close()

        listaMaterial?.sort()

        val adap = Adapter(listaMaterial!!, this)
        rv?.adapter = adap
    }

    fun agregarMaterial(view : View) {
        val ventana = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        // CARGA EL LAYOUT PERSONALIZADO DEL DIALOG//
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.veagregar_material_unidad, null)
        ventana.setView(dialogView)

        ventana.setTitle("Agregar material a la lista")
        val nuevo = dialogView.findViewById<TextInputEditText>(R.id.tietNuevo_materialUnidad)
        nuevo.hint = getString(R.string.material_str)

        ventana.setPositiveButton(R.string.agregar_str){ _, _ ->
            val materialNuevo = nuevo.text.toString()

            val objetobasededatos = BaseDeDatos(this, "material", null, 1)
            val flujodedatos = objetobasededatos.writableDatabase
            val consulta = flujodedatos.rawQuery("select material from ListaMateriales where material = '$materialNuevo'", null)

            if(!consulta.moveToFirst()){
                val addMaterial = ContentValues()
                addMaterial.put("material", materialNuevo)
                flujodedatos.insert("ListaMateriales", null, addMaterial)

                val toast = Toast(applicationContext)
                //// CARGA EL LAYOUT A UNA VISTA ////
                val view = layoutInflater.inflate(R.layout.usuario_registrado, null)
                toast.view = view
                toast.duration = Toast.LENGTH_LONG
                toast.setGravity(Gravity.BOTTOM,0, 30)
                view.findViewById<TextView>(R.id.tvToast_usuarioregistrado).text = getString(R.string.materialAgregado_str)
                toast.show()

                mostrarLista()
            } else {
                Toast.makeText(this, "Material ya existente", Toast.LENGTH_LONG).show()
            }
            flujodedatos.close()
            consulta.close()
        }
        ventana.setNeutralButton(R.string.cancelar_str){ _, _ ->
        }

        val dialog: AlertDialog = ventana.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

        nuevo.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = nuevo.length() > 1
            }
        })
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class Adapter(var listaMateriales: ArrayList<String>,val contextoActividad : Activity) : RecyclerView.Adapter<Adapter.ViewHolder>(){

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            val v = LayoutInflater.from(p0.context).inflate(R.layout.materiales_unidades, p0, false)
            return ViewHolder(v)
        }

        override fun getItemCount(): Int {
            return listaMateriales.size
        }

        override fun onBindViewHolder(p0: ViewHolder, pos: Int) {
            p0.mostrarDatos(listaMateriales[pos])

            p0.itemView.setOnLongClickListener{
                val materialSeleccionado = listaMateriales[pos]

                val ventana = AlertDialog.Builder(p0.itemView.context, R.style.CustomDialogTheme)
                // CARGA EL LAYOUT PERSONALIZADO//
                val inflater = LayoutInflater.from(p0.itemView.context)
                val dialogView = inflater.inflate(R.layout.veagregar_material_unidad, null)
                ventana.setView(dialogView)

                ventana.setTitle("Editar $materialSeleccionado")
                val CampoNuevo = dialogView.findViewById<TextInputEditText>(R.id.tietNuevo_materialUnidad)
                CampoNuevo.hint = "Material"
                CampoNuevo.setText(materialSeleccionado)

                ventana.setPositiveButton(R.string.agregar_str){ _, _ ->
                    val materialNuevo = CampoNuevo.text.toString()

                    val datos = ContentValues()
                    datos.put("material", materialNuevo)

                    val bd =  BaseDeDatos(p0.itemView.context, "material", null , 1)
                    val basededatos = bd.writableDatabase
                    basededatos.update("ListaMateriales", datos, "material=?", arrayOf(materialSeleccionado))
                    basededatos.close()

                    Toast.makeText(p0.itemView.context, "Material modificado!", Toast.LENGTH_SHORT).show()

                    contextoActividad.finish()
                    val i = Intent(contextoActividad, ListaMateriales::class.java)
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
                val tvMaterial : TextView = itemView.findViewById(R.id.tvTexto)
                tvMaterial.text = texto
            }
        }
    }
}