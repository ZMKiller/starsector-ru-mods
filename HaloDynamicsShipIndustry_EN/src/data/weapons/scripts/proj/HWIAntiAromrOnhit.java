package data.weapons.scripts.proj;

import java.awt.Color;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;

public class HWIAntiAromrOnhit implements OnHitEffectPlugin {
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit,
            ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
            if(target instanceof ShipAPI){
                ShipAPI ship = (ShipAPI)target;
                
                MutableShipStatsAPI stats = ship.getMutableStats();
                float armorEffective = stats.getEffectiveArmorBonus().computeEffective(ship.getArmorGrid().getArmorRating());
                float time = 900f/armorEffective*20f;
                if(time>20f) time = 20f;
                if(time<9f) time = 9f;
                ship.fadeToColor(ship,new Color(252, 145, 45, 255), time/2f, time/2f, 2f);
                stats.getEffectiveArmorBonus().modifyPercent("HWIAntiArmorHit", -60f);
                stats.getMaxArmorDamageReduction().modifyPercent("HWIAntiArmorHit", -25f);
                ship.addListener(new HWIAntiAromrEffectPlugin(ship, time));
            }
    }

    public class HWIAntiAromrEffectPlugin implements AdvanceableListener{
        private MutableShipStatsAPI stats;
        private ShipAPI ship;
        private float time;
        private IntervalUtil timer = new IntervalUtil(13f, 13f);
        public HWIAntiAromrEffectPlugin(ShipAPI ship,float time){
            this.ship = ship;
            this.time = time;
            timer = new IntervalUtil(time, time);
            stats = ship.getMutableStats();
        }

        public void advance(float amount){
            if(Global.getCombatEngine().isPaused()) return;
            timer.advance(amount);
            if(timer.intervalElapsed()&&ship.isAlive()){
                stats.getEffectiveArmorBonus().unmodify("HWIAntiArmorHit");
                stats.getMaxArmorDamageReduction().unmodify("HWIAntiArmorHit");
                ship.removeListener(this);
            }else if(!ship.isAlive()){
                ship.removeListener(this);
            }
        }
    }
}
