package com.example.lopputehtava_tulipaikat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.lopputehtava_tulipaikat.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val zoom = 12.0f

        val kemiKiikeli = LatLng(65.74113055245003, 24.54041091060241)
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



    }
}