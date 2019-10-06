package irvinc.example.com.inicioprincipal.Recicladora

import android.app.DatePickerDialog
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class RegistroVenta : AppCompatActivity() {

    private var usuarioLogeado : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_venta)

        supportActionBar?.hide()
        usuarioLogeado = intent.extras?.getString("usuario")

        val campoNombre = findViewById<TextInputEditText>(R.id.tietNombreVenta_registroVenta)
        val material = findViewById<Spinner>(R.id.spMaterialVendido_registroVenta)
        val campoCantidad = findViewById<TextInputEditText>(R.id.tietCantidad_registroVenta)
        val unidad = findViewById<Spinner>(R.id.spUnidad_registroVenta)
        val campoGanancia = findViewById<TextInputEditText>(R.id.tietGanacia_registroVenta)
        val btnRegistro = findViewById<MaterialButton>(R.id.mbRegistroVenta_registroVenta)
        btnRegistro.isEnabled = false
            //// CARGA LA FECHA DEL DIA ////
        val fecha = SimpleDateFormat("d/MM/yyyy").format(Date())
        val campoFecha = findViewById<TextInputEditText>(R.id.tietFecha_registroVenta)
        campoFecha.setText(fecha.toString())

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

            val ventanaFecha = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, mes, dia ->
                val mesReal = mes+1
                val fechaCompleta = "$dia/$mesReal/$year"
                campoFecha.setText(fechaCompleta)
            }, year, month, day)
            ventanaFecha.show()
        }

        findViewById<ImageButton>(R.id.btnAtras_registroVenta).setOnClickListener {
            onBackPressed()
        }
        ///////////
        campoNombre.addTextChangedListener(object : TextWatcher{
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
                    btnRegistro.isEnabled = false
                }
            }
        }

        campoCantidad.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(campoCantidad.length() > 0){
                    bcantidad = true

                    if(bnombre && bmaterial && bunidad && bganancia){
                        btnRegistro.isEnabled = true
                    }
                } else {
//                    campoCantidad.error = getString(R.string.mensajeUsuario_str)
//                    requestFocus(campoCantidad)
                    btnRegistro.isEnabled = false
                }
            }
        })

        unidad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if(!unidad.selectedItem.toString().contains("Seleccionar unidad")){
                    bunidad = true

                    if(bnombre && bcantidad && bcantidad && bganancia){
                        btnRegistro.isEnabled = true
                    }
                } else {
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
//                    campoGanancia.error = getString(R.string.mensajeUsuario_str)
//                    requestFocus(campoGanancia)
                    btnRegistro.isEnabled = false
                }
            }
        })

        btnRegistro.setOnClickListener {
            registrarVenta(campoNombre.text.toString(),
                material.selectedItem.toString(),
                campoCantidad.text.toString().toDouble(),
                unidad.selectedItem.toString(),
                campoGanancia.text.toString().toDouble(),campoFecha.text.toString())
        }
    }

    private fun registrarVenta(cliente : String, material : String, cantidad : Double, unidad : String, ganancia: Double, fecha : String){
        val cv = ContentValues()
        cv.put("usuarioRecicladora", usuarioLogeado)
        cv.put("nombreRecicladora", cliente)
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

        onBackPressed()
    }

    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }
}