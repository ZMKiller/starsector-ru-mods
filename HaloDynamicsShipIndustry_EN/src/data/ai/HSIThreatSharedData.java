package data.ai;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import data.ai.HSIWeaponAI.HSIMissilePDAI;
import org.lazywizard.lazylib.combat.CombatUtils;

public class HSIThreatSharedData {
    public List<DamagingProjectileAPI> projs = new ArrayList<>();

    private CombatEntityAPI self;

    public HSIThreatSharedData(CombatEntityAPI self) {
        this.self = self;
    }

    public static HSIThreatSharedData getInstance(CombatEntityAPI e){
        if(e.getCustomData().containsKey(HSIMissilePDAI.DATA_KEY)){
            return (HSIThreatSharedData) e.getCustomData().get(HSIMissilePDAI.DATA_KEY);
        }else{
            HSIThreatSharedData data = new HSIThreatSharedData(e);
            e.setCustomData(HSIMissilePDAI.DATA_KEY,data);
            return data;
        }
    }

    public float getDMGToThis() {
        float sum = 0;
        List<DamagingProjectileAPI> toRemove = new ArrayList<>();
        for (DamagingProjectileAPI proj : projs) {
            if(proj.getAI() instanceof GuidedMissileAI&&((GuidedMissileAI) proj.getAI()).getTarget()!=null&&!((GuidedMissileAI) proj.getAI()).getTarget().equals(self)){
                toRemove.add(proj);
                continue;
            }
            if (Global.getCombatEngine().isEntityInPlay(proj)) {
                sum += proj.getDamageAmount();
            }else{
                toRemove.add(proj);
            }
        }
        projs.removeAll(toRemove);
        return sum;
    }

    public void addProj(DamagingProjectileAPI proj) {
        if (!projs.contains(proj))
            projs.add(proj);
    }

    public static void addProjAuto(DamagingProjectileAPI proj,CombatEntityAPI target){
        if(target==null) return;
        if(target.getCustomData().containsKey(HSIMissilePDAI.DATA_KEY)){
            HSIThreatSharedData data = (HSIThreatSharedData)target.getCustomData().get(HSIMissilePDAI.DATA_KEY);
            data.addProj(proj);
        }else{
            HSIThreatSharedData data = new HSIThreatSharedData(target);
            data.addProj(proj);
            //target.getCustomData().put(HWIThreatAnalysisPDAI.DATA_KEY, data);
            target.setCustomData(HSIMissilePDAI.DATA_KEY, data);
        }
    }
}
