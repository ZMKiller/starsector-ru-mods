package data.campaign.HSIStellaArena.Buff;

import com.fs.starfarer.api.combat.ShipAPI;

public interface HSISABuff {

    void applyToShip(ShipAPI ship);

    void advance(ShipAPI ship,float amount);
}
