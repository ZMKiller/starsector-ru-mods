package SOTFECO

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.util.Misc
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

class SOTFECO_combatPingScript(
    val spec: CombatPingSpec
): BaseCombatLayeredRenderingPlugin() {

    companion object {
        const val BASE_SEGMENTS = 200f
    }

    init {
        Global.getSoundPlayer().playSound("default_campaign_ping", 0.5f, 1f, spec.location, Misc.ZERO)
    }

    var expired = false
    var timeLeft = spec.lifespan
    val sprite = Global.getSettings().getSprite("combat", "corona_soft")

    override fun advance(amount: Float) {
        super.advance(amount)

        val engine = Global.getCombatEngine()
        if (engine.isPaused) return

        timeLeft -= amount
        if (timeLeft <= 0f) {
            expired = true
            return
        }

    }

    override fun isExpired(): Boolean {
        return expired
    }

    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {
        super.render(layer, viewport)

        if (layer == CombatEngineLayers.ABOVE_SHIPS_LAYER) {
            val progress = 1 - (timeLeft / spec.lifespan)
            val segments = (BASE_SEGMENTS) * (1 + progress)
            val currRadius = (spec.maxRadius) * progress

            val startRad = Math.toRadians(0.0).toFloat()
            val endRad = Math.toRadians(360.0).toFloat()
            val spanRad = Misc.normalizeAngle(endRad - startRad)
            val anglePerSegment: Float = spanRad / segments

            GL11.glPushMatrix()
            GL11.glTranslatef(spec.location.x, spec.location.y, 0f)
            GL11.glEnable(GL11.GL_TEXTURE_2D)

            sprite.bindTexture()

            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

            GL11.glColor4ub(
                spec.color.red.toByte(),
                spec.color.green.toByte(),
                spec.color.blue.toByte(),
                (spec.color.alpha * progress).toInt().toByte()
            )

            var texX = 0f
            val incr: Float = 1f / segments
            GL11.glBegin(GL11.GL_QUAD_STRIP)
            var i = 0f
            while (i < (segments + 1)) {
                val last = i == segments.toFloat()
                if (last) i = 0f
                val theta: Float = anglePerSegment * i
                val cos = cos(theta.toDouble()).toFloat()
                val sin = sin(theta.toDouble()).toFloat()

                val m1 = 1f
                val m2 = 1f

                val x1: Float = cos * currRadius * m1
                val y1: Float = sin * currRadius * m1
                val x2: Float = cos * (currRadius + spec.thickness * m2)
                val y2: Float = sin * (currRadius + spec.thickness * m2)

                GL11.glTexCoord2f(0.5f, 0.05f)
                GL11.glVertex2f(x1, y1)

                GL11.glTexCoord2f(0.5f, 0.95f)
                GL11.glVertex2f(x2, y2)

                texX += incr
                if (last) break
                i++
            }

            GL11.glEnd()
            GL11.glPopMatrix()
        }
    }

    data class CombatPingSpec(
        val lifespan: Float,
        val maxRadius: Float,
        val thickness: Float,
        //val radiusToFade: Float,
        val color: Color,
        val location: Vector2f
    ) {

    }
}
