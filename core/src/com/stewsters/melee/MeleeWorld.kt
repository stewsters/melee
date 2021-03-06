package com.stewsters.melee

import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.math.Vector3
import org.jbox2d.collision.shapes.CircleShape
import org.jbox2d.collision.shapes.PolygonShape
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import org.jbox2d.dynamics.FixtureDef
import org.jbox2d.dynamics.World
import java.util.*

class MeleeWorld(val xSize: Float, val ySize: Float) {
    val r = Random()

    val actors = mutableListOf<Actor>()
    val players = mutableListOf<Actor>()
    val obstacles = mutableListOf<Obstacle>()

    val world: World = World(Vec2(0f, 0f))


    fun getCenter(): Vector3 {

        if (players.size <= 0)
            return Vector3(xSize / 2f, ySize / 2f, 0f)

        val x = players.sumByDouble { it.body.position.x.toDouble() } / players.size
        val y = players.sumByDouble { it.body.position.y.toDouble() } / players.size

        return Vector3(x.toFloat(), y.toFloat(), 0f)
    }

    fun buildWalls(walls: List<Obstacle>) {
        // build walls
        walls.forEach {
            val wall = BodyDef()
            wall.type = BodyType.STATIC
            val body = world.createBody(wall)

            body.position.x = it.x
            body.position.y = it.y

            val shape = PolygonShape()
            shape.setAsBox(it.xSize / 2f, it.ySize / 2f)

            body.createFixture(shape, 1f)
            it.body = body

            obstacles.add(it)
        }
    }

    fun createObstacle(x: Float, y: Float) {

        println("Creating Obstacle")
        val bodyDef = BodyDef().apply {
            position.x = x
            position.y = y
            angle = r.nextFloat() * Math.PI.toFloat() * 2f
            type = BodyType.DYNAMIC

            linearDamping = 0.8f
            angularDamping = 0.8f
            bullet = true // fast moving
        }
        val body = world.createBody(bodyDef)

        val bodyShape = CircleShape()
        bodyShape.radius = 5f

        val fixtureDef = FixtureDef().apply {
            shape = bodyShape
            friction = 0.3f
            restitution = 0.5f
            density = 1.0f // mass is calculated from this * area
        }

        body.createFixture(fixtureDef)

        val actor = Actor(body, bodyShape.radius * 2, bodyShape.radius * 2)
        this.actors.add(actor)
    }

    fun createPlayer(x: Float, y: Float, controller: Controller) {
        println("Creating Player")

        val bodyDef = BodyDef().apply {
            position.x = x
            position.y = y
            angle = r.nextFloat() * Math.PI.toFloat() * 2f
            type = BodyType.DYNAMIC

            linearDamping = 0.8f
            angularDamping = 0.8f
            bullet = true // fast moving
        }
        val body = world.createBody(bodyDef)


        val bodyShape = CircleShape()
        bodyShape.radius = 10f

        val fixtureDef = FixtureDef().apply {
            shape = bodyShape
            friction = 0.3f
            restitution = 0.5f
            density = 1.0f // mass is calculated from this * area
        }
        body.createFixture(fixtureDef)


        val swordShape = PolygonShape()
        swordShape.setAsBox(5f, 20f, Vec2(0f, 10f), 0f)

        val swordFixtureDef = FixtureDef().apply {
            shape = swordShape
            friction = 0.05f
            restitution = 3f
            density = 1.0f // mass is calculated from this * area

        }

        body.createFixture(swordFixtureDef)

        val actor = Actor(body, bodyShape.radius * 2, bodyShape.radius * 2, controller)
        this.actors.add(actor)
        this.players.add(actor)
    }


}