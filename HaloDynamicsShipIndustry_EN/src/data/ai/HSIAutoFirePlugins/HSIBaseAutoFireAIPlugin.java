package data.ai.HSIAutoFirePlugins;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.AutofireAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class HSIBaseAutoFireAIPlugin implements AutofireAIPlugin{
    private HSIAutoFireShipData weaponData;
    private boolean forceOff = false;


    public HSIBaseAutoFireAIPlugin(WeaponAPI weapon){
        this.weaponData = HSIAutoFireShipData.getInstance(weapon.getShip());
    }

    @Override
    public void advance(float amount) {
        
    }

    @Override
    public void forceOff() {
        this.forceOff = true;
    }

    @Override
    public Vector2f getTarget() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTarget'");
    }

    @Override
    public MissileAPI getTargetMissile() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTargetMissile'");
    }

    @Override
    public ShipAPI getTargetShip() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTargetShip'");
    }

    @Override
    public WeaponAPI getWeapon() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getWeapon'");
    }

    @Override
    public boolean shouldFire() {
        if(forceOff){
            forceOff = false;
            return false;
        }
        return false;
    }

    public HSIAutoFireShipData getWeaponData() {
        return weaponData;
    }
    
}
