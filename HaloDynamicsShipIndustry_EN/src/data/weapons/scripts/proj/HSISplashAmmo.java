package data.weapons.scripts.proj;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;
import com.fs.starfarer.api.util.Misc;

public class HSISplashAmmo implements ProximityExplosionEffect {
    @Override
    public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
        int num = 1;
        String subAmmo = "";
        float arc = 0;
        boolean evenSpread = true;
        float inaccuracy = 0;
        try{
            num = originalProjectile.getProjectileSpec().getBehaviorJSON().getInt("numShots");
            subAmmo = originalProjectile.getProjectileSpec().getBehaviorJSON().getString("projectileSpec");
            arc = (float)originalProjectile.getProjectileSpec().getBehaviorJSON().getDouble("arc");
            evenSpread = originalProjectile.getProjectileSpec().getBehaviorJSON().getBoolean("evenSpread");
            inaccuracy = (float)originalProjectile.getProjectileSpec().getBehaviorJSON().getDouble("spreadInaccuracy");
        }catch (Exception e){
            Global.getLogger(this.getClass()).error(e);
            return;
        }
        int count = 0;
        while (count<num){
            count++;
            float mid = originalProjectile.getFacing();
            if(evenSpread) inaccuracy = 0;
            float angle = mid;
            if(num==1){
                angle = mid+(float)(2f*inaccuracy*(1-Math.random()));
            }else{
                float fanAngle = arc/(num-1f)*(count-(num-1f)/2f);
                angle = mid+fanAngle+(float)(2f*inaccuracy*(1-Math.random()));
            }
            angle = Misc.normalizeAngle(angle);
            Global.getCombatEngine().spawnProjectile(originalProjectile.getSource(),originalProjectile.getWeapon(),subAmmo,explosion.getLocation(),angle,null);
        }
    }
}
