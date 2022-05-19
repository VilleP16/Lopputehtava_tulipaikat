package com.example.lopputehtava_tulipaikat

data class TulipaikkaLuokka(var Nimi:String, var Kuvaus:String, var Lat:Double, var Lng:Double, var Puut:Boolean, var Vessa:Boolean){

    constructor() : this("", "", 0.0,0.0,false, false)
}

