package org.dark.graphics.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.SwarmMember;
import com.fs.starfarer.api.input.InputEventAPI;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.dark.graphics.light.ThreatMapObject;
import org.dark.shaders.util.ShaderLib;

public class ThreatInjector extends BaseEveryFrameCombatPlugin {

    private static final String DATA_KEY = "GLib_ThreatInjector";

    private static final String THREAT_SWARM_SPRITE_CAT = "misc";
    private static final String THREAT_SWARM_SPRITE_KEY = "threat_swarm_pieces";

    private CombatEngineAPI engine;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }

        if (!Global.getCombatEngine().getCustomData().containsKey(DATA_KEY)) {
            Global.getCombatEngine().getCustomData().put(DATA_KEY, new LocalData());
        }
        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final Map<SwarmMember, ThreatMapObject> prevMembers = localData.prevMembers;

        // Retrieve all the roiling swarms
        final Map<CombatEntityAPI, RoilingSwarmEffect> shipMap = RoilingSwarmEffect.getShipMap();
        if (shipMap == null) {
            return;
        }

        // Collect every member of every Threat roiling swarm
        final Map<SwarmMember, RoilingSwarmEffect> currMembers = new LinkedHashMap<>();
        for (Map.Entry<CombatEntityAPI, RoilingSwarmEffect> shipMapEntry : shipMap.entrySet()) {
            final RoilingSwarmEffect swarmEffect = shipMapEntry.getValue();
            if ((swarmEffect != null) && swarmEffect.getParams().spriteCat.contentEquals(THREAT_SWARM_SPRITE_CAT)
                    && swarmEffect.getParams().spriteKey.contentEquals(THREAT_SWARM_SPRITE_KEY)) {
                for (SwarmMember member : swarmEffect.getMembers()) {
                    currMembers.put(member, swarmEffect);
                }
            }
        }

        // Prune map objects for swarm members that are no longer present
        final Iterator<Map.Entry<SwarmMember, ThreatMapObject>> iter = prevMembers.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<SwarmMember, ThreatMapObject> prevMemberEntry = iter.next();
            if (!currMembers.containsKey(prevMemberEntry.getKey())) {
                ShaderLib.removeMapObject(prevMemberEntry.getValue());
                iter.remove();
            }
        }

        // Add map objects for new swarm members
        for (Map.Entry<SwarmMember, RoilingSwarmEffect> currMemberEntry : currMembers.entrySet()) {
            SwarmMember currMember = currMemberEntry.getKey();
            if (!prevMembers.containsKey(currMember)) {
                ThreatMapObject mapObject = new ThreatMapObject(currMember, currMemberEntry.getValue());
                ShaderLib.addMapObject(mapObject);
                prevMembers.put(currMember, mapObject);
            }
        }
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        if (engine != null) {
            engine.getCustomData().put(DATA_KEY, new LocalData());
        }
    }

    private static final class LocalData {

        final Map<SwarmMember, ThreatMapObject> prevMembers = new LinkedHashMap<>();
    }
}
