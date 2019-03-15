package irvinc.example.com.inicioprincipal

import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AlertDialog
import android.view.KeyEvent
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import irvinc.example.com.inicioprincipal.UsuarioLogeado.SesionUsuario
import android.view.WindowManager

class IniciarSesion : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iniciar_sesion)
        supportActionBar?.hide()
/*
        var campoContra = findViewById<TextInputEditText>(R.id.etContra_inicioSesion)
        var mantenerSesion = findViewById<CheckBox>(R.id.cbMantenerSesion_InicioSesion)
*/
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

    fun registroUsuario(view : View){
        val ventana = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        ventana.setCancelable(false) // EVITA QUE SE CIERRE EL DIALOG CON UN CLICK AFUERA DE EL //
        // CARGA EL LAYOUT PERSONALIZADO//
        ventana.setView(layoutInflater.inflate(R.layout.ventana_registro_usuario, null))

        ventana.setPositiveButton(R.string.registrar_str){_, _ ->
            registrarUsuario()
        }
        ventana.setNeutralButton(R.string.cancelar_str){_,_ -> }

        val dialog: AlertDialog = ventana.create()
            //// CIERRA EL DIALOG CON EL BOTON HACIA ATRAS ////
        dialog.setOnKeyListener(object : DialogInterface.OnKeyListener {
            override fun onKey(
                arg0: DialogInterface, keyCode: Int, event: KeyEvent): Boolean {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss()
                }
                return true
            }
        })
        dialog.show()
    }

    private fun registrarUsuario(){
 //       validarUsuario()
    }
/*
    private fun validarUsuario() : Boolean {


        if (campoUsuario_registro.text.toString().trim().isEmpty()) {
            campoUsuario_registro.error = getString(R.string.mensajeUsuario_str)
            requestFocus(campoUsuario_registro)
            return false
        } else {
            tilCampoUsuario_registro.isErrorEnabled = false
        }
        return true
    }

    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }
    */
}
/*
           https://www.androidhive.info/2015/09/android-material-design-floating-labels-for-edittext/
*/