package data.weapons.scripts.proj;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatAsteroidAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;

public class HWIFlameThrowerEffect implements OnFireEffectPlugin {
    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        engine.addPlugin(new HWIFlameThrowerScript(projectile));
    }

    public class HWIFlameThrowerScript extends BaseEveryFrameCombatPlugin {
        private DamagingProjectileAPI proj;
        private CombatEngineAPI engine = Global.getCombatEngine();
        private FaderUtil fader = new FaderUtil(0, 2f);
        private static final float RANGE_BASE = 100f;
        private SpriteAPI sprite;
        private static final String FLAME_ID = "HWI_FlameThrower";
        private static final String INFERNO_ID = "HWI_InfernoThrower";
        private List<CombatEntityAPI> hit = new ArrayList<CombatEntityAPI>();
        private float dmg = 0f;
        private float eDmg = 0f;

        public HWIFlameThrowerScript(DamagingProjectileAPI proj) {
            this.proj = proj;
            if (proj.getWeapon().getSpec().getWeaponId().equals(FLAME_ID)) {
                sprite = Global.getSettings().getSprite("HSI_fx","Flame");
                //sprite = Global.getSettings().getSprite("misc", "nebula_particles");
                //sprite.setColor(new Color(251, 145, 12, 255));
            }
            if (proj.getWeapon().getSpec().getWeaponId().equals(INFERNO_ID)) {
                sprite = Global.getSettings().getSprite("HSI_fx","Inferno");
                //sprite = Global.getSettings().getSprite("misc", "nebula_particles");
                //sprite.setColor(new Color(36, 155, 255, 255));
            }
            float size = proj.getCollisionRadius() / RANGE_BASE;
            if (size > 1)
                size = 1;
            fader.setBrightness(size);
            fader.fadeIn();
            sprite.setAngle((float) (360f * Math.random()));
            dmg = proj.getDamageAmount();
        }

        private boolean isAlive() {
            return !proj.isExpired() && !proj.isFading() && engine.isEntityInPlay(proj);
        }

        public void advance(float amount, List<InputEventAPI> events) {
            if (!isAlive()) {
                engine.removePlugin(this);
                return;
            }
            if (engine.isPaused())
                return;
            proj.getVelocity().scale(0.99f);
            fader.advance(amount);
            proj.setDamageAmount(dmg*(1-0.5f*fader.getBrightness()));
            float size = fader.getBrightness() * RANGE_BASE;
            sprite.setSize(size, size);
            sprite.setAlphaMult(Math.max(0,1 - 0.8f * fader.getBrightness()) );
            sprite.setAngle((float) (sprite.getAngle() + ((Math.random() - 0.5)) * (2 * Math.random())));
            Iterator<Object> c = engine.getAllObjectGrid().getCheckIterator(proj.getLocation(), size / 8, size / 8);
            while (c.hasNext()) {
                Object o = c.next();
                if (!isInRange(o))
                    continue;
                if(hit.contains((CombatEntityAPI)o)) continue;
                if (o instanceof ShipAPI) {
                    ShipAPI s = (ShipAPI) o;
                    if (s != proj.getSource() && s.isFighter()) {
                        engine.applyDamage(s, s.getLocation(),
                                proj.getDamageAmount(), proj.getDamageType(), proj.getEmpAmount(),
                                false, true,
                                proj.getWeapon().getShip(), true);
                    }
                }
                if((o instanceof DamagingProjectileAPI)||(o instanceof CombatAsteroidAPI)){
                    CombatEntityAPI e = (CombatEntityAPI)o;
                    engine.applyDamage(e, e.getLocation(),
                                proj.getDamageAmount(), proj.getDamageType(), proj.getEmpAmount(),
                                false, true,
                                proj.getWeapon().getShip(), true);
                }
            }
        }

        private boolean isInRange(Object o) {
            if (o instanceof CombatEntityAPI) {
                CombatEntityAPI e = (CombatEntityAPI) o;
                if (e.getOwner()!=proj.getOwner()&&Misc.getDistance(e.getLocation(), proj.getLocation()) < e.getCollisionRadius()
                        + fader.getBrightness() * RANGE_BASE / 2) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        public void renderInWorldCoords(ViewportAPI viewport) {
            if (!viewport.isNearViewport(proj.getLocation(), 1000f))
                return;
            sprite.renderAtCenter(proj.getLocation().x, proj.getLocation().y);
        }
    }
}