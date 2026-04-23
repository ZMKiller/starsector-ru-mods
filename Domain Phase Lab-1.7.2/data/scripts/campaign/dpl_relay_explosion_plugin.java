//By VladimirVV. Implements the active skill that decreases enemy CR.
package data.scripts.campaign;

import java.awt.Color;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ExplosionEntityPlugin.ExplosionFleetDamage;
import com.fs.starfarer.api.impl.campaign.ExplosionEntityPlugin.ExplosionParams;
import com.fs.starfarer.api.impl.campaign.GateEntityPlugin;
import com.fs.starfarer.api.impl.campaign.JumpPointInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.JitterUtil;
import com.fs.starfarer.api.util.Misc;

public class dpl_relay_explosion_plugin implements EveryFrameScript, FleetEventListener {
	
	protected boolean done = false;
	protected boolean playedWindup = false;
	protected SectorEntityToken explosion = null;
	protected float delay = 0.5f;
	protected float delay2 = 5f;
	
	protected SectorEntityToken gate;
	
	
	public dpl_relay_explosion_plugin(SectorEntityToken gate) {
		this.gate = gate;
		
		delay = 3.2f; // plus approximately 2 seconds from how long plugin.jitter() takes to build up
		
	}

	public void advance(float amount) {
		if (done) return;
		
		if (!playedWindup) {
			if (gate.isInCurrentLocation()) {
				Global.getSoundPlayer().playSound("gate_explosion_windup", 1f, 1f, gate.getLocation(), Misc.ZERO);
			}
			playedWindup = true;
		}
		
		if (gate.hasTag("dpl_HELA_device")) {
			dpl_HELAEntityPlugin plugin = (dpl_HELAEntityPlugin) gate.getCustomPlugin();
			plugin.jitter();
			
			if (plugin.getJitterLevel() > 0.9f) {
				delay -= amount;
			}
		} else {
			delay -= amount;
		}
		
		LocationAPI cl = gate.getContainingLocation();
		Vector2f loc = gate.getLocation();
		Vector2f vel = gate.getVelocity();
		
		if (delay > 0) {
			Vector2f finalVel = new Vector2f();
			finalVel.x = vel.x + (float) (2*Math.random()-1)*50;
			finalVel.y = vel.y + (float) (2*Math.random()-1)*50;
			
			Vector2f finalLoc = new Vector2f();
			finalLoc.x = loc.x + (float) (2*Math.random()-1)*50;
			finalLoc.y = loc.y + (float) (2*Math.random()-1)*50;
			
			float size = 3f + (float) Math.random() * 5f;
			size *= 3f;
					
			cl.addParticle(finalLoc, finalVel, size, 0.4f, 0f, 1f, new Color(255,100,255,175));
			cl.addParticle(finalLoc, finalVel, size*0.25f, 0.4f, 0f, 1f, new Color(255,100,255,175));
			cl.addParticle(finalLoc, finalVel, size*0.15f, 1f, 0f, 1f, new Color(255,100,255,175));
		}
		
		if (delay <= 0 && explosion == null) {
			//Misc.fadeAndExpire(gate);

			//LocationAPI cl = gate.getContainingLocation();
			//Vector2f loc = gate.getLocation();
			//Vector2f vel = gate.getVelocity();
			
			float size = gate.getRadius() + 800f;
			Color color = new Color(150, 100, 255, 255);
			//color = new Color(255, 155, 255);
			//ExplosionParams params = new ExplosionParams(color, cl, loc, size, 1f);
			ExplosionParams params = new ExplosionParams(color, cl, loc, size, 2f);
			params.damage = ExplosionFleetDamage.NONE;
			
			explosion = cl.addCustomEntity(Misc.genUID(), "Resonance Explosion", 
											Entities.EXPLOSION, Factions.NEUTRAL, params);
			explosion.setLocation(loc.x, loc.y);
			if (!(gate instanceof CampaignFleetAPI)) {
				SectorEntityToken built = cl.addCustomEntity(null,
						 null,
				Entities.STABLE_LOCATION, // type of object, defined in custom_entities.json
				Factions.NEUTRAL); // faction
				if (gate.getOrbit() != null) {
				built.setOrbit(gate.getOrbit().makeCopy());
				}
				cl.removeEntity(gate);
				updateOrbitingEntities(cl, gate, built);
			} else {
				CampaignFleetAPI fleet = (CampaignFleetAPI) gate;
				if (fleet.isPlayerFleet()) {
					List<FleetMemberAPI> allMembers = fleet.getFleetData().getMembersListCopy();
					for (FleetMemberAPI ship : allMembers) {
						ship.getRepairTracker().setSuspendRepairs(true);
						ship.getRepairTracker().applyCREvent(-1, "Phase Resonance");
					}
				} else {
					List<FleetMemberAPI> allMembers = fleet.getFleetData().getMembersListCopy();
					for (FleetMemberAPI ship : allMembers) {
						ship.getRepairTracker().setSuspendRepairs(true);
						ship.getRepairTracker().applyCREvent(-1, "Phase Resonance");
						ship.getVariant().addPermaMod("dpl_burnt_electronics");
						if (ship.getVariant().hasHullMod("shard_spawner")) {
							ship.getVariant().addSuppressedMod("shard_spawner");
						}
						if (ship.isStation()) {
							fleet.removeFleetMemberWithDestructionFlash(ship);
						}
					}
					fleet.addEventListener(this);
					fleet.addTag("dpl_electronics_burnt");
				}
			}
		}
		
		if (explosion != null) {
			delay2 -= amount;
			if (delay2 <= 0) {
				done = true;
				
				StarSystemAPI system = gate.getStarSystem();
				if (system != null) {
					if (gate instanceof CampaignFleetAPI) {
						CampaignFleetAPI fleet = (CampaignFleetAPI) gate;
						List<FleetMemberAPI> allMembers = fleet.getFleetData().getMembersListCopy();
						for (FleetMemberAPI ship : allMembers) {
							ship.getRepairTracker().setSuspendRepairs(false);
						}
					}
				}
			}
		}
	}
	
	public void updateOrbitingEntities(LocationAPI loc, SectorEntityToken prev, SectorEntityToken built) {
		if (loc == null) return;
		for (SectorEntityToken other : loc.getAllEntities()) {
			if (other == prev) continue;
			if (other.getOrbit() == null) continue;
			if (other.getOrbitFocus() == prev) {
				other.setOrbitFocus(built);
			}
		}
	}
	
	public boolean isDone() {
		return done;
	}

	public boolean runWhilePaused() {
		return false;
	}

	@Override
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		if (!(battle.isPlayerInvolved())) {
			if (fleet.hasTag("dpl_electronics_burnt")) {
				List<FleetMemberAPI> allMembers = fleet.getFleetData().getMembersListCopy();
				for (FleetMemberAPI ship : allMembers) {
					fleet.removeFleetMemberWithDestructionFlash(ship);
				}
			}
		}
	}
}




