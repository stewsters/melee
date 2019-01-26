package com.stewsters.melee

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.controllers.Controllers
import com.badlogic.gdx.controllers.mappings.Xbox
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import org.jbox2d.common.Vec2
import java.lang.Math.toDegrees
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

val twoPi = 2 * Math.PI.toFloat()

class MeleeGame : ApplicationAdapter() {

    //    https://stackoverflow.com/questions/17902373/split-screen-in-libgdx
    internal lateinit var cam: OrthographicCamera

    internal lateinit var spriteBatch: SpriteBatch
    internal lateinit var shapeRenderer: ShapeRenderer

    internal lateinit var playerTextureBase: Texture
    internal lateinit var playerTexture: TextureRegion
    internal lateinit var obstacleTexture: Texture
    internal lateinit var mapTexture: Texture

    val meleeWorld = MeleeWorld(600f, 600f)

    override fun create() {

        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()

        mapTexture = Texture(Gdx.files.internal("ring.png"))
        playerTextureBase = Texture(Gdx.files.internal("face.png"))
        playerTexture = TextureRegion(playerTextureBase)
        obstacleTexture = Texture(Gdx.files.internal("ring.png")) // TODO: make a new texture

        // Constructs a new OrthographicCamera, using the given viewport width and height
        // Height is multiplied by aspect ratio.
        cam = OrthographicCamera(800f, 800 * (h / w))

        shapeRenderer = ShapeRenderer()
        spriteBatch = SpriteBatch()

        val obstacleCount = 4
        val dist = Math.PI * 2.0 / obstacleCount
        // add some extra debris
        for (i in 0 until obstacleCount) {

            val angle = i * dist

            val x = (cos(angle).toFloat() * meleeWorld.xSize / 4f) + meleeWorld.xSize / 2f
            val y = (sin(angle).toFloat() * meleeWorld.ySize / 4f) + meleeWorld.ySize / 2f

            println("${Math.toDegrees(angle)} $x $y")

            meleeWorld.createObstacle(x, y)
        }

        meleeWorld.buildWalls(listOf(
                Wall(0f, 300f, 20f, 600f),
                Wall(600f, 300f, 20f, 600f),
                Wall(300f, 0f, 600f, 20f),
                Wall(300f, 600f, 600f, 20f)
        ))


        val controllers = Controllers.getControllers()
        val distPlayer = Math.PI * 2.0 / controllers.size
        var i = 0

        for (controller in controllers) {
            val angle = i++ * distPlayer

            val x = (cos(angle).toFloat() * meleeWorld.xSize / 4f) + meleeWorld.xSize / 2f
            val y = (sin(angle).toFloat() * meleeWorld.ySize / 4f) + meleeWorld.ySize / 2f
            println("Controller added $x $y")
            meleeWorld.createPlayer(
                    x,
                    y,
                    controller
            )
        }

    }

    override fun render() {
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit()
        }

        val center = meleeWorld.getCenter()
        cam.position.set(center)
        cam.update()

        spriteBatch.setProjectionMatrix(cam.combined)
        shapeRenderer.setProjectionMatrix(cam.combined)

        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);



        for (player in meleeWorld.players) {

            if (player.controller != null) {
                val x = deadZone(player.controller.getAxis(Xbox.L_STICK_HORIZONTAL_AXIS))
                val y = deadZone(player.controller.getAxis(Xbox.L_STICK_VERTICAL_AXIS))

                if (abs(x) > 0.1 || abs(y) > 0.1) {
                    player.body.applyForceToCenter(Vec2(x * 100_000f, y * -100_000f));
                }

                val armX = player.controller.getAxis(Xbox.R_STICK_HORIZONTAL_AXIS)
                val armY = player.controller.getAxis(Xbox.R_STICK_VERTICAL_AXIS)

                if (abs(armX) > 0.2 || abs(armY) > 0.2) {

                    val normal = Vec2(armX, armY)
                    normal.normalize()

                    var targetAngle = (Math.atan2(normal.y.toDouble(), normal.x.toDouble()) + Math.PI / 2) % (twoPi)

                    var currentAngle = -player.body.angle % twoPi

                    if (currentAngle < 0) {
                        currentAngle = (currentAngle + twoPi) % twoPi
                    }

                    val turnForce = ((targetAngle + twoPi - currentAngle + twoPi) % twoPi) - Math.PI

                    val way = turnForce > 0

                    //         println("target ${Math.toDegrees(targetAngle)} current ${Math.toDegrees(currentAngle.toDouble())}")

                    player.body.applyTorque(if (way) turnForce.toFloat() * 600_000f else turnForce.toFloat() * 600_000f)

                }

            }
        }


        meleeWorld.world.step(Gdx.graphics.deltaTime, 5, 3)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (obstacle in meleeWorld.obstacles) {
            shapeRenderer.rect(obstacle.x - obstacle.xSize / 2, obstacle.y - obstacle.ySize / 2, obstacle.xSize, obstacle.ySize)
        }

        shapeRenderer.end()

        spriteBatch.begin();

        // Draw ring
        spriteBatch.draw(mapTexture, 50f, 50f, meleeWorld.xSize - 100f, meleeWorld.ySize - 100f)
        spriteBatch.end()

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (actor in meleeWorld.players) {
            val pos = actor.body.position
            shapeRenderer.rect(pos.x, pos.y,
                    0f, 0f,
                    5f, 30f,
                    1f, 1f,
                    toDegrees(actor.body.angle.toDouble()).toFloat()
            )

        }
        shapeRenderer.end()

        spriteBatch.begin();
        for (actor in meleeWorld.actors) {
            val pos = actor.body.position

            spriteBatch.draw(
                    playerTexture,
                    pos.x - actor.xSize / 2, pos.y - actor.ySize / 2,
                    actor.xSize / 2, actor.ySize / 2,
                    actor.xSize, actor.xSize,
                    1f, 1f,
                    toDegrees(actor.body.angle.toDouble()).toFloat()
            )

            val ballPos = actor.ballBody?.position
            if (ballPos != null)
                spriteBatch.draw(
                        playerTexture,
                        ballPos.x - actor.xSize / 2, ballPos.y - actor.ySize / 2,
                        actor.xSize / 2, actor.ySize / 2,
                        actor.xSize / 2, actor.xSize / 2,
                        1f, 1f,
                        0f
                )
        }
        spriteBatch.end();

    }

    override fun dispose() {
        spriteBatch.dispose()
        shapeRenderer.dispose()

        mapTexture.dispose()
        obstacleTexture.dispose()
        playerTextureBase.dispose()
    }
}


private fun deadZone(value: Float): Float = value * abs(value)
