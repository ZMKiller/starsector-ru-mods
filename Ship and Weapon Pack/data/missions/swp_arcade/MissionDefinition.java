package data.missions.swp_arcade;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import java.io.IOException;
import lunalib.lunaSettings.LunaSettings;
import org.apache.log4j.Level;
import org.json.JSONException;
import org.json.JSONObject;

public class MissionDefinition implements MissionDefinitionPlugin {

    static final String SETTINGS_FILE = "arcadeSettings.json";

    public static final String[] BRIEFING_ITEMS = {
        "Killing enemies makes you more powerful",
        "Your foe grows stronger over time",
        "Chain combos together for a score and CR multiplier",
        "Ammo regenerates when you kill enemies",
        "HEAVY STORY SPOILERS!"
    };
    private String ship = "swp_arcade_superhyperion_str";

    protected boolean campaignMode;    // set to true if mission is being created from an arcade machine in campaign

    @Override
    @SuppressWarnings("UseSpecificCatch")
    public void defineMission(MissionDefinitionAPI api) {
        try {
            reloadSettings();
        } catch (Exception ex) {
            Global.getLogger(MissionDefinition.class).log(Level.ERROR, ex);
        }

        api.initFleet(FleetSide.PLAYER, "", FleetGoal.ATTACK, false, 25);
        api.initFleet(FleetSide.ENEMY, "SS", FleetGoal.ATTACK, true, 10);

        api.setFleetTagline(FleetSide.PLAYER, "Player");
        api.setFleetTagline(FleetSide.ENEMY, "Bad Guys");

        for (String brief : BRIEFING_ITEMS) {
            api.addBriefingItem(brief);
        }

        if (!campaignMode) {
            api.addToFleet(FleetSide.PLAYER, ship, FleetMemberType.SHIP, "MM Randy Savage", true).getCaptain().setPersonality(
                    "aggressive");
        }

        api.addToFleet(FleetSide.ENEMY, "ox_Standard", FleetMemberType.SHIP, "Scape", false).getCaptain().setPersonality(
                "aggressive");
        api.addToFleet(FleetSide.ENEMY, "ox_Standard", FleetMemberType.SHIP, "Goat", false).getCaptain().setPersonality(
                "aggressive");

        float width = 6000f;
        float height = 6000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        MissionPlugin plugin = new MissionPlugin();
        plugin.setCampaignMode(campaignMode);
        api.addPlugin(plugin);
        api.addPlugin(new UnrealAnnouncer());
    }

    public void reloadSettings() throws IOException, JSONException {
        JSONObject settings = Global.getSettings().loadJSON(SETTINGS_FILE);

        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            ship = LunaSettings.getString("swp", "arcadePlayerShip");
        } else {
            ship = settings.getString("playerShip");
        }
    }

    public void setCampaignMode(boolean mode) {
        campaignMode = mode;
    }

    public String getPlayerShip() {
        return ship;
    }
}
