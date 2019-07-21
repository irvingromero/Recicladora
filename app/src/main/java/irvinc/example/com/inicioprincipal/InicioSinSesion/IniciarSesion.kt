package irvinc.example.com.inicioprincipal.InicioSinSesion

import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.design.widget.TextInputEditText
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import irvinc.example.com.inicioprincipal.BD.BaseDeDatos
import irvinc.example.com.inicioprincipal.R
import irvinc.example.com.inicioprincipal.Recicladora.SesionRecicladora
import irvinc.example.com.inicioprincipal.UsuarioLogeado.SesionUsuario

class IniciarSesion : AppCompatActivity() {

    private val bd =  BaseDeDatos(this, "Usuarios", null , 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iniciar_sesion)
        supportActionBar?.hide()

        val etUsuario_iniciarSesion = findViewById<TextInputEditText>(R.id.etUsuario_inicioSesion)
        val etContra_inicioSesion = findViewById<TextInputEditText>(R.id.etContra_inicioSesion)
        val btnIniciarSesion = findViewById<MaterialButton>(R.id.btnLogin_inicio)

        var usuarioOk_inicio = false
        var contraOk_inicio = false
        etUsuario_iniciarSesion.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(etUsuario_iniciarSesion.text!!.isNotEmpty())
                {
                    usuarioOk_inicio = true
                    if(usuarioOk_inicio && contraOk_inicio){
                        btnIniciarSesion.isEnabled = true
                    }
                } else {
                    usuarioOk_inicio = false
                    btnIniciarSesion.isEnabled = false
                }
            }
        })

        etContra_inicioSesion.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(etContra_inicioSesion.text!!.isNotEmpty())
                {
                    contraOk_inicio = true
                    if(usuarioOk_inicio && contraOk_inicio){
                        btnIniciarSesion.isEnabled = true
                    }
                } else {
                    contraOk_inicio = false
                    btnIniciarSesion.isEnabled = false
                }
            }
        })

        btnIniciarSesion.isEnabled = false
        btnIniciarSesion.setOnClickListener {
            iniciarSesion(etUsuario_iniciarSesion.text.toString(), etContra_inicioSesion.text.toString())
        }
        val botonBack = findViewById<ImageButton>(R.id.btnBack_inicioSesion)
        botonBack.setOnClickListener {
            onBackPressed()
                    //// ESCONDE EL TECLADO /////
            cerrarTeclado()
        }
    }

    private fun iniciarSesion(usuario : String, contra : String){
        val mantenerSesion = findViewById<CheckBox>(R.id.cbMantenerSesion_InicioSesion)

        val basededatos = bd.readableDatabase
        var usuarioExiste = false
        var recicladora = false
        var incorrecto = true

        val consultaRecicladora = basededatos.rawQuery("select usuario from Recicladoras where usuario = '$usuario' and contra = '$contra'",null)
        recicladora = consultaRecicladora.moveToFirst()

        val consultaUsuario = basededatos.rawQuery("select usuario from Usuarios where usuario ='$usuario' and contra = '$contra'",null)
         usuarioExiste = consultaUsuario.moveToFirst()
        bd.close()
        basededatos.close()

        if(usuarioExiste){
            cerrarTeclado() //// ESCONDE EL TECLADO /////

            val intent = Intent(this, SesionUsuario::class.java)
            intent.putExtra("usuario", usuario)
            finishAffinity()    //// CIERRA LAS DEMAS ACTIVITYS EN SEGUNDO PLANO////
            startActivity(intent)

            incorrecto = false
        }

        if(recicladora){
            cerrarTeclado()

            val intent = Intent(this, SesionRecicladora::class.java)
            intent.putExtra("usuario", usuario)
            finishAffinity()    //// CIERRA LAS DEMAS ACTIVITYS EN SEGUNDO PLANO////
            startActivity(intent)

            incorrecto = false
        }

        if(usuarioExiste || recicladora){
            if (mantenerSesion.isChecked) {
                val preferences = this.getSharedPreferences("user", Context.MODE_PRIVATE)
                /// GUARDAR SOLO USUARIO ///
                val editor = preferences.edit()
                editor.putString("usuario", usuario)
                editor.apply()
            }
        }

        if(incorrecto){
            Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_LONG).show()
        }
    }

    fun ventanaRecuperarContra(vista : View){
        val btn = findViewById<Button>(R.id.btnRecuperarContra_inicioSesion)
        btn.isEnabled = false

        val ventana = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        ventana.setCancelable(false)
        // CARGA EL LAYOUT PERSONALIZADO//
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.ventana_recuperar_contra, null)
        ventana.setView(dialogView)

        val etCorreo = dialogView.findViewById<TextInputEditText>(R.id.etCorreo_IniciarSesion)

        ventana.setPositiveButton(R.string.enviar_str){ _, _ ->
            btn.isEnabled = true
        }
        ventana.setNeutralButton(R.string.cancelar_str){ _, _ ->
            btn.isEnabled = true
        }

        val dialog: AlertDialog = ventana.create()
        //// CIERRA EL DIALOG CON EL BOTON HACIA ATRAS ////
        dialog.setOnKeyListener(object : DialogInterface.OnKeyListener {
            override fun onKey(
                arg0: DialogInterface, keyCode: Int, event: KeyEvent): Boolean {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    btn.isEnabled = true
                    dialog.dismiss()
                }
                return true
            }
        })
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

        etCorreo.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(etCorreo.length() < 1)
                {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                }
            }
        })
    }

    fun ventanaRegistroUsuario(view : View){
        val botonRegistro = findViewById<Button>(R.id.btnRegistro_inicioSesion)

        var usuarioOk = false
        var contraOk = usuarioOk
        var confirmContra = false
        var usuarioValido = false

        val ventana = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        ventana.setCancelable(false) // EVITA QUE SE CIERRE EL DIALOG CON UN CLICK AFUERA DE EL //
        // CARGA EL LAYOUT PERSONALIZADO//
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.ventana_registro_usuario, null)
        ventana.setView(dialogView)

        val etCorreo = dialogView.findViewById<TextInputEditText>(R.id.etCorreo_registroUsuario)
        val etContra = dialogView.findViewById<TextInputEditText>(R.id.etContra_registroUsuario)
        val etConfirmContra = dialogView.findViewById<TextInputEditText>(R.id.etConfirmarContra_registroUsuario)
        val etUsuario = dialogView.findViewById<TextInputEditText>(R.id.etUsuario_registroUsuario)
            //// TRANSFORMA EL FORMATO TEXT A TEXTPASSWORD ////
        etContra.transformationMethod =  PasswordTransformationMethod()
        etConfirmContra.transformationMethod =  PasswordTransformationMethod()

        etConfirmContra.isEnabled = false

        ventana.setPositiveButton(R.string.registrar_str){ _, _ ->
            registrarUsuario(etUsuario.text.toString(), etCorreo.text.toString(), etContra.text.toString())
            botonRegistro.isEnabled = true
            cerrarTeclado()
        }
        ventana.setNeutralButton(R.string.cancelar_str){ _, _ ->
            botonRegistro.isEnabled = true
            cerrarTeclado()
        }

        val dialog: AlertDialog = ventana.create()
            //// CIERRA EL DIALOG CON EL BOTON HACIA ATRAS ////
        dialog.setOnKeyListener(object : DialogInterface.OnKeyListener {
            override fun onKey(
                arg0: DialogInterface, keyCode: Int, event: KeyEvent): Boolean {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    botonRegistro.isEnabled = true
                    dialog.dismiss()
                }
                return true
            }
        })
        dialog.show()
        botonRegistro.isEnabled = false
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

        etUsuario.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(etUsuario.length() < 3)
                {
                    etUsuario.error = getString(R.string.mensajeUsuario_str)
                    requestFocus(etUsuario)
                    usuarioOk = false
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                } else {
                    usuarioOk = true
                    usuarioValido = validaUsuario(etUsuario.text.toString())

                    if(!usuarioValido){
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                        etUsuario.error = getString(R.string.yaexiste_str)
                        requestFocus(etContra)
                    } else {
                        usuarioValido = true
                    }

                    if(contraOk && confirmContra && usuarioValido)
                    {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    }
                }
            }
        })

        etContra.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(etContra.length() < 3)
                {
                    etContra.error = getString(R.string.mensajeUsuario_str)
                    requestFocus(etContra)
                    etConfirmContra.isEnabled = false
                    contraOk = false
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                } else {
                    etConfirmContra.isEnabled = true
                    contraOk = true
                    if(usuarioOk && confirmContra && usuarioValido){
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    }
                }
            }
        })

        etConfirmContra.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(etConfirmContra.text.toString().equals(etContra.text.toString())){
                    confirmContra = true
                    if(usuarioOk && contraOk && usuarioValido){
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    }
                } else {
                    etConfirmContra.error = getString(R.string.mensajeContras_str)
                    requestFocus(etConfirmContra)
                    confirmContra = false
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                }
            }
        })
    }

    private fun registrarUsuario(usuario : String, correo : String, contra : String){
        cerrarTeclado()

        val datosUsuario = ContentValues()
        datosUsuario.put("usuario", usuario)
        datosUsuario.put("correo", correo)
        datosUsuario.put("contra", contra)

        val basededatos = bd.writableDatabase
        basededatos.insert("Usuarios", null, datosUsuario)
        basededatos.close()

        val toast = Toast(applicationContext)
        //// CARGA EL LAYOUT A UNA VISTA ////
        val view = layoutInflater.inflate(R.layout.usuario_registrado, null)
        toast.view = view
        toast.duration = Toast.LENGTH_LONG
        toast.setGravity(Gravity.TOP,0, 0)
        view.findViewById<TextView>(R.id.tvToast_usuarioregistrado).text = getString(R.string.registrado_str)
        toast.show()
    }

    fun ventanaRegistroRecicladora(view : View){
        val botonRegistroRecicladora = findViewById<Button>(R.id.btnRegistroRecicladora_inicioSesion)

        var usuarioOk = false
        var contraOk = usuarioOk
        var confirmContra = false
        var usuarioValido = false

        val ventana = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        ventana.setCancelable(false) // EVITA QUE SE CIERRE EL DIALOG CON UN CLICK AFUERA DE EL //
        // CARGA EL LAYOUT PERSONALIZADO//
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.ventana_registro_usuario, null)
        ventana.setView(dialogView)

        val etCorreo = dialogView.findViewById<TextInputEditText>(R.id.etCorreo_registroUsuario)
        val etContra = dialogView.findViewById<TextInputEditText>(R.id.etContra_registroUsuario)
        val etConfirmContra = dialogView.findViewById<TextInputEditText>(R.id.etConfirmarContra_registroUsuario)
        val etUsuario = dialogView.findViewById<TextInputEditText>(R.id.etUsuario_registroUsuario)
        //// TRANSFORMA EL FORMATO TEXT A TEXTPASSWORD ////
        etContra.transformationMethod =  PasswordTransformationMethod()
        etConfirmContra.transformationMethod =  PasswordTransformationMethod()

        etConfirmContra.isEnabled = false

        ventana.setPositiveButton(R.string.registrar_str){ _, _ ->
            registrarRecicladora(etUsuario.text.toString(), etCorreo.text.toString(), etContra.text.toString())
            botonRegistroRecicladora.isEnabled = true
            cerrarTeclado()
        }
        ventana.setNeutralButton(R.string.cancelar_str){ _, _ ->
            botonRegistroRecicladora.isEnabled = true
            cerrarTeclado()
        }

        val dialog: AlertDialog = ventana.create()
        //// CIERRA EL DIALOG CON EL BOTON HACIA ATRAS ////
        dialog.setOnKeyListener(object : DialogInterface.OnKeyListener {
            override fun onKey(
                arg0: DialogInterface, keyCode: Int, event: KeyEvent): Boolean {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    botonRegistroRecicladora.isEnabled = true
                    dialog.dismiss()
                }
                return true
            }
        })
        dialog.show()
        botonRegistroRecicladora.isEnabled = false
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

        etUsuario.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(etUsuario.length() < 3)
                {
                    etUsuario.error = getString(R.string.mensajeUsuario_str)
                    requestFocus(etUsuario)
                    usuarioOk = false
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                } else {
                    usuarioOk = true
                    usuarioValido = validaRecicladora(etUsuario.text.toString())

                    if(!usuarioValido){
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                        etUsuario.error = getString(R.string.yaexiste_str)
                        requestFocus(etContra)
                    } else {
                        usuarioValido = true
                    }

                    if(contraOk && confirmContra && usuarioValido)
                    {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    }
                }
            }
        })

        etContra.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(etContra.length() < 3)
                {
                    etContra.error = getString(R.string.mensajeUsuario_str)
                    requestFocus(etContra)
                    etConfirmContra.isEnabled = false
                    contraOk = false
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                } else {
                    etConfirmContra.isEnabled = true
                    contraOk = true
                    if(usuarioOk && confirmContra && usuarioValido){
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    }
                }
            }
        })

        etConfirmContra.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(etConfirmContra.text.toString().equals(etContra.text.toString())){
                    confirmContra = true
                    if(usuarioOk && contraOk && usuarioValido){
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    }
                } else {
                    etConfirmContra.error = getString(R.string.mensajeContras_str)
                    requestFocus(etConfirmContra)
                    confirmContra = false
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                }
            }
        })
    }

    private fun registrarRecicladora(usuario : String, correo : String, contra : String){
        cerrarTeclado()

        val datosRecicladora = ContentValues()

        val nombre = "Sin datos"
        val telefono = "Sin datos"
        val calle = "Sin datos"
        val colonia = "Sin datos"
        val numeroInt = 0

        datosRecicladora.put("usuario", usuario)
        datosRecicladora.put("correo", correo)
        datosRecicladora.put("contra", contra)
        datosRecicladora.put("nombre", nombre)
        datosRecicladora.put("telefono", telefono)
        datosRecicladora.put("calle", calle)
        datosRecicladora.put("colonia", colonia)
        datosRecicladora.put("numeroInt", numeroInt)

        val basededatos = bd.writableDatabase
        basededatos.insert("Recicladoras", null, datosRecicladora)
        basededatos.close()

        val toast = Toast(applicationContext)
        //// CARGA EL LAYOUT A UNA VISTA ////
        val view = layoutInflater.inflate(R.layout.usuario_registrado, null)
        toast.view = view
        toast.duration = Toast.LENGTH_LONG
        toast.setGravity(Gravity.TOP,0, 0)
        view.findViewById<TextView>(R.id.tvToast_usuarioregistrado).text = getString(R.string.registrado_str)
        toast.show()
    }

    private fun validaUsuario(nombre : String) : Boolean {
        val basededatos = bd.readableDatabase

        val consultaUsuario = basededatos.rawQuery("select usuario from Usuarios where usuario ='$nombre'",null)
        if(consultaUsuario.moveToFirst())
        {
            bd.close()
            return false
        }
        bd.close()
        return true
    }

    private fun validaRecicladora(nombre : String) : Boolean {
        val basededatos = bd.readableDatabase

        val consultaReci = basededatos.rawQuery("select usuario from Recicladoras where usuario ='$nombre'",null)
        if(consultaReci.moveToFirst())
        {
            bd.close()
            return false
        }
        bd.close()
        return true
    }

    private fun cerrarTeclado(){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }
}