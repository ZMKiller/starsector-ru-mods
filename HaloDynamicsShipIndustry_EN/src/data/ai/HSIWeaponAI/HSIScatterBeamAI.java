package data.ai.HSIWeaponAI;

import com.fs.starfarer.api.combat.AutofireAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.weapons.scripts.beam.HSIScatterBeamEveryFrameEffect;
import org.lwjgl.util.vector.Vector2f;

public class HSIScatterBeamAI implements AutofireAIPlugin {

    private HSIScatterBeamEveryFrameEffect effect;
    private WeaponAPI weapon;

    private ShipAPI ship;

    public HSIScatterBeamAI(WeaponAPI weapon){
        this.weapon = weapon;
        if(weapon.getEffectPlugin() instanceof HSIScatterBeamEveryFrameEffect){
            effect = (HSIScatterBeamEveryFrameEffect) weapon.getEffectPlugin();
        }
        this.ship = weapon.getShip();
    }

    private boolean shouldFire = false;

    @Override
    public void advance(float amount) {
        shouldFire = effect.hasLegalTarget()
                &&(ship.getFluxTracker().getFluxLevel()<0.95f);
    }

    @Override
    public boolean shouldFire() {
        return shouldFire;
    }

    @Override
    public void forceOff() {

    }

    @Override
    public Vector2f getTarget() {
        return effect.getTargetLoc(weapon);
    }

    @Override
    public ShipAPI getTargetShip() {
        return effect.getTargetShip();
    }

    @Override
    public WeaponAPI getWeapon() {
        return weapon;
    }

    @Override
    public MissileAPI getTargetMissile() {
        return null;
    }

}
