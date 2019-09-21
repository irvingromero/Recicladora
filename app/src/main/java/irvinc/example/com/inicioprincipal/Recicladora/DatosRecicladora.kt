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
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import irvinc.example.com.inicioprincipal.BD.BaseDeDatos
import irvinc.example.com.inicioprincipal.R

class DatosRecicladora : AppCompatActivity() {

    private var usuarioLogeado: String? = null

    private var campoNombre: TextInputEditText? = null
    private var campoContra: TextInputEditText? = null
    private var campoCorreo: TextInputEditText? = null
    private var campoTel: TextInputEditText? = null
    private var campoCalle: TextInputEditText? = null
    private var campoColonia: TextInputEditText? = null
    private var campoNumeroInt: TextInputEditText? = null
    private var boton : MaterialButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datos_recicladora)

        supportActionBar?.hide()
        usuarioLogeado = intent.extras?.getString("usuario")

        campoNombre = findViewById(R.id.tietNombre_datosRecicladora)
        campoContra = findViewById(R.id.tietContra_datosRecicladora)
        campoCorreo = findViewById(R.id.tietCorreo_datosRecicadora)
        campoTel = findViewById(R.id.tietTelefono_datosRecicladora)
        campoCalle = findViewById(R.id.tietCalle_datosRecicladora)
        campoColonia = findViewById(R.id.tietColonia_datosRecicladora)
        campoNumeroInt = findViewById(R.id.tietNumeroint_datosRecicladora)
        boton = findViewById(R.id.mbModificardatos_datosRecicladora)
                // BANDERAS PARA SABER SI ESTA CORRECTO EL DATO ///
        var nombre = true
        var contra = true
        var correo = true
        var telefono = true
        var calle = true
        var colonia = true
        var numeroint = true

        cargarDatos()

        findViewById<ImageButton>(R.id.btnAtras_datosrecicladora).setOnClickListener {
            onBackPressed()
        }

        campoNombre?.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(campoNombre!!.length() > 2){
                    nombre = true

                    if(contra && correo && telefono && calle && colonia && numeroint){
                        boton?.isEnabled = true
                    }
                } else {
                    campoNombre!!.error = getString(R.string.mensajeUsuario_str)
                    requestFocus(campoNombre!!)
                    boton?.isEnabled = false
                    nombre = false
                }
            }
        })

        campoContra?.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(campoContra!!.length() > 2){
                    contra = true

                    if(nombre && correo && telefono && calle && colonia && numeroint){
                        boton?.isEnabled = true
                    }
                } else {
                    campoContra!!.error = getString(R.string.mensajeUsuario_str)
                    requestFocus(campoContra!!)
                    boton?.isEnabled = false
                    contra = false
                }
            }
        })

        campoCorreo?.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(campoCorreo!!.length() > 2){
                    if(campoCorreo?.text.toString().contains("@")){
                        correo = true

                        if(contra && nombre && telefono && calle && colonia && numeroint){
                            boton?.isEnabled = true
                        }
                    }else{
                        campoCorreo!!.error = getString(R.string.arroba_str)
                        requestFocus(campoCorreo!!)
                        correo = false
                        boton?.isEnabled = false
                    }
                } else {
                    campoCorreo!!.error = getString(R.string.mensajeUsuario_str)
                    requestFocus(campoCorreo!!)
                    correo = false
                    boton?.isEnabled = false
                }
            }
        })

        campoTel?.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(campoTel!!.length() > 2){
                    telefono = true

                    if(contra && correo && nombre && calle && colonia && numeroint){
                        boton?.isEnabled = true
                    }
                } else {
                    campoTel!!.error = getString(R.string.mensajeUsuario_str)
                    requestFocus(campoTel!!)
                    telefono = false
                    boton?.isEnabled = false
                }
            }
        })

        campoCalle?.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(campoCalle!!.length() > 2){
                    calle = true

                    if(contra && correo && telefono && nombre && colonia && numeroint){
                        boton?.isEnabled = true
                    }
                } else {
                    campoCalle!!.error = getString(R.string.mensajeUsuario_str)
                    requestFocus(campoCalle!!)
                    calle = false
                    boton?.isEnabled = false
                }
            }
        })

        campoColonia?.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(campoColonia!!.length() > 2){
                    colonia = true

                    if(contra && correo && telefono && calle && nombre && numeroint){
                        boton?.isEnabled = true
                    }
                } else{
                    campoColonia!!.error = getString(R.string.mensajeUsuario_str)
                    requestFocus(campoColonia!!)
                    colonia= false
                    boton?.isEnabled = false
                }
            }
        })

        campoNumeroInt?.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(campoNumeroInt!!.length() > 0){
                    numeroint = true

                    if(contra && correo && telefono && calle && colonia && nombre){
                        boton?.isEnabled = true
                    }
                } else {
                    campoNumeroInt!!.error = getString(R.string.mensajeUsuario_str)
                    requestFocus(campoNumeroInt!!)
                    numeroint = false
                    boton?.isEnabled = false
                }
            }
        })
    }

    private fun cargarDatos() {
        val objBD = BaseDeDatos(this, "Usuarios", null, 1)  // SI SE CAMBIA EL NAME NO CONECTA? //
        val basededatosq = objBD.readableDatabase
        val c = basededatosq.rawQuery("select nombre, contra, correo, telefono, calle, colonia, numeroInt from Recicladoras where usuario='$usuarioLogeado'", null)

        if(c.moveToFirst()) {
            campoNombre?.setText(c.getString(0))
            campoContra?.setText(c.getString(1))
            campoCorreo?.setText(c.getString(2))
            campoTel?.setText(c.getString(3))
            campoCalle?.setText(c.getString(4))
            campoColonia?.setText(c.getString(5))
            campoNumeroInt?.setText(c.getString(6))
        }
    }

    fun modificarDatos(v: View) {
        val datosNuevos = ContentValues()
        datosNuevos.put("correo", campoCorreo?.text.toString())
        datosNuevos.put("contra", campoContra?.text.toString())
        datosNuevos.put("nombre", campoNombre?.text.toString())
        datosNuevos.put("telefono", campoTel?.text.toString())
        datosNuevos.put("calle", campoCalle?.text.toString())
        datosNuevos.put("colonia", campoColonia?.text.toString())
        datosNuevos.put("numeroInt", campoNumeroInt?.text.toString())

        val bd =  BaseDeDatos(this, "Usuarios", null , 1)
        val basededatos = bd.writableDatabase
        basededatos.update("Recicladoras", datosNuevos, "usuario=?", arrayOf(usuarioLogeado))
        basededatos.close()

        val toast = Toast(applicationContext)
        //// CARGA EL LAYOUT A UNA VISTA ////
        val view = layoutInflater.inflate(R.layout.usuario_registrado, null)
        toast.view = view
        toast.duration = Toast.LENGTH_LONG
        toast.setGravity(Gravity.BOTTOM, 0, 30)
        view.findViewById<TextView>(R.id.tvToast_usuarioregistrado).text =
            getString(R.string.datosModificados_str)
        view.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))
        toast.show()

        onBackPressed()
    }

    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }
}