package data.campaign.HSIStellaArena;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.impl.combat.BattleCreationPluginImpl;

public class HSISAAdvBCP extends BattleCreationPluginImpl {

    @Override
    public void afterDefinitionLoad(final CombatEngineAPI engine) {
        super.afterDefinitionLoad(engine);
        engine.addPlugin(new HSISABuffManager());
    }

}
