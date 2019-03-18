package irvinc.example.com.inicioprincipal

import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.design.widget.TextInputEditText
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import irvinc.example.com.inicioprincipal.BD.BaseDeDatos
import irvinc.example.com.inicioprincipal.UsuarioLogeado.SesionUsuario

class IniciarSesion : AppCompatActivity() {

    private var usuario_registro : String? = null
    private var correo_registro : String? = null
    private var contra_registro : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iniciar_sesion)
        supportActionBar?.hide()


        val mantenerSesion = findViewById<CheckBox>(R.id.cbMantenerSesion_InicioSesion)

        val btnIniciarSesion = findViewById<MaterialButton>(R.id.btnLogin_inicio)

        btnIniciarSesion.setOnClickListener {
            val intent = Intent(this, SesionUsuario::class.java)
            finishAffinity()    //// CIERRA LAS DEMAS ACTIVITYS EN SEGUNDO PLANO////
            startActivity(intent)
        }

        val botonBack = findViewById<ImageButton>(R.id.btnBack_inicioSesion)
        botonBack.setOnClickListener {
            onBackPressed()
        }
    }

    fun ventanaRegistroUsuario(view : View){
        val botonRegistro = findViewById<Button>(R.id.btnRegistro_inicioSesion)
        var usuarioOk = false
        var contraOk = usuarioOk
        var confirmContra = false

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

        ventana.setPositiveButton(R.string.registrar_str){_, _ ->
            registrarUsuario()
            botonRegistro.isEnabled = true
        }
        ventana.setNeutralButton(R.string.cancelar_str){_,_ ->
            botonRegistro.isEnabled = true
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
                    usuario_registro = etUsuario.text.toString()
                    if(contraOk && confirmContra){
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                        correo_registro = etCorreo.text.toString()


                        //// VALIDAR SI EL USUARIO YA EXISTE ////
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
                    contra_registro = etContra.text.toString()
                    if(usuarioOk && confirmContra){
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                        correo_registro = etCorreo.text.toString()
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
                    if(usuarioOk && contraOk){
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                        correo_registro = etCorreo.text.toString()
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

    private fun registrarUsuario(){
        val datosUsuario = ContentValues()
        datosUsuario.put("usuario", usuario_registro)
        datosUsuario.put("correo", correo_registro)
        datosUsuario.put("contra", contra_registro)

        val bd =  BaseDeDatos(this, "Usuarios", null , 1)
        val basededatos = bd.writableDatabase
        basededatos.insert("Usuarios", null, datosUsuario)
        basededatos.close()
    }

    private fun usuarioExistente() : Boolean {

        return false
    }

    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }
}