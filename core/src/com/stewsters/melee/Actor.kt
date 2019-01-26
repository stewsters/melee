package com.stewsters.melee

import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import org.jbox2d.dynamics.Body

class Actor(
        val body: Body,
        val xSize: Float,
        val ySize: Float,
        val controller: Controller? = null
) {

//    val sprite: Sprite
//
//    init {
//
//        sprite = Sprite(texture)
////        sprite.setPosition(0f, 0f);
//        sprite.setSize(xSize, ySize);
//
//    }

}
