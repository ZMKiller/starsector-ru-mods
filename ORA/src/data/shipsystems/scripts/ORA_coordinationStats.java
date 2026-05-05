package data.shipsystems.scripts;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import static data.scripts.util.ORA_txt.txt;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class ORA_coordinationStats extends BaseShipSystemScript {

    private final float EFFECT_AREA=1500, CR_EFFECT=30f;	

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if(state == State.ACTIVE){
            for(ShipAPI s : AIUtils.getNearbyAllies(stats.getEntity(), EFFECT_AREA*1.25f)){
                if(s.isAlive() && MathUtils.isWithinRange(s, stats.getEntity(), EFFECT_AREA)){
                    addEffect(s, (ShipAPI)stats.getEntity(), id);
                } else {
                    removeEffect(s, id);
                }
            }
        }
        ((ShipAPI)stats.getEntity()).setJitter(stats.getEntity(), new Color(0.8f,0.8f,1f,0.5f), effectLevel*0.25f, Math.round(effectLevel*5), 1, effectLevel*5);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {    
        for(ShipAPI s : CombatUtils.getShipsWithinRange(stats.getEntity().getLocation(), EFFECT_AREA*1.25f)){
            //check if the ship got a bonus
            if(s.getMutableStats().getMaxCombatReadiness().getFlatStatMod(id)!=null){
                //use a max CR bonus to store the old CR value (divided by 100 in case something goes wrong)
                s.setCurrentCR(s.getMutableStats().getMaxCombatReadiness().getFlatStatMod(id).getValue()*100);
                s.getMutableStats().getCRLossPerSecondPercent().unmodify(id);
                s.getMutableStats().getMaxCombatReadiness().unmodify(id);
            }
        }
    }
    
    private void addEffect(ShipAPI s, ShipAPI source, String id){
        if(s.getMutableStats().getMaxCombatReadiness().getFlatStatMod(id)==null){
            //no decay for a while
            s.getMutableStats().getCRLossPerSecondPercent().modifyMult(id, 0);
            //use a max CR bonus to store the current CR value (divided by 100 in case something goes wrong)
            s.getMutableStats().getMaxCombatReadiness().modifyFlat(id, s.getCurrentCR()/100);
            
            //modify the CR for a while
            s.setCurrentCR(s.getCurrentCR()+CR_EFFECT/100);            
        }
        
        //visual fluff
//        if(s.isDrone() || s.isFighter()){
           s.setJitter(source, Color.WHITE, 0.25f, 2, 10);
//        } else 
        if(!s.isDrone() && !s.isFighter() && Math.random()<0.01f){
            Vector2f vel = MathUtils.getPoint(new Vector2f(), 150, VectorUtils.getAngle(s.getLocation(), source.getLocation()));
            s.addAfterimage(
                    new Color(0.8f,0.8f,1f,0.25f), 
                    0,
                    0,
                    vel.x, 
                    vel.y,
                    5,
                    0.1f,
                    0.2f,
                    0.4f,
                    true,
                    false,
                    false
            );
            source.addAfterimage(
                    new Color(0.8f,0.8f,1f,0.25f), 
                    0,
                    0,
                    -vel.x, 
                    -vel.y,
                    5,
                    0.1f,
                    0.2f,
                    0.4f,
                    true,
                    false,
                    false
            );
        }
    }
    
    private void removeEffect(ShipAPI s, String id){
        if(s.getMutableStats().getMaxCombatReadiness().getFlatStatMod(id)!=null){
            //use a max CR bonus to store the old CR value (divided by 100 in case something goes wrong)
            s.setCurrentCR(s.getMutableStats().getMaxCombatReadiness().getFlatStatMod(id).getValue()*100);
            s.getMutableStats().getCRLossPerSecondPercent().unmodify(id);
            s.getMutableStats().getMaxCombatReadiness().unmodify(id);
        }
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(txt("+")+(int)(CR_EFFECT)+txt("sstem_coord"), false);
        }
        return null;
    }
}