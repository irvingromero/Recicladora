package irvinc.example.com.inicioprincipal

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Gravity
import android.view.View

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import android.widget.ImageButton

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var drawerLayout : DrawerLayout? = null
    private var drawerOpen = false// Bandera para saber el estado del drawer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        supportActionBar?.hide()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        detectarSlide()

        var btnMenu = findViewById<ImageButton>(R.id.btnMenu)
        btnMenu.setOnClickListener {
            drawerLayout?.openDrawer(Gravity.LEFT)
            drawerOpen = true
        }
    }

    private fun detectarSlide(){
        //// DETECTA CUANDO AL MENU SE LE HACE SLIDE
        drawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout?.setDrawerListener(object : ActionBarDrawerToggle(this, drawerLayout,0,0)
        {
            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                drawerOpen = false//is Closed
            }
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                drawerOpen = true//is Opened
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
    }

    fun back(view : View){
        onBackPressed()
    }

    override fun onBackPressed() {
        if (drawerOpen)     //// GUARDAR EL ESTADO DE LA BANDERA CUANDO ENTRA EN LANDSCAPE /////
        {
            drawerLayout?.closeDrawer(Gravity.START)
            drawerOpen = false
        }
        else {
            super.onBackPressed()
        }
    }
}