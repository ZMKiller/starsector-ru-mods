package data.scripts.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class SWP_CristariumDronesStats extends BaseShipSystemScript {

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        stats.getBallisticRoFMult().modifyMult(id, 1f - effectLevel / 12.5f);
        stats.getEnergyRoFMult().modifyMult(id, 1f - effectLevel / 12.5f);
        stats.getMissileRoFMult().modifyMult(id, 1f - effectLevel / 12.5f);
        stats.getArmorDamageTakenMult().modifyMult(id, 1.01f - effectLevel / 12.5f);
        stats.getHullDamageTakenMult().modifyMult(id, 1f - effectLevel / 12.5f);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getBallisticRoFMult().unmodify(id);
        stats.getEnergyRoFMult().unmodify(id);
        stats.getMissileRoFMult().unmodify(id);
        stats.getArmorDamageTakenMult().unmodify(id);
        stats.getHullDamageTakenMult().unmodify(id);
    }
}
