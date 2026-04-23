package shmoneelse.sicdustkeeper.skills.dustkeeper;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.Console;
import second_in_command.SCData;
import second_in_command.SCUtils;
import com.fs.starfarer.api.util.Misc;
import second_in_command.misc.SCSettings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class HumanWarmind extends DustkeeperBaseAutoPointsSkillPlugin { // Now called Warmind Socialization
    private static final int MAX_POINTS = 175;
    private static final int FRIGATE_VAL = 15;
    private static final int DESTROYER_VAL = 25;
    private static final int CRUISER_VAL = 35;
    private static final int CAPITAL_VAL = 50;

    //private HashMap<ShipAPI, ShipAPI.HullSize> pointMap = new HashMap<ShipAPI, ShipAPI.HullSize>();
    //private HashSet<ShipAPI> pointSet = new HashSet<ShipAPI>();

    private int calcProvidedPoints() {
        if(Global.getSector() == null) return 0;
        if(Global.getSector().getPlayerFleet() == null) return 0;
        if(Global.getSector().getPlayerFleet().getFleetData() == null) return 0; // Everyone loves null checks

        List<FleetMemberAPI> fleet = Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy();

        int retval = 0;

        for(FleetMemberAPI member : fleet)
        {
            //Console.showMessage(member.getShipName());
            if(member.isCivilian()) continue;
            PersonAPI captain = member.getCaptain();
            if(captain == null) continue;
            if(captain.isAICore()) continue;
            if(captain.isDefault()) continue;

            if (member.isFrigate()) retval += FRIGATE_VAL;
            else if (member.isDestroyer()) retval += DESTROYER_VAL;
            else if (member.isCruiser()) retval += CRUISER_VAL;
            else if (member.isCapital()) retval += CAPITAL_VAL;
        }

        if(retval > MAX_POINTS) retval = MAX_POINTS;
        return Math.round(retval * SCSettings.getAutoPointsMult()); // Multiply by the Automated Points Mult

    }

    @Override
    public int getProvidedPoints() {
        return calcProvidedPoints();
    }

    @Override
    public void addTooltip(SCData data, @NotNull TooltipMakerAPI tooltip) { // Updated to factor in the autopoint mult
        if(data.isPlayer())
        {
            tooltip.addPara("Provides automated ship points that scale with the number of non-civilian ships with assigned officers", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
            tooltip.addPara("   - Each eligible ship provides %s/%s/%s/%s automated ship points, based on hullsize", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
                    Integer.toString(Math.round(FRIGATE_VAL * SCSettings.getAutoPointsMult())), Integer.toString(Math.round(DESTROYER_VAL * SCSettings.getAutoPointsMult())),
                    Integer.toString(Math.round(CRUISER_VAL * SCSettings.getAutoPointsMult())), Integer.toString(Math.round(CAPITAL_VAL * SCSettings.getAutoPointsMult())));
            tooltip.addPara("   - Only human officers selectable in the Officer Selection Window are eligible", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "25");
            tooltip.addPara("   - The maximum bonus is %s", 0f, Misc.getTextColor(), Misc.getHighlightColor(), String.valueOf(Math.round(MAX_POINTS * SCSettings.getAutoPointsMult())));

            int provided = getProvidedPoints();

            tooltip.addPara("Currently provides +" + Integer.toString(provided) + " automated ship points", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        }

        super.addTooltip(data, tooltip);
    }
}
