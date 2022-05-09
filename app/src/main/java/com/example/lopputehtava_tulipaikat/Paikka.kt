package com.example.lopputehtava_tulipaikat

import com.google.android.gms.maps.model.LatLng

class Paikka(val nimi:String, val koordinaatit: LatLng, val kuvaus:String, val onkoPuita:Int?=0, onkoVessa:Int?=0)