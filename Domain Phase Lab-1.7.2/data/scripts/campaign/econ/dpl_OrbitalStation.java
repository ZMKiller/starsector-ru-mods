package data.scripts.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.OrbitalStation;
import com.fs.starfarer.api.impl.campaign.ids.Factions;

public class dpl_OrbitalStation extends OrbitalStation {

    @Override
    public boolean isAvailableToBuild() {
        boolean canBuild = super.isAvailableToBuild();

        SectorAPI sector = Global.getSector();

        FactionAPI player = sector.getFaction(Factions.PLAYER);
        FactionAPI imperium = sector.getFaction("dpl_phase_lab");

        //If not unlocked, cannot build.
        if (!Global.getSector().getPlayerMemoryWithoutUpdate().getBoolean("$dpl_CanBuildStation")) {
        	canBuild = false;
        }

        return canBuild;
    }

    @Override
    public String getUnavailableReason() {
        if (!super.isAvailableToBuild()) {
            return super.getUnavailableReason();
        }
        return "Station type unavailable.";
    }

    @Override
    public boolean showWhenUnavailable() {
        if (!super.showWhenUnavailable()) {
            return false;
        }
        return Global.getSector().getPlayerFaction().knowsIndustry(getId());
    }
}
