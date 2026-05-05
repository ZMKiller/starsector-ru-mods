package data.campaign.ability.HSIDarkTide;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;

public class HSIDarkTideAbilityPlugin extends BaseDurationAbility{

    @Override
    protected void activateImpl() {
    
    }

    @Override
    protected void applyEffect(float amount, float level) {
        
    }

    @Override
    protected void cleanupImpl() {

    }

    @Override
    protected void deactivateImpl() {
        cleanupImpl();
    }

}
