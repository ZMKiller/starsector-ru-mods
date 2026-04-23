package org.amazigh.foundry.scripts;

import org.amazigh.foundry.scripts.everyframe.ASF_formiaRangePlugin;

import com.fs.starfarer.api.combat.*;

public class ASF_formiaWeaponScript implements EveryFrameWeaponEffectPlugin {

	    private ASF_formiaRangePlugin rangeModifier = null;

	    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

	        if (engine.isPaused() || weapon == null) {
	            return;
	        }

	        if (rangeModifier == null) {
	            rangeModifier = new ASF_formiaRangePlugin();
	        }
	        rangeModifier.advance(amount, engine, weapon);
	        
	    }
	}


