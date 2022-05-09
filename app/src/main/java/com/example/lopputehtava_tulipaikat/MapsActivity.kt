package com.example.lopputehtava_tulipaikat
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.lopputehtava_tulipaikat.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    //Current locationia varten
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    companion object{
        private const val  REQUEST_CODE=1 //voi olla mikä luku tahansa
    }
    val ekaPaikka=Paikka("Aulangon tulipaikka 1",LatLng(61.027766109993266, 24.47456521326111), "Järven rannalla, ei laavua")
    val tokaPaikka=Paikka("Aulangon nuotiopaikka 2", LatLng(61.03217572704121, 24.46314010584156),"Aulanko 2", 1)
    val kolmasPaikka=Paikka("Kiikelin laavu", LatLng(65.74113055245003, 24.54041091060241),"Jotain", 1)
    val listaPaikoista=arrayOf(ekaPaikka, tokaPaikka, kolmasPaikka)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled=true
        mMap.setOnMarkerClickListener(this)
        setUpMap()
        for(paikka in listaPaikoista){
            NaytaValmiitMarkkerit(paikka)
        }
    }
      //  val zoom = 12.0f

       /* val kemiKiikeli = LatLng(65.74113055245003, 24.54041091060241)
        mMap.addMarker(MarkerOptions()
            .position(kemiKiikeli)
            .title("Kiikelin laavu, Kemi")
            .snippet("Kiikelin laavu Kemissä...JNEJNEJNE")
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kemiKiikeli, zoom))

        val uiSettings = googleMap.uiSettings
        uiSettings.isZoomControlsEnabled = true

        mMap.setOnMarkerClickListener { marker ->
            val dialog = BottomSheetDialog(this)
            val view=layoutInflater.inflate(R.layout.bottom_sheet_dialog,null)
            dialog.setContentView(view)
            dialog.show()
            false
        }

*/



    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE)
            return
        }
        mMap.isMyLocationEnabled=true
        fusedLocationProviderClient.lastLocation.addOnSuccessListener(this) { location ->
            if(location !=null){
                lastLocation=location
                val currentLatLong= LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLong)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 12f))
            }
        }
    }
    private fun placeMarkerOnMap(currentLatLong: LatLng) {
        val markerOptions= MarkerOptions().position(currentLatLong)
        markerOptions.title("Olet tässä")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
        mMap.addMarker(markerOptions)

    }

    override fun onMarkerClick(marker: Marker): Boolean {
        //Toast.makeText(this, "jtn infoa"+ marker.title, Toast.LENGTH_SHORT).show()
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 12f))
        val dialog = BottomSheetDialog(this)
        val view=layoutInflater.inflate(R.layout.bottom_sheet_dialog,null)
        dialog.setContentView(view)
        dialog.show()
        false
        return false
    }
    //override fun onMarkerClick(p0: Marker)=false
    private fun NaytaValmiitMarkkerit(paikka:Paikka){
        val markerOptions= MarkerOptions().position(paikka.koordinaatit)
        markerOptions.title(paikka.nimi)
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
        mMap.addMarker(markerOptions)
    }
}