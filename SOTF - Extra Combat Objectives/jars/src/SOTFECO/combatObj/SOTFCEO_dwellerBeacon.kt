package SOTFECO.combatObj

import SOTFECO.SOFTECO_modPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.BattleObjectiveEffect.ShipStatusItem
import com.fs.starfarer.api.impl.campaign.AbyssalLightEntityPlugin
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial
import com.fs.starfarer.api.impl.combat.BaseBattleObjectiveEffect
import com.fs.starfarer.api.impl.combat.RiftCascadeEffect
import com.fs.starfarer.api.impl.combat.RiftLanceEffect
import com.fs.starfarer.api.impl.combat.dweller.DwellerShroud.SHROUD_COLOR
import com.fs.starfarer.api.impl.hullmods.ShardSpawner
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import data.scripts.combat.obj.SotfReinforcerEffect
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.util.MagicRender
import java.awt.Color

class SOTFCEO_dwellerBeacon: BaseBattleObjectiveEffect() {

    companion object {
        fun getRewardItemId(): String {
            if (SOFTECO_modPlugin.AOTDVaultsEnabled) return "aotd_shrouded_substrate"
            return Items.SHROUDED_SUBSTRATE
        }
    }

    var isDone: Boolean = false
    private var facing = 0f

    val coreGlow = Global.getSettings().getSprite("campaignEntities", "abyssal_light_glow")
    val glow = Global.getSettings().getSprite("campaignEntities", "abyssal_light_glow")

    var despawning = false
    val despawnInterval = IntervalUtil(1f, 1f) // seconds
    val soundInterval = IntervalUtil(20f, 40f) // seconds
    var phase: Float = 0f
    protected var frequencyMult: Float = 1f

    override fun init(engine: CombatEngineAPI?, objective: BattleObjectiveAPI?) {
        super.init(engine, objective)
        facing = Math.random().toFloat() * 360f
    }

    fun getGlowAlpha(): Float {
        var glowAlpha = 0f
        if (phase < 0.5f) glowAlpha = phase * 2f
        if (phase >= 0.5f) glowAlpha = (1f - (phase - 0.5f) * 2f)
        glowAlpha = 0.75f + glowAlpha * 0.35f

        if (glowAlpha < 0) glowAlpha = 0f
        if (glowAlpha > 1) glowAlpha = 1f
        return glowAlpha
    }

    override fun advance(amount: Float) {
        val engine = Global.getCombatEngine()
        if (isDone) return

        if (engine.isPaused) {
            return
        }
        render()


        phase += amount * AbyssalLightEntityPlugin.GLOW_FREQUENCY * frequencyMult
        while (phase > 1) phase--

        soundInterval.advance(amount)
        if (soundInterval.intervalElapsed()) {
            Global.getSoundPlayer().playSound("abyssal_light_random_sound", 0.5f, 1f, objective.location, Misc.ZERO)
        }

        if (objective.owner != 0 && objective.owner != 1) {
            // AI prioritises fighting over the fractal beacon
            if (!objective.customData.containsKey("sotf_didOrderFullAssault")) {
                engine.getFleetManager(1).getTaskManager(false)
                    .createAssignment(CombatAssignmentType.ASSAULT, objective, false)
                objective.customData["sotf_didOrderFullAssault"] = true
                for (assignment in engine.getFleetManager(1).getTaskManager(false).allAssignments) {
                    if (assignment.type == CombatAssignmentType.CAPTURE
                        || assignment.type == CombatAssignmentType.ASSAULT
                        && assignment.target !== objective
                    ) {
                        engine.getFleetManager(1).getTaskManager(false).removeAssignment(assignment)
                    }
                }
            }
            return
        }
        if (!despawning) {
            manifestDweller()
            // fade out the objective
            MagicRender.battlespace(
                objective.sprite,
                objective.location,
                Vector2f(0f, 0f),
                Vector2f(objective.sprite.width, objective.sprite.height),
                Vector2f(0f, 0f),
                facing - 90f,
                25f,
                Color.WHITE,
                true,
                0f,
                0f,
                0f,
                0f,
                0f,
                0f,
                0f,
                3f,
                CombatEngineLayers.BELOW_SHIPS_LAYER
            )
            // jitter
            MagicRender.battlespace(
                objective.sprite,
                objective.location,
                Vector2f(0f, 0f),
                Vector2f(objective.sprite.width, objective.sprite.height),
                Vector2f(0f, 0f),
                facing - 90f,
                25f,
                Misc.setAlpha(RiftCascadeEffect.STANDARD_RIFT_COLOR, 90),
                true,
                15f,
                5f,
                0f,
                0f,
                0f,
                0f,
                0f,
                3f,
                CombatEngineLayers.UNDER_SHIPS_LAYER
            )
            //Global.getCombatEngine().removeEntity(objective)

            //isDone = true
            despawning = true
            Global.getSoundPlayer().playSound("abyssal_light_expand_despawn_windup", 1f, 1f, objective.location, Misc.ZERO)
        } else {
            despawnInterval.advance(amount)
        }

        if (despawnInterval.intervalElapsed()) {
            engine.removeEntity(objective)
            isDone = true
        }
    }

    private fun render() {
        objective.sprite.alphaMult = 1f
        objective.sprite.angle = facing

        val engine = Global.getCombatEngine()

        var alphaMult: Float = engine.viewport.alphaMult
        if (alphaMult <= 0) return

        var f: Float = getDespawnProgress()

        if (f > 0.3f) {
            val fadeOut = (1f - f) / (1f - AbyssalLightEntityPlugin.DESPAWN_POOF_THRESHOLD)
            alphaMult *= fadeOut
        }

        /*val b: Float = AbyssalLightEntityPlugin.getPlayerProximityBasedBrightnessFactor(entity)
        alphaMult *= b*/


//		if (Misc.getDistanceToPlayerLY(entity) > 0.1f) {
//			Misc.fadeAndExpire(entity);
//			return;
//		}
//		if (!Global.getSector().isPaused()) {
//			System.out.println("Alpha: " + alphaMult);
//		}
        //val spec: CustomEntitySpecAPI = objective.getCustomEntitySpec() ?: return

        var w = 1024f
        var h = 1024f

        var cw = 256f
        var ch = 256f

        val loc: Vector2f = objective.location

        val glowAlpha: Float = getGlowAlpha()

        coreGlow.color = Color(200,200,255,255)
        glow.color = Color(200,200,255,255)

        //w = params.size
        //h = params.size

        var scale = 0.25f + alphaMult * 0.75f
        if (f > 0.3f) {
            scale *= 1.5f
        }

        w *= scale
        h *= scale
        cw *= scale
        ch *= scale


        //fringeScale = 1.5f;
        coreGlow.setAdditiveBlend()
        glow.setAdditiveBlend()

        coreGlow.setSize(cw, ch)
        glow.setSize(w, h)
        glow.alphaMult = alphaMult * glowAlpha * 0.5f
        coreGlow.alphaMult = alphaMult * glowAlpha * 0.5f
        //glow.renderAtCenter(loc.x, loc.y)
        MagicRender.singleframe(
            coreGlow,
            loc,
            Vector2f(cw, ch),
            0f,
            coreGlow.color,
            true,
            CombatEngineLayers.BELOW_SHIPS_LAYER,
        )
        MagicRender.singleframe(
            glow,
            loc,
            Vector2f(w, h),
            0f,
            glow.color,
            true,
            CombatEngineLayers.BELOW_SHIPS_LAYER,
        )

        

        /*if (layer == CampaignEngineLayers.TERRAIN_8) {
            //float flicker = getFlickerBasedMult();
            glow.setAlphaMult(alphaMult * glowAlpha * 0.5f)
            glow.setSize(w * scale * fringeScale, h * scale * fringeScale)

            glow.renderAtCenter(loc.x, loc.y)
        }

        if (layer == CampaignEngineLayers.STATIONS) {
            if (f > 0f) {
                var extra = 1f + f * 1.33f
                if (f > AbyssalLightEntityPlugin.DESPAWN_POOF_THRESHOLD) extra = 0f
                scale *= extra
            }
            var flicker: Float = getFlickerBasedMult()
            for (i in 0..4) {
                if (i != 0) flicker = 1f

                w *= 0.3f
                h *= 0.3f
                glow.setSize(w * scale, h * scale)
                glow.setAlphaMult(alphaMult * glowAlpha * 0.67f * flicker)
                glow.renderAtCenter(loc.x, loc.y)
            }
        }*/
    }

    fun getDespawnProgress(): Float {
        return despawnInterval.elapsed / despawnInterval.intervalDuration
    }

    override fun getLongDescription(): String {
        val min = Global.getSettings().getFloat("minFractionOfBattleSizeForSmallerSide")
        val total = Global.getSettings().battleSize
        Math.round(total * (1f - min))

        return String.format(
            """
            - unknown light source in local hyperspace volume
            - unknown emanations from within, unknown purpose
            - no detectable interface, expect lengthy capture time
            
            """.trimIndent()
        )
    }

    override fun getStatusItemsFor(ship: ShipAPI?): List<ShipStatusItem>? {
        return null
    }

    private fun manifestDweller() {
        var variant: String? = null
        val faction = pickFaction()
        val name = faction!!.pickRandomShipName()
        variant = pickShip()
        // shut it all down if we don't end up with a variant to spawn
        if (variant == null) {
            return
        }
        //emptyFleet.getInflater().setQuality(0.5f);
        //if (emptyFleet.getInflater() instanceof DefaultFleetInflater) {
        //	DefaultFleetInflater dfi = (DefaultFleetInflater) emptyFleet.getInflater();
        //	((DefaultFleetInflaterParams) dfi.getParams()).allWeapons = true;
        //}
        //emptyFleet.inflateIfNeeded();
        val fleetManager = engine.getFleetManager(objective.owner)
        var messageColor = Misc.getPositiveHighlightColor()
        var friendlyOrHostile = "friendly"
        if (objective.owner == 1) {
            messageColor = Misc.getNegativeHighlightColor()
            friendlyOrHostile = "hostile"
        }

        val shipsToCheck = fleetManager.deployedCopy
        shipsToCheck.addAll(fleetManager.reservesCopy)
        /*for (member in shipsToCheck) {
            for (hullmod in SotfOmegaReinforcerEffect.MUSIC_HULLMODS) {
                if (member.variant.hasHullMod(hullmod)) {
                    engine.combatUI.addMessage(
                        0,
                        objective,
                        messageColor,
                        objective.displayName,
                        Misc.getTextColor(),
                        " has vanished...?"
                    )
                    return
                }
            }
        }*/

        val wasSuppressed = fleetManager.isSuppressDeploymentMessages
        fleetManager.isSuppressDeploymentMessages = true

        val emptyFleet = Global.getFactory().createEmptyFleet(faction.id, "Reinforcements", true)
        val member = emptyFleet.fleetData.addFleetMember(variant)
        member.variant.addTag(Tags.SHIP_LIMITED_TOOLTIP)
        emptyFleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY] = true
        member.shipName = name
        //member.setAlly(true);
        member.owner = objective.owner
        /*member.captain = Misc.getAICoreOfficerPlugin(Commodities.OMEGA_CORE)
            .createPerson(Commodities.OMEGA_CORE, Factions.OMEGA, Random())*/

        val ship = engine.getFleetManager(objective.owner)
            .spawnFleetMember(member, Misc.getPointAtRadius(objective.location, 750f), 0f, 0f)
        engine.combatUI.addMessage(
            0,
            ship,
            Misc.getTextColor(),
            "Unidentified ",
            messageColor,
            friendlyOrHostile,
            Misc.getTextColor(),
            " entity has manifested"
        )
        engine.combatUI.addMessage(
            0,
            objective,
            messageColor,
            objective.displayName,
            Misc.getTextColor(),
            " has vanished"
        )
        fleetManager.isSuppressDeploymentMessages = wasSuppressed

        ship.variant.addTag(SotfReinforcerEffect.REINFORCEMENT_SHIP_KEY)
        ship.hullSpec.addTag("no_combat_chatter")
        if (ship.owner == 0) {
            ship.isAlly = true
        }
        //ship.getMutableStats().getSuppliesToRecover().modifyMult("sotf_reinforcementship", 0f);
        ship.mutableStats.dynamic.getMod(Stats.DEPLOYMENT_POINTS_MOD)
            .modifyMult(SotfReinforcerEffect.REINFORCEMENT_SHIP_KEY, 0.01f)
        ship.fleetMember.stats.dynamic.getMod(Stats.DEPLOYMENT_POINTS_MOD)
            .modifyMult(SotfReinforcerEffect.REINFORCEMENT_SHIP_KEY, 0.01f)

        if (Global.getSettings().modManager.isModEnabled("automatic-orders")) {
            ship.variant.addMod("automatic_orders_no_retreat")
        }

        if (objective.owner == 1 && MathUtils.getRandom().nextFloat() <= 0.3f) {
            var mainEnemyFleet: CampaignFleetAPI? = null
            for (enemyMember in engine.getFleetManager(1).deployedCopy) {
                if (enemyMember.fleetData != null) {
                    if (enemyMember.fleetData.fleet != null) {
                        // Fleet must actually exist in our current location - otherwise, might grab the shard's fleet (which we can't salvage from)
                        if (enemyMember.fleetData.fleet.containingLocation != null) {
                            mainEnemyFleet = enemyMember.fleetData.fleet
                        }
                    }
                }
            }
            if (mainEnemyFleet != null) {
                Global.getLogger(this.javaClass)
                    .info("Successfully found an enemy fleet to add Substrate to cargo of: " + mainEnemyFleet.fullName)
                val extraLoot = Global.getFactory().createCargo(true)
                extraLoot.addSpecial(SpecialItemData(getRewardItemId(), null), 1f)
                BaseSalvageSpecial.addExtraSalvage(mainEnemyFleet, extraLoot)
                //mainEnemyFleet.getCargo().addWeapons(picked.getId(), 1);
                Global.getLogger(this.javaClass)
                    .info("Successfully added Substrate to cargo of " + mainEnemyFleet.fullName)

            }
        }

        // dark blue fog like a dying Omega ship
        engine.addPlugin(SotfCEODwellerFadeInPlugin(ship, 4f, 180f * objective.owner))
    }

    class SotfCEODwellerFadeInPlugin(var ship: ShipAPI, var fadeInTime: Float, var angle: Float) :
        BaseEveryFrameCombatPlugin() {
        var elapsed: Float = 0f

        var interval: IntervalUtil = IntervalUtil(0.075f, 0.125f)


        override fun advance(amount: Float, events: List<InputEventAPI>) {
            if (Global.getCombatEngine().isPaused) return

            elapsed += amount

            val engine = Global.getCombatEngine()

            var progress = (elapsed) / fadeInTime
            if (progress > 1f) progress = 1f

            ship.alphaMult = progress

            if (progress < 0.5f) {
                ship.blockCommandForOneFrame(ShipCommand.ACCELERATE)
                ship.blockCommandForOneFrame(ShipCommand.TURN_LEFT)
                ship.blockCommandForOneFrame(ShipCommand.TURN_RIGHT)
                ship.blockCommandForOneFrame(ShipCommand.STRAFE_LEFT)
                ship.blockCommandForOneFrame(ShipCommand.STRAFE_RIGHT)
            }

            ship.blockCommandForOneFrame(ShipCommand.USE_SYSTEM)
            ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK)
            ship.blockCommandForOneFrame(ShipCommand.FIRE)
            ship.blockCommandForOneFrame(ShipCommand.PULL_BACK_FIGHTERS)
            ship.blockCommandForOneFrame(ShipCommand.VENT_FLUX)
            ship.isHoldFireOneFrame = true
            ship.isHoldFire = true


            ship.collisionClass = CollisionClass.NONE
            ship.mutableStats.hullDamageTakenMult.modifyMult("ShardSpawnerInvuln", 0f)
            if (progress < 0.5f) {
                ship.velocity.set(Vector2f())
            } else if (progress > 0.75f) {
                ship.collisionClass = CollisionClass.SHIP
                ship.mutableStats.hullDamageTakenMult.unmodifyMult("ShardSpawnerInvuln")
            }


            //					Vector2f dir = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(source.getLocation(), ship.getLocation()));
//					dir.scale(amount * 50f * progress);
//					Vector2f.add(ship.getLocation(), dir, ship.getLocation());
            var jitterLevel = progress
            if (jitterLevel < 0.5f) {
                jitterLevel *= 2f
            } else {
                jitterLevel = (1f - jitterLevel) * 2f
            }

            val jitterRange = 1f - progress
            val maxRangeBonus = 50f
            val jitterRangeBonus = jitterRange * maxRangeBonus
            var c = ShardSpawner.JITTER_COLOR

            ship.setJitter(this, c, jitterLevel, 25, 0f, jitterRangeBonus)

            interval.advance(amount)
            if (interval.intervalElapsed() && progress < 0.8f) {
                c = RiftLanceEffect.getColorForDarkening(SHROUD_COLOR)
                val baseDuration = 2f
                val vel = Vector2f(ship.velocity)
                val size = ship.collisionRadius * 0.35f
                for (i in 0..2) {
                    var point = Vector2f(ship.location)
                    point = Misc.getPointWithinRadiusUniform(point, ship.collisionRadius * 0.5f, Misc.random)
                    var dur = baseDuration + baseDuration * Math.random().toFloat()
                    val nSize = size
                    val pt = Misc.getPointWithinRadius(point, nSize * 0.5f)
                    val v = Misc.getUnitVectorAtDegreeAngle(Math.random().toFloat() * 360f)
                    v.scale(nSize + nSize * Math.random().toFloat() * 0.5f)
                    v.scale(0.2f)
                    Vector2f.add(vel, v, v)

                    val maxSpeed = nSize * 1.5f * 0.2f
                    val minSpeed = nSize * 1f * 0.2f
                    val overMin = v.length() - minSpeed
                    if (overMin > 0) {
                        var durMult = 1f - overMin / (maxSpeed - minSpeed)
                        if (durMult < 0.1f) durMult = 0.1f
                        dur *= 0.5f + 0.5f * durMult
                    }
                    engine.addNegativeNebulaParticle(
                        pt, v, nSize * 1f, 2f,
                        0.5f / dur, 0f, dur, c
                    )
                }
            }

            if (elapsed > fadeInTime) {
                ship.alphaMult = 1f
                ship.isHoldFire = false
                ship.collisionClass = CollisionClass.SHIP
                ship.mutableStats.hullDamageTakenMult.unmodifyMult("ShardSpawnerInvuln")
                engine.removePlugin(this)
            }
        }
    }

    private fun pickFaction(): FactionAPI? {
        var faction: FactionAPI? = null
        faction = if (Global.getSector() != null) {
            Global.getSector().getFaction(Factions.DWELLER)
        } else {
            Global.getSettings().createBaseFaction(Factions.DWELLER)
        }
        return faction
    }

    private fun pickShip(): String {
        val post = WeightedRandomPicker<String>()
        post.add("shrouded_tendril_Roiling", 2f)
        return post.pick()
    }
}