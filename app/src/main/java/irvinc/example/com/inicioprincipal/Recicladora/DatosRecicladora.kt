package irvinc.example.com.inicioprincipal.Recicladora

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import irvinc.example.com.inicioprincipal.BD.BaseDeDatos
import irvinc.example.com.inicioprincipal.R

class DatosRecicladora : AppCompatActivity() {

    private var usuarioLogeado : String? = null

    private var campoCorreo : TextInputEditText? = null
    private var campoContra : TextInputEditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datos_recicladora)

        usuarioLogeado = intent.extras?.getString("usuario")

        supportActionBar?.hide()

        campoCorreo = findViewById(R.id.tietCorreo_datosRecicadora)
        campoContra = findViewById<TextInputEditText>(R.id.tietContra_datosRecicladora)

        cargarDatos()

        findViewById<ImageButton>(R.id.btnAtras_datosrecicladora).setOnClickListener {
            onBackPressed()
        }
    }

    private fun cargarDatos(){
        val bd =  BaseDeDatos(this, "Recicladoras", null , 1)
        val basededatos = bd.readableDatabase
        val c = basededatos.rawQuery("select usuario from Recicladoras where usuario = '$usuarioLogeado'",null)
        if(c.moveToFirst())
        {
            campoCorreo?.setText(c.getString(0))
        }
    }

    fun modificarDatos(v : View){
        val toast = Toast(applicationContext)
        //// CARGA EL LAYOUT A UNA VISTA ////
        val view = layoutInflater.inflate(R.layout.usuario_registrado, null)
        toast.view = view
        toast.duration = Toast.LENGTH_LONG
        toast.setGravity(Gravity.BOTTOM,0, 30)
        view.findViewById<TextView>(R.id.tvToast_usuarioregistrado).text = getString(R.string.datosModificados_str)
        view.setBackgroundColor(resources.getColor(R.color.colorPrimaryDark))
        toast.show()

        onBackPressed()
    }
}