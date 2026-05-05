package data.weapons.scripts.beam;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.FaderUtil;
import data.scripts.HSIRenderer.HSICombatRendererV2;
import data.scripts.HSIRenderer.HSIThunderStormObject;
import org.lwjgl.util.vector.Vector2f;

public class HWISweeperEveryframe implements EveryFrameWeaponEffectPlugin {
    private int currAmmo = 0;

    public int cost = 0;

    private FaderUtil block = new FaderUtil(1,0.3f,0.3f);

    private boolean fired = true;


    //private boolean triggered = false;
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if(weapon.isFiring()){
            fired = false;
        }else{
            //triggered = false;
            block.advance(amount);
            //Global.getLogger(this.getClass()).info("Block"+block+"-Cost"+cost);
            if(!fired&&block.isFadedOut()) {
                fired = true;
                if (weapon.getShip().getCustomData().containsKey("HWISweeper_Data")) {
                    HSICombatRendererV2.getInstance().addFxObject(new HSIThunderStormObject(weapon.getShip(), (Vector2f) weapon.getShip().getCustomData().get("HWISweeper_Data"), cost, 250, 250, DamageType.ENERGY, 0.25f, weapon.getCurrAngle()));
                }
                cost = 0;
                currAmmo = weapon.getAmmo();
            }
        }
    }

    public void updateBlock(){
        block.setBrightness(1f);
        block.fadeOut();
    }
}
