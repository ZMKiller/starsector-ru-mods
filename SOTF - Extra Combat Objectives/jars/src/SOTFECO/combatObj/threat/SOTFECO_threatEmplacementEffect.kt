package SOTFECO.combatObj.threat

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.combat.BattleObjectiveAPI
import com.fs.starfarer.api.combat.BattleObjectiveEffect.ShipStatusItem
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Skills
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.combat.BaseBattleObjectiveEffect
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import data.scripts.combat.obj.SotfEmplacementEffect
import org.json.JSONException
import org.json.JSONObject
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.util.MagicRender
import java.awt.Color
import java.io.IOException

class SOTFECO_threatEmplacementEffect: SOTFCEO_threatSwarm() {

    companion object {
        const val VARIANT_CSV = "data/config/sotf/emplacement_variants.csv"
        const val RESPAWN_KEY = "sotf_emplacement_respawn_key"
        const val respawn_timer = 120f
    }

    class SOTFECOEmplacementRespawnTimer {
        var interval: IntervalUtil =
            IntervalUtil(respawn_timer, respawn_timer)
    }

    private var member: FleetMemberAPI? = null
    private var ship: ShipAPI? = null
    private var officer: PersonAPI? = null
    private var loc: Vector2f? = null
    private var type: String? = null
    private var radiusPad = 0f

    private var first_spawned = false
    private var facing = 0f

    override fun init(engine: CombatEngineAPI?, objective: BattleObjectiveAPI?) {
        super.init(engine, objective)

        /*officer = Global.getFactory().createPerson()
        officer!!.aiCoreId = Commodities.GAMMA_CORE
        officer!!.name = FullName(
            Global.getSettings().getCommoditySpec(Commodities.GAMMA_CORE).name,
            "",
            FullName.Gender.ANY
        )
        officer!!.portraitSprite = "graphics/portraits/portrait_ai1b.png"
        officer!!.stats.level = 4
        officer!!.stats.setSkillLevel(Skills.GUNNERY_IMPLANTS, 2f)
        officer!!.stats.setSkillLevel(Skills.TARGET_ANALYSIS, 2f)
        officer!!.stats.setSkillLevel(Skills.POINT_DEFENSE, 2f)
        officer!!.stats.setSkillLevel(Skills.STRIKE_COMMANDER, 2f) // deprecate THIS, Alex*/
        // pick a variant and location
        try {
            pickVariant()
        } catch (ex: IOException) {
            // oopsy daisy
        } catch (ex: JSONException) {
        }
        facing = Math.random().toFloat() * 360f
        loc = MathUtils.getRandomPointOnCircumference(
            objective?.location,
            500 + (150 * Math.random().toFloat()) + radiusPad
        )
    }

    override fun advance(amount: Float) {
        if (engine.isPaused) {
            return
        }
        var render_blueprint = true
        var blueprint_alpha = 200f
        val engine = Global.getCombatEngine()
        val hullspec = Global.getSettings().getVariant(type).hullSpec
        val hullsprite = Global.getSettings().getSprite(hullspec.spriteName)
        var blueprintcolor = getBlueprintColor()

        val key = RESPAWN_KEY + "_" + objective.toString()
        var data = engine.customData.get(key) as SOTFECOEmplacementRespawnTimer?
        if (data == null) {
            data = SOTFECOEmplacementRespawnTimer()
            engine.customData.put(key, data)
        }

        if (ship != null) {
            if ((objective.owner == 0 || objective.owner == 1) && !ship!!.isAlive && !engine.getFleetManager(
                    objective.owner
                ).getTaskManager(false).isInFullRetreat
            ) {
                data.interval.advance(amount)
                blueprint_alpha = 200f * (data.interval.elapsed / respawn_timer)
                if (data.interval.intervalElapsed()) {
                    replaceTurret()
                    var color = Misc.getPositiveHighlightColor()
                    if (objective.owner == 1) {
                        color = Misc.getNegativeHighlightColor()
                    }
                    Global.getCombatEngine().combatUI.addMessage(
                        1,
                        //ship,
                        color,
                        objective.displayName,
                        Misc.getTextColor(),
                        " has completed reconstitution"
                    )
                }
            } else {
                data.interval.elapsed = 0f
            }

            // change to the objective's side
            if (ship!!.isAlive) {
                render_blueprint = false
                val player_full_retreat = engine.getFleetManager(0).getTaskManager(false).isInFullRetreat
                val enemy_full_retreat = engine.getFleetManager(1).getTaskManager(true).isInFullRetreat
                var no_player_ships = true
                var no_enemy_ships = true

                for (ship in engine.ships) {
                    if (ship.owner == 0 && !ship.hullSpec
                            .hasTag("sotf_reinforcementship") && !ship.hullSpec
                            .hasTag("sotf_empl") && !ship.isHulk && !ship.isFighter
                    ) {
                        no_player_ships = false
                    }
                    if (ship.owner == 1 && !ship.hullSpec
                            .hasTag("sotf_reinforcementship") && !ship.hullSpec
                            .hasTag("sotf_empl") && !ship.isHulk && !ship.isFighter
                    ) {
                        no_enemy_ships = false
                    }
                }

                if (no_enemy_ships || player_full_retreat || enemy_full_retreat) {
                    engine.applyDamage(
                        ship,
                        ship!!.location,
                        1000000f,
                        DamageType.HIGH_EXPLOSIVE,
                        0f,
                        true,
                        false,
                        ship
                    )
                    for (module in ship!!.childModulesCopy) {
                        engine.applyDamage(
                            module,
                            module.location,
                            1000000f,
                            DamageType.HIGH_EXPLOSIVE,
                            0f,
                            true,
                            false,
                            module
                        )
                    }
                    return
                }

                facing = ship!!.facing
                if (objective.owner == 0 && ship!!.owner == 1) {
                    //setSide(0);
                    engine.removeEntity(ship)
                    replaceTurret()
                }
                if (objective.owner == 1 && ship!!.owner == 0) {
                    //setSide(1);
//					for (CombatFleetManagerAPI.AssignmentInfo assignment : engine.getFleetManager(0).getTaskManager(false).getAllAssignments().) {
//						if (assignment.getTarget().equals(ship)) {
//							engine.getFleetManager(0).getTaskManager(false).removeAssignment(assignment);
//						}
//					}
                    engine.removeEntity(ship)
                    replaceTurret()
                }
            }
        } else if ((objective.owner == 0 || objective.owner == 1) && !first_spawned) {
            first_spawned = true
            replaceTurret()
        }

        blueprintcolor = Misc.setAlpha(blueprintcolor, blueprint_alpha.toInt())
        if (render_blueprint) {
            MagicRender.singleframe(
                hullsprite,
                loc,
                Vector2f(hullsprite.width, hullsprite.height),
                facing - 90f,
                blueprintcolor,
                true
            )
        }
    }

    override fun captured(amount: Float) {
        return
    }


    override fun getLongDescription(): String {
        val min = Global.getSettings().getFloat("minFractionOfBattleSizeForSmallerSide")
        val total = Global.getSettings().battleSize
        val maxPoints = Math.round(total * (1f - min))
        var variant_string: String? = null
        if (type != null) {
            val variant = Global.getSettings().getVariant(type)
            variant_string =
                "type: " + variant.hullSpec.designation + ", " + variant.displayName + " variant"
        }
        return String.format(
            variant_string + "\n" +
                    "attacks enemies of objective holder\n" +
                    "reconstructed " + respawn_timer.toInt() + " seconds after destruction\n\n" +
                    "+%d bonus deployment points\n" +
                    "up to a maximum of " + maxPoints + " points",
            getBonusDeploymentPoints()
        )
    }

    override fun getStatusItemsFor(ship: ShipAPI?): MutableList<ShipStatusItem?>? {
        return null
    }

    @Throws(IOException::class, JSONException::class)
    private fun pickVariant() {
        val post = WeightedRandomPicker<JSONObject>()

        val variants = Global.getSettings().getMergedSpreadsheetDataForMod("id", VARIANT_CSV, "frontiersecrets")
        for (i in 0..<variants.length()) {
            val row = variants.getJSONObject(i)

            if (row.getString("obj_id") == objective.type) {
                post.add(row, row.getDouble("weight").toFloat())
            }
        }
        val pickedRow = post.pick()
        type = pickedRow.getString("id")
        radiusPad = pickedRow.getDouble("extra_distance").toFloat()
    }

    private fun setSide(side: Int) {
        ship!!.owner = side
        ship!!.originalOwner = side
        ship!!.fleetMember.owner = side
        ship!!.shipTarget = null
        ship!!.shipAI.cancelCurrentManeuver()
        ship!!.shipAI.forceCircumstanceEvaluation()
        for (wing in ship!!.allWings) {
            wing.wingOwner = side
            for (fighter in wing.wingMembers) {
                fighter.owner = side
            }
        }
        if (side == 0) {
            ship!!.isAlly = true
            ship!!.fleetMember.isAlly = true
        } else {
            ship!!.isAlly = false
            ship!!.fleetMember.isAlly = false
        }
        for (module in ship!!.childModulesCopy) {
            module.owner = side
            module.originalOwner = side
            module.isAlly = false
            module.shipTarget = null
            if (side == 0) {
                module.isAlly = true
            } else {
                module.isAlly = false
            }
            for (wing in module.allWings) {
                wing.wingOwner = side
                for (fighter in wing.wingMembers) {
                    fighter.owner = side
                }
            }
        }
    }

    private fun replaceTurret() {
        engine.getFleetManager(objective.owner).isSuppressDeploymentMessages = true
        // kill old turret's fighters
        if (ship != null) {
            for (wing in ship!!.allWings) {
                for (fighter in wing.wingMembers) {
                    engine.applyDamage(
                        fighter, fighter.location,
                        1000000f, DamageType.HIGH_EXPLOSIVE, 0f,
                        true, false,
                        null
                    )
                }
            }
        }
        member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, type)
        member!!.shipName = ""
        member!!.captain = officer
        member!!.owner = objective.owner
        ship = engine.getFleetManager(objective.owner).spawnFleetMember(member, loc, facing, 0f)
        ship!!.hullSpec.addTag("sotf_empl")
        ship!!.hullSpec.addTag("no_combat_chatter")
        ship!!.isStation = true
        ship!!.fixedLocation = ship!!.location
        ship!!.setMediumDHullOverlay()
        ship!!.mutableStats.breakProb.setBaseValue(1f)
        ship!!.mutableStats.hullCombatRepairRatePercentPerSecond.modifyFlat("sotf_empl", 0.5f)
        ship!!.mutableStats.maxCombatHullRepairFraction.modifyFlat("sotf_empl", 1f)
        ship!!.mutableStats.dynamic.getMod(Stats.SHIP_OBJECTIVE_CAP_RANGE_MOD).modifyFlat("sotf_empl", -5000f)
        ship!!.variant.addTag(Tags.VARIANT_DO_NOT_DROP_AI_CORE_FROM_CAPTAIN)
        if (objective.owner == 0) {
            ship!!.isAlly = true
            member!!.isAlly = true
        }
        for (weapon in ship!!.allWeapons) {
            weapon.disable(false)
        }
        for (module in ship!!.childModulesCopy) {
            for (weapon in module.allWeapons) {
                weapon.disable(false)
            }
        }
        engine.getFleetManager(objective.owner).isSuppressDeploymentMessages = false
        // Delete the emplacement from the owner's deployed ship list
        //com.fs.starfarer.combat.CombatFleetManager realMan = (com.fs.starfarer.combat.CombatFleetManager) engine.getFleetManager(objective.getOwner());
    }

    private fun getBlueprintColor(): Color {
        var color = Color.CYAN
        if (objective.owner == 0) {
            color = Misc.getHighlightColor()
        } else if (objective.owner == 1) {
            color = Misc.getNegativeHighlightColor()
        }
        return color
    }

}