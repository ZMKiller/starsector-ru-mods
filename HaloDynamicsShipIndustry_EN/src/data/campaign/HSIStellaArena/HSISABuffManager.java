package data.campaign.HSIStellaArena;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.HSISAAdvLoader;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.campaign.HSIStellaArena.Buff.HSISABuff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HSISABuffManager extends BaseEveryFrameCombatPlugin {

    public static String KEY = "$HSISA_Adv_Buffs";

    public CombatEngineAPI engine = Global.getCombatEngine();

    private IntervalUtil timer = new IntervalUtil(0.2f,0.4f);

    private List<HSISABuff> buffs = new ArrayList<>();

    private List<HSISAAdvLoader.HSIAdvBuffSpec> buffspecs = new ArrayList<>();
    public HSISABuffManager(){
        List<String> ids = new ArrayList<>();
        String buffList = "";
        if(Global.getSector().getMemoryWithoutUpdate().contains("$HSISA_LastAdventureBuffs")) {
            buffList =  Global.getSector().getMemoryWithoutUpdate().getString("$HSISA_LastAdventureBuffs");
        }
        ids = Arrays.asList(buffList.split(","));
        if(!ids.isEmpty()){
            for(String id:ids){
                if(HSISAAdvLoader.buffs.containsKey(id)){
                    buffspecs.add(HSISAAdvLoader.buffs.get(id));
                    buffs.add(HSISAAdvLoader.buffs.get(id).createBuff());
                }
            }
        }
    }


    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if(engine.isPaused()) return;
        for(ShipAPI ship:engine.getShips()){
            if(ship.getOwner() == 0&&!ship.isAlly()){
                if (ship.getCustomData().containsKey("HSISA_Adv_Buff_Affected")) {
                    for (HSISABuff buff : buffs) {
                        buff.applyToShip(ship);
                    }
                    ship.setCustomData("HSISA_Adv_Buff_Affected", true);
                }
                for (HSISABuff buff : buffs) {
                    buff.advance(ship,amount);
                }
            }
        }


        if(engine.getPlayerShip()!=null) {
            for (HSISAAdvLoader.HSIAdvBuffSpec buff : buffspecs) {
                Global.getCombatEngine().maintainStatusForPlayerShip(buff,
                        buff.getSprite(),
                        buff.getName(),
                        buff.getDesc(),
                        false);
            }
        }

    }
}
