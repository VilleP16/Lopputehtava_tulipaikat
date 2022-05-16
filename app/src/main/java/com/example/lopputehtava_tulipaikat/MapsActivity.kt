package com.example.lopputehtava_tulipaikat
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.*
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
import com.google.firebase.database.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var database: DatabaseReference
    //Current locationia varten
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    companion object{
        private const val  REQUEST_CODE=1 //voi olla mikä luku tahansa
    }

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
        lisaaMarkkeritKartalle()

        mMap.setOnMapLongClickListener {

            val klikinKoordinaatit = LatLng(it.latitude, it.longitude)
            avaaTulipaikanLisaysSheet(klikinKoordinaatit)

        }


    }

    private fun avaaTulipaikanLisaysSheet(koordinaatit : LatLng) {
        val dialog = BottomSheetDialog(this)
        val view=layoutInflater.inflate(R.layout.bottom_sheet_dialog2,null)
        dialog.setContentView(view)
        dialog.show()

        //tallennetaan data
        var paikanNimi = view.findViewById<EditText>(R.id.nimiInput)
        var paikanKuvaus = view.findViewById<EditText>(R.id.kuvausInput)
        var onkoPuita = view.findViewById<CheckBox>(R.id.polttopuutCb)
        var onkoVessaa = view.findViewById<CheckBox>(R.id.vessaCb)
        var paikanKoordinaatit = koordinaatit
        var lisaaButton = view.findViewById<Button>(R.id.tallennaBtn)
        var koordinaatitSheetissa = view.findViewById<TextView>(R.id.koordinaatitTxt)
        koordinaatitSheetissa.text = "Lat: " + koordinaatit.latitude +  "Lng: "  + koordinaatit.longitude

        lisaaButton.setOnClickListener(){


            database = FirebaseDatabase.getInstance().getReference("Tulipaikat")
            var key = database.push().key.toString()
            database.child(key).child("Nimi").setValue(paikanNimi.text.toString())
            database.child(key).child("Kuvaus").setValue(paikanKuvaus.text.toString())
            database.child(key).child("Puut").setValue((onkoPuita.isChecked))
            database.child(key).child("Vessa").setValue((onkoVessaa.isChecked))
            database.child(key).child("Lat").setValue(koordinaatit.latitude)
            database.child(key).child("Lng").setValue(koordinaatit.longitude)
            dialog.dismiss()

        }

    }
    private fun lisaaMarkkeritKartalle() {
        val zoom = 13.0
        database = FirebaseDatabase.getInstance().getReference("Tulipaikat")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    try {
                        for (paikka in snapshot.children) {
                        val lat = paikka.child("Lat").value
                        val lng = paikka.child("Lng").value
                        val paikannimi = paikka.child("Nimi").value
                        val lokaatio = LatLng(lat!! as Double, lng!! as Double)
                        val tagi = paikka.key as String
                        println(tagi)

                        var m = mMap.addMarker(MarkerOptions()
                            .position(lokaatio)
                            .title(paikannimi as String?))
                        m?.tag = tagi

                    }
                    } catch(e: Exception) {
                        println("Non nullable virhe mutta ei kaadu ohjelma")
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

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
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 12f))

        database = FirebaseDatabase.getInstance().getReference("Tulipaikat")
        database.child(marker.tag.toString()).get().addOnSuccessListener {
            if (it.exists()) {
                var paikkaNimi = it.child("Nimi").value as String
                var kuvaus = it.child("Kuvaus").value as String
                var lat = it.child("Lat").value as Double
                var lng = it.child("Lng").value as Double
                var onkoVessaa = it.child("Vessa").value as Boolean
                var onkoPuita = it.child("Puut").value as Boolean

                val dialog = BottomSheetDialog(this)
                val view=layoutInflater.inflate(R.layout.bottom_sheet_dialog,null)
                var paikka: TextView = view.findViewById(R.id.nimiTxt)
                var koordinaatit: TextView = view.findViewById(R.id.koordinaatitTxt)
                var kuvausTeksti: TextView = view.findViewById(R.id.kuvausTxt)
                var vessaCB: CheckBox = view.findViewById(R.id.vessaCb)
                var puutCB: CheckBox = view.findViewById(R.id.puutCb)

                paikka.text = paikkaNimi
                kuvausTeksti.text = kuvaus
                koordinaatit.text = "Lat: " + lat.toString() + " " + "Lng: " + lng.toString()
                if(onkoVessaa){
                    vessaCB.isChecked = true
                }
                if(onkoPuita){
                    puutCB.isChecked = true
                }
                dialog.setContentView(view)
                dialog.show()
            }
        }
        false
        return false
    }

}