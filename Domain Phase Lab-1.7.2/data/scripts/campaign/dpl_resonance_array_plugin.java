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

public class dpl_resonance_array_plugin implements EveryFrameScript{
	
	public static float UNSTABLE_DAYS_MIN = 200;
	public static float UNSTABLE_DAYS_MAX = 400;
	
	protected boolean done = false;
	protected float delay = 0.5f;
	protected float delay2 = 5f;
	
	protected SectorEntityToken gate;
	
	
	public dpl_resonance_array_plugin(SectorEntityToken gate) {
		this.gate = gate;
		delay = 3.2f; // plus approximately 2 seconds from how long plugin.jitter() takes to build up
		
	}

	public void advance(float amount) {
		if (done) return;

		delay -= amount;
		
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
		
		if (delay <= 0) {
			done = true;
		}
	}
	
	public boolean isDone() {
		return done;
	}

	public boolean runWhilePaused() {
		return false;
	}
}




