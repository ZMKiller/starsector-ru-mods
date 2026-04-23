package org.amazigh.foundry.scripts;

import java.awt.Color;
import java.util.List;

import org.amazigh.foundry.scripts.ASF_PainterBeamEffect.ASF_artyTargetListener;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class ASF_narcOnHitEffect implements OnHitEffectPlugin {
	
	public static final Color ARC_COLOR_O = new Color(64,70,216,255);
	public static final Color ARC_COLOR_I = new Color(249,243,253,255);
	
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		if (target != null) {
			if (target instanceof ShipAPI) {
				
				float dam = projectile.getDamageAmount() * 0.05f; //20
				float emp = projectile.getEmpAmount() * 2f; //200
				ShipAPI targetShip = (ShipAPI) target;
				
				// doing the charge stuff
				if (!targetShip.hasListenerOfClass(ASF_artyTargetListener.class)) {
					targetShip.addListener(new ASF_artyTargetListener(targetShip));
				}
				
				List<ASF_artyTargetListener> listeners = targetShip.getListeners(ASF_artyTargetListener.class);
				if (listeners.isEmpty()) return; // ??? (idk either alex, but sanity checks are a real one)
				
				ASF_artyTargetListener listener = listeners.get(0);
				
				for (int i=0; i < 3; i++) {
					if (listener.recentHits.getItems().size() >= 8) {
						engine.spawnEmpArc(
								projectile.getSource(), point, target, target,
								DamageType.ENERGY, 
								dam, // damage
								emp, // emp 
								1000f, // max range 
								"A_S-F_quiet_emp_impact",
								20f,
								ARC_COLOR_O,
								ARC_COLOR_I);
						
					} else {
						String uniqueHash = (String.valueOf(projectile.hashCode()) + String.valueOf((int) engine.getTotalElapsedTime(true)) + i);
						
						// generating a unique identifying int for each damage instance caused by the arc
						listener.notifyHit(uniqueHash);
					}
				}
			}
		}
		
	}
}