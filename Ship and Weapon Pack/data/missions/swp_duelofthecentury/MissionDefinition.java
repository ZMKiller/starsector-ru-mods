package data.missions.swp_duelofthecentury;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.codex.CodexDataV2;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.dark.shaders.post.PostProcessShader;
import org.lwjgl.util.vector.Vector3f;

public class MissionDefinition implements MissionDefinitionPlugin {

    public static final List<String[]> ENEMIES = new ArrayList<>();
    public static final Map<String, Integer> HARD_MODE_OFFICERS = new HashMap<>();
    public static final String PLAYER_VARIANT = "swp_excelsior_pro";

    public static boolean HARD_MODE = false;
    protected boolean campaignMode; // set to true if mission is being created from an arcade machine in campaign

    public static boolean printedCodexWarning = false;

    static {
        // name, required mod's ID, variant, backup mod's ID, backup variant, fallback variant
        ENEMIES.add(new String[]{"Mozart", null, "hyperion_Strike", null, null, null});
        ENEMIES.add(new String[]{"Tartini", "SEEKER", "SKR_endymion_assault", null, null, "hyperion_Attack"});
        ENEMIES.add(new String[]{"Beethoven", "shadow_ships", "ms_shamash_EMP", null, null, "swp_hyperion_flamer"});
        ENEMIES.add(new String[]{"Vivaldi", "blackrock_driveyards", "brdy_imaginos_shock", null, null, "swp_hyperion_shocker"});
        ENEMIES.add(new String[]{"Mendelssohn", "vic", "vic_nybbas_plasma", null, null, "swp_hyperion_assault"});
        ENEMIES.add(new String[]{"Paganini", "XhanEmpire", "XHAN_Pharrek_variant_EmperorsScalpel", null, null, "swp_hyperion_blaster"});
        ENEMIES.add(new String[]{"Dvorak", "Imperium", "ii_maximus_str", null, null, "swp_hyperion_nullifier"});
        ENEMIES.add(new String[]{"Saint-Saens", "SCY", "SCY_stymphalianbird_combat", null, null, "swp_hyperion_assassin"});
        ENEMIES.add(new String[]{"Haydn", "diableavionics", "diableavionics_versant_standard", null, null, "swp_hyperion_meltdowner"});
        ENEMIES.add(new String[]{"Rachmaninoff", "prv", "prv_sinne_assault", null, null, "swp_hyperion_berserker"});
        ENEMIES.add(new String[]{"Debussy", "timid_xiv", "eis_valorous_standard", null, null, "swp_hyperion_stunner"});

        /* Default is Level 3 */
        HARD_MODE_OFFICERS.put("swp_hyperion_berserker", 6);
        HARD_MODE_OFFICERS.put("ms_shamash_EMP", 5);
        HARD_MODE_OFFICERS.put("diableavionics_versant_standard", 5);
        HARD_MODE_OFFICERS.put("ii_maximus_str", 4);
        HARD_MODE_OFFICERS.put("SCY_stymphalianbird_combat", 4);
        HARD_MODE_OFFICERS.put("prv_sinne_assault", 4); // Rachmaninoff buff 3 -> 4
        HARD_MODE_OFFICERS.put("XHAN_Pharrek_variant_EmperorsScalpel", 2);
    }

    public void addEnemyShips(MissionDefinitionAPI api) {
        for (String[] entry : ENEMIES) {
            String name = entry[0];
            String modId = entry[1];
            String variantId = entry[2];
            String backupModId = entry[3];
            String backup = entry[4];
            String fallback = entry[5];

            if ((modId != null) && !Global.getSettings().getModManager().isModEnabled(modId)) {
                if (!Global.getSettings().getModManager().isModEnabled(backupModId)) {
                    variantId = fallback;
                } else {
                    variantId = backup;
                }
            }

            addEnemyShip(api, variantId, name);
        }
    }

    public void addEnemyShip(MissionDefinitionAPI api, String variantId, String name) {
        FleetMemberAPI member = api.addToFleet(FleetSide.ENEMY, variantId, FleetMemberType.SHIP, "Silent " + name, false);

        if (HARD_MODE) {
            int level = 3;
            if (HARD_MODE_OFFICERS.containsKey(variantId)) {
                level = HARD_MODE_OFFICERS.get(variantId);
            }
            if (name.contentEquals("Rachmaninoff")) {
                FactionAPI indies = Global.getSettings().createBaseFaction(Factions.INDEPENDENT);
                PersonAPI officer = indies.createRandomPerson(Gender.MALE);
                officer.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
                if (level >= 2) {
                    officer.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
                }
                if (level >= 3) {
                    officer.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 1);
                }
                if (level >= 4) {
                    officer.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 1);
                }
                if (level >= 5) {
                    officer.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 1);
                }
                if (level >= 6) {
                    officer.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 1);
                }
                officer.getStats().setLevel(level);
                officer.setFaction(Factions.INDEPENDENT);
                officer.setPersonality(Personalities.STEADY);
                officer.getName().setFirst("Rachmaninoff");
                officer.getName().setLast("");
                officer.getName().setGender(Gender.MALE);
                officer.setPortraitSprite("graphics/swp/portraits/swp_rachmaninoff.png");
                member.setCaptain(officer);
                float maxCR = member.getRepairTracker().getMaxCR();
                member.getRepairTracker().setCR(maxCR);
            } else {
                PersonAPI officer = OfficerManagerEvent.createOfficer(Global.getSector().getFaction(Factions.TRITACHYON), level, FleetFactoryV3.getSkillPrefForShip(member), true, null, true, true, 1, new Random());
                switch (officer.getPersonalityAPI().getId()) {
                    case "timid" ->
                        officer.setPersonality("steady");
                    case "cautious" ->
                        officer.setPersonality("aggressive");
                    default ->
                        officer.setPersonality("reckless");
                }
                member.setCaptain(officer);
                float maxCR = member.getRepairTracker().getMaxCR();
                member.getRepairTracker().setCR(maxCR);
            }
        }
    }

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        api.initFleet(FleetSide.PLAYER, "TTS", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "ISS", FleetGoal.ATTACK, true);

        api.setFleetTagline(FleetSide.PLAYER, "Captain Lee's stolen Tri-Tachyon prototype");
        api.setFleetTagline(FleetSide.ENEMY, "\"The Silent Hand\" elite mercenary squad");

        if (!campaignMode && !CodexDataV2.hasUnlockedEntryForShip("swp_excelsior") && !printedCodexWarning) {
            api.addBriefingItem("WARNING: POTENTIAL SPOILERS");
            api.addBriefingItem("Codex entries for certain ships featured in this mission are not yet unlocked");
            api.addBriefingItem("Click again to play the mission anyway!");
            printedCodexWarning = true;
            return;
        }

        if (Global.getSettings().getMissionScore("swp_duelofthecentury") >= 100) {
            HARD_MODE = true;
        }

        api.setHyperspaceMode(true);

        api.addBriefingItem("Defeat \"The Silent Hand\".");
        api.addBriefingItem("Captain Lee's skills are impeccable; the Excelsior is particularly effective in battle.");
        api.addBriefingItem("Flux will decrease more slowly over time due to flux synchonicity from the nearby star.");
        if (HARD_MODE) {
            api.addBriefingItem("HARD MODE: The Silent Hand's officers are on station.");
        }

        if (!campaignMode) {
            FleetMemberAPI member = api.addToFleet(FleetSide.PLAYER, PLAYER_VARIANT, FleetMemberType.SHIP, "TTS Aperioris", true);

            FactionAPI pirates = Global.getSettings().createBaseFaction(Factions.PIRATES);
            PersonAPI officer = pirates.createRandomPerson(Gender.MALE);
            officer.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
            officer.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 1);
            officer.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
            officer.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
            officer.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
            officer.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
            officer.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 1);
            officer.getStats().setSkillLevel(Skills.WOLFPACK_TACTICS, 1);
            officer.getStats().setSkillLevel(Skills.PHASE_CORPS, 1);
            officer.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);
            officer.getStats().setLevel(7);
            officer.setFaction(Factions.PIRATES);
            officer.setPersonality(Personalities.RECKLESS);
            officer.getName().setFirst("Mann");
            officer.getName().setLast("Lee");
            officer.getName().setGender(Gender.MALE);
            officer.setPortraitSprite("graphics/swp/portraits/swp_lee.png");
            member.setCaptain(officer);
            float maxCR = member.getRepairTracker().getMaxCR();
            member.getRepairTracker().setCR(maxCR);

            addEnemyShips(api);
        }

        float width = 10000f;
        float height = 10000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        api.addAsteroidField(0f, 0f, (float) Math.random() * 360f, width, 30f, 200f, 500);
        api.addPlugin(new Plugin());
        api.getContext().fightToTheLast = true;
    }

    public void setCampaignMode(boolean mode) {
        campaignMode = mode;
    }

    public final static class Plugin extends BaseEveryFrameCombatPlugin {

        private boolean reallyStarted = false;
        private boolean started = false;

        @Override
        public void advance(float amount, List<InputEventAPI> events) {
            if (!started) {
                started = true;
                return;
            }
            if (!reallyStarted) {
                reallyStarted = true;

                StandardLight sun = new StandardLight();
                sun.setType(3);
                sun.setDirection((Vector3f) (new Vector3f(-1f, -1f, -0.2f)).normalise());
                sun.setIntensity(0f);
                sun.setSpecularIntensity(2f);
                sun.setColor(new Color(212, 91, 22));
                sun.makePermanent();
                LightShader.addLight(sun);

                sun = new StandardLight();
                sun.setType(3);
                sun.setDirection((Vector3f) (new Vector3f(0f, 0f, -1f)).normalise());
                sun.setIntensity(0.75f);
                sun.setSpecularIntensity(0f);
                sun.setColor(new Color(212, 91, 22));
                sun.makePermanent();
                LightShader.addLight(sun);

                PostProcessShader.setSaturation(false, 1.1f);
                PostProcessShader.setLightness(false, 0.9f);
                PostProcessShader.setContrast(false, 1.1f);
                PostProcessShader.setNoise(false, 0.1f);
            }

            if (Global.getCombatEngine().isPaused()) {
                return;
            }

            for (ShipAPI ship : Global.getCombatEngine().getShips()) {
                if (ship.getHullSpec().getHullId().startsWith("swp_excelsior")) {
                    ship.getFluxTracker().increaseFlux(30f * amount, true);
                }
            }
        }

        @Override
        public void init(CombatEngineAPI engine) {
        }
    };
}
