package data.scripts.plugins;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;

import data.scripts.combatanalytics.damagedetection.EveryFrameDamageDetector;
import data.scripts.combatanalytics.DetailedCombatResultsModPlugin;

import org.apache.log4j.Level;

import java.util.List;

// Plugins have to be plain java files that are compiled by Janino.  Don't do any real work here.
public class DamageDetectionEveryFrameCombatPlugin extends BaseEveryFrameCombatPlugin {
    public DamageDetectionEveryFrameCombatPlugin(){
    }

    public void advance(float amount, List events)
    {
        EveryFrameDamageDetector.detectDamage(amount);
    }

    public void init(CombatEngineAPI eng) {
        EveryFrameDamageDetector.init(eng);
    }
}
