package data.scripts.campaign.intel.missions;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction.ShipSaleInfo;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyInteractionListener;
import com.fs.starfarer.api.campaign.listeners.ShipRecoveryListener;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI.SkillLevelAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Voices;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager.GenericBarEventCreator;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionTrigger.TriggerAction;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionTrigger.TriggerActionContext;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BreadcrumbSpecial;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipRecoverySpecialData;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.SWPModPlugin;
import data.scripts.util.SWP_Util;
import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.campaign.CampaignUtils;
import org.lwjgl.util.vector.Vector2f;

public class SWP_BuriedTreasure extends HubMissionWithBarEvent implements ShipRecoveryListener {

    public static enum Stage {
        FIND_FIRST_CLUE,
        FIND_SECOND_CLUE,
        FIND_THIRD_CLUE,
        FIND_FINAL_CLUE,
        FIND_TREASURE,
        GO_TO_TREASURE,
        COMPLETED
    }

    public static Logger log = Global.getLogger(SWP_BuriedTreasure.class);

    public static String BURIED_TREASURE = "swpBT";

    public static String EXCELSIOR_DESC_ALT_KEY = "$swpBT_excelsiorAltDesc";

    public static String TANNEN = "swp_tannen";
    public static String LEE = "swp_lee";
    public static String MOUSE = "swp_mouse";
    public static String RACHMANINOFF = "swp_rachmaninoff";
    public static String TT_INSIDER = "swp_tt_insider";
    public static String ELYON = "swp_elyon";
    public static String DAAT_ELYON = "swp_daat_elyon";

    public static String TANNEN_SHIP = "swpBT_tannenShip";
    public static String TANNEN_OFFICER_BONUS = "swpBT_tannenBonus";

    public static String LEE_ENTITY = "swp_leeEntity";
    public static String RESEARCH_BASE_TAG = "swp_researchBase";
    public static String UW_THUGS_FLAG = "$swpBT_thugFleet";
    public static String TANNEN_FLAG = "$swpBT_tannenFleet";

    public static String PRINT_RULES = "swp_print_rules";

    public static float SCAV_CONTACT_RAID_DIFFICULTY = 100f;

    protected PersonAPI captainTannen;
    protected int tannenBribe;
    protected int tannenBigBribe;
    protected boolean killedTannen = false;

    protected PersonAPI captainLee;
    protected StarSystemAPI leeSystem;
    protected List<Breadcrumb> leeBreadcrumbs;
    protected SectorEntityToken leeEntity;

    protected PersonAPI mouse;
    protected int mouseBribe;

    protected PersonAPI rachmaninoff;
    protected MarketAPI rachmaninoffMarket;
    protected boolean activateRachmaninoff = false;
    protected boolean completeRachmaninoff = false;

    protected PersonAPI scavContact;
    protected MarketAPI scavContactMarket;
    protected boolean activateScavContact = false;
    protected boolean completeScavContact = false;

    protected PersonAPI ttContact;
    protected PersonAPI ttInsider;
    protected MarketAPI ttInsiderMarket;
    protected boolean ttCluePossible;
    protected boolean ttClueLimitedInfo = false;
    protected boolean activateTTClue = false;
    protected boolean completeTTClue = false;
    protected PersonAPI daatElyon;

    protected PersonAPI uwContact;
    protected SectorEntityToken uwResearchBaseEntity;
    protected StarSystemAPI uwResearchBaseSystem;
    protected boolean uwClueLimitedInfo = false;
    protected boolean activateUWClue = false;
    protected boolean completeUWClue = false;
    protected float ttBarProb = 0.25f;
    protected float uwBarGenericProb = 0.15f;
    protected float uwBarGenericAgainProb = 0.25f;
    protected float uwBarPiratesProb = 0.3f;
    protected float uwBarPiratesAgainProb = 0.5f;
    protected int thugsBribe;
    protected PersonAPI elyon;

    protected int numCluesFound = 0;
    protected int numCluesTannenKnows = 0;
    protected int numCluesCanBeFound = 1;
    protected int numLeadsKnown = 0;
    protected boolean agreedToFirstRule = false; // Finders, Keepers
    protected boolean agreedToSecondRule = false; // Truce
    protected boolean agreedToThirdRule = false; // Secrecy
    protected boolean brokeThirdRule = false;

    protected IntervalUtil checkInterval = new IntervalUtil(1f, 2f);
    protected float musicEnderTimeout = 0f;

    /* Tannen reputation flowchart:
       1. [SP Option] Appeal to Tannen during initial bar event: +20 Rep
         Possible Reps: +0 (Neu), +20 (Fav)
       2. Distrst Tannen during initial bar event (requires option 1): -5 Rep
         Possible Reps: +0 (Neu), +15 (Fav), +20
       3A. Trust Tannen during initial bar event (precludes option 1): -5 Rep
       3B. Trust Tannen during initial bar event (requires option 1): +5 Rep
         Possible Reps: -5 (Neu), +0, +15 (Fav), +20, +25 (Wel)
       4. Quietly listen to the whole story during initial bar event (precludes options 2, 3A, and 3B): +10 Rep
         Possible Reps: -5 (Neu), +0, +10 (Fav), +15, +20, +25 (Wel), +30
       5A. [SP Option] Threaten Tannen during initial bar event (precludes option 1 and requires <= 0 Rep): -40 Rep
       5B. [SP Option] Tell Tannen about Rachmaninoff during initial bar event: +10 Rep
       5C. Punch Tannen during initial bar event (precludes option 1 and requires <= 0 Rep): -50 Rep
         Possible Reps: -55 (Hos), -50, -45 (Inh), -40, -5 (Neu), +0, +5, +10 (Fav), +15, +20, +25 (Wel), +30, +35, +40
       6A. Refuse to follow any of Tannen's rules of engagement during initial bar event (requires > -25 Rep): -15 Rep
       6B. Refuse to follow Tannen's second and third rules of engagement during initial bar event (requires > -25 rep): -10 Rep
       6C. Refuse to follow Tannen's third rule of engagement during initial bar event (requires > -25 Rep): -5 Rep
         Possible Reps: -55 (Hos), -50, -45 (Inh), -40, -20 (Sus), -15, -10, -5 (Neu), +0, +5, +10 (Fav), +15, +20, +25 (Wel), +30, +35, +40
       7. [SP Option] Convince Tannen to join your fleet during initial bar event (requires >= 20 Rep): +5 Rep
         Possible Reps: -55 (Hos), -50, -45 (Inh), -40, -20 (Sus), -15, -10, -5 (Neu), +0, +5, +10 (Fav), +15, +20, +25 (Wel), +30, +35, +40, +45
       8. [Repeatable] Try to dismiss Tannen from your fleet (requires option 7): -10 Rep
         Possible Reps: -55 (Hos), -50, -45 (Inh), -40, -20 (Sus), -15, -10, -5 (Neu), +0, +5, +10 (Fav), +15, +20, +25 (Wel), +30, +35, +40, +45
       9. Proceed to remove Tannen from your fleet (requires option 8): -20 Rep
         Possible Reps: -55 (Hos), -50, -45 (Inh), -40, -20 (Sus), -15, -10, -5 (Neu), +0, +5, +10 (Fav), +15, +20, +25 (Wel), +30, +35, +40, +45
       10A. Give a lame justification for removing Tannen from your fleet (requires option 8): -5 Rep
       10B. Give an offensive justification for removing Tannen from your fleet (requires option 8): -10 Rep
         Possible Reps: -55 (Hos), -50, -45 (Inh), -40, -20 (Sus), -15, -10, -5 (Neu), +0, +5, +10 (Fav), +15, +20, +25 (Wel), +30, +35, +40, +45
       11.1. [Repeatable] Go a cycle without progressing the quest (requires option 7): -5 Rep
       11.2. [Repeatable] Go two cycles without progressing the quest (requires option 11.1 and >= 20 Rep): -10 Rep
       11.3. [Repeatable] Go three cycles without progressing the quest (requires option 11.1 and >= 20 Rep): -15 Rep
       11.4. [Repeatable] Go four+ cycles without progressing the quest (requires option 11.1 and >= 20 Rep): -20 Rep
         Possible Reps: -55 (Hos), -50, -45 (Inh), -40, -20 (Sus), -15, -10, -5 (Neu), +0, +5, +10 (Fav), +15, +20, +25 (Wel), +30, +35, +40, +45
       12A. [Repeatable SP Option] Convince Captain Tannen to stay with your fleet (requires option 11.1, 11.2, 11.3, or 11.4 and >= 20 Rep): +5 Rep
       12B. Kick Tannen out of your fleet (requires option 11.1, 11.2, 11.3, or 11.4 and >= 35 Rep): -25 Rep
         Possible Reps: -55 (Hos), -50, -45 (Inh), -40, -20 (Sus), -15, -10, -5 (Neu), +0, +5, +10 (Fav), +15, +20, +25 (Wel), +30, +35, +40, +45
       13. [SP Option] Play up Captain Tannen's legend when interacting with the pirate thugs (requires option 7): +5 Rep
         Possible Reps: -55 (Hos), -50, -45 (Inh), -40, -20 (Sus), -15, -10, -5 (Neu), +0, +5, +10 (Fav), +15, +20, +25 (Wel), +30, +35, +40, +45, +50 (Fri)
       14A. Admit to breaing the third rule to Captain Tannen (precludes options 6*): -15 Rep
       14B. Get caught breaing the third rule by Captain Tannen (precludes options 6*): -25 Rep
         Possible Reps: -55 (Hos), -50, -45 (Inh), -40, -35, -30, -25, -20 (Sus), -15, -10, -5 (Neu), +0, +5, +10 (Fav), +15, +20, +25 (Wel), +30, +35, +40, +45, +50 (Fri)
       15A. [Repeatable up to 4x] Snub Captain Tannen when he ties to contact you during the quest (requires option 6A, 6B, or > -50 Rep): -10 Rep
       15B. [Repeatable up to 4x] Snub Captain Tannen after breaking the third rule (precludes options 6* and 14*): -25 Rep
         Possible Reps: -100 (Ven), -95, -90, -85, -80, -75, -70 (Hos), -65, -60, -55, -50, -45 (Inh), -40, -35, -30, -25, -20 (Sus), -15, -10, -5 (Neu), +0, +5, +10 (Fav), +15, +20, +25 (Wel), +30, +35, +40, +45, +50 (Fri)
       16. [Repeatable up to 3x] Tell Captain Tannen about one of the completed clues, except Rachmaninoff: +10 Rep
         Possible Reps: -100 (Ven), -95, -90, -85, -80, -75, -70 (Hos), -65, -60, -55, -50, -45 (Inh), -40, -35, -30, -25, -20 (Sus), -15, -10, -5 (Neu), +0, +5, +10 (Fav), +15, +20, +25 (Wel), +30, +35, +40, +45, +50 (Fri), +60, +70, +80
       17A. Tell Captain Tannen about the completed Rachmaninoff clue (requires option 5B): +5 Rep
       17B. Tell Captain Tannen about the completed Rachmaninoff clue (precludes option 5B): +10 Rep
         Possible Reps: -100 (Ven), -95, -90, -85, -80, -75, -70 (Hos), -65, -60, -55, -50, -45 (Inh), -40, -35, -30, -25, -20 (Sus), -15, -10, -5 (Neu), +0, +5, +10 (Fav), +15, +20, +25 (Wel), +30, +35, +40, +45, +50 (Fri), +55, +60, +65, +70, +75 (Coo), +80, +85

       Global Vars Used:
         $global.swpBT_knowCaptainTannen
         $global.swpBT_gotBeatUpByTannen
         $global.swpBT_beatUpTannen
         $global.swpBT_appealedToCaptainTannen
         $global.swpBT_rememberedRachmaninoff
         $global.swpBT_mentionedRachmaninoff
         $global.swpBT_numNoProgressEvents
            Increases by 1 for each consecutive cycle that no progress is made
         $global.swpBT_TTBarExhausted
         $global.swpBT_UWBarExhausted
         $global.swpBT_askedInAnyBar
         $global.swpBT_generalAwareness (generally 0 to 9)
            Increases by 1 for each of the following:
                - [Repeatable up to 3x] Asking about the black project in a TT bar
                - [Repeatable up to 6x] Asking about Captain Lee in a non-TT bar
                - [Repeatable up to 4x] Tell Tannen about a clue (unless secrecy rule in place)
                - Don't agree to (or violate) the secrecy rule
         $global.swpBT_TTAwareness (generally 1 to 4)
            Increases by 1 for each of the following:
                - [Repeatable up to 3x] Asking about the black project in a TT bar
                - [Repeatable 1x per contact] Asking a TT contact about the black project
         $global.swpBT_UWAwareness (generally 1 to 13)
            Increases by 1 for each of the following:
                - [Repeatable up to 3x] Asking about Captain Lee in a pirate bar
                - [Repeatable 1x per contact] Asking a UW contact about Captain Lee
                - [Repeatable 1x per contact] Mentioning Captain Lee's treasure to a UW contact
                - [Repeatable 1x per contact] Sharing leads to Captain Lee's treasure with a UW contact (if didn't give clues)
                - [Repeatable up to 2x] Mess with the Mouse
                - Introducing Tannen to the thug fleet
                - [Repeatable up to 4x] Tell Tannen about a clue (unless secrecy rule in place)
                - Don't agree to (or violate) the secrecy rule
            Increases by 2 for each of the following:
                - [Repeatable 1x per contact] Giving clues to Captain Lee's treasure to a UW contact
                - Threaten the Mouse
            Decreases by 1 for each of the following:
                - Defeat the thug fleet
            Decreases by 1 for each of the following:
                - [Repeatable up to 4x] Defeat (but don't kill) Tannen's fleet
            Decreases by 2 for each of the following:
                - Kill Tannen
         $global.swpBT_tannenAwareness (generally 0 to 1)
            Increases by 1 for each of the following:
                - Tell him about Rachmaninoff
                - [Repeatable up to 4x] Tell him about a clue
         $global.swpBT_metMouse
         $global.swpBT_metMouseAgain
         $global.swpBT_talkedWithMouse
         $global.swpBT_tellTannenMouseSaidHi
         $global.swpBT_haveMouseDagger
         $global.swpBT_elyonWithFleet
         $global.swpBT_visitedResearchBase
         $global.swpBT_completionCount
            Increases by 1 for each clue found
         $global.swpBT_completionEventCount
            Increases by 1 after each post-completion event finished (without skipping)
         $global.swpBT_completionEventTrigger
         $global.swpBT_completionEventProcChance
         $global.swpBT_liedToTannen
         $global.swpBT_tannenKnowsTTClue
         $global.swpBT_tannenKnowsUWClue
         $global.swpBT_tannenKnowsScavClue
         $global.swpBT_tannenKnowsRachClue
         $global.swpBT_refusedBribe
         $global.swpBT_finalTannenSpawned
         $global.swpBT_missionEnded
     */
    @Override
    protected boolean create(MarketAPI createdAt, boolean barEvent) {
        if (createdAt.isPlayerOwned()) {
            return false;
        }

        // if already accepted by the player, abort
        if (!setGlobalReference("$swpBT_ref", "$swpBT_inProgress")) {
            return false;
        }

        /* Make sure the quest can eventually pop up no matter how weirdly messed up the sector is */
        int failedGens = 0;
        if (Global.getSector().getMemoryWithoutUpdate().contains("$swpBT_failedBreadcrumbGen")) {
            failedGens = Global.getSector().getMemoryWithoutUpdate().getInt("$swpBT_failedBreadcrumbGen");
        }
        for (int i = 0; i < failedGens; i++) {
            getGenRandom().nextLong();
        }

        /* The requirements here are pretty sparse and can result in fairly dangerous systems that Lee spawns in.
         * This should add some variety to the quest and make it harder to guess where he is.
         */
        resetSearch();
        requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_HIDDEN, Tags.THEME_REMNANT_RESURGENT, "ix_fortified", "theme_breakers_suppressed",
                "theme_breakers_resurgent", Tags.THEME_CORE, Tags.SYSTEM_ALREADY_USED_FOR_STORY);
        requireSystemNotHasPulsar();
        requireSystem(new SystemHasNoColonyReq());
        if (getGenRandom().nextFloat() < 0.75) {
            /* Weight unexplored heavily, but not guaranteed */
            preferSystemUnexplored();
        }
        if (getGenRandom().nextFloat() < 0.75) {
            /* Weight dense heavily, but not guaranteed */
            preferSystemIsDense();
        }
        if (getGenRandom().nextFloat() < 0.75) {
            /* Weight fringe heavily, but not guaranteed */
            preferSystemOnFringeOfSector();
        }
        leeSystem = pickSystem();
        if (leeSystem == null) {
            return false;
        }
        PerShipData shipData = new PerShipData(Global.getSettings().getVariant("swp_excelsior_locked"), ShipCondition.PRISTINE, "TTS Aperioris", Factions.TRITACHYON, 0f);
        shipData.addDmods = false;
        shipData.nameAlwaysKnown = true;
        shipData.pruneWeapons = false;
        DerelictShipData derelictData = new DerelictShipData(shipData, false);
        if (getGenRandom().nextFloat() < 0.75) {
            /* Weight non-star-orbit heavily, but not guaranteed */
            leeEntity = spawnDerelict(derelictData, new LocData(EntityLocationType.HIDDEN_NOT_NEAR_STAR, null, leeSystem, false));
        } else {
            leeEntity = spawnDerelict(derelictData, new LocData(EntityLocationType.HIDDEN, null, leeSystem, false));
        }
        leeEntity.setId(LEE_ENTITY);
        leeEntity.setDiscoverable(true);
        leeEntity.removeTag(Tags.NEUTRINO_HIGH);
        leeEntity.removeTag(Tags.NEUTRINO);
        leeEntity.addTag(Tags.NEUTRINO_LOW);
        leeEntity.setName("Drifting Ship");
        leeEntity.setCustomDescriptionId("swp_excelsior_derelict_unknown");
        leeEntity.getDetectedRangeMod().modifyMult(BURIED_TREASURE, 0.5f);
        if (leeEntity == null) {
            return false;
        }
        ShipRecoverySpecialData leeRecovery = new ShipRecoverySpecialData(null);
        leeRecovery.notNowOptionExits = true;
        leeRecovery.noDescriptionText = true;
        leeRecovery.storyPointRecovery = false;
        DerelictShipEntityPlugin leePlugin = (DerelictShipEntityPlugin) leeEntity.getCustomPlugin();
        PerShipData leeShipDataCopy = (PerShipData) leePlugin.getData().ship.clone();
        leeShipDataCopy.variant = leeShipDataCopy.variant.clone();
        leeShipDataCopy.variant.addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
        leeRecovery.addShip(leeShipDataCopy);
        Misc.setSalvageSpecial(leeEntity, leeRecovery);
        leeBreadcrumbs = genBreadcrumbs(leeSystem);
        if (leeBreadcrumbs == null) {
            return false;
        }

        ImportantPeopleAPI ip = Global.getSector().getImportantPeople();

        /* Create Tannen */
        captainTannen = ip.getPerson(TANNEN);
        if (captainTannen == null) {
            captainTannen = Global.getFactory().createPerson();
            captainTannen.setId(TANNEN);
            captainTannen.setFaction(Factions.PIRATES);
            captainTannen.setGender(Gender.MALE);
            captainTannen.setRankId(Ranks.SPACE_COMMANDER);
            captainTannen.setPostId(Ranks.POST_PATROL_COMMANDER);
            captainTannen.setImportance(PersonImportance.MEDIUM);
            captainTannen.getName().setFirst("Jakk");
            captainTannen.getName().setLast("Tannen");
            captainTannen.setVoice(Voices.SPACER);
            captainTannen.setPortraitSprite("graphics/swp/portraits/swp_tannen.png");
            captainTannen.setPersonality("aggressive");

            /* Tannen has stats equivalent to the best possible cryopod officer
             * Note: Tannen is NOT a mercenary; the player should get a permanent +1 maximum officer increase so long as
             * Tannen remains with the fleet, and he will never leave on his own (aside from the quest mechanics)!
             */
            captainTannen.getStats().setLevel(7);
            captainTannen.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
            captainTannen.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 1);
            captainTannen.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
            captainTannen.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
            captainTannen.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
            captainTannen.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
            captainTannen.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 1);
            /* These are his commander skills - strip them if he joins the player's fleet */
            captainTannen.getStats().setSkillLevel(Skills.WOLFPACK_TACTICS, 1);
            captainTannen.getStats().setSkillLevel(Skills.PHASE_CORPS, 1);
            captainTannen.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);

            ip.addPerson(captainTannen);
        }

        setPersonOverride(captainTannen);

        Misc.setUnremovable(captainTannen, true); // Remove this tag when the quest is over; player can put him on any ship

        int bribeBase = 20000;
        int bribeMaxLevel = 100000;
        int bribeMin = bribeBase + Math.round((bribeMaxLevel - bribeBase) * (((float) Global.getSector().getPlayerStats().getLevel()) / getMaxPlayerLevel()));
        tannenBribe = genRoundNumber(bribeMin, Math.round(bribeMin * 1.25f));
        bribeBase = 50000;
        bribeMaxLevel = 200000;
        bribeMin = bribeBase + Math.round((bribeMaxLevel - bribeBase) * (((float) Global.getSector().getPlayerStats().getLevel()) / getMaxPlayerLevel()));
        tannenBigBribe = genRoundNumber(bribeMin, Math.round(bribeMin * 1.25f));

        /* Captain Lee is dead, so he isn't added to a market */
        captainLee = ip.getPerson(LEE);
        if (captainLee == null) {
            captainLee = Global.getFactory().createPerson();
            captainLee.setId(LEE);
            captainLee.setFaction(Factions.PIRATES);
            captainLee.setGender(Gender.MALE);
            captainLee.setRankId(Ranks.SPACE_COMMANDER);
            captainLee.setPostId(Ranks.POST_PATROL_COMMANDER);
            captainLee.setImportance(PersonImportance.MEDIUM);
            captainLee.getName().setFirst("Mann");
            captainLee.getName().setLast("Lee");
            captainLee.setVoice(Voices.VILLAIN);
            captainLee.setPortraitSprite("graphics/swp/portraits/swp_lee.png");
            ip.addPerson(captainLee);
        }

        /* The Mouse is a drifter, so she isn't added to a market */
        mouse = ip.getPerson(MOUSE);
        if (mouse == null) {
            mouse = Global.getFactory().createPerson();
            mouse.setId(MOUSE);
            mouse.setFaction(Factions.PIRATES);
            mouse.setGender(Gender.FEMALE);
            mouse.setRankId("swp_drifter");
            mouse.setPostId("swp_drifter");
            mouse.setImportance(PersonImportance.VERY_LOW);
            mouse.getName().setFirst("The Mouse");
            mouse.getName().setLast("");
            mouse.setVoice(Voices.VILLAIN);
            mouse.setPortraitSprite("graphics/swp/portraits/swp_maus.png");
            ip.addPerson(mouse);
        }

        bribeBase = 20000;
        bribeMaxLevel = 50000;
        bribeMin = bribeBase + Math.round((bribeMaxLevel - bribeBase) * (((float) Global.getSector().getPlayerStats().getLevel()) / getMaxPlayerLevel()));
        mouseBribe = genRoundNumber(bribeMin, Math.round(bribeMin * 1.25f));

        /* Find a suitable market for Rachmaninoff */
        resetSearch();
        requireMarketIsNot(createdAt);
        requireMarketNotHidden();
        requireMarketNotInHyperspace();
        requireMarketFactionNotPlayer();
        requireMarketFactionNot(Factions.PIRATES, Factions.TRITACHYON);
        preferMarketFactionNotHostileTo(Factions.PLAYER);
        preferMarketNotMilitary();
        preferMarketStabilityAtLeast(1);
        if (createdAt.getContainingLocation() != null) {
            preferMarketLocationNot(createdAt.getContainingLocation());
        }
        rachmaninoffMarket = pickMarket();
        if (rachmaninoffMarket == null) {
            return false;
        }

        /* Create Rachmaninoff */
        rachmaninoff = ip.getPerson(RACHMANINOFF);
        if (rachmaninoff == null) {
            rachmaninoff = Global.getFactory().createPerson();
            rachmaninoff.setId(RACHMANINOFF);
            rachmaninoff.setFaction(Factions.INDEPENDENT);
            rachmaninoff.setGender(Gender.MALE);
            rachmaninoff.setRankId(Ranks.CITIZEN);
            rachmaninoff.setPostId(Ranks.POST_SPACER);
            rachmaninoff.setImportance(PersonImportance.VERY_LOW);
            rachmaninoff.getName().setFirst("Rachmaninoff");
            rachmaninoff.getName().setLast("");
            rachmaninoff.setVoice(Voices.SPACER);
            rachmaninoff.setPortraitSprite("graphics/swp/portraits/swp_rachmaninoff.png");
            rachmaninoff.setPersonality("steady");

            /* Rachmaninoff is secretly an elite merc */
            rachmaninoff.getStats().setLevel(6);
            rachmaninoff.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
            rachmaninoff.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 1);
            rachmaninoff.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
            rachmaninoff.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 1);
            rachmaninoff.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 1);
            rachmaninoff.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 1);

            rachmaninoffMarket.getCommDirectory().addPerson(rachmaninoff);
            rachmaninoffMarket.getCommDirectory().getEntryForPerson(rachmaninoff).setHidden(true);
            rachmaninoffMarket.addPerson(rachmaninoff);

            ip.addPerson(rachmaninoff);
        }

        /* Find a suitable market for the scavenger contact */
        resetSearch();
        requireMarketIsNot(createdAt);
        requireMarketNotHidden();
        requireMarketNotInHyperspace();
        requireMarketFactionNotPlayer();
        requireMarketFactionNot(Factions.TRITACHYON);
        preferMarketFaction(Factions.INDEPENDENT, Factions.PIRATES);
        preferMarketStabilityAtLeast(1);
        if (createdAt.getContainingLocation() != null) {
            preferMarketLocationNot(createdAt.getContainingLocation());
        }
        scavContactMarket = pickMarket();
        if (scavContactMarket == null) {
            return false;
        }

        /* Get a contact to be the scavenger */
        scavContact = findOrCreateCriminalTrader(scavContactMarket, true);
        if (scavContact == null) {
            return false;
        }

        /* Is actually a splinter of Da'at Elyon */
        ttInsider = ip.getPerson(TT_INSIDER);
        if (ttInsider == null) {
            ttInsider = Global.getFactory().createPerson();
            ttInsider.setId(TT_INSIDER);
            ttInsider.setFaction(Factions.TRITACHYON);
            ttInsider.setGender(Gender.ANY);
            ttInsider.setRankId(Ranks.UNKNOWN);
            ttInsider.setPostId(Ranks.POST_UNKNOWN);
            ttInsider.setImportance(PersonImportance.HIGH);
            ttInsider.getName().setFirst("Da'at");
            ttInsider.getName().setLast("");
            ttInsider.setVoice(Voices.BUSINESS);
            ttInsider.setPortraitSprite("graphics/swp/portraits/static.png");
            ip.addPerson(ttInsider);
        }

        daatElyon = ip.getPerson(DAAT_ELYON);
        if (daatElyon == null) {
            daatElyon = Global.getFactory().createPerson();
            daatElyon.setId(DAAT_ELYON);
            daatElyon.setFaction(Factions.REMNANTS);
            daatElyon.setGender(Gender.ANY);
            daatElyon.setRankId(Ranks.UNKNOWN);
            daatElyon.setPostId(Ranks.POST_UNKNOWN);
            daatElyon.setImportance(PersonImportance.HIGH);
            daatElyon.getName().setFirst("Da'at");
            daatElyon.getName().setLast("Elyon");
            daatElyon.setPortraitSprite("graphics/swp/portraits/swp_portrait_ai1.png");
            ip.addPerson(daatElyon);
        }

        /* Find a suitable market for the Tri-Tachyon insider */
        resetSearch();
        requireMarketNotHidden();
        requireMarketNotInHyperspace();
        requireMarketFaction(Factions.TRITACHYON);
        preferMarketIsNot(createdAt);
        preferMarketStabilityAtLeast(1);
        if (createdAt.getContainingLocation() != null) {
            preferMarketLocationNot(createdAt.getContainingLocation());
        }
        ttInsiderMarket = pickMarket();
        if (ttInsiderMarket != null) {
            ttCluePossible = true;
            numCluesCanBeFound++;

            ttInsiderMarket.getCommDirectory().addPerson(ttInsider);
            ttInsiderMarket.getCommDirectory().getEntryForPerson(ttInsider).setHidden(true);
            ttInsiderMarket.addPerson(ttInsider);
        } else {
            ttCluePossible = false;
        }

        bribeBase = 50000;
        bribeMaxLevel = 200000;
        bribeMin = bribeBase + Math.round((bribeMaxLevel - bribeBase) * (((float) Global.getSector().getPlayerStats().getLevel()) / getMaxPlayerLevel()));
        thugsBribe = genRoundNumber(bribeMin, Math.round(bribeMin * 1.25f));

        /* Should be in a remnant system */
        resetSearch();
        requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_HIDDEN, Tags.THEME_CORE);
        requireSystem(new SystemHasNoColonyReq());
        requireSystemNot(leeSystem);
        requirePlanetNotStar();
        requirePlanetUnpopulated();
        preferSystemUnexplored();
        preferSystemTags(ReqMode.ANY, Tags.THEME_REMNANT_RESURGENT, Tags.THEME_REMNANT_SUPPRESSED);
        preferSystemTags(ReqMode.NOT_ANY, Tags.THEME_REMNANT_SECONDARY, Tags.SYSTEM_ALREADY_USED_FOR_STORY);
        preferPlanetNotNearJumpPoint(1500f);
        preferPlanetUnsurveyed();
        PlanetAPI uwResearchBasePlanet = pickPlanet();
        if (uwResearchBasePlanet == null) {
            return false;
        }
        uwResearchBaseSystem = uwResearchBasePlanet.getStarSystem();
        if (uwResearchBaseSystem == null) {
            return false;
        }
        uwResearchBaseEntity = spawnDebrisField(DEBRIS_LARGE, DEBRIS_DENSE, new LocData(EntityLocationType.ORBITING_PARAM, uwResearchBasePlanet, uwResearchBaseSystem, false));
        if (uwResearchBaseEntity == null) {
            return false;
        }
        uwResearchBaseEntity.setDiscoverable(true);
        uwResearchBaseEntity.removeTag(Tags.NEUTRINO_LOW);
        uwResearchBaseEntity.addTag(Tags.NEUTRINO);
        uwResearchBaseEntity.addTag(RESEARCH_BASE_TAG);
        CampaignTerrainAPI debrisTerrain = (CampaignTerrainAPI) uwResearchBaseEntity;
        DebrisFieldTerrainPlugin debrisTerrainPlugin = (DebrisFieldTerrainPlugin) debrisTerrain.getPlugin();
        debrisTerrainPlugin.params.defFaction = Factions.REMNANTS;
        debrisTerrainPlugin.params.defenderProb = 1f;
        int playerPower = SWP_Util.calculatePowerLevel(Global.getSector().getPlayerFleet());
        if (playerPower >= 1000) {
            debrisTerrainPlugin.params.maxDefenderSize = 4;
            debrisTerrainPlugin.params.minStr = 300;
            debrisTerrainPlugin.params.maxStr = 300;
        } else if (playerPower >= 500) {
            debrisTerrainPlugin.params.maxDefenderSize = 4;
            debrisTerrainPlugin.params.minStr = 200;
            debrisTerrainPlugin.params.maxStr = 200;
        } else if (playerPower >= 250) {
            debrisTerrainPlugin.params.maxDefenderSize = 3;
            debrisTerrainPlugin.params.minStr = 125;
            debrisTerrainPlugin.params.maxStr = 125;
        } else if (playerPower >= 125) {
            debrisTerrainPlugin.params.maxDefenderSize = 3;
            debrisTerrainPlugin.params.minStr = 75;
            debrisTerrainPlugin.params.maxStr = 75;
        } else {
            debrisTerrainPlugin.params.maxDefenderSize = 2;
            debrisTerrainPlugin.params.minStr = 50;
            debrisTerrainPlugin.params.maxStr = 50;
        }
        DropData dropData = new DropData();
        dropData.chances = 2;
        dropData.maxChances = 2;
        dropData.value = -1;
        dropData.group = "omega_weapons_small";
        uwResearchBaseEntity.addDropRandom(dropData);
        dropData = new DropData();
        dropData.chances = 8;
        dropData.maxChances = 8;
        dropData.value = -1;
        dropData.group = "rem_weapons2";
        uwResearchBaseEntity.addDropRandom(dropData);
        dropData = new DropData();
        dropData.chances = 3;
        dropData.maxChances = 3;
        dropData.value = -1;
        dropData.group = "any_hullmod_high";
        uwResearchBaseEntity.addDropRandom(dropData);
        dropData = new DropData();
        dropData.chances = 4;
        dropData.maxChances = 4;
        dropData.value = -1;
        dropData.group = "blueprints_low";
        uwResearchBaseEntity.addDropRandom(dropData);
        dropData = new DropData();
        dropData.chances = 1;
        dropData.maxChances = 1;
        dropData.value = -1;
        dropData.group = "rare_tech_low";
        uwResearchBaseEntity.addDropRandom(dropData);
        dropData = new DropData();
        dropData.chances = 2;
        dropData.maxChances = 2;
        dropData.value = -1;
        dropData.group = "ai_cores3";
        uwResearchBaseEntity.addDropRandom(dropData);
        spawnShipGraveyard(Factions.PIRATES, 4, 8, new LocData(uwResearchBaseEntity, false));
        spawnShipGraveyard(Factions.TRITACHYON, 4, 8, new LocData(uwResearchBaseEntity, false));

        elyon = ip.getPerson(ELYON);
        if (elyon == null) {
            elyon = Global.getFactory().createPerson();
            elyon.setId(ELYON);
            elyon.setFaction(Factions.TRITACHYON);
            elyon.setGender(Gender.ANY);
            elyon.setRankId(Ranks.UNKNOWN);
            elyon.setPostId(Ranks.POST_UNKNOWN);
            elyon.setImportance(PersonImportance.HIGH);
            elyon.getName().setFirst("Elyon");
            elyon.getName().setLast("");
            elyon.setPortraitSprite("graphics/swp/portraits/swp_portrait_ai1.png");
            ip.addPerson(elyon);
        }

        setStartingStage(Stage.FIND_FIRST_CLUE);
        addSuccessStages(Stage.COMPLETED);

        setStoryMission();

        connectWithCustomCondition(Stage.FIND_FIRST_CLUE, Stage.FIND_SECOND_CLUE, new ConditionChecker() {
            @Override
            public boolean conditionsMet() {
                return (numCluesFound >= 1) && (numCluesCanBeFound >= 2);
            }
        });
        connectWithCustomCondition(Stage.FIND_SECOND_CLUE, Stage.FIND_THIRD_CLUE, new ConditionChecker() {
            @Override
            public boolean conditionsMet() {
                return (numCluesFound >= 2) && (numCluesCanBeFound >= 3);
            }
        });
        connectWithCustomCondition(Stage.FIND_THIRD_CLUE, Stage.FIND_FINAL_CLUE, new ConditionChecker() {
            @Override
            public boolean conditionsMet() {
                return (numCluesFound >= 3) && (numCluesCanBeFound >= 4);
            }
        });
        connectWithCustomCondition(Stage.FIND_FIRST_CLUE, Stage.FIND_TREASURE, new ConditionChecker() {
            @Override
            public boolean conditionsMet() {
                return (numCluesFound >= 1) && (numCluesCanBeFound <= 1);
            }
        });
        connectWithCustomCondition(Stage.FIND_SECOND_CLUE, Stage.FIND_TREASURE, new ConditionChecker() {
            @Override
            public boolean conditionsMet() {
                return (numCluesFound >= 2) && (numCluesCanBeFound <= 2);
            }
        });
        connectWithCustomCondition(Stage.FIND_THIRD_CLUE, Stage.FIND_TREASURE, new ConditionChecker() {
            @Override
            public boolean conditionsMet() {
                return (numCluesFound >= 3) && (numCluesCanBeFound <= 3);
            }
        });
        connectWithCustomCondition(Stage.FIND_FINAL_CLUE, Stage.GO_TO_TREASURE, new ConditionChecker() {
            @Override
            public boolean conditionsMet() {
                return (numCluesFound >= 4) && (numCluesCanBeFound <= 4);
            }
        });

        beginStageTrigger(Stage.COMPLETED);
        triggerSetGlobalMemoryValue("$swpBT_missionCompleted", true);
        triggerMakeNonStoryCritical(rachmaninoffMarket, scavContactMarket);
        if (ttCluePossible) {
            triggerMakeNonStoryCritical(ttInsiderMarket);
        }
        endTrigger();

        setSystemWasUsedForStory(Stage.COMPLETED, leeSystem);

//        baird = getImportantPerson(People.BAIRD);
//        if (baird == null) {
//            return false;
//        }
//
//        callisto = getImportantPerson(People.IBRAHIM);
//        if (callisto == null) {
//            return false;
//        }
//
//        gargoyle = getImportantPerson(People.GARGOYLE);
//        if (gargoyle == null) {
//            return false;
//        }
//
//        culann = Global.getSector().getEconomy().getMarket("culann");
//        if (culann == null) {
//            return false;
//        }
//
//        donn = Global.getSector().getEconomy().getMarket("donn");
//        if (donn == null) {
//            return false;
//        }
//
//        arroyo = getImportantPerson(People.ARROYO);
//        if (arroyo == null) {
//            return false;
//        }
//
//        culannAdmin = getPersonAtMarketPost(culann, Ranks.POST_ADMINISTRATOR);
//        if (culannAdmin == null) {
//            return false;
//        }
//
//        well = (NascentGravityWellAPI) Global.getSector().getMemoryWithoutUpdate().get(TTBlackSite.NASCENT_WELL_KEY);
//        if (well == null || !well.isAlive()) {
//            return false;
//        }
//
//        float dir = Misc.getAngleInDegrees(culann.getLocationInHyperspace(), well.getLocationInHyperspace());
//
//        requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_UNSAFE, Tags.THEME_CORE, Tags.SYSTEM_ALREADY_USED_FOR_STORY);
//        preferSystemInDirectionFrom(culann.getLocationInHyperspace(), dir, 30f);
//        preferSystemWithinRangeOf(culann.getLocationInHyperspace(), 15f, 30f);
//        preferSystemWithinRangeOf(culann.getLocationInHyperspace(), 15f, 40f);
//        preferSystemUnexplored();
//        preferSystemNotPulsar();
//        requirePlanetNotGasGiant();
//        requirePlanetNotStar();
//        preferPlanetUnsurveyed();
//        baseRuins = pickPlanet(true);
//        if (baseRuins == null) {
//            return false;
//        }
//
//        relaySystem = baseRuins.getStarSystem();
//        if (relaySystem == null) {
//            return false;
//        }
//
//        relay = spawnEntity(Entities.GENERIC_PROBE, new LocData(EntityLocationType.HIDDEN, null, relaySystem));
//        if (relay == null) {
//            return false;
//        }
//        relay.setCustomDescriptionId("gaPZ_relay");
//
//        alphaSite = (StarSystemAPI) well.getTarget().getContainingLocation();
//
//        for (CampaignFleetAPI fleet : alphaSite.getFleets()) {
//            if (fleet.getMemoryWithoutUpdate().getBoolean("$ziggurat")) {
//                zigFleet = fleet;
//                break;
//            }
//        }
//        if (zigFleet == null) {
//            return false;
//        }
//
//        requireSystemIs(alphaSite);
//        requireEntityMemoryFlags("$hamatsu");
//        // hamatsu could be null if player salvaged it after dipping into alpha site then backing out
//        hamatsu = pickEntity();
//
//        paymentForCommFakes = genRoundNumber(10000, 15000);
//        paymentForCommFakesHigh = genRoundNumber(40000, 60000);
//        rkTithe = genRoundNumber(80000, 120000);
//
//        connectWithGlobalFlag(Stage.GET_KELISE_LEAD, Stage.SELL_BLACKMAIL_MATERIAL, "$gaPZ_sellBlackmail");
//        connectWithGlobalFlag(Stage.SELL_BLACKMAIL_MATERIAL, Stage.SOLD_BLACKMAIL_MATERIAL, "$gaPZ_soldBlackmail");
//        connectWithGlobalFlag(Stage.GET_KELISE_LEAD, Stage.TALK_TO_CALLISTO, "$gaPZ_talkToKallisto");
//        connectWithGlobalFlag(Stage.SOLD_BLACKMAIL_MATERIAL, Stage.TALK_TO_CALLISTO, "$gaPZ_talkToKallisto");
//        connectWithGlobalFlag(Stage.TALK_TO_CALLISTO, Stage.GO_TO_RELAY_SYSTEM, "$gaPZ_goToRelaySystem");
//        connectWithGlobalFlag(Stage.GO_TO_RELAY_SYSTEM, Stage.GO_TO_NASCENT_WELL, "$gaPZ_goToWell");
//        setStageOnEnteredLocation(Stage.INVESTIGATE_SITE, alphaSite);
//        //setStageOnGlobalFlag(Stage.RECOVER_ZIGGURAT, "$gaPZ_recoverZig");
//        setStageOnGlobalFlag(Stage.RETURN_TO_ACADEMY, SCANNED_ZIGGURAT);
//        setStageOnGlobalFlag(Stage.COMPLETED, "$gaPZ_completed");
//
//        //makeImportant(baird, null, Stage.TALK_TO_BAIRD);
//        //setStageOnMemoryFlag(Stage.COMPLETED, baird.getMarket(), "$gaPZ_completed");
//        makeImportant(culann, null, Stage.GET_KELISE_LEAD);
//        makeImportant(donn, null, Stage.SELL_BLACKMAIL_MATERIAL);
//        makeImportant(arroyo, null, Stage.SOLD_BLACKMAIL_MATERIAL);
//        makeImportant(callisto, null, Stage.TALK_TO_CALLISTO);
//        makeImportant(relay, "$gaPZ_relayImportant", Stage.GO_TO_RELAY_SYSTEM);
//        makeImportant(well, null, Stage.GO_TO_NASCENT_WELL);
//        makeImportant(zigFleet, "$gaPZ_ziggurat", Stage.INVESTIGATE_SITE);
//        makeImportant(baird, "$gaPZ_returnHere", Stage.RETURN_TO_ACADEMY);
//
//        setFlag(relay, "$gaPZ_relay", false);
//        setFlag(culannAdmin, "$gaPZ_culannAdmin", false, Stage.GET_KELISE_LEAD);
//        setFlag(baseRuins, "$gaPZ_baseRuins", false, Stage.GO_TO_RELAY_SYSTEM, Stage.GO_TO_NASCENT_WELL);
//
//        // Rogue Luddic Knight encounter as the player nears Arcadia
//        beginWithinHyperspaceRangeTrigger(callisto.getMarket(), 3f, true, Stage.TALK_TO_CALLISTO);
//        triggerCreateFleet(FleetSize.VERY_LARGE, FleetQuality.HIGHER, Factions.LUDDIC_CHURCH, FleetTypes.PATROL_LARGE, culann.getLocationInHyperspace());
//        triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
//        triggerMakeHostileAndAggressive();
//        triggerMakeLowRepImpact();
//        triggerFleetMakeFaster(true, 2, true);
//        triggerSetFleetAlwaysPursue();
//        triggerPickLocationTowardsEntity(callisto.getMarket().getStarSystem().getHyperspaceAnchor(), 30f, getUnits(1.5f));
//        triggerSpawnFleetAtPickedLocation("$gaPZ_rogueKnight", null);
//        triggerOrderFleetInterceptPlayer();
//        triggerOrderFleetEBurn(1f);
//        triggerFleetMakeImportant(null, Stage.TALK_TO_CALLISTO);
//        endTrigger();
//
//        // TriTach merc, phase fleet
//        beginEnteredLocationTrigger(relaySystem, Stage.GO_TO_RELAY_SYSTEM);
//        triggerCreateFleet(FleetSize.LARGE, FleetQuality.SMOD_2, Factions.MERCENARY, FleetTypes.MERC_BOUNTY_HUNTER, culann.getLocationInHyperspace());
//        triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
//        triggerSetFleetFaction(Factions.TRITACHYON);
//        triggerMakeHostileAndAggressive();
//        triggerSetFleetDoctrineComp(0, 0, 5);
//        triggerFleetMakeFaster(true, 1, true);
//        triggerPickLocationAtInSystemJumpPoint(relaySystem);
//        triggerSpawnFleetAtPickedLocation("$gaPZ_ttMerc", null);
//        triggerOrderFleetInterceptPlayer();
//        triggerFleetMakeImportant(null, Stage.GO_TO_RELAY_SYSTEM);
//        endTrigger();
        return true;
    }

    public static boolean staticCall(String action, String ruleId, InteractionDialogAPI dialog, List<Token> params,
            Map<String, MemoryAPI> memoryMap) {
        ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
        PersonAPI captainTannen = ip.getPerson(TANNEN);

        switch (action) {
            case "updateData": {
                MemoryAPI interactionMemory = memoryMap.get(MemKeys.LOCAL);
                if (interactionMemory == null) {
                    if (dialog.getInteractionTarget().getActivePerson() != null) {
                        interactionMemory = dialog.getInteractionTarget().getActivePerson().getMemoryWithoutUpdate();
                    } else {
                        interactionMemory = dialog.getInteractionTarget().getMemoryWithoutUpdate();
                    }
                }
                updateInteractionDataStatic(interactionMemory, false);
                return true;
            }

            case "declineForever":
            case "progressedQuest":
            case "firstRule":
            case "secondRule":
            case "thirdRule":
                /* Just do nothing if these get called */
                return false;

            case "recruitTannen": {
                FleetMemberAPI tannenShip = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "swp_afflictor_tannen");
                CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
                Global.getSector().getPlayerStats().getOfficerNumber().modifyFlat(TANNEN_OFFICER_BONUS, 1);
                tannenShip.setShipName("Star Runner");
                tannenShip.setVariant(tannenShip.getVariant().clone(), false, false);
                tannenShip.getVariant().setSource(VariantSource.REFIT);
                tannenShip.getVariant().addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
                tannenShip.getVariant().addTag(TANNEN_SHIP);
                playerFleet.getFleetData().addFleetMember(tannenShip);
                playerFleet.getCargo().addCrew(Math.round(tannenShip.getMinCrew()));
                playerFleet.getFleetData().addOfficer(captainTannen);
                Misc.setUnremovable(captainTannen, true);
                tannenShip.setCaptain(captainTannen);
                playerFleet.getFleetData().setSyncNeeded();
                playerFleet.getFleetData().syncIfNeeded();
                tannenShip.getRepairTracker().setCR(tannenShip.getRepairTracker().getMaxCR());
                captainTannen.setRankId(Ranks.SPACE_CAPTAIN);
                captainTannen.setPostId(Ranks.POST_OFFICER);
                captainTannen.getStats().setSkillLevel(Skills.WOLFPACK_TACTICS, 0);
                captainTannen.getStats().setSkillLevel(Skills.PHASE_CORPS, 0);
                captainTannen.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 0);
                if (dialog != null) {
                    AddRemoveCommodity.addOfficerGainText(captainTannen, dialog.getTextPanel());
                    AddRemoveCommodity.addFleetMemberGainText(tannenShip, dialog.getTextPanel());
                    AddRemoveCommodity.addCommodityGainText(Commodities.CREW, Math.round(tannenShip.getMinCrew()), dialog.getTextPanel());
                }
                Global.getSector().addScript(new TannenScript());
                if (!Global.getSector().getListenerManager().hasListenerOfClass(TannenListener.class)) {
                    Global.getSector().getListenerManager().addListener(new TannenListener());
                }
                return true;
            }

            case "gameOver": {
                Global.getSector().addScript(new EveryFrameScript() {
                    @Override
                    public boolean isDone() {
                        return false;
                    }

                    @Override
                    public boolean runWhilePaused() {
                        return true;
                    }

                    @Override
                    public void advance(float amount) {
                        Global.getSector().getCampaignUI().cmdExitWithoutSaving();
                    }
                });
                return true;
            }

            case "restoreTannen": {
                CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
                playerFleet.getFleetData().addOfficer(captainTannen);
                Misc.setUnremovable(captainTannen, true);
                captainTannen.setRankId(Ranks.SPACE_CAPTAIN);
                captainTannen.setPostId(Ranks.POST_OFFICER);
                captainTannen.getStats().setSkillLevel(Skills.WOLFPACK_TACTICS, 0);
                captainTannen.getStats().setSkillLevel(Skills.PHASE_CORPS, 0);
                captainTannen.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 0);
                FleetMemberAPI tannenShip = null;
                for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
                    if (member.getVariant().hasTag(TANNEN_SHIP)) {
                        tannenShip = member;
                    }
                }
                if (tannenShip != null) {
                    tannenShip.setCaptain(captainTannen);
                }
                playerFleet.getFleetData().setSyncNeeded();
                playerFleet.getFleetData().syncIfNeeded();
                return true;
            }

            case "removeTannen": {
                CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
                OfficerDataAPI tannenData = playerFleet.getFleetData().getOfficerData(captainTannen);
                if (tannenData != null) {
                    playerFleet.getFleetData().removeOfficer(captainTannen);
                }
                Misc.setUnremovable(captainTannen, false);
                Global.getSector().getPlayerStats().getOfficerNumber().unmodify(TANNEN_OFFICER_BONUS);
                FleetMemberAPI tannenShip = null;
                for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
                    if (member.getVariant().hasTag(TANNEN_SHIP)) {
                        tannenShip = member;
                    }
                }
                if (tannenShip != null) {
                    playerFleet.getFleetData().removeFleetMember(tannenShip);
                    playerFleet.getCargo().removeCrew(Math.round(tannenShip.getMinCrew()));
                }
                playerFleet.getFleetData().setSyncNeeded();
                playerFleet.getFleetData().syncIfNeeded();
                if (dialog != null) {
                    AddRemoveCommodity.addOfficerLossText(captainTannen, dialog.getTextPanel());
                    if (tannenShip != null) {
                        AddRemoveCommodity.addFleetMemberLossText(tannenShip, dialog.getTextPanel());
                        AddRemoveCommodity.addCommodityLossText(Commodities.CREW, Math.round(tannenShip.getMinCrew()), dialog.getTextPanel());
                    }
                }
                captainTannen.setFaction(Factions.PIRATES);
                captainTannen.setRankId(Ranks.SPACE_COMMANDER);
                captainTannen.setPostId(Ranks.POST_PATROL_COMMANDER);
                captainTannen.getStats().setSkillLevel(Skills.WOLFPACK_TACTICS, 1);
                captainTannen.getStats().setSkillLevel(Skills.PHASE_CORPS, 1);
                captainTannen.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);
                Global.getSector().removeScriptsOfClass(TannenScript.class);
                Global.getSector().getListenerManager().removeListenerOfClass(TannenListener.class);
                return true;
            }

            case "killedTannen": {
                CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
                OfficerDataAPI tannenData = playerFleet.getFleetData().getOfficerData(captainTannen);
                if (tannenData != null) {
                    playerFleet.getFleetData().removeOfficer(captainTannen);
                }
                Misc.setUnremovable(captainTannen, false);
                Global.getSector().getPlayerStats().getOfficerNumber().unmodify(TANNEN_OFFICER_BONUS);
                playerFleet.getFleetData().setSyncNeeded();
                playerFleet.getFleetData().syncIfNeeded();
                if (dialog != null) {
                    addOfficerDeathText(captainTannen, dialog.getTextPanel());
                }
                FleetMemberAPI tannenShip = null;
                for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
                    if (member.getVariant().hasTag(TANNEN_SHIP)) {
                        tannenShip = member;
                    }
                }
                if (tannenShip != null) {
                    tannenShip.getVariant().removeTag(Tags.SHIP_CAN_NOT_SCUTTLE);
                    tannenShip.getVariant().removeTag(TANNEN_SHIP);
                }
                captainTannen.setFaction(Factions.PIRATES);
                captainTannen.setRankId(Ranks.SPACE_COMMANDER);
                captainTannen.setPostId(Ranks.POST_PATROL_COMMANDER);
                captainTannen.getStats().setSkillLevel(Skills.WOLFPACK_TACTICS, 1);
                captainTannen.getStats().setSkillLevel(Skills.PHASE_CORPS, 1);
                captainTannen.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);
                Global.getSector().removeScriptsOfClass(TannenScript.class);
                Global.getSector().getListenerManager().removeListenerOfClass(TannenListener.class);
                return true;
            }

            case "learnAboutExcelsior":
                if (Global.getSector().getMemoryWithoutUpdate().getBoolean(EXCELSIOR_DESC_ALT_KEY)) {
                    Global.getSector().getMemoryWithoutUpdate().set(EXCELSIOR_DESC_ALT_KEY, false);
                    Global.getSettings().getDescription("swp_excelsior", Description.Type.SHIP).setText1(SWPModPlugin.EXCELSIOR_DESCRIPTION);
                }
                return true;

            default:
                break;
        }

        return false;
    }

    @Override
    protected boolean callAction(String action, String ruleId, InteractionDialogAPI dialog, List<Token> params,
            Map<String, MemoryAPI> memoryMap) {
        switch (action) {
            case "endNowDebug":
                endAbandon();
                endImmediately();
                return true;

            case "yearLateDebug": {
                double daysPerMil = Global.getSector().getClock().getElapsedDaysSince(Global.getSector().getClock().getTimestamp() - 1000000L);
                long timestampDiff = (long) (1000000.0 * (366.0 / daysPerMil));
                Global.getSector().getMemoryWithoutUpdate().set("$swpBT_lastProgressTimestamp", Global.getSector().getClock().getTimestamp() - timestampDiff);
                return true;
            }

            case "confrontNowDebug":
                Global.getSector().getMemoryWithoutUpdate().set("$swpBT_lastCompletionTimer", 0f);
                return true;

            case "genBreadcrumbsDebug": {
                resetSearch();
                requireSystemTags(ReqMode.NOT_ANY, Tags.THEME_HIDDEN, Tags.THEME_REMNANT_RESURGENT, "ix_fortified", "theme_breakers_suppressed",
                        "theme_breakers_resurgent", Tags.THEME_CORE, Tags.SYSTEM_ALREADY_USED_FOR_STORY);
                requireSystemNotHasPulsar();
                requireSystem(new SystemHasNoColonyReq());
                preferSystemUnexplored();
                if (getGenRandom().nextFloat() < 0.75) {
                    preferSystemOnFringeOfSector();
                }
                StarSystemAPI system = pickSystem();
                if (system != null) {
                    genBreadcrumbs(system);
                }
                return true;
            }

            case "findClueDebug":
                numCluesFound = Math.min(numCluesCanBeFound, numCluesFound + 1);
                return true;

            case "printLocationsDebug":
                dialog.getTextPanel().addPara("Lee: %s", Misc.getHighlightColor(), leeSystem.getName());
                dialog.getTextPanel().addPara("Research Base: %s", Misc.getHighlightColor(), uwResearchBaseSystem.getName());
                return true;

            case "makeLameTTContactDebug": {
                resetSearch();
                requireMarketNotHidden();
                requireMarketFaction(Factions.TRITACHYON);
                preferMarketIsNot(ttInsiderMarket);
                preferMarketStabilityAtLeast(1);
                MarketAPI contactMarket = pickMarket();
                if (contactMarket != null) {
                    PersonAPI newTTContact = findOrCreatePerson(Factions.TRITACHYON, contactMarket, false, Ranks.CITIZEN, Ranks.POST_SPACER);
                    if (newTTContact != null) {
                        newTTContact.setImportance(PersonImportance.VERY_LOW);
                        newTTContact.addTag(Tags.CONTACT_TRADE);
                        ContactIntel.addPotentialContact(1f, newTTContact, contactMarket, dialog != null ? dialog.getTextPanel() : null);
                    }
                }
                return true;
            }

            case "makeTTContactDebug": {
                resetSearch();
                requireMarketNotHidden();
                requireMarketFaction(Factions.TRITACHYON);
                preferMarketIsNot(ttInsiderMarket);
                preferMarketStabilityAtLeast(1);
                MarketAPI contactMarket = pickMarket();
                if (contactMarket != null) {
                    PersonAPI newTTContact = findOrCreatePerson(Factions.TRITACHYON, contactMarket, false, Ranks.CITIZEN, Ranks.POST_ENTREPRENEUR);
                    if (newTTContact != null) {
                        newTTContact.setImportance(PersonImportance.MEDIUM);
                        newTTContact.addTag(Tags.CONTACT_TRADE);
                        ContactIntel.addPotentialContact(1f, newTTContact, contactMarket, dialog != null ? dialog.getTextPanel() : null);
                    }
                }
                return true;
            }

            case "makeLameUWContactDebug": {
                resetSearch();
                requireMarketNotHidden();
                preferMarketStabilityAtLeast(1);
                MarketAPI contactMarket = pickMarket();
                if (contactMarket != null) {
                    PersonAPI newUWContact = findOrCreateCriminal(contactMarket, false);
                    if (newUWContact != null) {
                        newUWContact.setImportance(PersonImportance.VERY_LOW);
                        newUWContact.addTag(Tags.CONTACT_UNDERWORLD);
                        ContactIntel.addPotentialContact(1f, newUWContact, contactMarket, dialog != null ? dialog.getTextPanel() : null);
                    }
                }
                return true;
            }

            case "makeUWContactDebug": {
                resetSearch();
                requireMarketNotHidden();
                preferMarketStabilityAtLeast(1);
                MarketAPI contactMarket = pickMarket();
                if (contactMarket != null) {
                    PersonAPI newUWContact = findOrCreateCriminal(contactMarket, false);
                    if (newUWContact != null) {
                        newUWContact.setImportance(PersonImportance.MEDIUM);
                        newUWContact.addTag(Tags.CONTACT_UNDERWORLD);
                        ContactIntel.addPotentialContact(1f, newUWContact, contactMarket, dialog != null ? dialog.getTextPanel() : null);
                    }
                }
                return true;
            }

            case "setTTBarProb": {
                ttBarProb = params.get(1).getFloat(memoryMap);
                return true;
            }

            case "setUWBarGenericProb": {
                uwBarGenericProb = params.get(1).getFloat(memoryMap);
                uwBarGenericAgainProb = uwBarGenericProb;
                return true;
            }

            case "setUWBarPiratesProb": {
                uwBarPiratesProb = params.get(1).getFloat(memoryMap);
                uwBarPiratesAgainProb = uwBarPiratesProb;
                return true;
            }

            case "printIntel":
                sendUpdateForNextStep(NEXT_STEP_UPDATE, dialog == null ? null : dialog.getTextPanel());
                return true;

            case "printIntelRules":
                sendUpdateForNextStep(PRINT_RULES, dialog == null ? null : dialog.getTextPanel());
                return true;

            case "declineForever": {
                PortsideBarEvent ourBarEvent = null;
                for (PortsideBarEvent barEvent : BarEventManager.getInstance().getActive().getItems()) {
                    if (barEvent.getBarEventId().contentEquals(BURIED_TREASURE)) {
                        ourBarEvent = barEvent;
                        break;
                    }
                }
                if (ourBarEvent == null) {
                    return false;
                }
                BarEventManager.getInstance().notifyWasInteractedWith(ourBarEvent);
                GenericBarEventCreator ourCreator = BarEventManager.getInstance().getCreatorFor(ourBarEvent);
                if (ourCreator == null) {
                    return false;
                }
                BarEventManager.getInstance().setTimeout(ourCreator.getClass(), 10000000000f);
                return true;
            }

            case "progressedQuest":
                Global.getSector().getMemoryWithoutUpdate().set("$swpBT_numNoProgressEvents", 0);
                Global.getSector().getMemoryWithoutUpdate().set("$swpBT_lastProgressTimestamp", Global.getSector().getClock().getTimestamp());
                return true;

            case "firstRule":
                agreedToFirstRule = params.get(1).getBoolean(memoryMap);
                return true;

            case "secondRule":
                agreedToSecondRule = params.get(1).getBoolean(memoryMap);
                return true;

            case "thirdRule": {
                int generalAwareness = 0;
                int uwAwareness = 0;
                if (Global.getSector().getMemoryWithoutUpdate().contains("$swpBT_generalAwareness")) {
                    generalAwareness = Global.getSector().getMemoryWithoutUpdate().getInt("$swpBT_generalAwareness");
                }
                if (Global.getSector().getMemoryWithoutUpdate().contains("$swpBT_UWAwareness")) {
                    uwAwareness = Global.getSector().getMemoryWithoutUpdate().getInt("$swpBT_UWAwareness");
                }
                if (agreedToThirdRule && !params.get(1).getBoolean(memoryMap)) {
                    /* Tannen tells his friends now that the secrecy rule is gone */
                    Global.getSector().getMemoryWithoutUpdate().set("$swpBT_generalAwareness", generalAwareness + 1 + numCluesTannenKnows);
                    Global.getSector().getMemoryWithoutUpdate().set("$swpBT_UWAwareness", uwAwareness + 1 + numCluesTannenKnows);
                }
                if (!agreedToThirdRule && params.get(1).getBoolean(memoryMap)) {
                    Global.getSector().getMemoryWithoutUpdate().set("$swpBT_generalAwareness", generalAwareness - 1);
                    Global.getSector().getMemoryWithoutUpdate().set("$swpBT_UWAwareness", uwAwareness - 1);
                }
                agreedToThirdRule = params.get(1).getBoolean(memoryMap);
                return true;
            }

            case "brokeThirdRule":
                brokeThirdRule = true;
                return true;

            /* Non-static portion - fall through to static portion instead of returning */
            case "killedTannen":
                killedTannen = true;
                break;

            case "killedTannenBattle":
                killedTannen = true;
                return true;

            case "defeatedTannen":
                Global.getSector().getMemoryWithoutUpdate().set("$swpBT_defeatedTannenTimestamp", Global.getSector().getClock().getTimestamp());
                Global.getSector().getMemoryWithoutUpdate().set("$swpBT_defeatedTannenTimer", MathUtils.getRandomNumberInRange(300f, 400f));
                return true;

            case "activateRachmaninoff":
                if (!activateRachmaninoff) {
                    makeImportant(rachmaninoff, null, Stage.FIND_FIRST_CLUE, Stage.FIND_SECOND_CLUE, Stage.FIND_THIRD_CLUE, Stage.FIND_FINAL_CLUE);
                    Misc.makeStoryCritical(rachmaninoffMarket, BURIED_TREASURE);
                    activateRachmaninoff = true;
                    numCluesCanBeFound++;
                    numLeadsKnown++;
                }
                return true;

            case "activateScavContact":
                if (!activateScavContact) {
                    makeImportant(scavContact, null, Stage.FIND_FIRST_CLUE, Stage.FIND_SECOND_CLUE, Stage.FIND_THIRD_CLUE, Stage.FIND_FINAL_CLUE);
                    Misc.makeStoryCritical(scavContactMarket, BURIED_TREASURE);
                    activateScavContact = true;
                    numCluesCanBeFound++;
                    numLeadsKnown++;
                }
                return true;

            case "activateTTClue":
            case "activateTTClue2":
                if (!activateTTClue) {
                    if (ttContact != null) {
                        makeUnimportant(ttContact);
                    }
                    makeImportant(ttInsider, null, Stage.FIND_FIRST_CLUE, Stage.FIND_SECOND_CLUE, Stage.FIND_THIRD_CLUE, Stage.FIND_FINAL_CLUE);
                    activateTTClue = true;
                    numLeadsKnown++;
                    if (action.contentEquals("activateTTClue2")) {
                        ttClueLimitedInfo = true;
                    }
                }
                return true;

            case "activateUWClue":
            case "activateUWClue2":
                if (!activateUWClue) {
                    if (uwContact != null) {
                        makeUnimportant(uwContact);
                    }
                    setSystemWasUsedForStory(Stage.COMPLETED, uwResearchBaseSystem);
                    makeImportant(uwResearchBaseEntity, null, Stage.FIND_FIRST_CLUE, Stage.FIND_SECOND_CLUE, Stage.FIND_THIRD_CLUE, Stage.FIND_FINAL_CLUE);
                    makePrimaryObjective(uwResearchBaseEntity); // Very important; should basically always be the primary objective if available
                    activateUWClue = true;
                    numLeadsKnown++;
                    if (action.contentEquals("activateUWClue2")) {
                        uwClueLimitedInfo = true;
                    }
                }
                if (!action.contentEquals("activateUWClue2")) {
                    uwClueLimitedInfo = false;
                }
                return true;

            case "completeRachmaninoff":
                if (!completeRachmaninoff) {
                    makeUnimportant(rachmaninoff);
                    Misc.makeNonStoryCritical(rachmaninoffMarket, BURIED_TREASURE);
                    completeRachmaninoff = true;
                    numCluesFound++;
                    Global.getSector().getMemoryWithoutUpdate().set("$swpBT_lastCompletionTimestamp", Global.getSector().getClock().getTimestamp());
                    Global.getSector().getMemoryWithoutUpdate().set("$swpBT_lastCompletionTimer", MathUtils.getRandomNumberInRange(20f, 40f));
                    Global.getSector().getMemoryWithoutUpdate().set("$swpBT_completionCount", numCluesFound);
                }
                return true;

            case "completeScavContact":
                if (!completeScavContact) {
                    makeUnimportant(scavContact);
                    Misc.makeNonStoryCritical(scavContactMarket, BURIED_TREASURE);
                    completeScavContact = true;
                    numCluesFound++;
                    Global.getSector().getMemoryWithoutUpdate().set("$swpBT_lastCompletionTimestamp", Global.getSector().getClock().getTimestamp());
                    Global.getSector().getMemoryWithoutUpdate().set("$swpBT_lastCompletionTimer", MathUtils.getRandomNumberInRange(20f, 40f));
                    Global.getSector().getMemoryWithoutUpdate().set("$swpBT_completionCount", numCluesFound);
                }
                return true;

            case "completeTTClue":
                if (!completeTTClue && ttCluePossible) {
                    if (ttContact != null) {
                        makeUnimportant(ttContact);
                    }
                    makeUnimportant(ttInsider);
                    Misc.makeNonStoryCritical(ttInsiderMarket, BURIED_TREASURE);
                    completeTTClue = true;
                    numCluesFound++;
                    Global.getSector().getMemoryWithoutUpdate().set("$swpBT_lastCompletionTimestamp", Global.getSector().getClock().getTimestamp());
                    Global.getSector().getMemoryWithoutUpdate().set("$swpBT_lastCompletionTimer", MathUtils.getRandomNumberInRange(20f, 40f));
                    Global.getSector().getMemoryWithoutUpdate().set("$swpBT_completionCount", numCluesFound);
                }
                return true;

            case "completeUWClue":
                if (!completeUWClue) {
                    if (uwContact != null) {
                        makeUnimportant(uwContact);
                    }
                    makeUnimportant(uwResearchBaseEntity);
                    completeUWClue = true;
                    numCluesFound++;
                    Global.getSector().getMemoryWithoutUpdate().set("$swpBT_lastCompletionTimestamp", Global.getSector().getClock().getTimestamp());
                    Global.getSector().getMemoryWithoutUpdate().set("$swpBT_lastCompletionTimer", MathUtils.getRandomNumberInRange(20f, 40f));
                    Global.getSector().getMemoryWithoutUpdate().set("$swpBT_completionCount", numCluesFound);
                }
                return true;

            case "didCompletionEvent": {
                if (!agreedToThirdRule) {
                    /* Tannen tells his friends */
                    int generalAwareness = 0;
                    int uwAwareness = 0;
                    if (Global.getSector().getMemoryWithoutUpdate().contains("$swpBT_generalAwareness")) {
                        generalAwareness = Global.getSector().getMemoryWithoutUpdate().getInt("$swpBT_generalAwareness");
                    }
                    if (Global.getSector().getMemoryWithoutUpdate().contains("$swpBT_UWAwareness")) {
                        uwAwareness = Global.getSector().getMemoryWithoutUpdate().getInt("$swpBT_UWAwareness");
                    }
                    Global.getSector().getMemoryWithoutUpdate().set("$swpBT_generalAwareness", generalAwareness + (numCluesFound - numCluesTannenKnows));
                    Global.getSector().getMemoryWithoutUpdate().set("$swpBT_UWAwareness", uwAwareness + (numCluesFound - numCluesTannenKnows));
                }
                int tannenAwareness = 0;
                if (Global.getSector().getMemoryWithoutUpdate().contains("$swpBT_TannenAwareness")) {
                    tannenAwareness = Global.getSector().getMemoryWithoutUpdate().getInt("$swpBT_TannenAwareness");
                }
                Global.getSector().getMemoryWithoutUpdate().set("$swpBT_TannenAwareness", tannenAwareness + (numCluesFound - numCluesTannenKnows));
                Global.getSector().getMemoryWithoutUpdate().set("$swpBT_lastCompletionTimestamp", Global.getSector().getClock().getTimestamp());
                Global.getSector().getMemoryWithoutUpdate().set("$swpBT_completionEventCount", numCluesFound);
                numCluesTannenKnows = numCluesFound;
                return true;
            }

            case "escapedCompletionEvent":
                Global.getSector().getMemoryWithoutUpdate().set("$swpBT_lastCompletionTimestamp", Global.getSector().getClock().getTimestamp());
                Global.getSector().getMemoryWithoutUpdate().set("$swpBT_completionEventCount", numCluesFound);
                return true;

            case "skippedCompletionEvent":
                Global.getSector().getMemoryWithoutUpdate().set("$swpBT_lastCompletionTimestamp", Global.getSector().getClock().getTimestamp());
                Global.getSector().getMemoryWithoutUpdate().set("$swpBT_lastCompletionTimer", MathUtils.getRandomNumberInRange(90f, 120f));
                return true;

            case "sendUWThugs": {
                String marketId = params.get(1).getString(memoryMap);
                MarketAPI market = Global.getSector().getEconomy().getMarket(marketId);

                FleetSize thugsSize;
                int playerPower = SWP_Util.calculatePowerLevel(Global.getSector().getPlayerFleet());
                if (playerPower >= 1000) {
                    thugsSize = FleetSize.HUGE;
                } else if (playerPower >= 500) {
                    thugsSize = FleetSize.VERY_LARGE;
                } else if (playerPower >= 250) {
                    thugsSize = FleetSize.LARGE;
                } else if (playerPower >= 125) {
                    thugsSize = FleetSize.MEDIUM;
                } else {
                    thugsSize = FleetSize.SMALL;
                }

                beginWithinHyperspaceRangeTrigger(market.getPrimaryEntity(), 3f, false, Stage.FIND_FIRST_CLUE, Stage.FIND_SECOND_CLUE, Stage.FIND_THIRD_CLUE, Stage.FIND_FINAL_CLUE);
                triggerCreateFleet(thugsSize, FleetQuality.HIGHER, Factions.PIRATES, FleetTypes.MERC_PATROL, market.getPrimaryEntity());
                triggerSetFleetFaction(Factions.PIRATES);
                triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
                triggerFleetSetName("Thugs");
                triggerMakeHostileAndAggressive();
                triggerMakeLowRepImpact();
                triggerFleetAllowLongPursuit();
                triggerSetFleetAlwaysPursue();
                triggerFleetMakeFaster(true, 2, true);
                triggerPickLocationAroundEntity(market.getPrimaryEntity(), DEFAULT_MIN_DIST_FROM_PLAYER);
                triggerSetFleetMissionRef("$swpBT_ref");
                triggerSpawnFleetAtPickedLocation(UW_THUGS_FLAG, null);
                triggerOrderFleetEBurn(1f);
                triggerFleetMakeImportantPermanent(null);
                triggerOrderFleetInterceptPlayer();
                triggerFleetAddDefeatTriggerPermanent("swpBTDefeatThugs");
                endTrigger();
                return true;
            }

            /* Non-static portion - fall through to static portion instead of returning */
            case "learnAboutExcelsior":
                leeEntity.setCustomDescriptionId("swp_excelsior_derelict");
                makeImportantDoNotShowAsIntelMapLocation(leeEntity, null, Stage.FIND_FIRST_CLUE, Stage.FIND_SECOND_CLUE, Stage.FIND_THIRD_CLUE, Stage.FIND_FINAL_CLUE, Stage.FIND_TREASURE);
                makeImportant(leeEntity, null, Stage.GO_TO_TREASURE);
                break;

            default:
                break;
        }

        return staticCall(action, ruleId, dialog, params, memoryMap);
    }

    public static void addOfficerDeathText(PersonAPI officer, TextPanelAPI text) {
        text.setFontSmallInsignia();
        String rank = officer.getRank();
        if (rank != null) {
            rank = Misc.ucFirst(rank);
        }
        String str = officer.getName().getFullName();
        if (rank != null) {
            str = rank + " " + str;
        }
        text.addParagraph(str + " has died", Misc.getNegativeHighlightColor());
        text.highlightInLastPara(Misc.getHighlightColor(), str);
        text.setFontInsignia();
    }

    @Override
    protected void notifyEnded() {
        super.notifyEnded();
        Global.getSector().getMemoryWithoutUpdate().set("$swpBT_missionEnded", true);

        Global.getSector().getListenerManager().removeListener(this);

        /* Fallback in case something goes wrong */
        Misc.makeNonStoryCritical(rachmaninoffMarket, BURIED_TREASURE);
        Misc.makeNonStoryCritical(scavContactMarket, BURIED_TREASURE);
        if (ttCluePossible) {
            Misc.makeNonStoryCritical(ttInsiderMarket, BURIED_TREASURE);
        }
    }

    private void updateKnownContactRefs() {
        PersonAPI medOrBetterTTContact = null;
        PersonAPI medOrBetterPirateContact = null;
        for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(ContactIntel.class)) {
            ContactIntel contactIntel = (ContactIntel) intel;
            float importance = contactIntel.getPerson().getImportance().getValue();
            if (contactIntel.getPerson().getFaction().getId().contentEquals(Factions.TRITACHYON) && (importance >= PersonImportance.MEDIUM.getValue())) {
                if ((medOrBetterTTContact == null) || (importance > medOrBetterTTContact.getImportance().getValue())) {
                    medOrBetterTTContact = contactIntel.getPerson();
                }
            }
            if (contactIntel.getPerson().getFaction().getId().contentEquals(Factions.PIRATES)
                    && contactIntel.getPerson().hasTag(Tags.CONTACT_UNDERWORLD) && (importance >= PersonImportance.MEDIUM.getValue())) {
                if ((medOrBetterPirateContact == null) || (importance > medOrBetterPirateContact.getImportance().getValue())) {
                    medOrBetterPirateContact = contactIntel.getPerson();
                }
            }
        }

        if (ttContact != medOrBetterTTContact) {
            if (ttContact != null) {
                makeUnimportant(ttContact);
            }
            ttContact = medOrBetterTTContact;
            if ((ttContact != null) && ttCluePossible && !activateTTClue && !completeTTClue
                    && ((currentStage == Stage.FIND_FIRST_CLUE)
                    || (currentStage == Stage.FIND_SECOND_CLUE)
                    || (currentStage == Stage.FIND_THIRD_CLUE)
                    || (currentStage == Stage.FIND_FINAL_CLUE))) {
                makeImportant(ttContact, null, Stage.FIND_FIRST_CLUE, Stage.FIND_SECOND_CLUE, Stage.FIND_THIRD_CLUE, Stage.FIND_FINAL_CLUE);
            }
        }
        if (uwContact != medOrBetterPirateContact) {
            if (uwContact != null) {
                makeUnimportant(uwContact);
            }
            uwContact = medOrBetterPirateContact;
            if ((uwContact != null) && !activateUWClue && !completeUWClue
                    && ((currentStage == Stage.FIND_FIRST_CLUE)
                    || (currentStage == Stage.FIND_SECOND_CLUE)
                    || (currentStage == Stage.FIND_THIRD_CLUE)
                    || (currentStage == Stage.FIND_FINAL_CLUE))) {
                makeImportant(uwContact, null, Stage.FIND_FIRST_CLUE, Stage.FIND_SECOND_CLUE, Stage.FIND_THIRD_CLUE, Stage.FIND_FINAL_CLUE);
            }
        }
    }

    private static void updateInteractionDataStatic(MemoryAPI interactionMemory, boolean cascade) {
        int playerCombatLevel = 0;
        int playerMinCombatLevel = 0;
        for (SkillLevelAPI skill : Global.getSector().getPlayerStats().getSkillsCopy()) {
            if (skill.getSkill().isCombatOfficerSkill()) {
                if (Math.round(skill.getLevel()) >= 1) {
                    playerCombatLevel += 10;
                    playerCombatLevel += Math.round(skill.getLevel()) - 1;
                }
            }
            /* Being a skilled marine commander gives you a leg up when it comes to CQC */
            if (skill.getSkill().getId().contentEquals(Skills.TACTICAL_DRILLS)) {
                playerCombatLevel += 10;
                playerMinCombatLevel = 55;
            }
        }
        playerCombatLevel = Math.max(playerCombatLevel, playerMinCombatLevel);

        ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
        PersonAPI captainTannen = ip.getPerson(TANNEN);
        PersonAPI rachmaninoff = ip.getPerson(RACHMANINOFF);
        PersonAPI mouse = ip.getPerson(MOUSE);
        PersonAPI ttInsider = ip.getPerson(TT_INSIDER);

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        OfficerDataAPI tannenData = null;
        if (playerFleet != null) {
            tannenData = playerFleet.getFleetData().getOfficerData(captainTannen);
        }

        setStatic(interactionMemory, "$swpBT_neverFindOut", "never get to find out what the \"buried treasure\" was");
        setStatic(interactionMemory, "$swpBT_mateOrLuv", (Global.getSector().getPlayerPerson().getGender() == Gender.MALE) ? "mate" : "luv");
        setStatic(interactionMemory, "$swpBT_bastardOrBitch", (Global.getSector().getPlayerPerson().getGender() == Gender.MALE) ? "bastard" : "bitch");
        setStatic(interactionMemory, "$swpBT_ladOrLass", (Global.getSector().getPlayerPerson().getGender() == Gender.MALE) ? "lad"
                : ((Global.getSector().getPlayerPerson().getGender() == Gender.FEMALE) ? "lass" : "fellow"));
        setStatic(interactionMemory, "$swpBT_playerCombatLevel", playerCombatLevel);
        setStatic(interactionMemory, "$swpBT_playerCombatPct", "" + playerCombatLevel + "%");
        setStatic(interactionMemory, "$swpBT_emotionalAppeal", (Global.getSector().getPlayerPerson().getGender() == Gender.MALE)
                ? "a particularly effective emotional appeal" : "genuine compassion and comfort");
        setStatic(interactionMemory, "$swpBT_appealOption", (Global.getSector().getPlayerPerson().getGender() == Gender.MALE)
                ? "Make an emotional appeal" : "Show compassion and comfort");
        setStatic(interactionMemory, "$swpBT_shootTheShit", (Global.getSector().getPlayerPerson().getGender() == Gender.MALE)
                ? "shoot the shit wit'ye" : "flirt fer a bit");
        setStatic(interactionMemory, "$swpBT_lookinFerLadies", (Global.getSector().getPlayerPerson().getGender() == Gender.MALE)
                ? "lookin' fer ladies with ye" : "take ye to bed fer a night");
        setStatic(interactionMemory, "$swpBT_handsomeBastard", (Global.getSector().getPlayerPerson().getGender() == Gender.MALE)
                ? "handsome bastard" : "cunning vixen");
        setStatic(interactionMemory, "$swpBT_playerMale", Global.getSector().getPlayerPerson().getGender() == Gender.MALE);
        setStatic(interactionMemory, "$swpBT_silentHandRecall", "recall something important about The Silent Hand");
        setStatic(interactionMemory, "$swpBT_doubleIt", "Double it");
        if (captainTannen != null) {
            setStatic(interactionMemory, "$swpBT_tannenRepPct", Math.round(captainTannen.getRelToPlayer().getRel() * 100f));
        }
        if (rachmaninoff != null) {
            setStatic(interactionMemory, "$swpBT_rachmaninoffName", rachmaninoff.getNameString());
        }
        setStatic(interactionMemory, "$swpBT_scavContactRaidDifficulty", SCAV_CONTACT_RAID_DIFFICULTY);
        setStatic(interactionMemory, "$swpBT_scavContactMarines", Misc.getWithDGS(getAdjustedMarinesRequired(Math.round(SCAV_CONTACT_RAID_DIFFICULTY))));
        if (ttInsider != null) {
            setStatic(interactionMemory, "$swpBT_ttInsiderName", ttInsider.getNameString());
        }
        if (mouse != null) {
            setStatic(interactionMemory, "$swpBT_mouseRepPct", Math.round(mouse.getRelToPlayer().getRel() * 100f));
        }
        setStatic(interactionMemory, "$swpBT_tenDays", "at least ten days");
        setStatic(interactionMemory, "$swpBT_mediumImportance", PersonImportance.MEDIUM.getDisplayName());
        setStatic(interactionMemory, "$swpBT_manOrWoman", (Global.getSector().getPlayerPerson().getGender() == Gender.MALE) ? "man"
                : ((Global.getSector().getPlayerPerson().getGender() == Gender.FEMALE) ? "woman" : "fellow"));
        setStatic(interactionMemory, "$swpBT_applyPressure", "apply some pressure");
        setStatic(interactionMemory, "$swpBT_spilledTheBeans", "You have a sinking feeling that, sooner or later, Captain Tannen will find out that you spilled the beans.");
        setStatic(interactionMemory, "$swpBT_thugWarningMessage", "////////////////////////////////////////\n "
                + "UNKNOWN FLEET CONTACT. XPDR OFF. APPROACHING AT MAXIMUM BURN. PLEASE ADVISE.\n"
                + "////////////////////////////////////////");
        setStatic(interactionMemory, "$swpBT_sonOrDaughter", (Global.getSector().getPlayerPerson().getGender() == Gender.MALE) ? "son"
                : ((Global.getSector().getPlayerPerson().getGender() == Gender.FEMALE) ? "daughter" : "child"));
        setStatic(interactionMemory, "$swpBT_tannenInFleet", tannenData != null);
        setStatic(interactionMemory, "$swpBT_guyOrGal", (Global.getSector().getPlayerPerson().getGender() == Gender.MALE) ? "guy"
                : ((Global.getSector().getPlayerPerson().getGender() == Gender.FEMALE) ? "gal" : "fellow"));
        setStatic(interactionMemory, "$swpBT_spatialAnomaly", "spatial anomaly");
        setStatic(interactionMemory, "$swpBT_goneForGood", "gone for good");
        setStatic(interactionMemory, "$swpBT_backForMore", "back for more");

        if (!cascade) {
            setStatic(interactionMemory, "$swpBT_TTClueComplete", true);
            setStatic(interactionMemory, "$swpBT_UWCluePrimed", false);
        }
    }

    private static void setStatic(MemoryAPI interactionMemory, String key, Object value) {
        if (value instanceof Enum) {
            value = ((Enum) value).name();
        }
        interactionMemory.set(key, value, 0f);
    }

    @Override
    protected void updateInteractionDataImpl() {
        updateKnownContactRefs();

        set("$swpBT_stage", getCurrentStage());
        set("$swpBT_tannenBribe", Misc.getWithDGS(tannenBribe));
        set("$swpBT_tannenBigBribe", Misc.getWithDGS(tannenBigBribe));
        set("$swpBT_rachmaninoffMarketName", rachmaninoffMarket.getName());
        set("$swpBT_rachmaninoffActive", activateRachmaninoff);
        set("$swpBT_scavContactName", scavContact.getNameString());
        set("$swpBT_scavContactMarketName", scavContactMarket.getName());
        set("$swpBT_scavContactHisOrHer", scavContact.getHisOrHer());
        set("$swpBT_scavContactHimOrHer", scavContact.getHimOrHer());
        set("$swpBT_scavContactHeOrShe", scavContact.getHeOrShe());
        set("$swpBT_scavContactLadOrLass", (scavContact.getGender() == Gender.MALE) ? "lad"
                : ((scavContact.getGender() == Gender.FEMALE) ? "lass" : "fellow"));
        set("$swpBT_talkToTTContact", !activateTTClue && !completeTTClue && ttCluePossible);
        set("$swpBT_talkToUWContact", !activateUWClue && !completeUWClue);
        set("$swpBT_UWCluePrimed", activateUWClue && !completeUWClue);
        set("$swpBT_TTClueComplete", completeTTClue);
        set("$swpBT_UWClueComplete", completeUWClue);
        set("$swpBT_scavContactComplete", completeScavContact);
        set("$swpBT_rachmaninoffComplete", completeRachmaninoff);
        set("$swpBT_ttInsiderName", ttInsider.getName());
        set("$swpBT_ttInsiderMarketName", ttInsiderMarket.getName());
        set("$swpBT_firstRule", agreedToFirstRule);
        set("$swpBT_secondRule", agreedToSecondRule);
        set("$swpBT_thirdRule", agreedToThirdRule);
        set("$swpBT_brokeThirdRule", brokeThirdRule);
        set("$swpBT_leadsKnown", numLeadsKnown);
        set("$swpBT_cluesFound", numCluesFound);
        set("$swpBT_TTBarProb", ttBarProb);
        set("$swpBT_UWBarGenericProb", uwBarGenericProb);
        set("$swpBT_UWBarGenericAgainProb", uwBarGenericAgainProb);
        set("$swpBT_UWBarPiratesProb", uwBarPiratesProb);
        set("$swpBT_UWBarPiratesAgainProb", uwBarPiratesAgainProb);
        set("$swpBT_mouseBribe", Misc.getWithDGS(mouseBribe));
        set("$swpBT_thugsBribe", Misc.getWithDGS(thugsBribe));
        set("$swpBT_killedTannen", killedTannen);
        if (uwResearchBaseSystem.getConstellation() != null) {
            set("$swpBT_researchBaseConstellation", uwResearchBaseSystem.getConstellation().getNameWithType());
        } else {
            set("$swpBT_researchBaseConstellation", uwResearchBaseSystem.getName());
        }
        set("$swpBT_researchBaseSystem", uwResearchBaseSystem.getName());
        set("$swpBT_researchBaseSystemShort", uwResearchBaseSystem.getNameWithTypeIfNebula());

        String nextMove1 = "";
        if (ttCluePossible) {
            if (ttContact != null) {
                nextMove1 = " Perhaps you can prod " + ttContact.getNameString() + " for insider info on the research base that Captain Lee raided?";
            } else {
                nextMove1 = " Perhaps you can look for a Tri-Tachyon insider on the research base that Captain Lee raided?";
            }
        }
        String nextMove2;
        if (uwContact != null) {
            nextMove2 = " Maybe you can hit up " + uwContact.getNameString() + " for clues that could lead to Captain Lee?";
        } else {
            nextMove2 = " Maybe you can make waves within the underworld and gather rumors about Captain Lee's whereabouts?";
        }
        String nextMove3 = "";
        if (!nextMove1.isEmpty() && !nextMove2.isEmpty()) {
            nextMove3 = " So many options to choose from...";
        }
        set("$swpBT_nextMove", nextMove1 + nextMove2 + nextMove3);

        List<String> searchLines = getSearchLines();
        if (!searchLines.isEmpty()) {
            set("$swpBT_newestClue", searchLines.get(searchLines.size() - 1));
        }
        if (!searchLines.isEmpty()) {
            set("$swpBT_breadcrumbs", Misc.getAndJoined(searchLines));
        }
        if (numCluesCanBeFound > numCluesFound) {
            set("$swpBT_cluesLeft", "You still have yet to discover all you can about Lee's buried treasure, so your search may not yet be over.");
        } else if (numCluesFound < 4) {
            set("$swpBT_cluesLeft", "You have found every clue that you're likely to discover; all you can do now is search the Sector for Lee's buried treasure before it's too late.");
        } else {
            set("$swpBT_cluesLeft", "Armed with this knowledge, all there is left to do is make a beeline for Lee's buried treasure, before it's too late.");
        }

        updateInteractionDataStatic(interactionMemory, true);
    }

    @Override
    public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        float bpad = 3f;

        List<String> searchLines = getSearchLines();
        String searchLinesFormat = "";
        for (int i = 0; i < searchLines.size(); i++) {
            if (i == 0) {
                searchLinesFormat += "%s";
            } else if (searchLines.size() == 2) {
                searchLinesFormat += " and %s";
            } else if (i == (searchLines.size() - 1)) {
                searchLinesFormat += ", and %s";
            } else {
                searchLinesFormat += ", %s";
            }
        }
        List<StarSystemAPI> searchSystems = getSearchSystems();
        List<Color> searchSystemColors = new ArrayList<>(searchSystems.size());
        List<String> searchSystemHighlights = new ArrayList<>(searchSystems.size());
        String systemSearchFormat = "";
        for (int i = 0; i < searchSystems.size(); i++) {
            StarSystemAPI system = searchSystems.get(i);
            if (system.getStar() != null) {
                searchSystemColors.add(system.getStar().getSpec().getIconColor());
                searchSystemHighlights.add(system.getNameWithTypeIfNebula());
                if (i == 0) {
                    systemSearchFormat += "%s";
                } else if (searchSystems.size() == 2) {
                    systemSearchFormat += " and %s";
                } else if (i == (searchSystems.size() - 1)) {
                    systemSearchFormat += ", and %s";
                } else {
                    systemSearchFormat += ", %s";
                }
            } else {
                if (i == 0) {
                    systemSearchFormat += system.getNameWithTypeIfNebula();
                } else if (searchSystems.size() == 2) {
                    systemSearchFormat += " and " + system.getNameWithTypeIfNebula();
                } else if (i == (searchSystems.size() - 1)) {
                    systemSearchFormat += ", and " + system.getNameWithTypeIfNebula();
                } else {
                    systemSearchFormat += ", " + system.getNameWithTypeIfNebula();
                }
            }
        }

        boolean haveDagger;
        if (Global.getSector().getMemoryWithoutUpdate().contains("$swpBT_haveMouseDagger")) {
            haveDagger = Global.getSector().getMemoryWithoutUpdate().getBoolean("$swpBT_haveMouseDagger");
        } else {
            haveDagger = false;
        }

        if (currentStage == Stage.FIND_FIRST_CLUE) {
            if (numLeadsKnown < numCluesCanBeFound) {
                info.addPara("Search for clues that could lead you to the lost treasure of Captain Lee, which he stole from "
                        + "a secret Tri-Tachyon research base and hid away " + searchLinesFormat + ".", opad, Misc.getHighlightColor(), searchLines.toArray(new String[searchLines.size()]));
            } else if (numLeadsKnown > numCluesFound) {
                String leads = ((numLeadsKnown - numCluesFound) > 1) ? "leads" : "a lead";
                info.addPara("Follow up on " + leads + " to the lost treasure of Captain Lee, which he stole from a secret "
                        + "Tri-Tachyon research base and hid away " + searchLinesFormat + ".", opad, Misc.getHighlightColor(), searchLines.toArray(new String[searchLines.size()]));
            }
        } else if (currentStage == Stage.FIND_SECOND_CLUE) {
            if (numLeadsKnown < numCluesCanBeFound) {
                info.addPara("Search for another clue leading to the lost treasure of Captain Lee, hidden " + searchLinesFormat + ".",
                        opad, Misc.getHighlightColor(), searchLines.toArray(new String[searchLines.size()]));
            } else if (numLeadsKnown > numCluesFound) {
                String leads = ((numLeadsKnown - numCluesFound) > 1) ? "more leads" : "another lead";
                info.addPara("Follow up on " + leads + " to the lost treasure of Captain Lee, hidden " + searchLinesFormat + ".",
                        opad, Misc.getHighlightColor(), searchLines.toArray(new String[searchLines.size()]));
            }
            info.addPara("You've discovered one clue, but it would require incredible luck to find the lost treasure without "
                    + "gathering more information.", opad);
        } else if (currentStage == Stage.FIND_THIRD_CLUE) {
            if (numLeadsKnown < numCluesCanBeFound) {
                info.addPara("Search for a third clue to point you towards the lost treasure of Captain Lee, hidden " + searchLinesFormat + ".",
                        opad, Misc.getHighlightColor(), searchLines.toArray(new String[searchLines.size()]));
            } else if (numLeadsKnown > numCluesFound) {
                String leads = ((numLeadsKnown - numCluesFound) > 1) ? "additional leads" : "a third lead";
                info.addPara("Follow up on " + leads + " to the lost treasure of Captain Lee, hidden " + searchLinesFormat + ".",
                        opad, Misc.getHighlightColor(), searchLines.toArray(new String[searchLines.size()]));
            }
            info.addPara("Now that you've discovered two clues, it might be possible - albeit difficult - to find the lost "
                    + "treasure. However, your chances will improve greatly if you obtain more information.", opad);
            info.addPara("If you feel you're ready, you should hunt within the following star systems: " + systemSearchFormat + ".",
                    opad, searchSystemColors.toArray(new Color[searchSystemColors.size()]), searchSystemHighlights.toArray(new String[searchSystemHighlights.size()]));
        } else if (currentStage == Stage.FIND_FINAL_CLUE) {
            if (numLeadsKnown < numCluesCanBeFound) {
                info.addPara("Search for one last clue in order to pinpoint the lost treasure of Captain Lee, hidden " + searchLinesFormat + ".",
                        opad, Misc.getHighlightColor(), searchLines.toArray(new String[searchLines.size()]));
            } else if (numLeadsKnown > numCluesFound) {
                String leads = ((numLeadsKnown - numCluesFound) > 1) ? "some final leads" : "one final lead";
                info.addPara("Follow up on " + leads + " to the lost treasure of Captain Lee, hidden " + searchLinesFormat + ".",
                        opad, Misc.getHighlightColor(), searchLines.toArray(new String[searchLines.size()]));
            }
            info.addPara("Having discovered three different clues, you are well-equipped to search for the lost treasure, "
                    + "but you can only be truly certain once you have all the information on hand.", opad);
            info.addPara("If you feel you're ready, you should hunt within the following star systems: " + systemSearchFormat + ".",
                    opad, searchSystemColors.toArray(new Color[searchSystemColors.size()]), searchSystemHighlights.toArray(new String[searchSystemHighlights.size()]));
        } else if (currentStage == Stage.FIND_TREASURE) {
            info.addPara("You've pursued every available lead, narrowing the search for the lost treasure of Captain Lee: " + searchLinesFormat + ".",
                    opad, Misc.getHighlightColor(), searchLines.toArray(new String[searchLines.size()]));
            info.addPara("Hunt within the following star systems for the Excelsior technology cache: " + systemSearchFormat + ".",
                    opad, searchSystemColors.toArray(new Color[searchSystemColors.size()]), searchSystemHighlights.toArray(new String[searchSystemHighlights.size()]));
        } else if (currentStage == Stage.GO_TO_TREASURE) {
            info.addPara("By tracking down every possible lead, you've pinpointed the lost treasure of Captain Lee: " + searchLinesFormat + ".",
                    opad, Misc.getHighlightColor(), searchLines.toArray(new String[searchLines.size()]));
            info.addPara("Hunt within the " + leeSystem.getNameWithLowercaseTypeShort() + " for the Excelsior technology cache.", opad);
        }
        if (killedTannen) {
            info.addPara("You killed Captain Tannen, ensuring that he cannot interfere with your plans. However, any "
                    + "secrets he was keeping have died with him...", opad);
        } else {
            CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
            if (playerFleet != null) {
                OfficerDataAPI tannenData = playerFleet.getFleetData().getOfficerData(captainTannen);
                if (tannenData != null) {
                    info.addPara("You've recruited Captain Tannen into your fleet.", opad);
                }
            }
        }
        if (haveDagger) {
            info.addPara("You've obtained Captain Lee's dagger from his presumptive daughter, a young woman called "
                    + "'The Mouse'.", opad);
        }
        if ((currentStage == Stage.FIND_FIRST_CLUE)
                || (currentStage == Stage.FIND_SECOND_CLUE)
                || (currentStage == Stage.FIND_THIRD_CLUE)
                || (currentStage == Stage.FIND_FINAL_CLUE)) {
            updateKnownContactRefs();

            if (activateRachmaninoff && !completeRachmaninoff) {
                info.addSectionHeading("Lead: " + rachmaninoff.getNameString(), Alignment.MID, opad);
                info.addPara("You know of a strange spacer called Rachmaninoff, whom you suspect is a former member of "
                        + "\"The Silent Hand\", the elite mercenary company that hunted down Captain Lee - and suffered "
                        + "defeat at his hands.", opad);
                info.addPara(getGoTalkToPersonText(rachmaninoff) + ".", opad);
            }
            if (activateScavContact && !completeScavContact) {
                info.addSectionHeading("Lead: Shady Scavenger", Alignment.MID, opad);
                info.addPara("Captain Tannen informed you about a shady scavenger by the name of " + scavContact.getNameString()
                        + ", who found the wreckage of a Hyperion from Captain Lee's last stand against \"The Silent Hand\".", opad);
                info.addPara(getGoTalkToPersonText(scavContact) + ".", opad);
            }
            if (activateTTClue && !completeTTClue) {
                info.addSectionHeading("Lead: Tri-Tachyon Insider", Alignment.MID, opad);
                if (ttClueLimitedInfo) {
                    info.addPara("You've received info pointing you to a Tri-Tachyon insider named " + ttInsider.getNameString()
                            + ", who might know more about Captain Lee and the secret Tri-Tachyon base he raided to obtain his treasure.", opad);
                } else {
                    info.addPara("You've received info pointing you to a Tri-Tachyon insider named " + ttInsider.getNameString()
                            + ", who seems to be involved with \"Project Excelsior\", which Captain Lee likely encountered during "
                            + "his final raid.", opad);
                }
                info.addPara(getGoTalkToPersonText(ttInsider) + ".", opad);
            }
            if (activateUWClue && !completeUWClue) {
                info.addSectionHeading("Lead: Lee's Disappearance", Alignment.MID, opad);
                if (uwClueLimitedInfo) {
                    info.addPara("You've discovered that Captain Lee's last known destination was the " + uwResearchBaseSystem.getNameWithLowercaseTypeShort()
                            + ". You speculate that this may be the location of the secret research base that Lee raided.", opad);
                } else {
                    info.addPara("You've discovered that Captain Lee was last seen entering the " + uwResearchBaseSystem.getNameWithLowercaseTypeShort()
                            + " before his disappearance. This may be the location of the secret research base that Lee raided, "
                            + "but you've been warned that the system is dangerous.", opad);
                }
                info.addPara("Search for clues within the " + uwResearchBaseSystem.getNameWithLowercaseTypeShort() + ".", opad);
            }
            int numPossibleLeads = 0;
            if (ttCluePossible && !activateTTClue && !completeTTClue) {
                numPossibleLeads++;
            }
            if (!activateUWClue && !completeUWClue) {
                numPossibleLeads++;
            }
            if (numPossibleLeads == 1) {
                info.addSectionHeading("Possible Lead", Alignment.MID, opad);
            } else if (numPossibleLeads > 1) {
                info.addSectionHeading("Possible Leads", Alignment.MID, opad);
            }
            if (ttCluePossible && !activateTTClue && !completeTTClue) {
                if (ttContact != null) {
                    info.addPara("Your contact " + ttContact.getNameString() + " might be able to reveal information "
                            + "about the black-ops project that Captain Lee raided, or at least guide you toward his treasure.", opad);
                    info.addPara(getGoTalkToPersonText(ttContact) + ".", opad);
                } else {
                    info.addPara("A Tri-Tachyon insider might be able to reveal information about the black-ops project that "
                            + "Captain Lee raided, or at least guide you toward his treasure.", opad);
                }
            }
            if (!activateUWClue && !completeUWClue) {
                if (uwContact != null) {
                    info.addPara("If you make waves within the underworld and gather rumors, you might gain insight into "
                            + "Captain Lee's whereabouts. Alternatively, your contact " + uwContact.getNameString()
                            + " might know something as well.", opad);
                    info.addPara(getGoTalkToPersonText(uwContact) + ".", opad);
                } else {
                    info.addPara("If you make waves within the underworld and gather rumors, you might gain insight into "
                            + "Captain Lee's whereabouts.", opad);
                }
            }
        }
        if (!killedTannen && (agreedToFirstRule || agreedToSecondRule || agreedToThirdRule)) {
            int numRules = (agreedToFirstRule) ? 1 : 0;
            numRules += (agreedToSecondRule) ? 1 : 0;
            numRules += (agreedToThirdRule) ? 1 : 0;
            info.addSectionHeading("Rules of Engagement", Alignment.MID, opad);
            info.addPara("Captain Tannen convinced you to abide by " + ((numRules > 1) ? "certain rules" : "a rule")
                    + " of engagement:", opad);

            indent(info);
            if (agreedToFirstRule) {
                info.addPara("%s Once the treasure reaches the Core Worlds, the game is up. No thieving or assassination "
                        + "attempts afterwards.", bpad, Misc.getHighlightColor(), "Finders, Keepers:");
            }
            if (agreedToSecondRule) {
                info.addPara("%s No fighting until the treasure is found. Once it's found, you have until it reaches the "
                        + "Core Worlds to try and take it for yourself.", bpad, Misc.getHighlightColor(), "Truce:");
            }
            if (agreedToThirdRule) {
                if (brokeThirdRule) {
                    info.addPara("%s Refrain from telling anyone about the treasure. Don't disclose specific details about "
                            + "the treasure to anyone, under any circumstances. Unfortunately, you have %s.", bpad,
                            new Color[]{Misc.getHighlightColor(), Misc.getNegativeHighlightColor()}, "Secrecy:", "broken this rule");
                } else {
                    info.addPara("%s Refrain from telling anyone about the treasure. Don't disclose specific details about "
                            + "the treasure to anyone, under any circumstances.", bpad, Misc.getHighlightColor(), "Secrecy:");
                }
            }
            unindent(info);
        }
//        if (currentStage == Stage.GET_KELISE_LEAD
//                || currentStage == Stage.SELL_BLACKMAIL_MATERIAL
//                || currentStage == Stage.GO_TO_RELAY_SYSTEM
//                || currentStage == Stage.TALK_TO_CALLISTO
//                || currentStage == Stage.SOLD_BLACKMAIL_MATERIAL) {
//            info.addPara("Get a lead on the whereabouts of Kelise Astraia, who is associated with "
//                    + "a secret Tri-Tachyon research base called \"Alpha Site\".", opad);
//        }
//        if (currentStage == Stage.GET_KELISE_LEAD) {
//            addStandardMarketDesc("She was formerly employed as a weapons engineer " + culann.getOnOrAt(),
//                    culann, info, opad);
//            if (pointAtArroyo) {
//                info.addPara("You've talked to Gargoyle, who advised you to talk to Rayan Arroyo, "
//                        + "\"a Tri-Tach goon from top to bottom\", who is likely to have the right connections "
//                        + "to help with the investigation.", opad);
//                addStandardMarketDesc("Arroyo is located " + arroyo.getMarket().getOnOrAt(), arroyo.getMarket(), info, opad);
//            }
//            if (pointAtCulannAdmin) {
//                info.addPara("You've learned that the administrator of Culann Starforge will have access to "
//                        + "personnel records that may shed light on Kelise's whereabouts.", opad);
//                addStandardMarketDesc("", culannAdmin.getMarket(), info, opad);
//            }
//        } else if (currentStage == Stage.SELL_BLACKMAIL_MATERIAL) {
//            info.addPara("Rayan Arroyo has agreed to help locate her, in exchange for a favor - selling bad "
//                    + "comm fakes to any reasonably highly placed pirate leader, which would serve "
//                    + "his ends as part of a disinformation campaign.", opad);
//            addStandardMarketDesc("A reasonable place to find such a pirate would be "
//                    + donn.getOnOrAt(), donn, info, opad);
//        } else if (currentStage == Stage.SOLD_BLACKMAIL_MATERIAL) {
//            info.addPara("You've sold the blackmail materials to a pirate leader, in exchange for which Arroyo "
//                    + "has agreed to help you find Kelise Astraia.", opad);
//            info.addPara(getGoTalkToPersonText(arroyo) + ".", opad);
//        } else if (currentStage == Stage.TALK_TO_CALLISTO) {
//            info.addPara("You've learned that Kelise chartered - and paid for unknown special modifications to - "
//                    + "the ISS Hamatsu, a Venture-class starship, before flying it out of the system. The ship's owner is one "
//                    + callisto.getNameString() + ". She may have more information.", opad);
//            info.addPara(getGoTalkToPersonText(callisto) + ".", opad);
//        } else if (currentStage == Stage.GO_TO_RELAY_SYSTEM) {
//            info.addPara("You've learned that the flight plan of the ISS Hamatsu - the ship chartered by Kelise Astraia - "
//                    + "led to the " + relaySystem.getNameWithLowercaseTypeShort() + ".", opad);
//        } else if (currentStage == Stage.GO_TO_NASCENT_WELL) {
//            info.addPara("You've found a hidden relay in the " + relaySystem.getNameWithLowercaseTypeShort() + ", "
//                    + "that was likely used for communications with \"Alpha Site\". "
//                    + "Investigate the hyperspace coordinates that the relay was transmitting to.", opad);
//
//            if (well.isInCurrentLocation() && Misc.getDistanceToPlayerLY(well) < 0.2f) {
//                info.addPara("Use %s to traverse the nascent gravity well located at the coordinates.",
//                        opad, Misc.getHighlightColor(), "Transverse Jump");
//            }
//
//        } else if (currentStage == Stage.INVESTIGATE_SITE) {
//            info.addPara("Learn what Tri-Tachyon was doing at Alpha Site.", opad);
//            info.addPara("Optional: look for the ISS Hamatsu and, if found, return it "
//                    + "to " + callisto.getNameString() + ".", opad);
////			addStandardMarketDesc("Optional: look for the ISS Hamatsu and, if found, return it "
////					+ "to " + callisto.getNameString() + " on", callisto.getMarket(), info, opad);
//            info.addPara("Optional: locate Kelise Astraia.", opad);
//        } else if (currentStage == Stage.RECOVER_ZIGGURAT) {
//            info.addPara("Recover the Ziggurat-class phase vessel, which was apparently developed in "
//                    + "secret by Tri-Tachyon at Alpha Site.", opad);
//        } else if (currentStage == Stage.RETURN_TO_ACADEMY) {
//            info.addPara("Return to the Galatia Academy with the scan data and report your findings to Provost "
//                    + getPerson().getNameString() + ".", opad);
//        }
    }

    @Override
    public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
        if (getListInfoParam() == PRINT_RULES) {
            if (agreedToFirstRule) {
                info.addPara("Abide by the \"Finders, Keepers\" rule", tc, pad);
            }
            if (agreedToSecondRule) {
                info.addPara("Maintain a truce with Captain Tannen", tc, pad);
            }
            if (agreedToThirdRule) {
                info.addPara("Keep the treasure hunt a secret", tc, pad);
            }
            return true;
        }

        boolean printed = false;
        if (numLeadsKnown < numCluesCanBeFound) {
            if (currentStage == Stage.FIND_FIRST_CLUE) {
                info.addPara("Search for clues that could lead to Captain Lee's lost treasure", tc, pad);
                printed = true;
            } else if (currentStage == Stage.FIND_SECOND_CLUE) {
                info.addPara("Search for another clue leading to Captain Lee's lost treasure", tc, pad);
                printed = true;
            } else if (currentStage == Stage.FIND_THIRD_CLUE) {
                info.addPara("Search for a third clue to point the way to Captain Lee's lost treasure", tc, pad);
                printed = true;
            } else if (currentStage == Stage.FIND_FINAL_CLUE) {
                info.addPara("Search for one last clue to pinpoint Captain Lee's lost treasure", tc, pad);
                printed = true;
            }
        }
        if ((currentStage == Stage.FIND_FIRST_CLUE)
                || (currentStage == Stage.FIND_SECOND_CLUE)
                || (currentStage == Stage.FIND_THIRD_CLUE)
                || (currentStage == Stage.FIND_FINAL_CLUE)) {
            if (activateRachmaninoff && !completeRachmaninoff) {
                info.addPara(getGoTalkToPersonText(rachmaninoff), tc, pad);
                printed = true;
            }
            if (activateScavContact && !completeScavContact) {
                info.addPara(getGoTalkToPersonText(scavContact), tc, pad);
                printed = true;
            }
            if (activateTTClue && !completeTTClue) {
                info.addPara(getGoTalkToPersonText(ttInsider), tc, pad);
                printed = true;
            }
            if (activateUWClue && !completeUWClue) {
                info.addPara("Search within the " + uwResearchBaseSystem.getNameWithLowercaseTypeShort() + ".", tc, pad);
                printed = true;
            }
        } else if (currentStage == Stage.FIND_TREASURE) {
            info.addPara("Hunt for the Excelsior technology cache", tc, pad);
            printed = true;
        } else if (currentStage == Stage.GO_TO_TREASURE) {
            info.addPara("Hunt within the " + leeSystem.getNameWithLowercaseTypeShort() + " for the Excelsior technology cache", tc, pad);
            printed = true;
        }
        return printed;
//        if (currentStage == Stage.GET_KELISE_LEAD) {
//            info.addPara("Get a lead on the whereabouts of Kelise Astraia", tc, pad);
//            return true;
//        } else if (currentStage == Stage.SELL_BLACKMAIL_MATERIAL) {
//            info.addPara("Sell Arroyo's comm fakes to any highly-placed pirate", tc, pad);
//            return true;
//        } else if (currentStage == Stage.SOLD_BLACKMAIL_MATERIAL) {
//            info.addPara(getGoTalkToPersonText(arroyo), tc, pad);
//            return true;
//        } else if (currentStage == Stage.TALK_TO_CALLISTO) {
//            info.addPara(getGoTalkToPersonText(callisto), tc, pad);
//            return true;
//        } else if (currentStage == Stage.GO_TO_RELAY_SYSTEM) {
//            info.addPara(getGoToSystemTextShort(relaySystem), tc, pad);
//            return true;
//        } else if (currentStage == Stage.GO_TO_NASCENT_WELL) {
//            info.addPara("Investigate hyperspace area the relay was transmitting to", tc, pad);
//            return true;
//        } else if (currentStage == Stage.INVESTIGATE_SITE) {
//            info.addPara("Investigate Alpha Site", tc, pad);
//            return true;
//        } else if (currentStage == Stage.RECOVER_ZIGGURAT) {
//            info.addPara("Recover the Ziggurat-class phase vessel", tc, pad);
//            return true;
//        } else if (currentStage == Stage.RETURN_TO_ACADEMY) {
//            info.addPara("Return to the Galatia Academy and report to Provost Baird", tc, pad);
//            return true;
//        }
    }

    @Override
    public String getBaseName() {
        return "Buried Treasure";
    }

    @Override
    public String getPostfixForState() {
        if (startingStage != null) {
            return "";
        }
        return super.getPostfixForState();
    }

    @Override
    public void setCurrentStage(Object next, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.setCurrentStage(next, dialog, memoryMap);
//        if (next == Stage.RETURN_TO_ACADEMY) {
//            // "At The Gates" is offered in response to this variable being set.
//            // It can be set either by scanning the Ziggurat without doing this mission, OR
//            // by finishing this mission.
//            // What we want to avoid is a scenario when this variable remains set before the mission is finished
//            // and the player returns to the GA and is potentially offered At The Gates before PZ is completed.
//            Global.getSector().getMemoryWithoutUpdate().unset(SCANNED_ZIGGURAT);
//        } else if (next == Stage.GO_TO_RELAY_SYSTEM) {
//            // just talked to Callisto
//            if (hamatsu != null) {
//                Misc.makeImportant(hamatsu, getReason());
//            }
//        }
    }

    @Override
    public void acceptImpl(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.acceptImpl(dialog, memoryMap);
        Global.getSector().getListenerManager().addListener(this);
        if (activateRachmaninoff) {
            makePrimaryObjective(rachmaninoff); // This guy costs SP to even know about; prioritize
        }
        if (ttCluePossible) {
            Misc.makeStoryCritical(ttInsiderMarket, BURIED_TREASURE);
        }

        int generalAwareness = 0;
        int ttAwareness = 0;
        int uwAwareness = 0;
        int tannenAwareness = 0;
        if (Global.getSector().getMemoryWithoutUpdate().contains("$swpBT_generalAwareness")) {
            generalAwareness = Global.getSector().getMemoryWithoutUpdate().getInt("$swpBT_generalAwareness");
        }
        if (Global.getSector().getMemoryWithoutUpdate().contains("$swpBT_TTAwareness")) {
            generalAwareness = Global.getSector().getMemoryWithoutUpdate().getInt("$swpBT_TTAwareness");
        }
        if (Global.getSector().getMemoryWithoutUpdate().contains("$swpBT_UWAwareness")) {
            generalAwareness = Global.getSector().getMemoryWithoutUpdate().getInt("$swpBT_UWAwareness");
        }
        if (Global.getSector().getMemoryWithoutUpdate().contains("$swpBT_TannenAwareness")) {
            generalAwareness = Global.getSector().getMemoryWithoutUpdate().getInt("$swpBT_TannenAwareness");
        }
        generalAwareness++; // Secrecy rule not agreed to yet
        uwAwareness++; // Secrecy rule not agreed to yet
        Global.getSector().getMemoryWithoutUpdate().set("$swpBT_generalAwareness", generalAwareness);
        Global.getSector().getMemoryWithoutUpdate().set("$swpBT_TTAwareness", ttAwareness);
        Global.getSector().getMemoryWithoutUpdate().set("$swpBT_UWAwareness", uwAwareness);
        Global.getSector().getMemoryWithoutUpdate().set("$swpBT_TannenAwareness", tannenAwareness);

        Global.getSector().getMemoryWithoutUpdate().set("$swpBT_numNoProgressEvents", 0);
        Global.getSector().getMemoryWithoutUpdate().set("$swpBT_lastProgressTimestamp", Global.getSector().getClock().getTimestamp());

        Global.getSector().getMemoryWithoutUpdate().set("$swpBT_lastCompletionTimestamp", Global.getSector().getClock().getTimestamp());
        Global.getSector().getMemoryWithoutUpdate().set("$swpBT_lastCompletionTimer", 30f);
        Global.getSector().getMemoryWithoutUpdate().set("$swpBT_defeatedTannenTimestamp", Global.getSector().getClock().getTimestamp());
        Global.getSector().getMemoryWithoutUpdate().set("$swpBT_defeatedTannenTimer", 0f);
        Global.getSector().getMemoryWithoutUpdate().set("$swpBT_completionCount", 0);
        Global.getSector().getMemoryWithoutUpdate().set("$swpBT_completionEventCount", 0);

        Global.getSector().getMemoryWithoutUpdate().set(EXCELSIOR_DESC_ALT_KEY, true);
        Global.getSettings().getDescription("swp_excelsior", Description.Type.SHIP).setText1(SWPModPlugin.EXCELSIOR_ALT_DESCRIPTION);
    }

    @Override
    public void reportShipsRecovered(List<FleetMemberAPI> ships, InteractionDialogAPI dialog) {
        if (isEnded()) {
            return;
        }

        if (!(dialog instanceof RuleBasedInteractionDialogPluginImpl)) {
            //return;
        }

//        for (FleetMemberAPI member : ships) {
//            if (member.getHullId().equals("ziggurat")) {
//                Global.getSector().getMemoryWithoutUpdate().set(RECOVERED_ZIGGURAT, true, 0);
//                checkStageChangesAndTriggers(dialog, ((RuleBasedInteractionDialogPluginImpl) dialog).getMemoryMap());
//                Global.getSector().getMemoryWithoutUpdate().unset(RECOVERED_ZIGGURAT);
//            }
//        }
    }

    @Override
    public void addDescriptionForCurrentStage(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;
        if (isSucceeded()) {
            info.addPara("You have obtained the Excelsior.", opad);
        } else if (isFailed()) {
            info.addPara("You failed to obtain Captain Lee's lost treasure.", opad);
        } else {
            addDescriptionForNonEndStage(info, width, height);
        }
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;

        if (captainTannen != null) {
            info.addImage(captainTannen.getPortraitSprite(), width, 128, opad);
            info.addPara("Captain " + captainTannen.getNameString() + " told you the story of Captain Lee and his lost treasure.", opad);
        }

        addDescriptionForCurrentStage(info, width, height);

        addBulletPoints(info, ListInfoMode.IN_DESC);

        if (abandonStage != null && !isAbandoned() && !isSucceeded() && !isFailed()) {
            addAbandonButton(info, width);
        }
    }

    @Override
    public void advanceImpl(float amount) {
        super.advanceImpl(amount);

        if (musicEnderTimeout > 0f) {
            musicEnderTimeout -= amount;
        }

        checkInterval.advance(amount);
        if (checkInterval.intervalElapsed()) {
            if (!Global.getSector().getCampaignUI().isShowingDialog() && !Global.getSector().getCampaignUI().isShowingMenu()
                    && (Global.getSector().getCampaignUI().getCurrentInteractionDialog() == null) && (musicEnderTimeout <= 0f)) {
                if ((Global.getSoundPlayer().getCurrentMusicId() != null) && Global.getSoundPlayer().getCurrentMusicId().replaceAll(".ogg", "").contentEquals("swp_ghostly_apparition")) {
                    Global.getSoundPlayer().setSuspendDefaultMusicPlayback(false);
                    Global.getSoundPlayer().playCustomMusic(1, 0, null, false);
                    musicEnderTimeout = 10f;
                }
            }

            CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
            OfficerDataAPI tannenData = null;
            if (playerFleet != null) {
                tannenData = playerFleet.getFleetData().getOfficerData(captainTannen);
            }

            if (!killedTannen && (tannenData == null)) {
                long lastCompletionTimesamp = Global.getSector().getMemoryWithoutUpdate().getLong("$swpBT_lastCompletionTimestamp");
                float lastCompletionTimer = Global.getSector().getMemoryWithoutUpdate().getFloat("$swpBT_lastCompletionTimer");
                long defeatedTannenTimesamp = Global.getSector().getMemoryWithoutUpdate().getLong("$swpBT_defeatedTannenTimestamp");
                float defeatedTannenTimer = Global.getSector().getMemoryWithoutUpdate().getFloat("$swpBT_defeatedTannenTimer");
                int completionCount = Global.getSector().getMemoryWithoutUpdate().getInt("$swpBT_completionCount");
                int completionEventCount = Global.getSector().getMemoryWithoutUpdate().getInt("$swpBT_completionEventCount");
                float daysSinceLastCompletion = Global.getSector().getClock().getElapsedDaysSince(lastCompletionTimesamp);
                float daysSinceDefeatedTannen = Global.getSector().getClock().getElapsedDaysSince(defeatedTannenTimesamp);
                if ((daysSinceLastCompletion >= lastCompletionTimer) && (daysSinceDefeatedTannen >= defeatedTannenTimer)
                        && ((completionCount > (completionEventCount + 1)) || ((completionCount > 0) && (completionEventCount == 0)))) {
                    if (agreedToSecondRule && !brokeThirdRule && (captainTannen.getRelToPlayer().getRel() > -0.25f)) {
                        Global.getSector().getMemoryWithoutUpdate().set("$swpBT_completionEventTrigger", true);
                        Global.getSector().getMemoryWithoutUpdate().set("$swpBT_completionEventProcChance", 0.25f * ((1f + daysSinceLastCompletion) / (1f + lastCompletionTimer)));
                    } else if ((playerFleet != null) && (playerFleet.getContainingLocation() instanceof StarSystemAPI)) {
                        /* Don't spawn another for a long time */
                        Global.getSector().getMemoryWithoutUpdate().set("$swpBT_lastCompletionTimestamp", Global.getSector().getClock().getTimestamp());
                        Global.getSector().getMemoryWithoutUpdate().set("$swpBT_lastCompletionTimer", MathUtils.getRandomNumberInRange(90f, 120f));

                        StarSystemAPI playerSystem = (StarSystemAPI) playerFleet.getContainingLocation();

                        FleetSize tannenSize;
                        int playerPower = SWP_Util.calculatePowerLevel(Global.getSector().getPlayerFleet());
                        if (playerPower >= 1000) {
                            tannenSize = FleetSize.MAXIMUM;
                        } else if (playerPower >= 500) {
                            tannenSize = FleetSize.HUGE;
                        } else if (playerPower >= 250) {
                            tannenSize = FleetSize.VERY_LARGE;
                        } else if (playerPower >= 125) {
                            tannenSize = FleetSize.LARGE;
                        } else {
                            tannenSize = FleetSize.MEDIUM;
                        }

                        beginWithinHyperspaceRangeTrigger(playerSystem, 3f, false, Stage.FIND_FIRST_CLUE, Stage.FIND_SECOND_CLUE, Stage.FIND_THIRD_CLUE, Stage.FIND_FINAL_CLUE);
                        triggerCreateFleet(tannenSize, FleetQuality.SMOD_1, Factions.PIRATES, FleetTypes.MERC_PATROL, playerSystem);
                        triggerSetFleetFaction(Factions.PIRATES);
                        triggerSetFleetOfficers(OfficerNum.MORE, OfficerQuality.HIGHER);
                        ShipVariantAPI tannenShipVariant = Global.getSettings().getVariant("swp_harbinger_tannen");
                        tannenShipVariant = tannenShipVariant.clone();
                        tannenShipVariant.setSource(VariantSource.REFIT);
                        tannenShipVariant.addTag(TANNEN_SHIP);
                        triggerFleetSetFlagship(tannenShipVariant);
                        triggerSetFleetCommander(captainTannen);
                        triggerCustomAction(new TriggerAction() {
                            @Override
                            public void doAction(final TriggerActionContext context) {
                                context.fleet.getFlagship().setShipName("Star Runner II");
                                context.fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON, true);

                                /* Get rid of the fleet after 2 months; don't want multiple Tannens running about, do we? */
                                context.fleet.addScript(new EveryFrameScript() {
                                    public float elapsedWaitingForDespawn = 0f;

                                    @Override
                                    public boolean isDone() {
                                        return false;
                                    }

                                    @Override
                                    public boolean runWhilePaused() {
                                        return false;
                                    }

                                    @Override
                                    public void advance(float amount) {
                                        boolean missionEnded = false;
                                        if (Global.getSector().getMemoryWithoutUpdate().contains("$swpBT_missionEnded")) {
                                            missionEnded = Global.getSector().getMemoryWithoutUpdate().getBoolean("$swpBT_missionEnded");
                                        }
                                        boolean finalTannenSpawned = false;
                                        if (Global.getSector().getMemoryWithoutUpdate().contains("$swpBT_finalTannenSpawned")) {
                                            finalTannenSpawned = Global.getSector().getMemoryWithoutUpdate().getBoolean("$swpBT_finalTannenSpawned");
                                        }

                                        elapsedWaitingForDespawn += Global.getSector().getClock().convertToDays(amount);
                                        if ((elapsedWaitingForDespawn > 60f) || (currentStage == Stage.FIND_TREASURE)
                                                || (currentStage == Stage.GO_TO_TREASURE) || missionEnded || finalTannenSpawned) {
                                            if (finalTannenSpawned || (!context.fleet.isInCurrentLocation() && (Misc.getDistanceToPlayerLY(context.fleet) > 3f))) {
                                                if (context.fleet.getBattle() == null) {
                                                    context.fleet.despawn(FleetDespawnReason.PLAYER_FAR_AWAY, null);
                                                    elapsedWaitingForDespawn = 0f;
                                                }
                                            } else if (context.fleet.isInCurrentLocation()) {
                                                Misc.giveStandardReturnToSourceAssignments(context.fleet);
                                            }
                                        }
                                    }
                                });
                            }
                        });
                        triggerFleetSetName("Tannen's Fleet");
                        triggerFleetSetNoFactionInName();
                        if (!agreedToSecondRule || brokeThirdRule) {
                            triggerMakeHostileAndAggressive();
                        } else {
                            triggerMakeNonHostile();
                        }
                        triggerMakeLowRepImpact();
                        triggerFleetAllowLongPursuit();
                        triggerSetFleetAlwaysPursue();
                        triggerFleetOnlyEngageableWhenVisibleToPlayer();
                        triggerMakeFleetIgnoreOtherFleetsExceptPlayer();
                        triggerFleetMakeFaster(true, 2, true);
                        triggerPickLocationAtClosestToPlayerJumpPoint(playerSystem);
                        triggerSetFleetMissionRef("$swpBT_ref");
                        triggerSpawnFleetAtPickedLocation(TANNEN_FLAG, null);
                        triggerOrderFleetEBurn(1f);
                        triggerFleetMakeImportantPermanent(null);
                        triggerOrderFleetInterceptPlayer();
                        triggerFleetAddDefeatTriggerPermanent("swpBTDefeatTannen");
                        endTrigger();
                    }
                }
            }

            updateKnownContactRefs();
        }
    }

    public static enum BreadcrumbType {
        DIRECTION_FROM_CORE,
        DISTANCE_FROM_CORE,
        DIRECTION_FROM_CONSTELLATION,
        DISTANCE_FROM_CONSTELLATION,
        NUM_STARS_IN_SYSTEM,
        TYPE_STAR_IN_SYSTEM,
        COLOR_STAR_IN_SYSTEM
    }

    public static enum DirectionType {
        NORTH(90f, "north"),
        NORTH_EAST(45f, "north-east"),
        EAST(0f, "east"),
        SOUTH_EAST(-45f, "south-east"),
        SOUTH(-90f, "south"),
        SOUTH_WEST(-135f, "south-west"),
        WEST(180f, "west"),
        NORTH_WEST(135f, "north-west");

        public static final float ARC_WIDTH = 90f;

        public final float arcDir;
        public final String displayName;

        DirectionType(float arcDir, String displayName) {
            this.arcDir = arcDir;
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }

        public boolean isAngleInDirection(float angle) {
            float absDiff = Math.abs(Misc.getAngleDiff(arcDir, angle));
            return absDiff < (ARC_WIDTH / 2f);
        }

        public static DirectionType getDirectionForAngle(float angle) {
            DirectionType direction = null;
            float shortestDiff = 720f;
            for (DirectionType d : DirectionType.values()) {
                float absDiff = Math.abs(Misc.getAngleDiff(d.arcDir, Misc.normalizeAngle(angle)));
                if (absDiff < shortestDiff) {
                    direction = d;
                    shortestDiff = absDiff;
                }
            }
            return direction;
        }
    }

    public static final String STAR_TYPE_NEBULA = "nebula";
    public static final String STAR_TYPE_BLACK_HOLE = "black hole";
    public static final boolean DEBUG_BREADCRUMBS = true;
    protected static final float DISTANCE_MIN_LY = 5f;
    protected static final float DISTANCE_RANGE_BAND_MIN_LY = 5f;
    protected static final float DISTANCE_RANGE_BAND_MAX_LY = 20f;
    protected static final int ATTEMPTS_PER_GEN = 100;

    public static class Breadcrumb {

        public BreadcrumbType type;
        public DirectionType direction = null;
        public float distanceNear = 0f;
        public float distanceFar = 0f;
        public Constellation constellation = null;
        public int numStars = 0;
        public String starType = null;
        public String starColor = null; // "neutron" is a color
        public Set<String> possibleSystems = new HashSet<>();
    }

    /* Generates three different ways (sequential; it doesn't matter which order you do the clues in; you'll always get
     * the same sequence of breadcrumbs) to narrow down the search to these approximate amounts:
     *   No clues: Just about any system (~225 in vanilla)
     *   1st clue: Narrow down to 50-100 systems (impractical, gruelling search)
     *   2nd clue: Narrow down to 15-30 systems (long, but doable search)
     *   3rd clue: Narrow down to 5-10 systems (fairly easy search)
     *   4th clue: Narrow down to 1 system (no search necessary)
     * Possible types of breadcrumbs:
     *   - In a certain direction from core, implemented as a cone, expressed as a cardinal direction (e.g. galactic north-east)
     *   - A certain distance from core, expressed as a range of distances in light-years from X to Y
     *   - In a certain direction from a non-core constellation, implemented as a cone, expressed as a cardinal direction (e.g. galactic north-east)
     *       (must not be IN that constellation)
     *   - A certain distance from a non-core constellation, expressed as a range of distances in light-years from 0 to X
     *       (must not be IN that constellation)
     *   - A certain number of stars
     *   - A certain type of star present in the system (or a nebula or black hole)
     *   - A certain color of star present in the system (if applicable)
     * Note: The center of core is calculated using the spatial average of systems with the core tag.  This will
     * generally be around (-4000, -6000), rather than (0, 0).
     */
    public List<Breadcrumb> genBreadcrumbs(StarSystemAPI toSystem) {
        List<StarSystemAPI> allSystems = new ArrayList<>(Global.getSector().getStarSystems());
        List<Constellation> allConstellations = new ArrayList<>();
        Iterator<StarSystemAPI> systemIter = allSystems.iterator();
        StarSystemRequirement req = new SystemHasNoColonyReq();
        Vector2f coreCenter = new Vector2f();
        int numCore = 0;
        float furthestLY = 0f;
        while (systemIter.hasNext()) {
            StarSystemAPI system = systemIter.next();
            if (system.hasTag(Tags.THEME_CORE)) {
                Vector2f.add(coreCenter, system.getLocation(), coreCenter);
                numCore++;
            }
            furthestLY = Math.max(furthestLY, Misc.getDistanceLY(coreCenter, system.getLocation()));
            if (system.hasTag(Tags.THEME_HIDDEN) || system.hasTag(Tags.THEME_CORE_POPULATED)) {
                systemIter.remove();
                continue;
            }
            if (!req.systemMatchesRequirement(system)) {
                systemIter.remove();
                continue;
            }
            Constellation constellation = system.getConstellation();
            if (constellation != null) {
                if (!allConstellations.contains(constellation)) {
                    allConstellations.add(constellation);
                }
            }
        }
        if (toSystem.getConstellation() != null) {
            allConstellations.remove(toSystem.getConstellation());
        }
        coreCenter.scale(1f / (float) numCore);

        int failedGens = 0;
        if (Global.getSector().getMemoryWithoutUpdate().contains("$swpBT_failedBreadcrumbGen")) {
            failedGens = Global.getSector().getMemoryWithoutUpdate().getInt("$swpBT_failedBreadcrumbGen");
        }

        Random breadcrumbRand = Misc.getRandom(getGenRandom().nextLong(), failedGens + 1);
        WeightedRandomPicker<String> starTypes = new WeightedRandomPicker<>(breadcrumbRand);
        WeightedRandomPicker<String> starColors = new WeightedRandomPicker<>(breadcrumbRand);
        int numStars = 0;
        boolean allowNumStars = false;
        if (!toSystem.isNebula()) {
            if (toSystem.getStar() != null) {
                numStars++;
                if (!toSystem.getStar().getSpec().isBlackHole()) {
                    allowNumStars = true;
                    String type = BreadcrumbSpecial.getStarTypeName(toSystem.getStar());
                    String color = BreadcrumbSpecial.getStarColorName(toSystem.getStar());
                    if (type != null) {
                        starTypes.add(type);
                    }
                    if (color != null) {
                        starColors.add(color);
                    }
                } else {
                    starTypes.add(STAR_TYPE_BLACK_HOLE);
                }
            }
            if (toSystem.getSecondary() != null) {
                numStars++;
                if (!toSystem.getSecondary().getSpec().isBlackHole()) {
                    allowNumStars = true;
                    String type = BreadcrumbSpecial.getStarTypeName(toSystem.getSecondary());
                    String color = BreadcrumbSpecial.getStarColorName(toSystem.getSecondary());
                    if (type != null) {
                        starTypes.add(type);
                    }
                    if (color != null) {
                        starColors.add(color);
                    }
                } else {
                    starTypes.add(STAR_TYPE_BLACK_HOLE);
                }
            }
            if (toSystem.getTertiary() != null) {
                numStars++;
                if (!toSystem.getTertiary().getSpec().isBlackHole()) {
                    allowNumStars = true;
                    String type = BreadcrumbSpecial.getStarTypeName(toSystem.getTertiary());
                    String color = BreadcrumbSpecial.getStarColorName(toSystem.getTertiary());
                    if (type != null) {
                        starTypes.add(type);
                    }
                    if (color != null) {
                        starColors.add(color);
                    }
                } else {
                    starTypes.add(STAR_TYPE_BLACK_HOLE);
                }
            }
        } else {
            starTypes.add(STAR_TYPE_NEBULA);
        }

        if (DEBUG_BREADCRUMBS) {
            log.info("Generating breadcrumbs leading to " + toSystem.getName());
        }
        ATTEMPT:
        for (int attempt = 0; attempt < ATTEMPTS_PER_GEN; attempt++) {
            List<StarSystemAPI> possibleSystems = new ArrayList<>(allSystems);

            WeightedRandomPicker<BreadcrumbType> unpicked = new WeightedRandomPicker<>(breadcrumbRand);
            unpicked.addAll(EnumSet.allOf(BreadcrumbType.class));
            if (!allowNumStars) {
                unpicked.remove(BreadcrumbType.NUM_STARS_IN_SYSTEM);
            }
            if (starTypes.isEmpty()) {
                unpicked.remove(BreadcrumbType.TYPE_STAR_IN_SYSTEM);
            }
            if (starColors.isEmpty()) {
                unpicked.remove(BreadcrumbType.COLOR_STAR_IN_SYSTEM);
            }

            WeightedRandomPicker<Constellation> constellationPicker = new WeightedRandomPicker<>(breadcrumbRand);
            constellationPicker.addAll(allConstellations);
            if (constellationPicker.getItems().size() < 2) {
                unpicked.remove(BreadcrumbType.DIRECTION_FROM_CONSTELLATION);
                unpicked.remove(BreadcrumbType.DISTANCE_FROM_CONSTELLATION);
            }

            int searchPool = allSystems.size();
            int prevSearchWindow = searchPool;
            if (DEBUG_BREADCRUMBS) {
                log.info("Starting attempt " + (attempt + 1) + " with " + searchPool + " possible systems");
            }
            List<Breadcrumb> breadcrumbs = new ArrayList<>(3);
            for (int breadcrumbOrd = 0; breadcrumbOrd < 3; breadcrumbOrd++) {
                Breadcrumb breadcrumb = new Breadcrumb();
                BreadcrumbType picked = unpicked.pickAndRemove();

                breadcrumb.type = picked;
                if (breadcrumb.type == null) {
                    log.error("NOT ENOUGH BREADCRUMB TYPES!");
                    return null;
                }
                if (DEBUG_BREADCRUMBS) {
                    log.info("Picked " + breadcrumb.type.name() + " breadcrumb");
                }
                switch (breadcrumb.type) {
                    case DIRECTION_FROM_CORE: {
                        /* Initialize the breadcrumb */
                        float angle = Misc.getAngleInDegreesStrict(coreCenter, toSystem.getLocation());
                        float bias = SWP_Util.lerp(-DirectionType.ARC_WIDTH / 2f, DirectionType.ARC_WIDTH / 2f, breadcrumbRand.nextFloat());
                        breadcrumb.direction = DirectionType.getDirectionForAngle(angle + bias);
                        if (breadcrumb.direction == null) {
                            log.error("BREADCRUMB DIRECTION ERROR!");
                            return null;
                        }
                        if (DEBUG_BREADCRUMBS) {
                            log.info("Direction: " + breadcrumb.direction.name());
                        }

                        /* Filter out systems that don't match the breadcrumb */
                        systemIter = possibleSystems.iterator();
                        while (systemIter.hasNext()) {
                            StarSystemAPI system = systemIter.next();
                            angle = Misc.getAngleInDegreesStrict(coreCenter, system.getLocation());
                            if (!breadcrumb.direction.isAngleInDirection(angle)) {
                                systemIter.remove();
                            }
                        }
                        break;
                    }

                    case DISTANCE_FROM_CORE: {
                        /* Initialize the breadcrumb */
                        float distance = Misc.getDistanceLY(coreCenter, toSystem.getLocation());
                        float rangeBand = SWP_Util.lerp(DISTANCE_RANGE_BAND_MIN_LY, DISTANCE_RANGE_BAND_MAX_LY, breadcrumbRand.nextFloat());
                        float bias = SWP_Util.lerp(-rangeBand / 2f, rangeBand / 2f, breadcrumbRand.nextFloat());
                        breadcrumb.distanceFar = Math.round((distance + bias) + (rangeBand / 2f));
                        breadcrumb.distanceNear = Math.round((distance + bias) - (rangeBand / 2f));
                        if (breadcrumb.distanceFar > (float) Math.ceil(furthestLY)) {
                            breadcrumb.distanceNear -= Math.round(breadcrumb.distanceFar - furthestLY);
                            breadcrumb.distanceFar = (float) Math.ceil(furthestLY);
                        }
                        if (breadcrumb.distanceNear < DISTANCE_MIN_LY) {
                            breadcrumb.distanceNear = Math.min(DISTANCE_MIN_LY, (float) Math.floor(distance));
                        }
                        if ((distance > breadcrumb.distanceFar) || (distance < breadcrumb.distanceNear)) {
                            if (DEBUG_BREADCRUMBS) {
                                log.info("Failed to create sane range band; discarding attempt!");
                                log.info("-----");
                            }
                            continue ATTEMPT;
                        }
                        if (DEBUG_BREADCRUMBS) {
                            log.info("Distance: " + breadcrumb.distanceNear + " to " + breadcrumb.distanceFar + " LY");
                        }

                        /* Filter out systems that don't match the breadcrumb */
                        systemIter = possibleSystems.iterator();
                        while (systemIter.hasNext()) {
                            StarSystemAPI system = systemIter.next();
                            distance = Misc.getDistanceLY(coreCenter, system.getLocation());
                            if ((distance > breadcrumb.distanceFar) || (distance < breadcrumb.distanceNear)) {
                                systemIter.remove();
                            }
                        }
                        break;
                    }

                    case DIRECTION_FROM_CONSTELLATION: {
                        /* Initialize the breadcrumb */
                        breadcrumb.constellation = constellationPicker.pickAndRemove();
                        if (breadcrumb.constellation == null) {
                            log.error("BREADCRUMB CONSTELLATION ERROR!");
                            return null;
                        }
                        float angle = Misc.getAngleInDegreesStrict(breadcrumb.constellation.getLocation(), toSystem.getLocation());
                        float bias = SWP_Util.lerp(-DirectionType.ARC_WIDTH / 2f, DirectionType.ARC_WIDTH / 2f, breadcrumbRand.nextFloat());
                        breadcrumb.direction = DirectionType.getDirectionForAngle(angle + bias);
                        if (breadcrumb.direction == null) {
                            log.error("BREADCRUMB DIRECTION ERROR!");
                            return null;
                        }
                        if (DEBUG_BREADCRUMBS) {
                            log.info("Constellation: " + breadcrumb.constellation.getNameWithType());
                            log.info("Direction: " + breadcrumb.direction.name());
                        }

                        /* Filter out systems that don't match the breadcrumb */
                        systemIter = possibleSystems.iterator();
                        while (systemIter.hasNext()) {
                            StarSystemAPI system = systemIter.next();
                            angle = Misc.getAngleInDegreesStrict(breadcrumb.constellation.getLocation(), system.getLocation());
                            if (!breadcrumb.direction.isAngleInDirection(angle) || (system.getConstellation() == breadcrumb.constellation)) {
                                systemIter.remove();
                            }
                        }

                        /* Chance to pick this type of breadcrumb twice */
                        if (breadcrumbRand.nextBoolean() && (constellationPicker.getItems().size() >= 2)) {
                            unpicked.add(breadcrumb.type);
                        }
                        break;
                    }

                    case DISTANCE_FROM_CONSTELLATION: {
                        /* Initialize the breadcrumb */
                        breadcrumb.constellation = constellationPicker.pickAndRemove();
                        if (breadcrumb.constellation == null) {
                            log.error("BREADCRUMB CONSTELLATION ERROR!");
                            return null;
                        }
                        float distance = Misc.getDistanceLY(breadcrumb.constellation.getLocation(), toSystem.getLocation());
                        float rangeBand = SWP_Util.lerp(DISTANCE_RANGE_BAND_MIN_LY, DISTANCE_RANGE_BAND_MAX_LY, breadcrumbRand.nextFloat());
                        float bias = SWP_Util.lerp(-rangeBand / 2f, rangeBand / 2f, breadcrumbRand.nextFloat());
                        breadcrumb.distanceFar = Math.round((distance + bias) + (rangeBand / 2f));
                        breadcrumb.distanceNear = Math.round((distance + bias) - (rangeBand / 2f));
                        if (breadcrumb.distanceFar > (float) Math.ceil(furthestLY)) {
                            breadcrumb.distanceNear -= Math.round(breadcrumb.distanceFar - furthestLY);
                            breadcrumb.distanceFar = (float) Math.ceil(furthestLY);
                        }
                        if (breadcrumb.distanceNear < DISTANCE_MIN_LY) {
                            breadcrumb.distanceNear = Math.min(DISTANCE_MIN_LY, (float) Math.floor(distance));
                        }
                        if ((distance > breadcrumb.distanceFar) || (distance < breadcrumb.distanceNear)) {
                            if (DEBUG_BREADCRUMBS) {
                                log.info("Failed to create sane range band; discarding attempt!");
                                log.info("-----");
                            }
                            continue ATTEMPT;
                        }
                        if (DEBUG_BREADCRUMBS) {
                            log.info("Constellation: " + breadcrumb.constellation.getNameWithType());
                            log.info("Distance: " + breadcrumb.distanceNear + " to " + breadcrumb.distanceFar + " LY");
                        }

                        /* Filter out systems that don't match the breadcrumb */
                        systemIter = possibleSystems.iterator();
                        while (systemIter.hasNext()) {
                            StarSystemAPI system = systemIter.next();
                            distance = Misc.getDistanceLY(breadcrumb.constellation.getLocation(), system.getLocation());
                            if ((distance > breadcrumb.distanceFar) || (distance < breadcrumb.distanceNear)
                                    || (system.getConstellation() == breadcrumb.constellation)) {
                                systemIter.remove();
                            }
                        }

                        /* Chance to pick this type of breadcrumb twice */
                        if (breadcrumbRand.nextBoolean() && (constellationPicker.getItems().size() >= 2)) {
                            unpicked.add(breadcrumb.type);
                        }
                        break;
                    }

                    case NUM_STARS_IN_SYSTEM: {
                        /* Initialize the breadcrumb */
                        breadcrumb.numStars = numStars;
                        if (DEBUG_BREADCRUMBS) {
                            log.info("Stars: " + breadcrumb.numStars);
                        }

                        /* Filter out systems that don't match the breadcrumb */
                        systemIter = possibleSystems.iterator();
                        while (systemIter.hasNext()) {
                            StarSystemAPI system = systemIter.next();
                            int stars = 0;
                            if (!system.isNebula()) {
                                if (system.getStar() != null) {
                                    stars++;
                                }
                                if (system.getSecondary() != null) {
                                    stars++;
                                }
                                if (system.getTertiary() != null) {
                                    stars++;
                                }
                            }
                            if (stars != breadcrumb.numStars) {
                                systemIter.remove();
                            }
                        }
                        break;
                    }

                    case TYPE_STAR_IN_SYSTEM: {
                        /* Initialize the breadcrumb */
                        breadcrumb.starType = starTypes.pick();
                        if (breadcrumb.starType == null) {
                            log.error("BREADCRUMB STAR TYPE ERROR!");
                            return null;
                        }
                        if (DEBUG_BREADCRUMBS) {
                            log.info("Star Type: " + breadcrumb.starType);
                        }

                        /* Filter out systems that don't match the breadcrumb */
                        systemIter = possibleSystems.iterator();
                        while (systemIter.hasNext()) {
                            StarSystemAPI system = systemIter.next();
                            if (!system.isNebula()) {
                                if (system.getStar() != null) {
                                    if (!system.getStar().getSpec().isBlackHole()) {
                                        String type = BreadcrumbSpecial.getStarTypeName(system.getStar());
                                        if ((type != null) && breadcrumb.starType.contentEquals(type)) {
                                            continue;
                                        }
                                    } else if (breadcrumb.starType.contentEquals(STAR_TYPE_BLACK_HOLE)) {
                                        continue;
                                    }
                                }
                                if (system.getSecondary() != null) {
                                    if (!system.getSecondary().getSpec().isBlackHole()) {
                                        String type = BreadcrumbSpecial.getStarTypeName(system.getSecondary());
                                        if ((type != null) && breadcrumb.starType.contentEquals(type)) {
                                            continue;
                                        }
                                    } else if (breadcrumb.starType.contentEquals(STAR_TYPE_BLACK_HOLE)) {
                                        continue;
                                    }
                                }
                                if (system.getTertiary() != null) {
                                    if (!system.getTertiary().getSpec().isBlackHole()) {
                                        String type = BreadcrumbSpecial.getStarTypeName(system.getTertiary());
                                        if ((type != null) && breadcrumb.starType.contentEquals(type)) {
                                            continue;
                                        }
                                    } else if (breadcrumb.starType.contentEquals(STAR_TYPE_BLACK_HOLE)) {
                                        continue;
                                    }
                                }
                            } else if (breadcrumb.starType.contentEquals(STAR_TYPE_NEBULA)) {
                                continue;
                            }
                            systemIter.remove();
                        }
                        break;
                    }

                    case COLOR_STAR_IN_SYSTEM: {
                        /* Initialize the breadcrumb */
                        breadcrumb.starColor = starColors.pick();
                        if (breadcrumb.starColor == null) {
                            log.error("BREADCRUMB STAR COLOR ERROR!");
                            return null;
                        }
                        if (DEBUG_BREADCRUMBS) {
                            log.info("Star Color: " + breadcrumb.starColor);
                        }

                        /* Filter out systems that don't match the breadcrumb */
                        systemIter = possibleSystems.iterator();
                        while (systemIter.hasNext()) {
                            StarSystemAPI system = systemIter.next();
                            if (!system.isNebula()) {
                                if (system.getStar() != null) {
                                    if (!system.getStar().getSpec().isBlackHole()) {
                                        String color = BreadcrumbSpecial.getStarColorName(system.getStar());
                                        if ((color != null) && breadcrumb.starColor.contentEquals(color)) {
                                            continue;
                                        }
                                    }
                                }
                                if (system.getSecondary() != null) {
                                    if (!system.getSecondary().getSpec().isBlackHole()) {
                                        String color = BreadcrumbSpecial.getStarColorName(system.getSecondary());
                                        if ((color != null) && breadcrumb.starColor.contentEquals(color)) {
                                            continue;
                                        }
                                    }
                                }
                                if (system.getTertiary() != null) {
                                    if (!system.getTertiary().getSpec().isBlackHole()) {
                                        String color = BreadcrumbSpecial.getStarColorName(system.getTertiary());
                                        if ((color != null) && breadcrumb.starColor.contentEquals(color)) {
                                            continue;
                                        }
                                    }
                                }
                            }
                            systemIter.remove();
                        }
                        break;
                    }

                    default:
                        log.error("BREADCRUMB SET ERROR!");
                        return null;
                }

                int searchWindow = possibleSystems.size();
                if (DEBUG_BREADCRUMBS) {
                    log.info("Narrowed down to " + searchWindow + " possible systems");
                }

                int highLimit;
                int lowLimit;
                switch (breadcrumbOrd) {
                    case 0:
                        highLimit = 100 + failedGens;
                        lowLimit = Math.max(1, highLimit / 2);
                        if (highLimit > ((searchPool / 2) + failedGens)) {
                            highLimit = Math.min(searchPool, Math.max(1, (searchPool / 2) + failedGens));
                            lowLimit = Math.max(1, highLimit / 2);
                        }
                        break;
                    case 1:
                        highLimit = Math.max(30 + (failedGens / 2), Math.round(prevSearchWindow / 2.5f));
                        lowLimit = Math.max(1, highLimit / 2);
                        if (highLimit > ((searchPool / 5) + (failedGens / 2))) {
                            highLimit = Math.min(searchPool, Math.max(1, (searchPool / 5) + (failedGens / 2)));
                            lowLimit = Math.max(1, highLimit / 2);
                        }
                        break;
                    case 2:
                    default:
                        highLimit = Math.max(10 + (failedGens / 3), Math.round(prevSearchWindow / 2.5f));
                        lowLimit = Math.max(1, highLimit / 2);
                        if (highLimit > ((searchPool / 10) + (failedGens / 3))) {
                            highLimit = Math.min(searchPool, Math.max(1, (searchPool / 10) + (failedGens / 3)));
                            lowLimit = Math.max(1, highLimit / 2);
                        }
                        break;
                }
                if ((searchWindow > highLimit) || (searchWindow < lowLimit)) {
                    if (DEBUG_BREADCRUMBS) {
                        log.info("Search window exceeded limit [" + lowLimit + ", " + highLimit + "]; discarding attempt!");
                        log.info("-----");
                    }
                    break;
                }
                if (DEBUG_BREADCRUMBS) {
                    log.info("Search window is within limit [" + lowLimit + ", " + highLimit + "]");
                }

                for (StarSystemAPI system : possibleSystems) {
                    breadcrumb.possibleSystems.add(system.getId());
                }

                prevSearchWindow = searchWindow;
                breadcrumbs.add(breadcrumb);
            }

            if (breadcrumbs.size() >= 3) {
                return breadcrumbs;
            }
        }

        log.info("Unable to generate breadcrumbs within " + ATTEMPTS_PER_GEN + " attempts; will try again (" + (failedGens + 1) + " failed generations)");
        Global.getSector().getMemoryWithoutUpdate().set("$swpBT_failedBreadcrumbGen", failedGens + 1);
        return null;
    }

    public List<String> getSearchLines() {
        List<String> searchLines = new ArrayList<>();
        if (numCluesFound == 0) {
            searchLines.add("somewhere in the Persean Sector");
            return searchLines;
        }
        if (numCluesFound > 3) {
            searchLines.add("the " + leeSystem.getName());
            return searchLines;
        }

        for (int i = 0; i < numCluesFound; i++) {
            Breadcrumb breadcrumb = leeBreadcrumbs.get(i);
            switch (breadcrumb.type) {
                case DIRECTION_FROM_CORE:
                    searchLines.add(breadcrumb.direction.toString() + " of the Core Worlds");
                    break;
                case DISTANCE_FROM_CORE:
                    searchLines.add("within " + Math.round(breadcrumb.distanceNear) + " and " + Math.round(breadcrumb.distanceFar) + " LY of the Core Worlds");
                    break;
                case DIRECTION_FROM_CONSTELLATION:
                    searchLines.add(breadcrumb.direction.toString() + " of the " + breadcrumb.constellation.getNameWithType());
                    break;
                case DISTANCE_FROM_CONSTELLATION:
                    searchLines.add("within " + Math.round(breadcrumb.distanceNear) + " and " + Math.round(breadcrumb.distanceFar) + " LY of the " + breadcrumb.constellation.getNameWithType());
                    break;
                case NUM_STARS_IN_SYSTEM:
                    switch (breadcrumb.numStars) {
                        case 0:
                            searchLines.add("in a starless system");
                            break;
                        case 1:
                            searchLines.add("in a single star system");
                            break;
                        case 2:
                            searchLines.add("in a binary star system");
                            break;
                        case 3:
                            searchLines.add("in a trinary star system");
                            break;
                        default:
                            break;
                    }
                    break;
                case TYPE_STAR_IN_SYSTEM:
                    switch (breadcrumb.starType) {
                        case STAR_TYPE_NEBULA:
                            searchLines.add("in a nebula");
                            break;
                        case STAR_TYPE_BLACK_HOLE:
                            searchLines.add("in a black hole system");
                            break;
                        default:
                            searchLines.add("in a system with a " + breadcrumb.starType + " star");
                            break;
                    }
                    break;
                case COLOR_STAR_IN_SYSTEM:
                    searchLines.add("in a system with " + Misc.getAOrAnFor(breadcrumb.starColor) + " " + breadcrumb.starColor + " star");
                    break;
                default:
                    break;
            }
        }

        return searchLines;
    }

    public List<StarSystemAPI> getSearchSystems() {
        List<StarSystemAPI> searchSystems = new ArrayList<>();
        if (numCluesFound == 0) {
            return searchSystems;
        }
        if (numCluesFound > 3) {
            searchSystems.add(leeSystem);
            return searchSystems;
        }

        Breadcrumb breadcrumb = leeBreadcrumbs.get(numCluesFound - 1);
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            if (breadcrumb.possibleSystems.contains(system.getId())) {
                searchSystems.add(system);
            }
        }

        return searchSystems;
    }

    public static class SystemHasNoColonyReq implements StarSystemRequirement {

        @Override
        public boolean systemMatchesRequirement(StarSystemAPI system) {
            for (MarketAPI market : Misc.getMarketsInLocation(system)) {
                if (market.isPlanetConditionMarketOnly()) {
                    continue;
                }
                if (market.getMemoryWithoutUpdate().getBoolean(MemFlags.HIDDEN_BASE_MEM_FLAG)) {
                    continue;
                }
                return false;
            }
            return true;
        }
    }

    public static class TannenListener implements ColonyInteractionListener {

        @Override
        public void reportPlayerOpenedMarket(MarketAPI market) {
        }

        @Override
        public void reportPlayerClosedMarket(MarketAPI market) {
        }

        @Override
        public void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market) {
        }

        @Override
        public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {
            boolean missionEnded = false;
            if (Global.getSector().getMemoryWithoutUpdate().contains("$swpBT_missionEnded")) {
                missionEnded = Global.getSector().getMemoryWithoutUpdate().getBoolean("$swpBT_missionEnded");
            }

            FleetMemberAPI tannenShip = null;
            for (ShipSaleInfo sale : transaction.getShipsSold()) {
                if (sale.getMember().getVariant().hasTag(TANNEN_SHIP)) {
                    tannenShip = sale.getMember();
                }
            }
            if (tannenShip != null) {
                ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
                PersonAPI captainTannen = ip.getPerson(TANNEN);

                CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
                playerFleet.getFleetData().addFleetMember(tannenShip);
                tannenShip.setCaptain(captainTannen);
                tannenShip.getRepairTracker().setMothballed(false);
                playerFleet.getFleetData().setSyncNeeded();
                playerFleet.getFleetData().syncIfNeeded();
                playerFleet.getFleetData().syncMemberLists();
                boolean skipCost = false;
                float tariff = transaction.getSubmarket().getTariff();
                if (transaction.getSubmarket().getPlugin() != null) {
                    tariff = transaction.getSubmarket().getPlugin().getTariff();
                    if (transaction.getSubmarket().getPlugin().isFreeTransfer()) {
                        skipCost = true;
                    }
                }
                if (!skipCost) {
                    float credits = tannenShip.getHullSpec().getBaseValue() * (1f + tariff) * Global.getSettings().getFloat("shipWeaponBuyPriceMult");
                    playerFleet.getCargo().getCredits().subtract(credits);
                    Global.getSector().getCampaignUI().getMessageDisplay().addMessage("\"Don't ye dare sell me Star Runner, ye dirty bastard!\"", Misc.getNegativeHighlightColor());
                } else {
                    if (!missionEnded) {
                        Global.getSector().getCampaignUI().getMessageDisplay().addMessage("\"Don't ye dare store me Star Runner, ye daft bastard!\"", Misc.getNegativeHighlightColor());
                    }
                }

                /* After mission ends, you can store the Star Runner */
                if (!skipCost || !missionEnded) {
                    CargoAPI cargo = transaction.getSubmarket().getCargo();
                    if (cargo != null) {
                        FleetDataAPI ships = cargo.getMothballedShips();
                        if (ships != null) {
                            ships.removeFleetMember(tannenShip);
                        }
                    }
                }
            }
        }
    }

    public static class TannenScript implements EveryFrameScript {

        private final IntervalUtil checkInterval = new IntervalUtil(1f, 2f);

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public boolean runWhilePaused() {
            return true;
        }

        @Override
        public void advance(float amount) {
            ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
            PersonAPI captainTannen = ip.getPerson(TANNEN);

            CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
            if (playerFleet == null) {
                return;
            }

            boolean missionEnded = false;
            if (Global.getSector().getMemoryWithoutUpdate().contains("$swpBT_missionEnded")) {
                missionEnded = Global.getSector().getMemoryWithoutUpdate().getBoolean("$swpBT_missionEnded");
            }

            OfficerDataAPI tannenData = playerFleet.getFleetData().getOfficerData(captainTannen);
            if (tannenData == null) {
                if (!Global.getSector().getCampaignUI().isShowingDialog() && !Global.getSector().getCampaignUI().isShowingMenu()
                        && (Global.getSector().getCampaignUI().getCurrentInteractionDialog() == null)) {
                    captainTannen.setFaction(Factions.PIRATES);
                    captainTannen.setRankId(Ranks.SPACE_COMMANDER);
                    captainTannen.setPostId(Ranks.POST_PATROL_COMMANDER);
                    Global.getSector().getCampaignUI().showCoreUITab(null);
                    Global.getSector().getCampaignUI().showInteractionDialog(new RuleBasedInteractionDialogPluginImpl("swpBTDismissTannen"), playerFleet);
                }
                return;
            }

            FleetMemberAPI currTannenShip = playerFleet.getFleetData().getMemberWithCaptain(captainTannen);
            FleetMemberAPI tannenShip = null;
            for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
                if (member.getVariant().hasTag(TANNEN_SHIP)) {
                    tannenShip = member;
                }
            }

            if (tannenShip != null) {
                if (!tannenShip.getShipName().contains("Star Runner")) {
                    tannenShip.setShipName("Star Runner");
                    playerFleet.getFleetData().setSyncNeeded();
                    playerFleet.getFleetData().syncIfNeeded();
                    playerFleet.getFleetData().syncMemberLists();
                    Global.getSector().getCampaignUI().getMessageDisplay().addMessage("\"Ye don't get t'rename me ship! It's the Star Runner!\"", Misc.getNegativeHighlightColor());
                }
            }

            if (!missionEnded) {
                if (tannenShip != currTannenShip) {
                    if (tannenShip != null) {
                        if (currTannenShip != null) {
                            currTannenShip.setCaptain(tannenShip.getCaptain());
                        }
                        tannenShip.setCaptain(captainTannen);
                        playerFleet.getFleetData().setSyncNeeded();
                        playerFleet.getFleetData().syncIfNeeded();
                        playerFleet.getFleetData().syncMemberLists();
                        Global.getSector().getCampaignUI().getMessageDisplay().addMessage("\"I'll never leave the Star Runner!\"", Misc.getNegativeHighlightColor());
                    } else {
                        if (currTannenShip != null) {
                            currTannenShip.setCaptain(null);
                            playerFleet.getFleetData().setSyncNeeded();
                            playerFleet.getFleetData().syncIfNeeded();
                            playerFleet.getFleetData().syncMemberLists();
                            Global.getSector().getCampaignUI().getMessageDisplay().addMessage("\"Nothing can ever replace the Star Runner...\"", Misc.getNegativeHighlightColor());
                        }
                    }
                }

                checkInterval.advance(amount);
                if (checkInterval.intervalElapsed()) {
                    boolean hostileFleetNearby = false;
                    List<CampaignFleetAPI> hostileFleets = CampaignUtils.getNearbyHostileFleets(playerFleet, Global.getSettings().getMaxSensorRange(playerFleet.getContainingLocation()));
                    for (CampaignFleetAPI hostileFleet : hostileFleets) {
                        if (!hostileFleet.isVisibleToSensorsOf(playerFleet)) {
                            continue;
                        }
                        if (hostileFleet.getAI() == null) {
                            continue;
                        }
                        if (hostileFleet.getAI().isHostileTo(playerFleet)) {
                            hostileFleetNearby = true;
                            break;
                        }
                    }

                    if (!Global.getSector().getCampaignUI().isShowingDialog() && !Global.getSector().getCampaignUI().isShowingMenu()
                            && (Global.getSector().getCampaignUI().getCurrentInteractionDialog() == null) && !hostileFleetNearby) {
                        long lastProgressTimesamp = Global.getSector().getMemoryWithoutUpdate().getLong("$swpBT_lastProgressTimestamp");
                        float daysSinceLastProgress = Global.getSector().getClock().getElapsedDaysSince(lastProgressTimesamp);
                        if (daysSinceLastProgress >= 365f) {
                            int numNoProgressEvents = 0;
                            if (Global.getSector().getMemoryWithoutUpdate().contains("$swpBT_numNoProgressEvents")) {
                                numNoProgressEvents = Global.getSector().getMemoryWithoutUpdate().getInt("$swpBT_numNoProgressEvents");
                            }
                            numNoProgressEvents++;
                            Global.getSector().getMemoryWithoutUpdate().set("$swpBT_numNoProgressEvents", numNoProgressEvents);
                            Global.getSector().getCampaignUI().showCoreUITab(null);
                            boolean shown = Global.getSector().getCampaignUI().showInteractionDialog(new RuleBasedInteractionDialogPluginImpl("swpBTNoProgress"), playerFleet);
                            if (shown) {
                                Global.getSector().getMemoryWithoutUpdate().set("$swpBT_lastProgressTimestamp", Global.getSector().getClock().getTimestamp());
                            } else {
                                Global.getSector().getMemoryWithoutUpdate().set("$swpBT_numNoProgressEvents", numNoProgressEvents - 1);
                            }
                        }

                        long lastCompletionTimesamp = Global.getSector().getMemoryWithoutUpdate().getLong("$swpBT_lastCompletionTimestamp");
                        float lastCompletionTimer = Global.getSector().getMemoryWithoutUpdate().getFloat("$swpBT_lastCompletionTimer");
                        int completionCount = Global.getSector().getMemoryWithoutUpdate().getInt("$swpBT_completionCount");
                        int completionEventCount = Global.getSector().getMemoryWithoutUpdate().getInt("$swpBT_completionEventCount");
                        float daysSinceLastCompletion = Global.getSector().getClock().getElapsedDaysSince(lastCompletionTimesamp);
                        if ((daysSinceLastCompletion >= lastCompletionTimer) && ((completionCount > (completionEventCount + 1)) || ((completionCount > 0) && (completionEventCount == 0)))) {
                            Global.getSector().getCampaignUI().showCoreUITab(null);
                            Global.getSector().getCampaignUI().showInteractionDialog(new RuleBasedInteractionDialogPluginImpl("swpBTCompletion"), playerFleet);
                        }
                    }
                }
            }
        }
    }
}
