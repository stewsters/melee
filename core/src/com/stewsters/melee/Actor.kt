package com.stewsters.melee

import com.badlogic.gdx.controllers.Controller
import org.jbox2d.dynamics.Body

class Actor(
        val body: Body,
        val xSize: Float,
        val ySize: Float,
        val controller: Controller? = null,
        val ballBody: Body?=null
)
