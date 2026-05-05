package data.weapons.scripts.buff;

import com.fs.starfarer.api.combat.ShipAPI;

public class HWIMjolnirUpdBuff extends HWIBaseBuffWithTimer {
    protected float elapsed = 0f;
    protected static final String id = "HWI_Mjolnir_Debuff";

    public HWIMjolnirUpdBuff(ShipAPI target) {
        super(target);
    }

    public static HWIMjolnirUpdBuff getInstance(ShipAPI target) {
        if (target.hasListenerOfClass(HWIMjolnirUpdBuff.class)) {
            return target.getListeners(HWIMjolnirUpdBuff.class).get(0);
        } else {
            HWIMjolnirUpdBuff l = new HWIMjolnirUpdBuff(target);
            target.addListener(l);
            return l;
        }
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
        elapsed += amount;
        if (elapsed <= HWIMjolnirUpdEffect.dur) {
            ship.getMutableStats().getMaxSpeed().modifyMult(id,
                    HWIMjolnirUpdEffect.mult + (1f - HWIMjolnirUpdEffect.mult) * elapsed / HWIMjolnirUpdEffect.dur);
        }
    }

    @Override
    public void add(Object provider, float time) {
        super.add(provider, time);
        elapsed = 0;
    }

    @Override
    public void clear() {
        ship.getMutableStats().getMaxSpeed().unmodify(id);
    }
}
