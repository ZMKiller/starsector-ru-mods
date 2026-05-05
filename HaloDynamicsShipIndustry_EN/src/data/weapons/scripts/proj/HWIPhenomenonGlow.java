package data.weapons.scripts.proj;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.RealityDisruptorChargeGlow;
import com.fs.starfarer.api.impl.combat.RiftLanceEffect;
import com.fs.starfarer.api.util.Misc;

public class HWIPhenomenonGlow extends RealityDisruptorChargeGlow{
    public static Color RIFT_COLOR = new Color(100, 60, 255, 255);
	public static Color UNDERCOLOR = new Color(50, 50, 225, 100);
	public static Color NEBULA_SOURCE_COLOR = new Color(80, 105, 250, 75);

    public HWIPhenomenonGlow(WeaponAPI weapon) {
        super(weapon);
    }
    @Override
    public void spawnArc() {
		CombatEngineAPI engine = Global.getCombatEngine();
		
		float emp = proj.getEmpAmount();
		float dam = proj.getDamageAmount();
	
		CombatEntityAPI target = findTarget(proj, weapon, engine);
		float thickness = 20f;
		float coreWidthMult = 0.67f;
		Color color = NEBULA_SOURCE_COLOR;
		//color = new Color(255,100,100,255);
		if (target != null) {
			EmpArcEntityAPI arc = engine.spawnEmpArc(proj.getSource(), proj.getLocation(), null,
					   target,
					   DamageType.ENERGY, 
					   dam,
					   emp, // emp 
					   100000f, // max range 
					   "realitydisruptor_emp_impact",
					   thickness, // thickness
					   color,
					   new Color(255,255,255,255)
					   );
			arc.setCoreWidthOverride(thickness * coreWidthMult);
			
			spawnEMPParticles(EMPArcHitType.SOURCE, proj.getLocation(), null);
			spawnEMPParticles(EMPArcHitType.DEST, arc.getTargetLocation(), target);
		
		} else {
			Vector2f from = new Vector2f(proj.getLocation());
			Vector2f to = pickNoTargetDest(proj, weapon, engine);
			EmpArcEntityAPI arc = engine.spawnEmpArcVisual(from, null, to, null, thickness, color, Color.white);
			arc.setCoreWidthOverride(thickness * coreWidthMult);
			Global.getSoundPlayer().playSound("realitydisruptor_emp_impact", 1f, 1f, to, new Vector2f());
			
			spawnEMPParticles(EMPArcHitType.SOURCE, from, null);
			spawnEMPParticles(EMPArcHitType.DEST_NO_TARGET, to, null);
		}
	}

    @Override
    public void addChargingParticles(WeaponAPI weapon) {
		//CombatEngineAPI engine = Global.getCombatEngine();
		Color color = RiftLanceEffect.getColorForDarkening(NEBULA_SOURCE_COLOR);
		
//		float b = 1f;
//		color = Misc.scaleAlpha(color, b);
		//undercolor = Misc.scaleAlpha(undercolor, b);
		
		float size = 50f;
		float underSize = 75f;
		//underSize = 100f;
		
		float in = 0.25f;
		float out = 0.75f;
		
		out *= 3f;
		
		float velMult = 0.2f;
		
		if (isWeaponCharging(weapon)) {
			size *= 0.25f + weapon.getChargeLevel() * 0.75f;
		}
		
		addParticle(size, in, out, 1f, size * 0.5f * velMult, 0f, color);
		randomizePrevParticleLocation(size * 0.33f);
		
		if (proj != null) {
			Vector2f dir = Misc.getUnitVectorAtDegreeAngle(proj.getFacing() + 180f);
			//size = 40f;
			if (proj.getElapsed() > 0.2f) {
				addParticle(size, in, out, 1.5f, size * 0.5f * velMult, 0f, color);
				Vector2f offset = new Vector2f(dir);
				offset.scale(size * 0.6f + (float) Math.random() * 0.2f);
				Vector2f.add(prev.offset, offset, prev.offset);
			}
			if (proj.getElapsed() > 0.4f) {
				addParticle(size * 1f, in, out, 1.3f, size * 0.5f * velMult, 0f, color);
				Vector2f offset = new Vector2f(dir);
				offset.scale(size * 1.2f + (float) Math.random() * 0.2f);
				Vector2f.add(prev.offset, offset, prev.offset);
			}
			if (proj.getElapsed() > 0.6f) {
				addParticle(size * .8f, in, out, 1.1f, size * 0.5f * velMult, 0f, color);
				Vector2f offset = new Vector2f(dir);
				offset.scale(size * 1.6f + (float) Math.random() * 0.2f);
				Vector2f.add(prev.offset, offset, prev.offset);
			}
			
			if (proj.getElapsed() > 0.8f) {
				addParticle(size * .8f, in, out, 1.1f, size * 0.5f * velMult, 0f, color);
				Vector2f offset = new Vector2f(dir);
				offset.scale(size * 2.0f + (float) Math.random() * 0.2f);
				Vector2f.add(prev.offset, offset, prev.offset);
			}

		}

		
		addParticle(underSize * 0.5f, in, out, 1.5f * 3f, 0f, 0f, UNDERCOLOR);
		randomizePrevParticleLocation(underSize * 0.67f);
		addParticle(underSize * 0.5f, in, out, 1.5f * 3f, 0f, 0f, UNDERCOLOR);
		randomizePrevParticleLocation(underSize * 0.67f);

	}

    public void spawnEMPParticles(EMPArcHitType type, Vector2f point, CombatEntityAPI target) {
		CombatEngineAPI engine = Global.getCombatEngine();
		
		Color color = RiftLanceEffect.getColorForDarkening(RIFT_COLOR);
		
		float size = 30f;
		float baseDuration = 1.5f;
		Vector2f vel = new Vector2f();
		int numNegative = 5;
		switch (type) {
		case DEST:
			size = 50f;
			vel.set(target.getVelocity());
			if (vel.length() > 100f) {
				vel.scale(100f / vel.length());
			}
			break;
		case DEST_NO_TARGET:
			break;
		case SOURCE:
			size = 40f;
			numNegative = 10;
			break;
		}
		Vector2f dir = Misc.getUnitVectorAtDegreeAngle(proj.getFacing() + 180f);
		//dir.negate();
		//numNegative = 0;
		for (int i = 0; i < numNegative; i++) {
			float dur = baseDuration + baseDuration * (float) Math.random();
			//float nSize = size * (1f + 0.0f * (float) Math.random());
			//float nSize = size * (0.75f + 0.5f * (float) Math.random());
			float nSize = size;
			if (type == EMPArcHitType.SOURCE) {
				nSize *= 1.5f;
			}
			Vector2f pt = Misc.getPointWithinRadius(point, nSize * 0.5f);
			Vector2f v = Misc.getUnitVectorAtDegreeAngle((float) Math.random() * 360f);
			v.scale(nSize + nSize * (float) Math.random() * 0.5f);
			v.scale(0.2f);
			
			float endSizeMult = 2f;
			if (type == EMPArcHitType.SOURCE) {
				pt = Misc.getPointWithinRadius(point, nSize * 0f);
				Vector2f offset = new Vector2f(dir);
				offset.scale(size * 0.2f * i);
				Vector2f.add(pt, offset, pt);
				endSizeMult = 1.5f;
				v.scale(0.5f);
			}
			Vector2f.add(vel, v, v);
			
			float maxSpeed = nSize * 1.5f * 0.2f; 
			float minSpeed = nSize * 1f * 0.2f; 
			float overMin = v.length() - minSpeed;
			if (overMin > 0) {
				float durMult = 1f - overMin / (maxSpeed - minSpeed);
				if (durMult < 0.1f) durMult = 0.1f;
				dur *= 0.5f + 0.5f * durMult;
			}
			
//			if (type == EMPArcHitType.DEST || type == EMPArcHitType.DEST_NO_TARGET) {
//				v.set(0f, 0f);
//			}
			
			//engine.addNegativeNebulaParticle(pt, v, nSize * 1f, endSizeMult,
			engine.addNegativeSwirlyNebulaParticle(pt, v, nSize * 1f, endSizeMult,
											0.25f / dur, 0f, dur, color);
		}
		
		float dur = baseDuration; 
		float rampUp = 0.5f / dur;
		color = UNDERCOLOR;
		for (int i = 0; i < 7; i++) {
			Vector2f loc = new Vector2f(point);
			loc = Misc.getPointWithinRadius(loc, size * 1f);
			float s = size * 4f * (0.5f + (float) Math.random() * 0.5f);
			engine.addSwirlyNebulaParticle(loc, vel, s, 1.5f, rampUp, 0f, dur, color, false);
		}
	}
    
}
