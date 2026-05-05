package data.weapons.scripts.buff;

import com.fs.starfarer.api.combat.ShipAPI;

public class HWINeedlerUpdBuff extends HWIBaseBuffWithTimer {
    protected static final String id = "HWI_Needler_Debuff";
    protected static final float mult = 0.95f;
    public HWINeedlerUpdBuff(ShipAPI target) {
        super(target);
    }

    public static HWINeedlerUpdBuff getInstance(ShipAPI target) {
        if (target.hasListenerOfClass(HWINeedlerUpdBuff.class)) {
            return target.getListeners(HWINeedlerUpdBuff.class).get(0);
        } else {
            HWINeedlerUpdBuff l = new HWINeedlerUpdBuff(target);
            target.addListener(l);
            return l;
        }
    }

    public void advance(float amount) {
        super.advance(amount);
        ship.getMutableStats().getShieldDamageTakenMult().modifyPercent(id, (float)(1f+mult*(1f-Math.pow(mult, providers.getItems().size()-1f))/(1f-mult)));
    }

    public void add(Object provider, float time) {
        super.add(provider, time);
    }

    @Override
    public void clear() {
        ship.getMutableStats().getShieldDamageTakenMult().unmodify(id);
    }
}
