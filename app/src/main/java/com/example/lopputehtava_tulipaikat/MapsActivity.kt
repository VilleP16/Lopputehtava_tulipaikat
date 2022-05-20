package com.example.lopputehtava_tulipaikat

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.bumptech.glide.Glide
import com.example.lopputehtava_tulipaikat.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.*
import com.google.firebase.database.collection.LLRBNode
import com.google.firebase.storage.FirebaseStorage


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var database=FirebaseDatabase.getInstance().getReference("Tulipaikat")
    //Current locationia varten
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    companion object{
        private const val  REQUEST_CODE=1 //voi olla mikä luku tahansa
    }
    //uuden paikan lisäykseen
    private lateinit var paikanNimi:EditText
    private lateinit var paikanKuvaus: EditText
    private lateinit var onkoPuita: CheckBox
    private lateinit var onkoVessaa: CheckBox
    private  lateinit var tallennaTulipaikkaButton: Button
    private  lateinit var koordinaatitSheetissa: TextView
    private lateinit var key: String
    //kuvan lisäykseen
    private lateinit var tallennaKuvaBtn:Button
    private var imageUri: Uri?=null




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
        var i = R.drawable.tuli
        var ikoni= i.toBitmap(this)
        if (ikoni != null) {
            lisaaMarkkeritKartalle(ikoni)
        }
        Toast.makeText(applicationContext,"Voit lisätä uuden tulipaikan painamalla karttaa pitkään",Toast.LENGTH_LONG).show()
        
        mMap.setOnMapLongClickListener {

            val klikinKoordinaatit = LatLng(it.latitude, it.longitude)
            lisaaUusiTulipaikka(klikinKoordinaatit)
        }
    }
//Muutetaan ikoni bitmapiksi, jotta voidaan käyttää markkereina
fun Int.toBitmap(context: Context): Bitmap? {

    // retrieve the actual drawable
    val drawable = ContextCompat.getDrawable(context, this) ?: return null
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    val bm = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)

    // draw it onto the bitmap
    val canvas = Canvas(bm)
    drawable.draw(canvas)
    return bm
}
    @SuppressLint("InflateParams")
    private fun lisaaUusiTulipaikka(koordinaatit : LatLng) {
        val dialog = BottomSheetDialog(this)
        val view=layoutInflater.inflate(R.layout.bottom_sheet_dialog2,null)
        dialog.setContentView(view)
        dialog.show()

        paikanNimi = view.findViewById(R.id.nimiInput)
        paikanKuvaus = view.findViewById(R.id.kuvausInput)
        onkoPuita = view.findViewById(R.id.puutCb)
        onkoVessaa = view.findViewById(R.id.vessaCb)
        tallennaTulipaikkaButton = view.findViewById(R.id.tallennaBtn)
        koordinaatitSheetissa = view.findViewById(R.id.koordinaatitTxt)
        koordinaatitSheetissa.text = "" + koordinaatit.latitude +  " , "  + koordinaatit.longitude
        tallennaKuvaBtn=view.findViewById(R.id.kuvaBtn)
        key = database.push().key.toString()

        tallennaTulipaikkaButton.setOnClickListener{

            if(tekstiKentatTayttavatEhdot(paikanNimi.text.toString(),paikanKuvaus.text.toString())){
               // database = FirebaseDatabase.getInstance().getReference("Tulipaikat")
                var tulipaikka=TulipaikkaLuokka()
                with(tulipaikka){
                    Nimi=paikanNimi.text.toString()
                    Kuvaus=paikanKuvaus.text.toString()
                    Lat=koordinaatit.latitude
                    Lng=koordinaatit.longitude
                    Vessa=onkoVessaa.isChecked
                    Puut=onkoPuita.isChecked

                    database.child(key).setValue(tulipaikka)
                    println(key)

                }


            }
            //tallennetaan kuva storageen
            imageUri?.let { it1 -> uploadImg(it1, key) }
            dialog.dismiss()
            Toast.makeText(this, "Tulipaikka lisätty",Toast.LENGTH_LONG).show()


        }
        tallennaKuvaBtn.setOnClickListener{
            selectImage()
        }
    }

    private fun tekstiKentatTayttavatEhdot(nimi:String, kuvaus: String): Boolean {
            if(nimi.isNotEmpty() && nimi.length <= 20 && kuvaus.isNotEmpty() && kuvaus.length <= 100){

                return true
            }
        Toast.makeText(this, "Virhe, tarkista vaadittavat kentät",Toast.LENGTH_SHORT).show()
        return false

    }

    //Kuvan valitseminen puhelimesta
    private fun selectImage() {
        val intent= Intent()
        intent.type="image/*"
        intent.action= Intent.ACTION_GET_CONTENT
        startActivityForResult(intent,100)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==100 && resultCode== RESULT_OK){
            imageUri= data?.data!!


        }
    }
    private fun uploadImg(imagUri: Uri, id:String) {
        if(imagUri!=null){
        var storage= FirebaseStorage.getInstance().reference.child("images/$id")
        storage.putFile(imagUri)

        }
    }

    private fun lisaaMarkkeritKartalle(ikoni:Bitmap) {

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    try {
                        for (paikka in snapshot.children) {
                        val lat = paikka.child("lat").value
                        val lng = paikka.child("lng").value
                        val paikannimi = paikka.child("nimi").value
                        val lokaatio = LatLng(lat!! as Double, lng!! as Double)
                        val tagi = paikka.key as String

                            val m = mMap.addMarker(MarkerOptions()
                            .position(lokaatio)
                            .title(paikannimi as String?).icon(ikoni?.let { BitmapDescriptorFactory.fromBitmap(it) })
                               )
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
    //Tämä kommentoitu pois jottei näytä markkeria nykyisessä sijainnissa. Näkyy vain sininen pallo
    private fun placeMarkerOnMap(currentLatLong: LatLng) {
       // val markerOptions= MarkerOptions().position(currentLatLong)
     //   markerOptions.title("Olet tässä")
      // markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
      //  mMap.addMarker(markerOptions)
    }

    @SuppressLint("InflateParams")
    override fun onMarkerClick(marker: Marker): Boolean {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 12f))
        val dialog = BottomSheetDialog(this)
        val view=layoutInflater.inflate(R.layout.bottom_sheet_dialog,null)
        database = FirebaseDatabase.getInstance().getReference("Tulipaikat")
        database.child(marker.tag.toString()).get().addOnSuccessListener {
            if (it.exists()) {
                val paikkaNimi = it.child("nimi").value as String
                val kuvaus = it.child("kuvaus").value as String
                val lat = it.child("lat").value as Double
                val lng = it.child("lng").value as Double
                val onkoVessaa = it.child("vessa").value as Boolean
                val onkoPuita = it.child("puut").value as Boolean

               // val dialog = BottomSheetDialog(this)
               // val view=layoutInflater.inflate(R.layout.bottom_sheet_dialog,null)
                val paikka: TextView = view.findViewById(R.id.nimiTxt)
                val koordinaatit: TextView = view.findViewById(R.id.koordinaatitTxt)
                val kuvausTeksti: TextView = view.findViewById(R.id.kuvausTxt)
                val vessaCB: CheckBox = view.findViewById(R.id.vessaCb)
                val puutCB: CheckBox = view.findViewById(R.id.puutCb)

                paikka.text = paikkaNimi
               // testi: paikka.text=marker.tag.toString()
                kuvausTeksti.text = kuvaus
                koordinaatit.text = "$lat , $lng"
                    vessaCB.isChecked = onkoVessaa
                    puutCB.isChecked = onkoPuita

                dialog.setContentView(view)
                dialog.show()
            }


        }
        val kuvanNimi=marker.tag
        println("hakee kuvaa nimellä $kuvanNimi")
        //  Toast.makeText(this, kuvanNimi,Toast.LENGTH_LONG).show()//testi. Kuvan nimi on sama kuin markkerin key/tag
        val storageRef = FirebaseStorage.getInstance().reference
        val kuvanPaikka:ImageView=view.findViewById(R.id.kuvaImg)
        storageRef.child("images/$kuvanNimi").downloadUrl.addOnSuccessListener {
            Glide.with(this ).load(it).into(kuvanPaikka)
        }.addOnFailureListener {
            // Handle any errors
        }


      //  false  18.5 en tiiä mikä tämä oli, vissiin vahinko false
        return false
    }

}