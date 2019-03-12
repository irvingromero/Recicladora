package irvinc.example.com.inicioprincipal

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.Toast

class IniciarSesion : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iniciar_sesion)
        supportActionBar?.hide()
    }

    fun registrarse(view : View){

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
