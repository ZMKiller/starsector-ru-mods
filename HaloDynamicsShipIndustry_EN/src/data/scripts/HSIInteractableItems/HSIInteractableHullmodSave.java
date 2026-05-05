package data.scripts.HSIInteractableItems;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.campaign.BuffManagerAPI.Buff;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

public class HSIInteractableHullmodSave implements Buff{
    protected static String id = HSIInteractableHullmodSave.class.getName();
    private Map<String,String> mem = new HashMap<>();

    @Override
    public void advance(float amount) {
        
    }

    @Override
    public void apply(FleetMemberAPI member) {
        
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    public Map<String,String> getMem(){
        return mem;
    }

    public static HSIInteractableHullmodSave getInstance(FleetMemberAPI member){
        if(member==null) return null;
        if(member.getBuffManager().getBuff(id)!=null){
            return (HSIInteractableHullmodSave)member.getBuffManager().getBuff(id);
        }else{
            member.getBuffManager().addBuff(new HSIInteractableHullmodSave());
            return (HSIInteractableHullmodSave)member.getBuffManager().getBuff(id);
        }
    }
    
}
