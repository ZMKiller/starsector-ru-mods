package org.niatahl.tahlan.weapons.deco

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import org.magiclib.util.MagicRender
import org.niatahl.tahlan.utils.TahlanIDs.DAEMONIC_HEART
import java.awt.Color
import java.io.IOException
import kotlin.collections.ArrayList
import kotlin.math.ceil

class HelAltarEffectScript : EveryFrameWeaponEffectPlugin {
    private var loaded = false
    private var rotation = 0f
    private val targetList = ArrayList<ShipAPI>()
    override fun advance(amount: Float, engine: CombatEngineAPI, weapon: WeaponAPI) {
        if (engine.isPaused) return
        val ship = weapon.ship ?: return
        if (!ship.isAlive || ship.isHulk || ship.isPiece) {
            return
        }

        //Glows off in refit screen
        if (ship.originalOwner == -1) {
            return
        }

        val sprite1 = Global.getSettings().getSprite("fx", "tahlan_altar_aura")
        val sprite2 = Global.getSettings().getSprite("fx", "tahlan_altar_aura")

        val loc = ship.location
        MagicRender.singleframe(
            sprite1,
            loc,
            Vector2f(EWARSuiteEffectScript.EFFECT_RANGE *2f, EWARSuiteEffectScript.EFFECT_RANGE *2f),
            rotation,
            Color(180,20,20,15),
            true
        )
        MagicRender.singleframe(
            sprite2,
            loc,
            Vector2f(EWARSuiteEffectScript.EFFECT_RANGE *2f, EWARSuiteEffectScript.EFFECT_RANGE *2f),
            -rotation,
            Color(180,20,20,15),
            true
        )

        // Spin it
        rotation += ROTATION_SPEED * amount
        if (rotation > 360f) {
            rotation -= 360f
        }
        for (target in CombatUtils.getShipsWithinRange(ship.location, EFFECT_RANGE)) {
            if (target.owner == ship.owner && !targetList.contains(target) && target.variant.hullMods.contains(DAEMONIC_HEART)) {
                targetList.add(target)
            }
        }
        val purgeList = ArrayList<ShipAPI>()
        for (target in targetList) {
            if (MathUtils.getDistance(target.location, ship.location) <= EFFECT_RANGE) {
                target.mutableStats.apply {
                    shieldDamageTakenMult.modifyMult(ALTAR_ID, DAMAGE_MULT)
                    armorDamageTakenMult.modifyMult(ALTAR_ID, DAMAGE_MULT)
                    hullDamageTakenMult.modifyMult(ALTAR_ID, DAMAGE_MULT)
                    damageToMissiles.modifyMult(ALTAR_ID, PDDMG_MULT)
                    damageToFighters.modifyMult(ALTAR_ID, PDDMG_MULT)
                }
            } else {
                target.mutableStats.apply {
                    shieldDamageTakenMult.unmodify(ALTAR_ID)
                    armorDamageTakenMult.unmodify(ALTAR_ID)
                    hullDamageTakenMult.unmodify(ALTAR_ID)
                    damageToMissiles.unmodify(ALTAR_ID)
                    damageToFighters.unmodify(ALTAR_ID)
                }
                purgeList.add(target)
            }
        }
        for (purge in purgeList) {
            targetList.remove(purge)
        }
    }

    companion object {
        private const val ALTAR_ID = "HelAltar_ID"
        const val EFFECT_RANGE = 2000f
        const val DAMAGE_MULT = 0.9f
        const val PDDMG_MULT = 1.5f

        // sprite path - necessary if loaded here and not in settings.json
        const val SPRITE_PATH = "graphics/tahlan/fx/tahlan_tempshield_ring_b.png"
        val COLOR = Color(186, 47, 52)
        const val ROTATION_SPEED = 5f
    }
}