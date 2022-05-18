package com.example.lopputehtava_tulipaikat

data class TulipaikkaLuokka(var nimi:String, var kuvaus:String, var lat:Double, var long:Double, val onkoPuita:Boolean, val onkoVessa:Boolean){

    constructor() : this("", "", 0.0,0.0,false, false)
}

