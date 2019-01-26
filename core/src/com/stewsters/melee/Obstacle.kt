package com.stewsters.melee

import org.jbox2d.dynamics.Body

class Obstacle(
        val x: Float,
        val y: Float,
        val xSize: Float,
        val ySize: Float
){
    var body:Body?=null
}
