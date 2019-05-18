package irvinc.example.com.inicioprincipal.Recicladora

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import irvinc.example.com.inicioprincipal.R

class SesionRecicladora : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sesion_recicladora)

        supportActionBar?.hide()
    }
}