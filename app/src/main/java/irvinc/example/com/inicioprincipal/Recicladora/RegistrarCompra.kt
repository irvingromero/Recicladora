package irvinc.example.com.inicioprincipal.Recicladora

import android.content.ContentValues
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.design.widget.TextInputEditText
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import irvinc.example.com.inicioprincipal.BD.BaseDeDatos
import irvinc.example.com.inicioprincipal.R

class RegistrarCompra : AppCompatActivity() {

    private var usuarioLogeado : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_compra)

        supportActionBar?.hide()
        usuarioLogeado = intent.extras?.getString("usuario")

        findViewById<ImageButton>(R.id.btnAtras_registroCompra).setOnClickListener {
            onBackPressed()
        }

        val campoNombre = findViewById<TextInputEditText>(R.id.tietNombreCompra_registroCompra)
        val material = findViewById<Spinner>(R.id.spMaterialCompra_registroCompra)
        val campoCantidad = findViewById<TextInputEditText>(R.id.tietCantidadCompra_registroCompra)
        val unidad = findViewById<Spinner>(R.id.spUnidadCompra_registroCompra)
        val campoGasto = findViewById<TextInputEditText>(R.id.tietGasto_registroCompra)
        val botonRegistrar = findViewById<MaterialButton>(R.id.mbRegistroCompra_registroCompra)
        val dp = findViewById<DatePicker>(R.id.dpFechaCompra_registroCompra)

        val rbEmp = findViewById<RadioButton>(R.id.radioButton)
        rbEmp.isChecked = true
        rbEmp.setOnClickListener {
            campoNombre.isEnabled = true
            campoNombre.setText("")
        }
        val rbPer = findViewById<RadioButton>(R.id.radioButton2)
        rbPer.setOnClickListener {
            campoNombre.isEnabled = false
            campoNombre.setText("Persona")
        }

        var bnombre = false
        var bmaterial = false
        var bcantidad = false
        var bunidad = false
        var bganancia = false

        campoNombre.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(campoNombre.length() > 2){
                    bnombre = true

                    if(bmaterial && bcantidad && bunidad && bganancia){
                        botonRegistrar.isEnabled = true
                    }

                } else {
                    bnombre = false

                    campoNombre.error = getString(R.string.mensajeUsuario_str)
                    requestFocus(campoNombre)
                    botonRegistrar.isEnabled = false
                }
            }
        })

        material.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if(!material.selectedItem.toString().contains("Seleccionar material")){
                    bmaterial = true

                    if(bnombre && bcantidad && bunidad && bganancia){
                        botonRegistrar.isEnabled = true
                    }

                } else {
                    botonRegistrar.isEnabled = false
                }
            }
        }

        campoCantidad.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(campoCantidad.length() > 0){
                    bcantidad = true

                    if(bnombre && bmaterial && bunidad && bganancia){
                        botonRegistrar.isEnabled = true
                    }
                } else {
//                    campoCantidad.error = getString(R.string.mensajeUsuario_str)
//                    requestFocus(campoCantidad)
                    botonRegistrar.isEnabled = false
                }
            }
        })

        unidad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if(!unidad.selectedItem.toString().contains("Seleccionar unidad")){
                    bunidad = true

                    if(bnombre && bcantidad && bcantidad && bganancia){
                        botonRegistrar.isEnabled = true
                    }
                } else {
                    botonRegistrar.isEnabled = false
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
                        botonRegistrar.isEnabled = true
                    }
                } else {
//                    campoGanancia.error = getString(R.string.mensajeUsuario_str)
//                    requestFocus(campoGanancia)
                    botonRegistrar.isEnabled = false
                }
            }
        })


        botonRegistrar.setOnClickListener {
            val mesReal = dp.month+1
            val fecha = "dia: ${dp.dayOfMonth} mes: $mesReal ano: ${dp.year}"

            registrarCompra(campoNombre.text.toString(),
                material.selectedItem.toString(),
                campoCantidad.text.toString().toDouble(),
                unidad.selectedItem.toString(),
                campoGasto.text.toString().toDouble(),
                fecha)

            onBackPressed()
        }
    }

    private fun registrarCompra(cliente : String, material : String, cantidad : Double, unidad : String, gasto: Double, fecha : String){
        val cv = ContentValues()
        cv.put("usuarioRecicladora", usuarioLogeado)
        cv.put("nombreRecicladora", cliente)
        cv.put("material", material)
        cv.put("cantidad", cantidad)
        cv.put("unidad", unidad)
        cv.put("ganancia", gasto)
        cv.put("fecha", fecha)

//        val cvClientesRecicladora = ContentValues()
//        cvClientesRecicladora.put("nombre", cliente)

        val bd =  BaseDeDatos(this, "Compra", null , 1)
        val basededatos = bd.writableDatabase
        basededatos.insert("Compras", null, cv)

//        basededatos.insert("ClientesRecicladora", null, cvClientesRecicladora)
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

    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }
}