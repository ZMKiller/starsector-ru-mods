package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.magiclib.util.MagicRender;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class ORA_beamDefensePlugin extends BaseEveryFrameCombatPlugin {
  
    private CombatEngineAPI ENGINE;
    private float timer=0;
    private boolean playerDebuff=false;
    private List<ShipAPI> OTHER_SHIPS= new ArrayList<>();
    private List<Vector2f> ORA_FIELDS= new ArrayList<>();
    
    private final float TIC=0.5f;
    private final String ID="ora_beamDebuff";
    public static final Object DEBUFF_TARGET = new Object();
    
    @Override
    public void init(CombatEngineAPI engine) { 
        ENGINE=engine;
    }
    
    @Override
    public void advance(float amount, List events) {
        if(ENGINE!=Global.getCombatEngine()){
            ENGINE=Global.getCombatEngine();
        }
        if(ENGINE.isPaused()){
            return;
        }
        //BEAM DEBUFF
        timer+=amount;
        if(timer>TIC){
            timer=0;
            OTHER_SHIPS.clear();
            ORA_FIELDS.clear();
            playerDebuff=false;
            
            ShipAPI player = ENGINE.getPlayerShip();
            
            for(ShipAPI s : ENGINE.getShips()){
                if(s.isAlive()){
                    if(s.getHullSpec().getHullId().startsWith("ora_") && !s.isFighter() && !s.isDrone()){
                        ORA_FIELDS.add(s.getLocation());
                        //objectspaceRender(SpriteAPI sprite, CombatEntityAPI anchor, Vector2f offset, Vector2f vel, Vector2f size, Vector2f growth, float angle, float spin, boolean parent, Color color, boolean additive, float fadein, float full, float fadeout, boolean fadeOnDeath)
                        MagicRender.objectspace(
                                Global.getSettings().getSprite("graphics/fx/wormhole_ring_bright2.png"),
                                s,
                                new Vector2f(),
                                new Vector2f(),
                                new Vector2f(4100,4100),
                                new Vector2f(100,100),
                                0,
                                0,
                                false,
                                new Color(200,50,100,50),
                                true,
                                0.5f,
                                0.0f,
                                0.5f,
                                true
                        );
                    } else {
                        OTHER_SHIPS.add(s);
                    }
                }
            }
            
            for(ShipAPI s : OTHER_SHIPS){
                boolean debuff=false;
                for(Vector2f v : ORA_FIELDS){
                    if(MathUtils.isPointWithinCircle(s.getLocation(), v, 2000)){
                        debuff=true;                        
                        if(!s.getMutableStats().getBeamWeaponRangeBonus().getMultBonuses().containsKey(ID)){
                            for(WeaponAPI w : s.getAllWeapons()){
                                if((w.isBeam()||w.isBurstBeam())&&!w.getSlot().isDecorative()){
                                    Vector2f offset = w.getLocation();
                                    Vector2f.sub(offset, s.getLocation(), offset);
                                    VectorUtils.rotate(offset, -s.getFacing(), offset);
                                    float size;
                                    if (w.getSprite()!=null){
                                        size=w.getSprite().getWidth();
                                    } else {
                                        size = 32;
                                    }
                                    MagicRender.objectspace(
                                            Global.getSettings().getSprite("graphics/warroom/taskicons/icon_cancel_order.png"),
                                            s,
                                            offset,
                                            new Vector2f(),
                                            new Vector2f(size,size),
                                            new Vector2f(),
                                            0,
                                            0, 
                                            true,
                                            new Color(255,255,255,255),
                                            true,
                                            0.1f,
                                            0.5f,
                                            1f,
                                            true
                                    );
                                }
                            }
                        }                        
                        
                        s.getMutableStats().getBeamWeaponRangeBonus().modifyMult(ID, 0.8f);                    
                        s.getMutableStats().getBeamWeaponDamageMult().modifyMult(ID, 0.8f);
                        
                        if (s == player) { 
                            playerDebuff=true;
                        }
                        break;
                    }
                }
                if(!debuff){                    
                    s.getMutableStats().getBeamWeaponRangeBonus().unmodify(ID);
                    s.getMutableStats().getBeamWeaponDamageMult().unmodify(ID);
                }
            }
        }
        
        //UI HINT
        if(playerDebuff){
            ENGINE.maintainStatusForPlayerShip(DEBUFF_TARGET, 
                    "graphics/icons/hullsys/entropy_amplifier.png",
                    "Alliance's Beam Suppression", 
                    "-20% beam damage and range", true);
        }        
    }
    
}
