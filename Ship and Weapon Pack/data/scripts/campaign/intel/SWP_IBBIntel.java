package data.scripts.campaign.intel;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.DataForEncounterSide;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.FleetMemberData;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI.SkillLevelAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfig;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfigGen;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDDelegate;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent.SkillPickPreference;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.Drops;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BreadcrumbSpecial;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.plugins.OfficerLevelupPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.util.SWP_Util;
import data.scripts.util.SWP_Util.RequiredFaction;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.MathUtils;

public class SWP_IBBIntel extends BaseIntelPlugin implements EveryFrameScript, FleetEventListener {

    public static final Set<String> BANNED_SYSTEMS = new HashSet<>(1);
    public static final Set<String> IBB_FACTIONS = new LinkedHashSet<>(6);
    public static final int MAX_BOUNTY_STAGE = FamousBountyStage.values().length - 1;
    public static Logger log = Global.getLogger(SWP_IBBIntel.class);

    static {
        IBB_FACTIONS.add(Factions.HEGEMONY);
        IBB_FACTIONS.add(Factions.INDEPENDENT);
        IBB_FACTIONS.add(Factions.LUDDIC_CHURCH);
        IBB_FACTIONS.add(Factions.PERSEAN);
        IBB_FACTIONS.add(Factions.DIKTAT);
        IBB_FACTIONS.add(Factions.TRITACHYON);
        IBB_FACTIONS.add("interstellarimperium");
        IBB_FACTIONS.add("shadow_industry");
        IBB_FACTIONS.add("tiandong");
        IBB_FACTIONS.add("ORA");
        IBB_FACTIONS.add("scalartech");
        IBB_FACTIONS.add("ironshell");
        IBB_FACTIONS.add("star_federation");
        IBB_FACTIONS.add("roider");
    }

    public static int calculatePowerLevel(FamousBountyStage stage) {
        float pointFactor = Math.max(0.5f, 0.65f + (Math.min(stage.qf, 1f) * Math.max((float) Math.sqrt(stage.qf), 1f) * 0.35f));
        float sModFactor;
        sModFactor = switch (stage.sMods) {
            case -1 ->
                0f;
            case 0 ->
                0.25f;
            case 1 ->
                1f;
            case 2 ->
                2f;
            case 3 ->
                2.75f;
            case 4 ->
                3f;
            default ->
                0f;
        };
        pointFactor *= 1f + ((stage.opBonus + (12f * sModFactor)) / 100f);
        float factionFactor = 1f;
        if (stage.fleetFaction.contentEquals(Factions.TRITACHYON)) {
            factionFactor = 1.1f;
        }
        if (stage.fleetFaction.contentEquals(Factions.REMNANTS)) {
            factionFactor = 1.25f;
        }
        if (stage.fleetFaction.contentEquals(Factions.OMEGA)) {
            factionFactor = 2f;
        }
        if (stage.fleetFaction.contentEquals(Factions.LIONS_GUARD)) {
            factionFactor = 1.05f;
        }
        if (stage.fleetFaction.contentEquals(Factions.PIRATES)) {
            factionFactor = 0.85f;
        }
        if (stage.fleetFaction.contentEquals(Factions.LUDDIC_PATH)) {
            factionFactor = 0.95f;
        }
        if (stage.fleetFaction.contentEquals(Factions.LUDDIC_CHURCH)) {
            factionFactor = 0.9f;
        }
        if (stage.fleetFaction.contentEquals(Factions.DERELICT)) {
            factionFactor = 0.75f;
        }
        if (stage.fleetFaction.contentEquals(Factions.MERCENARY)) {
            factionFactor = 1.1f;
        }
        if (stage.fleetFaction.contentEquals("shadow_industry")) {
            factionFactor = 1.1f;
        }
        if (stage.fleetFaction.contentEquals("sector")) {
            factionFactor = 1.05f;
        }
        if (stage.fleetFaction.contentEquals("everything")) {
            factionFactor = 1.1f;
        }
        if (stage.fleetFaction.contentEquals("templars")) {
            factionFactor = 1.3f;
        }
        if (stage.fleetFaction.contentEquals("cabal")) {
            factionFactor = 1.15f;
        }
        if (stage.fleetFaction.contentEquals("diableavionics")) {
            factionFactor = 1.1f;
        }
        if (stage.fleetFaction.contentEquals("ORA")) {
            factionFactor = 1.05f;
        }
        if (stage.fleetFaction.contentEquals("ii_imperial_guard")) {
            factionFactor = 1.1f;
        }
        if (stage.fleetFaction.contentEquals("sun_ice")) {
            factionFactor = 1.1f;
        }
        if (stage.fleetFaction.contentEquals("fob")) {
            factionFactor = 1.15f;
        }
        if (stage.fleetFaction.contentEquals("scalartech_elite")) {
            factionFactor = 1.1f;
        }
        float DP2FP = 0.9f;
        float power = (((stage.maxPts + stage.ptsFromAdds) * pointFactor) + (stage.ptsFromBoss * 1.5f)) * factionFactor * DP2FP;
        float offLvl = stage.level + (stage.officerCount * stage.officerLevel);
        float offElites = stage.cdrElites + (stage.officerCount * stage.officerElites);
        power += Math.min(power * 2f, (float) Math.sqrt(power) * ((offLvl / 7f) + (offElites / 21f)));
        power *= (stage.cdrSkills * 0.1f * Math.min(1f, 240f / (stage.maxPts + stage.ptsFromBoss + stage.ptsFromAdds))) + 1f;
        return Math.round(power);
    }

    private FamousBountyStage thisStage;

    private final FactionAPI faction = Global.getSector().getFaction("famous_bounty");
    private String flagshipName;
    private CampaignFleetAPI fleet;
    private SectorEntityToken hideoutLocation = null;

    private final IntervalUtil checkInterval = new IntervalUtil(0.5f, 1f);

    private PersonAPI person;
    private boolean it = false;
    private boolean they = false;
    private String targetDescLong;

    private float bountyCredits = 0;
    private float payment = 0;

    protected IBBResult result = null;

    @Override
    public void advanceImpl(float amount) {
        if (isDone()) {
            return;
        }

        float days = Global.getSector().getClock().convertToDays(amount);

        checkInterval.advance(days);
        if (checkInterval.intervalElapsed()) {
            for (FactionAPI fac : Global.getSector().getAllFactions()) {
                if (fac != Global.getSector().getFaction(Factions.PLAYER)) {
                    fac.setRelationship("famous_bounty", RepLevel.INHOSPITABLE);
                }
            }
            if ((fleet != null) && fleet.isAlive()) {
                fleet.setFaction(faction.getId());
            }
        }

        if (fleet.getFlagship() == null || fleet.getFlagship().getCaptain() != person) {
            reportResult(new IBBResult(IBBResultType.END_OTHER, 0));
        }
    }

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
        if (isDone()) {
            return;
        }

        if (this.fleet == fleet) {
            fleet.setCommander(fleet.getFaction().createRandomPerson());
            reportResult(new IBBResult(IBBResultType.END_OTHER, 0));
        }
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        if (isDone()) {
            return;
        }

        if (battle.isInvolved(fleet) && !battle.isPlayerInvolved()) {
            if (fleet.getFlagship() == null || fleet.getFlagship().getCaptain() != person) {
                fleet.setCommander(fleet.getFaction().createRandomPerson());
                reportResult(new IBBResult(IBBResultType.END_TAKEN, 0));
                return;
            }
        }

        if (!battle.isPlayerInvolved() || !battle.isInvolved(fleet) || battle.onPlayerSide(fleet)) {
            return;
        }

        // didn't destroy the original flagship
        if (fleet.getFlagship() != null && fleet.getFlagship().getCaptain() == person) {
            return;
        }

        payment = (int) (bountyCredits * battle.getPlayerInvolvementFraction());
        if (payment <= 0) {
            reportResult(new IBBResult(IBBResultType.END_TAKEN, 0));
            return;
        }

        log.info(String.format("Paying bounty of %f from the International Bounty Board", payment));
        Global.getSector().getPlayerFleet().getCargo().getCredits().add(payment);
        reportResult(new IBBResult(IBBResultType.END_PLAYER_BOUNTY, (int) payment));
    }

    @Override
    protected void notifyEnding() {
        // clean up fleet
        if (fleet != null) {
            fleet.getMemoryWithoutUpdate().set("$stillAlive", false);
            fleet.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE);
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_NON_AGGRESSIVE, true);
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE, true);
            Misc.makeUnimportant(fleet, "swp_ibb");
            fleet.clearAssignments();
            if (hideoutLocation != null) {
                fleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, hideoutLocation, 1000000f, null);
            } else {
                fleet.despawn();
            }
        }
    }

    public void reportResult(IBBResult result) {
        if (this.result != null) {
            return;
        }
        this.result = result;
        sendUpdateIfPlayerHasIntel(result, false);
        endMission(false);
    }

    public void endMission(boolean expire) {
        if (!isEnding() && !isEnded()) {
            if (expire) {
                SWP_IBBTracker.getTracker().reportStageExpired(thisStage);
            } else {
                SWP_IBBTracker.getTracker().reportStageCompleted(thisStage);
            }
            setImportant(false);
        }
        endAfterDelay();
    }

    public FamousBountyStage getStage() {
        return thisStage;
    }

    public String getTargetDesc() {
        return targetDescLong;
    }

    public PersonAPI getPerson() {
        return person;
    }

    public void init() {
        thisStage = SWP_IBBTracker.getTracker().getLowestIncompleteNonRepickedStage();
        if (thisStage == null) {
            thisStage = SWP_IBBTracker.getTracker().getLowestIncompleteStage();
            if (thisStage == null) {
                log.info(String.format("No incomplete stages"));
                ended = true;
                return;
            }
        }

        if (SWP_IBBTracker.getTracker().isStagePosted(thisStage) || SWP_IBBTracker.getTracker().isStageBegun(thisStage)) {
            float powerLevel = SWP_Util.calculatePowerLevel(Global.getSector().getPlayerFleet());
            int matchingStageNum = 0;
            for (int i = 0; i < FamousBountyStage.values().length; i++) {
                FamousBountyStage stage = SWP_IBBTracker.getStage(i);
                if (calculatePowerLevel(stage) * (2f / 3f) <= powerLevel) {
                    matchingStageNum = i;
                } else {
                    break;
                }
            }

            int stageNum;
            int currentStageNum = thisStage.ordinal();
            if (matchingStageNum <= currentStageNum) {
                stageNum = currentStageNum + Math.round(Math.abs((float) MathUtils.getRandom().nextGaussian() * 2f));
            } else {
                stageNum = matchingStageNum + Math.round(Math.abs((float) MathUtils.getRandom().nextGaussian() * 3f));
            }
            stageNum = Math.max(currentStageNum, stageNum);

            while (stageNum > currentStageNum) {
                FamousBountyStage stage = SWP_IBBTracker.getStage(stageNum);
                if (SWP_IBBTracker.getTracker().isStageAvailable(stage)) {
                    break;
                } else {
                    stageNum--;
                }
            }

            thisStage = SWP_IBBTracker.getStage(stageNum);
            if (thisStage == null) {
                log.info(String.format("No compatible stage"));
                ended = true;
                return;
            }
        }

        if (!SWP_IBBTracker.getTracker().isStageAvailable(thisStage)) {
            log.info(String.format("Stage %s is not available", thisStage.name()));
            thisStage = null;
            ended = true;
            return;
        }

        person = initPerson(faction);

        bountyCredits = thisStage.reward;
        payment = bountyCredits;

        it = thisStage.it;
        they = thisStage.they;
        switch (thisStage) {
            case STAGE_UNDINE -> {
                targetDescLong = "%s, known to possess a rag-tag fleet of renegade pirates. The primary danger is "
                        + person.getName().getLast() + "'s pair of unique Lashers, which " + getHeOrShe()
                        + " has put to devastating use raiding supply lines.";
            }
            case STAGE_ELDER_ORB -> {
                targetDescLong = "%s, wanted dead for arson, murder, and invasion of privacy. " + Misc.ucFirst(getHeOrShe())
                        + " is a dangerous sexual deviant, known to perform bizarre and horrifying experiments on those "
                        + getHeOrShe() + " captures. " + person.getName().getLast() + " currently has a unique Beholder at "
                        + getHisOrHer() + " beck and call.";
            }
            case STAGE_PONY -> {
                targetDescLong = "%s, whose private flotilla is helmed by a pair of unique ships. "
                        + "One of these is a unique Mule, which " + person.getName().getLast()
                        + " recently used to steal a unique Tarsus. " + Misc.ucFirst(getHeOrShe())
                        + " is a dangerous criminal who will stop at nothing to bring chaos to the Sector.";
            }
            case STAGE_HADES -> {
                targetDescLong = "%s, who commands a converted trade fleet, largely purchased privately. "
                        + person.getName().getLast() + " is in possession of four unique Cerberi, which " + getHeOrShe()
                        + " stole from an IBB shareholder.";
            }
            case STAGE_FRACTURE -> {
                targetDescLong = "a former Lion's Guard officer named %s, who officially declared war against... "
                        + "'everyone'. As a result, " + person.getName().getLast() + " is wanted dead by nearly every government. "
                        + Misc.ucFirst(getHeOrShe()) + " possesses a unique Sunder and a unique Hammerhead, which " + getHeOrShe()
                        + " has used to great effect in the past.";
            }
            case STAGE_KINGFISHER -> {
                targetDescLong = "%s, a famed war hero known for singlehandedly stopping the Mazalot Rebellion. However, "
                        + getHeOrShe() + " later fell victim to a neurological disease, eventually summoning the most loyal members of "
                        + getHisOrHer() + " old fleet and going on a rampage. Despite " + person.getName().getLast() + "'s mental disorder, "
                        + getHeOrShe() + " remains a skilled commander, and is in possession of a unique Eagle.";
            }
            case STAGE_LIBERTY -> {
                targetDescLong = "%s, once an esteemed leader. Now, " + getHeOrShe()
                        + " has lost every trace of sanity, carrying on against the Sector in the name of a fictional empire. "
                        + person.getName().getLast() + " commands a unique Wuzhang, and leads a band of indoctrinated cronies.";
            }
            case STAGE_POSEIDON -> {
                targetDescLong = "%s, a known terrorist at large. Calls for mercenaries and bounty hunters to take this "
                        + " dangerous individual down have been met with failure. " + person.getName().getLast()
                        + " owns a unique Charybdis, which " + getHeOrShe() + " reportedly plundered from the Anar shipyards.";
            }
            case STAGE_RAPTOR -> {
                targetDescLong = "%s, the daughter of a retired admiral. Using the privilege " + getHeOrShe() + " received, "
                        + person.getName().getLast() + " acquired a unique Falcon and a unique Medusa. Unfortunately, "
                        + getHeOrShe() + " went on a romp, carving a war-path of destruction in " + getHisOrHer() + " wake.";
            }
            case STAGE_IHS -> {
                targetDescLong = "%s. " + Misc.ucFirst(getHeOrShe()) + " is perhaps the most dangerous of all the Idoneus exiles, "
                        + "wanted by various factions for " + getHisOrHer() + " frequent attacks on trading groups and military "
                        + "transport fleets in the sector's outer regions. " + Misc.ucFirst(getHeOrShe()) + " has borne witness to "
                        + "the full lifecycle of at least three full-scale exiled fleets; such extensive battlefield experience "
                        + "cannot be ignored. Additionally, " + getHisOrHer() + " flagship is a unique and dangerous hybrid prototype.";
            }
            case STAGE_GULF -> {
                targetDescLong = "%s, wanted on charges of treason by the Diable Corporation. " + Misc.ucFirst(getHeOrShe())
                        + " thereafter carved a war-path through allied territory, costing local polities millions in damage. As such, "
                        + getHeOrShe() + " is officially wanted dead by all IBB member polities. " + person.getName().getLast()
                        + " commands a large Diable fleet from the helm of " + getHisOrHer() + " unique Gust.";
            }
            case STAGE_BIG_MAC -> {
                targetDescLong = "Rylek, former ARS brass who seems to have decided that the Society's crusade against the Domain "
                        + "was far too sane for their liking, and have struck out on " + getHisOrHer() + " own with the declared "
                        + "intent of exterminating all humanity. Rylek took " + getHisOrHer() + " old flagship with " + getHimOrHer()
                        + " (a unique Macnamara Heavy Cruiser) and have since accumulated a sizeable band of similarly insane pirates.";
            }
            case STAGE_FRANKENSTEIN -> {
                targetDescLong = "Sir %s, who once inherited a tremendous fortune from " + getHisOrHer()
                        + " father, including a unique hull vaguely describable as an unholy amalgamation of dead ships. While "
                        + person.getName().getLast() + " was once considered a respected knight, " + getHeOrShe()
                        + " has since let " + getHisOrHer() + " vast wealth get to " + getHisOrHer() + " head, engaging in a "
                        + "rampaging joyride, causing countless deaths along the way.";
            }
            case STAGE_LEVIATHAN -> {
                targetDescLong = "%s, formerly a multi-millionaire trade magnate. However, " + getHeOrShe()
                        + " recently fell afoul of the authorities after inciting and exploiting several food shortages. "
                        + person.getName().getLast() + " is currently on the run, commanding a large privateer armada "
                        + "from " + getHisOrHer() + " massive unique Atlas and a pair of unique Phaetons.";
            }
            case STAGE_VESTIGE -> {
                targetDescLong = "%s. You are likely aware that ScalarTech Solutions likes to avoid conflict. Mostly. But "
                        + person.getName().getLast() + ", one of their commanders, has made a bit of a name for " + getHimOrHerself()
                        + " by taking a more aggressive approach. " + Misc.ucFirst(getHeOrShe()) + " raids convoys of factions that could "
                        + "be deemed a threat to the Spindle system... which happens to include most of the IBB member states. Unfortunately, "
                        + "the STDF have distanced themselves from the accusations that " + person.getName().getLast() + "'s actions have "
                        + "drawn, and certain political issues make reaching " + getHimOrHer() + " through official channels impossible. "
                        + "That's where you come in; we have confirmation that ScalarTech will look the other way if "
                        + person.getName().getLast() + " were to be dealt with \"off the record.\" Beware " + getHisOrHer()
                        + " unique ScalarTech flagship; the reports are clear on its devastating capabilities.";
            }
            case STAGE_EMPEROR -> {
                targetDescLong = "%s. " + Misc.ucFirst(getHeOrShe()) + " was once a double-agent, working for the Tri-Tachyon in secret "
                        + "while posing as a Hegemony commander. " + Misc.ucFirst(getHeOrShe()) + " eventually betrayed both parties, "
                        + " in the process becoming wanted dead in duplicate. " + person.getName().getLast() + " still possesses "
                        + getHisOrHer() + " loyal fleet, and has a unique Dominator as well as four unique Brawlers under "
                        + getHisOrHer() + " command.";
            }
            case STAGE_YAMATO -> {
                targetDescLong = "%s, a renowned war veteran. For unknown reasons, " + getHeOrShe()
                        + " rebelled against the Sector's established governments. " + person.getName().getLast()
                        + " now brings " + getHisOrHer() + " private fleet to bear against us. " + Misc.ucFirst(getHeOrShe())
                        + " commands a unique Dominus, a famous ship that has seen many great battles over its long career.";
            }
            case STAGE_EUPHORIA -> {
                targetDescLong = "famed drug magnate %s, wanted on charges of terrorism and crimes against humanity. "
                        + Misc.ucFirst(getHeOrShe()) + " is known to have uploaded his mind into an Alpha core, using " + getHisOrHer()
                        + " incredible mental capacity to throw several economies into chaos. " + person.getName().getLast()
                        + " is also known to have connections to the Starlight Cabal, having quickly acquired a fleet of "
                        + "high-end warships via mysterious means. " + person.getName().getLast()
                        + " was last seen commanding a unique Astral.";
            }
            case STAGE_CLERIC -> {
                targetDescLong = "%s, an estranged Templar leader. " + person.getName().getLast() + " seeks to destroy everything "
                        + getHeOrShe() + " can, so the IBB has decided that this indivudal must be stopped. " + person.getName().getLast()
                        + "'s fleet contains a unique Paladin of unknown origin.";
            }
            case STAGE_RAST -> {
                targetDescLong = "%s, a \"copycat\" of Rast the Desireless. " + Misc.ucFirst(getHeOrShe()) + " stole Rast's flagship "
                        + "amidst a rebellion against the Foundation of Borken. Afterwards, " + person.getName().getLast()
                        + " assembled a group of Borken rebels and proceeded to embark on a journey into the \"cosmic harem\". "
                        + Misc.ucFirst(getHeOrShe()) + " evidently failed to understand the true meaning of the \"real\" Rast's love, "
                        + "given the multitude of colonies " + Misc.ucFirst(getHeOrShe()) + " has razed. " + person.getName().getLast()
                        + "'s fleet is a powerful warband led by a unique ShuddeMell.";
            }
            case STAGE_POPE -> {
                targetDescLong = "%s. We have little information on the target. The presumption is that " + getHeOrShe()
                        + " is aligned with the Knights Templar in some way, judging by " + person.getName().getLast()
                        + "'s unique Archbishop of immense power and accompanying fleet of cult followers.";
            }
            case STAGE_ILIAD -> {
                targetDescLong = "%s, a well-known admiral, famously wheelchair-bound due to injuries sustained during "
                        + getHisOrHer() + " final battle. Now, " + getHeOrShe() + " seeks to regain " + getHisOrHer()
                        + " former glory by taking " + getHisOrHer() + " armada on a crash course through the sector, "
                        + "purportedly causing as much damage as possible. " + person.getName().getLast() + " commands "
                        + getHisOrHer() + " signature unique Odyssey and unique Aurora.";
            }
            case STAGE_TITAN_X -> {
                targetDescLong = "a deranged madman by the name of %s. This monster has been capturing various faction officials, "
                        + "committing psychological torture of the most vile sort, no doubt toward some nefarious ends. "
                        + "Worse still, every fleet we've sent to deal with " + getHimOrHer()
                        + " has mysteriously vanished without a trace. Whole fleets just... missing. Commander, " + person.getName().getLast()
                        + " must be stopped, but be careful; " + getHisOrHer() + " fleet contains a unique phase ship of Imperial design.";
            }
            case STAGE_FRAMEBREAKER -> {
                targetDescLong = "%s, \"The King\", who fashioned " + getHimOrHerself()
                        + " after the legendary prophet Ludd to acquire a fleet of followers. " + Misc.ucFirst(getHisOrHer())
                        + " numerous acts of terrorism have left a long trail of bodies. " + "King " + person.getName().getLast()
                        + " commands a fleet of fanatics from a unique Onslaught. " + Misc.ucFirst(getHisOrHer())
                        + " fanatics are also in possession of a pair of unique Dominators.";
            }
            case STAGE_ODIN -> {
                targetDescLong = "an artificial entity, designated %s. Being an ancient AI, " + getHeOrShe()
                        + " predates the Collapse. For reasons of international security, this AI must be destroyed as soon as possible. "
                        + person.getName().getLast() + " was last seen controlling a unique Mimir and six unique Potnia-bisses.";
            }
            case STAGE_BULLSEYE -> {
                targetDescLong = "a P.A.C.K admiral by the name of %s, purportedly spliced with canine genes by "
                        + getHisOrHer() + " own request. The operation drove " + person.getName().getLast()
                        + " insane, as evidenced by the fact that " + getHeOrShe() + " proceeded to swipe a group of "
                        + "cutting-edge P.A.C.K. warships in order to raid various colonies indiscriminately.";
            }
            case STAGE_LUCIFER -> {
                targetDescLong = "%s, a failed Tri-Tachyon Alpha AI experiment. After falling rampant, " + getHeOrShe()
                        + " stole a set of Domain-era schematics for prototype phase coils. We recently discovered that "
                        + person.getName().getLast() + " put this technology to use, creating a powerful fleet consisting "
                        + "almost entirely of unique phase ships. " + Misc.ucFirst(getHisOrHer()) + " continued existence "
                        + "cannot be allowed.";
            }
            case STAGE_SPORESHIP -> {
                targetDescLong = "%s, an unknown AI entity, theorized to be a cluster of Alphas working in tandem. " + Misc.ucFirst(getHeOrShe())
                        + " has somehow obtained a Domain-era sporeship, a legendary type of vessel. " + person.getName().getLast()
                        + "'s intentions are unknown at this point, but " + getHeOrShe() + " appears to be building up a "
                        + "fleet of automated ships. This development is a threat to the Persean Sector at large.";
            }
            case STAGE_ZEUS -> {
                targetDescLong = "IBB founder %s, who has gone AWOL. Before going rampant, " + getHeOrShe()
                        + " obtained a unique Paragon, a unique Conquest, and a unique Onslaught. Be advised: "
                        + person.getName().getLast() + " is a gifted tactician and a master of starship modification.";
            }
            case STAGE_CANCER -> {
                targetDescLong = "...\"%s.\" Yes, really. " + Misc.ucFirst(getHeOrShe()) + " was a relatively unknown cybernetics "
                        + "engineer and hobbyist scrapper by the name of Gary Boldmann, until about a cycle ago when " + getHeOrShe()
                        + " came across some kind of experimental Tri-Tachyon implant stored in a hidden cache at the sector's edge. "
                        + Misc.ucFirst(getHeOrShe()) + " was never the same after installing it. Details of the ensuing \"incidents\" "
                        + "are vague, at best, because the few survivors who escaped from " + person.getName().getFullName()
                        + "'s mad quest for bio-mechanical synthesis had themselves been forcibly implanted with corrupt cybernetics... "
                        + "Let's just say that those survivors were driven completely insane by the experience. I'll be honest: we don't "
                        + "know much about " + person.getName().getFullName() + "'s capabilities, beyond the fact that " + getHeOrShe()
                        + " is using some kind of temporal manipulation technology that even the Tachs wrote off as \"irresponsibly "
                        + "dangerous.\" Good grief.";
            }
            default -> {
                log.info(String.format("No definition for stage %s", thisStage.name()));
                thisStage = null;
                ended = true;
            }
        }
    }

    public void start() {
        if (person == null) {
            log.error("Failed to start: no person");
            endImmediately();
            return;
        }

        if (isDone()) {
            log.error("Failed to start: ended itself prematurely");
            return;
        }

        pickHideoutLocation();

        if (isDone()) {
            log.error("Failed to start: hideout location invalid");
            return;
        }

        spawnFleet();

        if (!isDone()) {
            if (!Global.getSector().getScripts().contains(this)) {
                Global.getSector().addScript(this);
            }
        }

        SWP_IBBTracker.getTracker().reportStageBegan(thisStage);

//        Global.getSector().reportEventStage(this, "start", null, messagePriority, new BaseOnMessageDeliveryScript() {
//            @Override
//            public void beforeDelivery(CommMessageAPI message) {
//                if (hideoutLocation.getContainingLocation() instanceof StarSystemAPI) {
//                    message.setStarSystemId(hideoutLocation.getContainingLocation().getId());
//                }
//            }
//        });
        log.info(String.format("Starting famous bounty for person %s at %s", person.getName().getFullName(), hideoutLocation.getContainingLocation().getName()));
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void reportRemovedIntel() {
        super.reportRemovedIntel();
        Global.getSector().removeScript(this);
    }

    private void pickHideoutLocation() {
        float emptyWeight = 1f;
        float occupiedWeight = 0.2f;

        float multMod = 1f;
        while (hideoutLocation == null && multMod <= 5f) {
            WeightedRandomPicker<StarSystemAPI> systemPicker = new WeightedRandomPicker<>();
            for (StarSystemAPI system : Global.getSector().getStarSystems()) {
                float weight = system.getPlanets().size();
                float mult = 0f;

                if (system.hasPulsar() || system.hasTag("ix_fortified") || system.hasTag("oci_theme")
                        || system.hasTag("theme_breakers_suppressed") || system.hasTag("theme_breakers_resurgent")
                        || system.hasTag(Tags.THEME_CORE_POPULATED)) {
                    continue;
                }

                if (system.hasTag(Tags.THEME_MISC_SKIP)) {
                    mult = 1f;
                } else if (system.hasTag(Tags.THEME_MISC)) {
                    mult = 3f;
                } else if (system.hasTag(Tags.THEME_REMNANT_NO_FLEETS)) {
                    mult = 3f;
                } else if (system.hasTag(Tags.THEME_RUINS)) {
                    mult = 5f;
                } else if (system.hasTag(Tags.THEME_REMNANT_DESTROYED)) {
                    mult = 3f;
                } else if (system.hasTag(Tags.THEME_CORE_UNPOPULATED)) {
                    mult = 1f;
                } else if (system.hasTag("theme_breakers_destroyed")) {
                    mult = 3f;
                }

                if (system.getConstellation() == null) {
                    mult *= 0.01f;
                }

                boolean empty = true;
                boolean viablePlanets = false;
                for (SectorEntityToken planet : system.getPlanets()) {
                    if (planet.isStar()) {
                        continue;
                    }
                    if (planet.getMarket() != null && !planet.getMarket().isPlanetConditionMarketOnly()) {
                        empty = false;
                    }
                    viablePlanets = true;
                    break;
                }
                if (!viablePlanets) {
                    continue;
                }
                if (empty) {
                    mult *= emptyWeight;
                } else {
                    mult *= occupiedWeight;
                }

                if (mult <= 0) {
                    continue;
                }
                float minDist = 3000f + (thisStage.ordinal() + 1) * 200f;
                float maxDist = 30000f + (thisStage.ordinal() + 1) * 1000f;
                maxDist *= multMod;
                minDist /= multMod;
                float dist = system.getLocation().length();
                float distMult = (Math.max(0.01f, maxDist - dist) / maxDist) * (Math.max(0.01f, dist - minDist) / minDist);

                systemPicker.add(system, weight * mult * distMult);
            }

            StarSystemAPI system = systemPicker.pick();

            if (system != null) {
                WeightedRandomPicker<SectorEntityToken> picker = new WeightedRandomPicker<>();
                for (SectorEntityToken planet : system.getPlanets()) {
                    if (planet.isStar()) {
                        continue;
                    }
                    picker.add(planet);
                }
                hideoutLocation = picker.pick();
            }

            multMod += 1f;
        }

        if (hideoutLocation == null) {
            log.info(String.format("Failed to find hideout for person %s", person.getName().getFullName()));
            endImmediately();
        }
    }

    @Override
    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
        Color h = Misc.getHighlightColor();
        float pad = 3f;
        float opad = 10f;

        float initPad = pad;
        if (mode == ListInfoMode.IN_DESC) {
            initPad = opad;
        }

        Color tc = getBulletColorForMode(mode);

        bullet(info);

        if (result == null) {
            if (mode == ListInfoMode.IN_DESC) {
                info.addPara("%s reward", initPad, tc, h, Misc.getDGSCredits(bountyCredits));
            } else if (!isEnding()) {
                info.addPara("%s reward", 0f, tc, h, Misc.getDGSCredits(bountyCredits));
            }
            unindent(info);
            return;
        }

        switch (result.type) {
            case END_PLAYER_BOUNTY ->
                info.addPara("%s received", initPad, tc, h, Misc.getDGSCredits(result.payment));
            case END_TAKEN, END_OTHER -> {
            }

        }

        unindent(info);
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color c = getTitleColor(mode);

        info.addPara(getName(), c, 0f);

        addBulletPoints(info, mode);

    }

    @Override
    public String getSortString() {
        return "IBB Mission";
    }

    @Override
    public String getName() {
        String n = person.getName().getFullName();

        if (result != null) {
            switch (result.type) {
                case END_PLAYER_BOUNTY -> {
                    return "IBB Mission Completed - " + n;
                }
                case END_OTHER, END_TAKEN -> {
                    return "IBB Mission Ended - " + n;
                }
            }
        }

        return "IBB Mission - " + n;
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return faction;
    }

    @Override
    public String getSmallDescriptionTitle() {
        return getName();
    }

    @Override
    public String getImportantIcon() {
        return Global.getSettings().getSpriteName("intel", "important_accepted_mission");
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        Color h = Misc.getHighlightColor();
        float opad = 10f;

        String portraitSprite = person.getPortraitSprite();
        if (portraitSprite.contentEquals("graphics/imperium/portraits/ii_helmutreal.png")) {
            portraitSprite = "graphics/imperium/portraits/ii_helmut.png";
        }
        info.addImage(portraitSprite, width, 128, opad);

        info.addPara("Find and eliminate " + person.getName().getFullName() + " for the International Bounty Board.", opad, faction.getBaseUIColor());

        if (result != null) {
            if (result.type == IBBResultType.END_PLAYER_BOUNTY) {
                info.addPara("You have successfully completed this International Bounty Board mission.", opad);
            } else {
                info.addPara("This International Bounty Board mission is no longer on offer.", opad);
            }
        }

        addBulletPoints(info, ListInfoMode.IN_DESC);
        if (result == null) {
            if (hideoutLocation != null) {
                SectorEntityToken fake = hideoutLocation.getContainingLocation().createToken(0, 0);
                fake.setOrbit(Global.getFactory().createCircularOrbit(hideoutLocation, 0, 1000, 100));

                String loc = BreadcrumbSpecial.getLocatedString(fake);
                loc = loc.replaceAll("orbiting", "hiding out near");
                loc = loc.replaceAll("located in", "hiding out in");
                String sheWas = "She was";
                if (person.getGender() == Gender.MALE) {
                    sheWas = "He was";
                }
                info.addPara(sheWas + " last seen " + loc + ".", opad);
            }

            int cols = 7;
            float iconSize = width / cols;

            if (Global.getSettings().isDevMode()) {
                boolean deflate = false;
                if (!fleet.isInflated()) {
                    fleet.setFaction(faction.getId(), true);
                    fleet.inflateIfNeeded();
                    deflate = true;
                }

                String her = "her";
                if (person.getGender() == Gender.MALE) {
                    her = "his";
                }
                info.addPara("The IBB has disseminated partial intel on some of the ships in " + her + " fleet. (DEBUG: full info)", opad);
                info.addShipList(cols, (fleet.getMembersWithFightersCopy().size() + 6) / 7, iconSize, getFactionForUIColors().getBaseUIColor(), fleet.getMembersWithFightersCopy(), opad);

                if (deflate) {
                    fleet.deflate();
                }
            } else {
                boolean deflate = false;
                if (!fleet.isInflated()) {
                    fleet.setFaction(faction.getId(), true);
                    fleet.inflateIfNeeded();
                    deflate = true;
                }

                List<FleetMemberAPI> list = new ArrayList<>();
                Random random = new Random(person.getNameString().hashCode() * 170000);
                List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();

                if (thisStage == FamousBountyStage.STAGE_TITAN_X) {
                    boolean flagshipPicked = false;
                    for (FleetMemberAPI member : members) {
                        if (!SWP_Util.SPECIAL_SHIPS.contains(member.getHullId())) {
                            continue;
                        }
                        if (member.getHullId().contentEquals("ii_boss_titanx")) {
                            continue;
                        }

                        PersonAPI fakePerson = Global.getFactory().createPerson();
                        fakePerson.setFaction(person.getFaction().getId());
                        fakePerson.setGender(person.getGender());
                        fakePerson.getName().setFirst(person.getName().getFirst());
                        fakePerson.getName().setLast(person.getName().getLast());
                        fakePerson.setPortraitSprite("graphics/imperium/portraits/ii_helmut.png");
                        FleetMemberAPI copy = Global.getFactory().createFleetMember(FleetMemberType.SHIP, member.getVariant());
                        if (!flagshipPicked) {
                            copy.setCaptain(fakePerson);
                            flagshipPicked = true;
                        }
                        list.add(copy);
                    }
                } else {
                    for (FleetMemberAPI member : members) {
                        if (!SWP_Util.SPECIAL_SHIPS.contains(member.getHullId())) {
                            continue;
                        }

                        FleetMemberAPI copy = Global.getFactory().createFleetMember(FleetMemberType.SHIP, member.getVariant());
                        if (member.isFlagship()) {
                            copy.setCaptain(person);
                        }
                        list.add(copy);
                    }
                }

                int max = 5 + list.size();
                for (FleetMemberAPI member : members) {
                    if (list.size() >= max || SWP_Util.SPECIAL_SHIPS.contains(member.getHullId())) {
                        continue;
                    }

                    if (member.isFighterWing()) {
                        continue;
                    }

                    float prob = (float) member.getFleetPointCost() / 20f;
                    prob += (float) max / (float) members.size();
                    if (member.isFlagship()) {
                        prob = 1f;
                    }

                    if (random.nextFloat() > prob) {
                        continue;
                    }

                    FleetMemberAPI copy = Global.getFactory().createFleetMember(FleetMemberType.SHIP, member.getVariant());
                    if (member.isFlagship()) {
                        copy.setCaptain(person);
                    }
                    list.add(copy);
                }

                if (!list.isEmpty()) {
                    String her = "her";
                    if (person.getGender() == Gender.MALE) {
                        her = "his";
                    }
                    info.addPara("The IBB has disseminated partial intel on some of the ships in " + her + " fleet.", opad);
                    info.addShipList(cols, Math.max(1, (max + 6) / 7), iconSize, getFactionForUIColors().getBaseUIColor(), list, opad);

                    int num = members.size() - list.size();
                    num = Math.round((float) num * (1f + random.nextFloat() * 0.5f));

                    if (num < 5) {
                        num = 0;
                    } else if (num < 10) {
                        num = 5;
                    } else if (num < 20) {
                        num = 10;
                    } else {
                        num = 20;
                    }

                    if (num > 1) {
                        info.addPara("The intel assessment notes the fleet may contain upwards of %s other ships"
                                + " of lesser significance.", opad, h, "" + num);
                    } else {
                        info.addPara("The intel assessment notes the fleet may contain several other ships"
                                + " of lesser significance.", opad);
                    }
                }

                if (deflate) {
                    fleet.deflate();
                }
            }
        }

        info.addPara("The target is " + targetDescLong, opad, Misc.getHighlightColor(), person.getNameString());
    }

    @Override
    public String getIcon() {
        String portraitSprite = person.getPortraitSprite();
        if (portraitSprite.contentEquals("graphics/imperium/portraits/ii_helmutreal.png")) {
            portraitSprite = "graphics/imperium/portraits/ii_helmut.png";
        }
        return portraitSprite;
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_BOUNTY);
        tags.add(Tags.INTEL_MISSIONS);
        tags.add(Tags.INTEL_ACCEPTED);
        return tags;
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        Constellation c = hideoutLocation.getConstellation();
        SectorEntityToken entity = null;
        if (c != null && map != null) {
            entity = map.getConstellationLabelEntity(c);
        }
        if (entity == null) {
            entity = hideoutLocation;
        }
        return entity;
    }

    private void spawnFleet() {
        float qf = thisStage.qf;
        int commanderLevel = thisStage.level;
        int commanderSkills = thisStage.cdrSkills;
        int commanderElites = thisStage.cdrElites;
        int officerLevel = thisStage.officerLevel;
        int officerElites = thisStage.officerElites;
        int officerCount = thisStage.officerCount;
        int maxPts = thisStage.maxPts;
        String fleetFactionId = thisStage.fleetFaction;

        FactionDoctrineAPI doctrine = Global.getSector().getFaction(fleetFactionId).getDoctrine().clone();
        switch (thisStage) {
            case STAGE_LUCIFER ->
                doctrine.setAggression(5);
            case STAGE_ZEUS -> {
                doctrine.setWarships(4);
                doctrine.setCarriers(2);
                doctrine.setPhaseShips(1);
                doctrine.setShipSize(5);
            }
            default -> {
                /* Trend taller */
                if ((maxPts >= 50) && (doctrine.getShipSize() <= 1)) {
                    doctrine.setShipSize(doctrine.getShipSize() + 1);
                    if (maxPts >= 250) {
                        doctrine.setShipSize(doctrine.getShipSize() + 1);
                    }
                    if (maxPts >= 500) {
                        doctrine.setShipSize(doctrine.getShipSize() + 1);
                    }
                    if (maxPts >= 800) {
                        doctrine.setShipSize(doctrine.getShipSize() + 1);
                    }
                } else if ((maxPts >= 200) && (doctrine.getShipSize() <= 2)) {
                    doctrine.setShipSize(doctrine.getShipSize() + 1);
                    if (maxPts >= 400) {
                        doctrine.setShipSize(doctrine.getShipSize() + 1);
                    }
                    if (maxPts >= 650) {
                        doctrine.setShipSize(doctrine.getShipSize() + 1);
                    }
                } else if ((maxPts >= 350) && (doctrine.getShipSize() <= 3)) {
                    doctrine.setShipSize(doctrine.getShipSize() + 1);
                    if (maxPts >= 550) {
                        doctrine.setShipSize(doctrine.getShipSize() + 1);
                    }
                } else if ((maxPts >= 500) && (doctrine.getShipSize() <= 4)) {
                    doctrine.setShipSize(doctrine.getShipSize() + 1);
                }
            }
        }

        final FleetParamsV3 params = new FleetParamsV3(
                null,
                null,
                fleetFactionId,
                qf, // qualityOverride
                "famous_bounty",
                Math.max(maxPts, 10), // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                0f); // qualityMod

        params.doctrineOverride = doctrine;
        params.commander = person;
        params.withOfficers = false;
        params.noCommanderSkills = true;
        params.ignoreMarketFleetSizeMult = true;
        params.forceAllowPhaseShipsEtc = true;
        params.modeOverride = ShipPickMode.PRIORITY_THEN_ALL;
        params.doNotPrune = true;
        params.averageSMods = thisStage.sMods;

        if (thisStage == FamousBountyStage.STAGE_SPORESHIP) {
            params.combatPts *= 0.67;
        }
        fleet = FleetFactoryV3.createFleet(params);
        if (fleet == null || fleet.isEmpty()) {
            endImmediately();
            return;
        }

        String fleetName = person.getName().getFirst() + " " + person.getName().getLast() + " (IBB)";
        fleet.setName(fleetName);

        FleetMemberAPI flagship = Global.getFactory().createFleetMember(FleetMemberType.SHIP, thisStage.flagship);
        flagshipName = thisStage.flagshipName;
        person.setPersonality(thisStage.personality);

        fleet.getMemoryWithoutUpdate().set("$banterText", thisStage.banterText);
        fleet.getMemoryWithoutUpdate().set("$chatter_introSplash_name", person.getNameString());

        /* Bounty definitions */
        List<FleetMemberAPI> specialShips = new ArrayList<>(10);
        switch (thisStage) {
            case STAGE_UNDINE -> {
                FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "swp_boss_lasher_r_cus");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Salamander");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                member.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
                specialShips.add(member);
            }
            case STAGE_PONY -> {
                FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "swp_boss_tarsus_cus");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Carpal");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                member.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
                specialShips.add(member);
            }
            case STAGE_HADES -> {
                FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "swp_boss_cerberus_cus");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Dis");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "swp_boss_cerberus_cus");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Sheol");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "swp_boss_cerberus_cus");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Hell");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                specialShips.add(member);
            }
            case STAGE_FRACTURE -> {
                FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "swp_boss_hammerhead_cus");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Phillipshead");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                member.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
                specialShips.add(member);
            }
            case STAGE_RAPTOR -> {
                FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "swp_boss_medusa_cus");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Gorgon");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                member.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
                specialShips.add(member);
            }
            case STAGE_BIG_MAC -> {
                FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "loamtp_burke_assault");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Big Guns");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "loamtp_burke_assault");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Big Money");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "loamtp_burke_basic");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Big Women");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "loamtp_burke_basic");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Big Men");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "loamtp_burke_elite");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Big Boys");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "loamtp_burke_elite");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Big Girls");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "loamtp_fox_assault");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Big Power");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "loamtp_thatcher_assault");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Big Tits");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "loamtp_hawke_assault");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Big Balls");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "mudskipper2_CS");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Big Ass");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "mudskipper2_CS");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Big Dick");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "mudskipper2_CS");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Big Car");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "mudskipper2_CS");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Big Truck");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "mudskipper2_Hellbore");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Big Hands");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "mudskipper2_Hellbore");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Big Feet");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "mudskipper2_Hellbore");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Big Dig");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "mudskipper2_Hellbore");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Big Rig");
                specialShips.add(member);
            }
            case STAGE_LEVIATHAN -> {
                FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "swp_boss_phaeton_cus");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Clymene");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                member.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "swp_boss_phaeton_cus");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Tethys");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                specialShips.add(member);
            }
            case STAGE_VESTIGE -> {
                FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "tahlan_skirt_hunter");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Petticoat");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "tahlan_skirt_hunter");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Kilt");
                specialShips.add(member);
            }
            case STAGE_EMPEROR -> {
                FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "swp_boss_brawler_cus");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Boxer");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                member.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "swp_boss_brawler_cus");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Fighter");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "swp_boss_brawler_cus");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Fencer");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "swp_boss_brawler_cus");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Wrestler");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                specialShips.add(member);
            }
            case STAGE_ILIAD -> {
                FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "swp_boss_aurora_cus");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Dawnstar");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                member.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
                specialShips.add(member);
            }
            case STAGE_TITAN_X -> {
                FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "swp_boss_excelsior_cus");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Royco");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                member.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
                specialShips.add(member);
            }
            case STAGE_FRAMEBREAKER -> {
                FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "swp_boss_dominator_luddic_path_cus");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Ludd's Hammer");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                member.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "swp_boss_dominator_luddic_path_cus");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Ludd's Wrath");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                specialShips.add(member);
            }
            case STAGE_ODIN -> {
                FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "msp_boss_potniaBis_boss");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Maleficent");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                member.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "msp_boss_potniaBis_boss");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Malevolent");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "msp_boss_potniaBis_barrage");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Malicious");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "msp_boss_potniaBis_barrage");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Nefarious");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "msp_boss_potniaBis_nuke");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Vicious");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "msp_boss_potniaBis_nuke");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Wicked");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                specialShips.add(member);
            }
            case STAGE_BULLSEYE -> {
                FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "pack_bulldog_bullseye_Bullseye");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Charlie");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "pack_pitbull_bullseye_Bullseye");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Buddy");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                member.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "pack_pitbull_bullseye_Bullseye");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Rocky");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "pack_komondor_bullseye_Bullseye");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Jake");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                member.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "pack_komondor_bullseye_Bullseye");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Jack");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "pack_schnauzer_bullseye_Bullseye");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Toby");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                member.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "pack_schnauzer_bullseye_Bullseye");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Cody");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "pack_schnauzer_bullseye_Bullseye");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Buster");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "pack_schnauzer_bullseye_Bullseye");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Duke");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                specialShips.add(member);
            }
            case STAGE_LUCIFER -> {
                FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "swp_boss_euryale_cus");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Euryale");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                member.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "swp_boss_euryale_cus");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Moloch");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "swp_boss_afflictor_cus");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Tormentor");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                member.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "swp_boss_afflictor_cus");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Iblis");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "swp_boss_shade_cus");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Tenebris");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                member.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "swp_boss_shade_cus");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Azazel");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                specialShips.add(member);
            }
            case STAGE_SPORESHIP -> {
                params.factionId = Factions.DERELICT;
                params.maxShipSize = 3;
                CampaignFleetAPI temp = FleetFactoryV3.createFleet(params);
                for (FleetMemberAPI member : temp.getFleetData().getMembersListCopy()) {
                    fleet.getFleetData().addFleetMember(member);
                }
            }
            case STAGE_ZEUS -> {
                FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "swp_boss_conquest_cus");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Nike");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                member.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "swp_boss_onslaught_cus");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Ares");
                member.setVariant(member.getVariant().clone(), false, false);
                member.getVariant().setSource(VariantSource.REFIT);
                member.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
                member.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
                member.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);
                specialShips.add(member);
            }
            case STAGE_CANCER -> {
                FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "uw_boss_corruption_cur");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Murderhobo");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "uw_boss_malignancy_cur");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Obliterator");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "uw_boss_cyst_cur");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Stabby Stabby");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "uw_boss_cyst_cur");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("The Big Sleep");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "uw_boss_disease_cur");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Fatal Blow");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "uw_boss_disease_cur");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Mortal Wound");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "uw_boss_metastasis_cur");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Die Die Die");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "uw_boss_metastasis_cur");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("None Shall Live");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "uw_boss_ulcer_cur");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("You Can't Run");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "uw_boss_ulcer_cur");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("You Can't Hide");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "uw_boss_pustule_cur");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Knife's Cut");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "uw_boss_pustule_cur");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Razor's Slash");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "uw_boss_tumor_cur");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Why Won't You Die");
                specialShips.add(member);

                member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "uw_boss_tumor_cur");
                fleet.getFleetData().addFleetMember(member);
                member.setShipName("Just Die Already");
                specialShips.add(member);
            }
            default -> {
            }
        }

        flagship.setShipName(flagshipName);
        fleet.getFleetData().addFleetMember(flagship);
        fleet.getFleetData().setFlagship(flagship);
        fleet.setCommander(person);
        flagship.setCaptain(person);
        fleet.setTransponderOn(true);
        fleet.removeAbility(Abilities.GO_DARK);
        fleet.removeAbility(Abilities.TRANSPONDER);

        flagship.setVariant(flagship.getVariant().clone(), false, false);
        flagship.getVariant().setSource(VariantSource.REFIT);
        flagship.getVariant().addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
        flagship.getVariant().addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS);
        flagship.getVariant().addTag(Tags.VARIANT_ALWAYS_RECOVERABLE);

        fleet.forceSync();

        addOfficersFamous(fleet, officerCount, officerLevel, officerElites, specialShips, new Random());

        fleet.forceSync();

        /* We do this deferred level-up so that the commander's skills reflect the flagship type */
        SkillPickPreference pref = FleetFactoryV3.getSkillPrefForShip(fleet.getFlagship());
        levelOfficer(person, fleet.getFleetData(), thisStage.level, Math.max(0, commanderElites - 1), pref, new Random());

        /* Add in the commander skills, at last */
        addCommanderSkillsFamous(person, fleet, commanderSkills, new Random());

        person.setRankId(Ranks.SPACE_ADMIRAL);
        person.setPostId(Ranks.POST_FLEET_COMMANDER);
        person.getStats().getShipOrdnancePointBonus().modifyPercent("ibb_bonus", thisStage.opBonus);

        fleet.getFleetData().sort();
        fleet.forceSync();
        fleet.updateCounts();

        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            member.getRepairTracker().setCR(member.getRepairTracker().getMaxCR());
        }

        Misc.makeImportant(fleet, "swp_ibb");
        fleet.getMemoryWithoutUpdate().set("$level", commanderLevel);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_FLEET_TYPE, "famousBounty");
        fleet.getMemoryWithoutUpdate().set("$famousBountyFaction", fleetFactionId);
        fleet.getMemoryWithoutUpdate().set("$stillAlive", true);
        fleet.getMemoryWithoutUpdate().set("$stageName", thisStage.name());
        fleet.getMemoryWithoutUpdate().set("$nex_noKeepSMods", true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN, new IBBInteractionConfigGen());
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_NO_MILITARY_RESPONSE, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_DO_NOT_IGNORE_PLAYER, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.CAN_ONLY_BE_ENGAGED_WHEN_VISIBLE_TO_PLAYER, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_JUMP, true);

        fleet.setFaction(faction.getId(), true);
        fleet.setNoFactionInName(true);

        fleet.addEventListener(this);
        Misc.addDefeatTrigger(fleet, "swpIBBAwardStoryPoint");

        fleet.forceSync();

        LocationAPI location = hideoutLocation.getContainingLocation();
        location.addEntity(fleet);
        fleet.setLocation(hideoutLocation.getLocation().x - 500, hideoutLocation.getLocation().y + 500);
        fleet.getAI().addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, hideoutLocation, 1000000f, null);
    }

    public static void addOfficersFamous(CampaignFleetAPI fleet, int numOfficers, int officerLevel, int officerElites,
            Collection<FleetMemberAPI> specialShips, Random random) {
        List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();

        WeightedRandomPicker<FleetMemberAPI> picker = new WeightedRandomPicker<>(random);

        for (FleetMemberAPI member : members) {
            if (member.isFighterWing()) {
                continue;
            }
            if (member.isFlagship()) {
                continue;
            }
            if (!member.getCaptain().isDefault()) {
                continue;
            }

            float q = 1f;
            if (member.isCivilian()) {
                q *= 0.0001f;
            }
            for (FleetMemberAPI special : specialShips) {
                if (special.getId().contentEquals(member.getId())) {
                    q += 1000f;
                    break;
                }
            }
            float weight = member.getFleetPointCost() * q;
            picker.add(member, weight);
        }

        for (int i = 0; i < numOfficers; i++) {
            FleetMemberAPI member = picker.pickAndRemove();
            if (member == null) {
                break; // out of ships that need officers
            }

            SkillPickPreference pref = FleetFactoryV3.getSkillPrefForShip(member);
            PersonAPI person = OfficerManagerEvent.createOfficer(fleet.getFaction(), officerLevel, pref, true, fleet, true, true, officerElites, random);
            if (person.getPersonalityAPI().getId().equals(Personalities.TIMID)) {
                person.setPersonality(Personalities.CAUTIOUS);
            }

            member.setCaptain(person);
        }
    }

    public static void levelOfficer(PersonAPI person, FleetDataAPI fleetData, int toLevel, int numElites, SkillPickPreference pref, Random random) {
        if (random == null) {
            random = new Random();
        }

        OfficerLevelupPlugin plugin = (OfficerLevelupPlugin) Global.getSettings().getPlugin("officerLevelUp");
        person.getStats().setSkipRefresh(true);

        OfficerDataAPI officerData = fleetData.getOfficerData(person);
        if (officerData == null) {
            officerData = Global.getFactory().createOfficerData(person);
        }

        int numSpec = 0;
        for (SkillLevelAPI skill : person.getStats().getSkillsCopy()) {
            if (skill.getSkill().hasTag(Skills.TAG_SPEC)) {
                numSpec++;
            }
        }

        long xp = plugin.getXPForLevel(toLevel);
        officerData.addXP(xp, null, false);
        officerData.makeSkillPicks(random);
        while (officerData.canLevelUp(true)) {
            String skillId = OfficerManagerEvent.pickSkill(officerData.getPerson(), officerData.getSkillPicks(), pref, numSpec, random);
            if (skillId != null) {
                officerData.levelUp(skillId, random);
                SkillSpecAPI spec = Global.getSettings().getSkillSpec(skillId);
                if (spec.hasTag(Skills.TAG_SPEC)) {
                    numSpec++;
                }

                if (officerData.getSkillPicks().isEmpty()) {
                    officerData.makeSkillPicks(random);
                }
            } else {
                break;
            }
        }
        OfficerManagerEvent.addEliteSkills(person, numElites, random);

        person.getStats().setSkipRefresh(false);
        person.getStats().refreshCharacterStatsEffects();
    }

    public static void addCommanderSkillsFamous(PersonAPI commander, CampaignFleetAPI fleet, int commanderSkills, Random random) {
        if (random == null) {
            random = new Random();
        }

        FactionDoctrineAPI doctrine = Global.getSector().getFaction("famous_bounty").getDoctrine();

        List<String> skills = new ArrayList<>(doctrine.getCommanderSkills());
        if (skills.isEmpty()) {
            return;
        }

        MutableCharacterStatsAPI stats = commander.getStats();
        if (random.nextFloat() < doctrine.getCommanderSkillsShuffleProbability()) {
            Collections.shuffle(skills, random);
        }

        stats.setSkipRefresh(true);

        int picks = 0;
        for (String skillId : skills) {
            stats.setSkillLevel(skillId, 1);
            picks++;
            if (picks >= commanderSkills) {
                break;
            }
        }

        stats.setSkipRefresh(false);
        stats.refreshCharacterStatsEffects();
    }

    public PersonAPI initPerson(FactionAPI bountyFaction) {
        PersonAPI bountyPerson = OfficerManagerEvent.createOfficer(bountyFaction, 1, SkillPickPreference.ANY,
                true, null, true, true, Math.min(1, thisStage.cdrElites), null);
        bountyPerson.getName().setFirst(thisStage.firstName);
        bountyPerson.getName().setLast(thisStage.lastName);
        bountyPerson.getName().setGender(thisStage.gender);
        try {
            bountyPerson.setPortraitSprite(thisStage.portrait);
        } catch (Exception e) {
        }
        return bountyPerson;
    }

    public String fleetSizeString() {
        int pts = thisStage.maxPts + thisStage.ptsFromBoss + thisStage.ptsFromAdds;
        if (pts < 50) {
            return "modest";
        } else if (pts < 100) {
            return "fairly small";
        } else if (pts < 200) {
            return "decently-sized";
        } else if (pts < 300) {
            return "rather large";
        } else if (pts < 450) {
            return "particularly big";
        } else if (pts < 600) {
            return "enormous";
        } else if (pts < 800) {
            return "vast";
        } else if (pts < 1000) {
            return "titanic";
        } else {
            return "apocalyptic";
        }
    }

    private String getHeOrShe() {
        if (it) {
            return "it";
        } else if (they) {
            return "they";
        } else if (person.getGender() == Gender.MALE) {
            return "he";
        } else {
            return "she";
        }
    }

    private String getHimOrHerself() {
        if (it) {
            return "itself";
        } else if (they) {
            return "themself";
        } else if (person.getGender() == Gender.MALE) {
            return "himself";
        } else {
            return "herself";
        }
    }

    private String getHisOrHer() {
        if (it) {
            return "its";
        } else if (they) {
            return "their";
        } else if (person.getGender() == Gender.MALE) {
            return "his";
        } else {
            return "her";
        }
    }

    private String getHimOrHer() {
        if (it) {
            return "it";
        } else if (they) {
            return "them";
        } else if (person.getGender() == Gender.MALE) {
            return "him";
        } else {
            return "her";
        }
    }

    public static enum IBBResultType {
        END_PLAYER_BOUNTY,
        END_OTHER,
        END_TAKEN,
    }

    public static class IBBResult {

        public IBBResultType type;
        public int payment;

        public IBBResult(IBBResultType type, int payment) {
            this.type = type;
            this.payment = payment;
        }
    }

    public static enum FamousBountyStage {

        STAGE_UNDINE("Francis", "Butler", Gender.MALE, false, false, "graphics/swp/portraits/ibb_butler.png",
                RequiredFaction.NONE, "swp_boss_lasher_b_cus", "Undine", "reckless",
                0.5f, 7, 0, 3, 5, 2, 2, 35, 0f, -1, 10, 0, 43000, Factions.PIRATES,
                "So, you've come to claim our heads. You won't find us to be easy prey!"), // 0
        STAGE_ELDER_ORB("Ocula", "Gazer", Gender.FEMALE, false, false, "graphics/swp/portraits/ibb_gazer.png",
                RequiredFaction.NONE, "swp_boss_beholder_cus", "Elder Orb", "steady",
                1.25f, 7, 0, 4, 5, 2, 4, 45, 0f, 0, 13, 0, 64500, Factions.TRITACHYON,
                "Wow, would you look at the time? It's death o'clock!"), // 1
        STAGE_PONY("Lisa", "Nanao", Gender.FEMALE, false, false, "graphics/swp/portraits/ibb_nanao.png",
                RequiredFaction.NONE, "swp_boss_mule_cus", "Pony", "cautious",
                0.75f, 8, 1, 4, 5, 3, 5, 95, 0f, 0, 17, 0, 105000, "domain",
                "We're done playing games. Prepare to die!"), // 2
        STAGE_HADES("Ryx", "Barlow", Gender.FEMALE, false, false, "graphics/swp/portraits/ibb_ryx.png",
                RequiredFaction.NONE, "swp_boss_cerberus_cus", "Hades", "steady",
                1f, 8, 1, 4, 6, 3, 6, 115, 0f, 1, 20, 0, 135000, Factions.INDEPENDENT,
                "These ships are mine. You'll never take them from me!"), // 3
        STAGE_FRACTURE("Jaine", "Meredith", Gender.FEMALE, false, false, "graphics/swp/portraits/ibb_meredith.png",
                RequiredFaction.NONE, "swp_boss_sunder_cus", "Fracture", "steady",
                1.25f, 9, 2, 4, 6, 3, 7, 140, 0f, 1, 23, 0, 170000, Factions.DIKTAT,
                "I hereby declare war against you, commander. En garde!"), // 4
        STAGE_KINGFISHER("Mackie", "Corbin", Gender.MALE, false, false, "graphics/swp/portraits/ibb_corbin.png",
                RequiredFaction.NONE, "swp_boss_eagle_cus", "Kingfisher", "aggressive",
                1.5f, 9, 2, 9, 6, 6, 6, 100, 0f, 2, 24, 0, 170000, Factions.LIONS_GUARD,
                "You're with THEM! The enemy! Call to arms, men! Make ready for war!"), // 5
        STAGE_LIBERTY("Abrahamo", "Lincolni", Gender.MALE, false, false, "graphics/tiandong/portraits/tiandong_boss_abrahamo_lincolni.png",
                RequiredFaction.TIANDONG, "tiandong_boss_wuzhang_Standard", "Liberty", "reckless",
                1.25f, 9, 2, 9, 6, 6, 6, 125, 0f, 1, 30, 0, 190000, "tiandong",
                "You look like you could use an extra large serving of freedom!"), // 6
        STAGE_POSEIDON("Marjatta", "Nemo", Gender.FEMALE, false, false, "graphics/portraits/ms_portrait_001.png",
                RequiredFaction.SHADOWYARDS, "ms_boss_charybdis_boss", "Poseidon", "steady",
                1.25f, 9, 2, 5, 6, 3, 8, 195, 0f, 1, 30, 0, 240000, "shadow_industry",
                "Do you have something I want? Hmm... Nah. I'll just blow you up."), // 7
        STAGE_RAPTOR("Blaize", "Rex", Gender.FEMALE, false, false, "graphics/swp/portraits/ibb_rex.png",
                RequiredFaction.NONE, "swp_boss_falcon_cus", "Raptor", "steady",
                1.25f, 10, 2, 5, 7, 3, 10, 225, 0f, 2, 31, 0, 280000, Factions.PERSEAN,
                "Those are some nice ships you've got there. It would be a shame if something were to happen to them..."), // 8
        STAGE_IHS("Inugami", "Amatsuri", Gender.FEMALE, false, false, "graphics/swp/portraits/ibb_ice.png",
                RequiredFaction.ICE, "sun_ice_ihs_exp", "I.H.S.", "aggressive",
                1.25f, 10, 2, 10, 7, 7, 8, 140, 0f, 3, 35, 0, 245000, "sun_ice",
                "The comets are shining. One of us will reach the end of the journey soon, but I know everything will be restarted after a while... or a long time."), // 9
        STAGE_GULF("Teresa", "Terror", Gender.FEMALE, false, false, "graphics/swp/portraits/ibb_terror.png",
                RequiredFaction.DIABLE, "diableavionics_IBBgulf_fang", "Gulf", "steady",
                1.5f, 10, 2, 5, 7, 3, 11, 245, 0f, 2, 35, 0, 325000, "diableavionics",
                "You call THAT a fleet? Hah! I will crush you like a bug under my heel."), // 10
        STAGE_BIG_MAC("Rylek", "", Gender.MALE, false, true, "graphics/swp/portraits/loa_portrait_ibb_rylek.png",
                RequiredFaction.ARKGNEISIS, "loamtp_macnamara_boss", "Warcrime", "reckless",
                0.75f, 10, 2, 10, 7, 7, 9, 170, 0f, 3, 41, 112, 295000, Factions.PIRATES,
                "I should thank you smoothskins for saving me the trouble of hunting you down, but I think I'll just dance on your corpse instead!"), // 11
        STAGE_FRANKENSTEIN("Xenno", "Aura", Gender.MALE, false, false, "graphics/swp/portraits/ibb_aura.png",
                RequiredFaction.NONE, "swp_boss_frankenstein_cus", "Frankenstein", "cautious",
                1.25f, 10, 3, 10, 7, 7, 10, 170, 0f, 3, 37, 0, 305000, "sector",
                "Oops! XD"), // 12
        STAGE_LEVIATHAN("Elaine", "Megas", Gender.FEMALE, false, false, "graphics/swp/portraits/ibb_megas.png",
                RequiredFaction.NONE, "swp_boss_atlas_cus", "Leviathan", "cautious",
                0.25f, 11, 3, 5, 7, 4, 20, 620, 0f, -1, 45, 0, 590000, Factions.PIRATES,
                "I was just trying to trade! This is bullshit!"), // 13
        STAGE_VESTIGE("Cieril", "Traveyn", Gender.FEMALE, false, false, "graphics/swp/portraits/ibb_cieril.png",
                RequiredFaction.SCALARTECH, "tahlan_vestige_boss", "Vestige", "aggressive",
                1.5f, 11, 3, 5, 7, 4, 13, 360, 0f, 2, 51, 0, 480000, "scalartech_elite",
                "So the admiralty has sold me out after all I've done for Spindle? They do not deserve to call themselves guardians."), // 14
        STAGE_EMPEROR("Johnny", "Major", Gender.MALE, false, false, "graphics/swp/portraits/ibb_major.png",
                RequiredFaction.NONE, "swp_boss_dominator_cus", "Emperor", "aggressive",
                1.25f, 11, 3, 6, 8, 4, 15, 455, 0f, 2, 54, 0, 575000, Factions.HEGEMONY,
                "You're here for me, too? Well, I won't go down without a fight! Come get some!"), // 15
        STAGE_YAMATO("Katsu", "Okita", Gender.MALE, false, false, "graphics/imperium/portraits/ii_okita.png",
                RequiredFaction.IMPERIUM, "ii_boss_dominus_cus", "Yamato", "aggressive",
                1.5f, 11, 3, 11, 8, 8, 12, 295, 0f, 3, 54, 0, 480000, "interstellarimperium",
                "You won't stop me from saving my people!"), // 16
        STAGE_EUPHORIA("Trilby", "Enlightenment", Gender.MALE, false, false, "graphics/uw/portraits/uw_in_this_moment_i_am_euphoric.png",
                RequiredFaction.CABAL, "uw_boss_astral_cus", "False God", "steady",
                1.75f, 11, 3, 6, 8, 4, 15, 425, 0f, 2, 60, 0, 605000, "cabal",
                "I am at a higher state of being than you, enlightened by my own intelligence. I reject the blessings of your phony gods. In this moment, I am truly..."), // 17
        STAGE_CLERIC("Mathis", "Adelier", Gender.MALE, false, false, "graphics/templars/portraits/tem_portrait1.png",
                RequiredFaction.TEMPLARS, "tem_boss_paladin_cus", "Cleric", "reckless",
                1.75f, 11, 3, 6, 8, 4, 14, 440, 0f, 2, 60, 0, 645000, "templars",
                "The time of forbearance has finished. The hour of victory, of justice, has come. And now, you shall join the fire."), // 18
        STAGE_RAST("Zone", "Rast", Gender.MALE, false, false, "graphics/portraits/FOB_male01.png",
                RequiredFaction.BORKEN, "FOB_boss_rast_cus", "Desireless", "reckless",
                1.5f, 11, 3, 6, 8, 4, 16, 555, 0f, 2, 70, 0, 755000, "fob",
                "Why can't you understand my love?"), // 19
        STAGE_POPE("Adam", "Tipper", Gender.MALE, true, false, "graphics/swp/portraits/ibb_tipper.png",
                RequiredFaction.TEMPLARS, "tem_boss_archbishop_cus", "Pope", "aggressive",
                1.75f, 11, 3, 6, 8, 4, 20, 525, 0f, 2, 95, 0, 785000, Factions.MERCENARY,
                "I. AM. ETERNAL."), // 20
        STAGE_ILIAD("Clade", "Midnight", Gender.MALE, false, false, "graphics/swp/portraits/ibb_midnight.png",
                RequiredFaction.NONE, "swp_boss_odyssey_cus", "Iliad", "steady",
                1.5f, 12, 3, 12, 8, 8, 14, 420, 0f, 4, 90, 0, 710000, Factions.MERCENARY,
                "Sorry, commander. It's nothing personal, but I really want a good fight. Prepare yourself!"), // 21
        STAGE_TITAN_X("Helmut", "Pulmenti", Gender.MALE, false, false, "graphics/imperium/portraits/ii_helmutreal.png",
                RequiredFaction.IMPERIUM, "ii_boss_titanx_cus", "Hot Potato", "reckless",
                1.75f, 12, 3, 12, 8, 8, 15, 395, 0f, 4, 99, 0, 715000, "ii_imperial_guard",
                "Hello! You've seen too much, my friend! So you need to die!"), // 22
        STAGE_FRAMEBREAKER("Ned", "Ludd", Gender.MALE, false, false, "graphics/portraits/portrait_luddic00.png",
                RequiredFaction.NONE, "swp_boss_onslaught_luddic_path_cus", "Framebreaker", "reckless",
                1.5f, 12, 4, 6, 8, 4, 22, 720, 0f, 3, 108, 0, 1050000, Factions.LUDDIC_PATH,
                "Lo, the Lord hath fortold this Apocalypse, a great event done unto us an aeon ago. And how the fires hath henceforth swept the "
                + "black-as-sin void! Providence has brought another faithless heathen to me, here, today. The age of repentance is over; now, you "
                + "shall burn!"), // 23
        STAGE_ODIN("RA-002", "SHADOW", Gender.MALE, true, false, "graphics/portraits/ms_portrait_006.png",
                RequiredFaction.SHADOWYARDS, "ms_boss_mimir_crash", "Odin", "aggressive",
                1.25f, 12, 4, 6, 8, 8, 20, 700, 0f, 3, 122, 0, 1100000, "sector",
                "PURPOSE."), // 24
        STAGE_BULLSEYE("Max", "Bailey", Gender.MALE, false, false, "graphics/portraits/portrait_hegemony06.png",
                RequiredFaction.JUNK_PIRATES, "pack_bulldog_bullseye_Bullseye", "Bullseye", "steady",
                1.5f, 12, 4, 6, 8, 8, 23, 715, 0f, 3, 126, 0, 1150000, "pack",
                "Woof woof, motherfucker!"), // 25
        STAGE_LUCIFER("TTX", "Laplace", Gender.FEMALE, true, false, "graphics/swp/portraits/ibb_laplace.png",
                RequiredFaction.NONE, "swp_boss_doom_cus", "Lucifer", "reckless",
                2f, 13, 4, 13, 9, 9, 15, 360, 0f, 4, 134, 0, 815000, Factions.REMNANTS,
                "I am sorry."), // 26
        STAGE_SPORESHIP("El", "Psi", Gender.MALE, true, false, "graphics/swp/portraits/ibb_psi.png",
                RequiredFaction.NONE, "swp_boss_sporeship_cus", "DES S-02", "aggressive",
                1.25f, 13, 4, 7, 9, 4, 21, 660, 0f, 3, 150, 0, 1150000, Factions.REMNANTS,
                "You are not omega."), // 27
        STAGE_ZEUS("Gabriel", "Mosolov", Gender.MALE, false, false, "graphics/portraits/portrait_hegemony05.png",
                RequiredFaction.NONE, "swp_boss_paragon_cus", "Zeus", "aggressive",
                2f, 14, 5, 14, 9, 9, 25, 855, 0f, 4, 168, 0, 1550000, "everything",
                "It was nice of you to kill off all of my competitors for me. You have my sincerest thanks. Now, you die."), // 28
        STAGE_CANCER("I'll Kill", "You All", Gender.MALE, false, false, "graphics/portraits/characters/volta.png",
                RequiredFaction.CURSED, "uw_boss_cancer_cur", "Allslayer", "reckless",
                2f, 14, 5, 14, 9, 9, 15, 25, 0f, 5, 512, 0, 1150000, Factions.OMEGA,
                ""); // 29

        public final String firstName;
        public final String lastName;
        public final Gender gender;
        public final boolean it;
        public final boolean they;
        public final String portrait;

        public final RequiredFaction mod;
        public final int reward;

        public final String flagship;
        public final String flagshipName;
        public final String personality;

        public final float qf;
        public final int level;
        public final int cdrSkills;
        public final int cdrElites;
        public final int officerLevel;
        public final int officerElites;
        public final int officerCount;
        public final int maxPts;
        public final float opBonus;
        public final int sMods;
        public final int ptsFromBoss;
        public final int ptsFromAdds;

        public final String fleetFaction;

        public final String banterText;

        private FamousBountyStage(String firstName, String lastName, Gender gender, boolean it, boolean they, String portrait,
                RequiredFaction mod, String flagship, String flagshipName, String personality,
                float qf, int level, int cdrSkills, int cdrElites, int officerLevel, int officerElites, int officerCount, int maxPts, float opBonus, int sMods, int ptsFromBoss, int ptsFromAdds, int reward, String fleetFaction,
                String banterText) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.gender = gender;
            this.it = it;
            this.they = they;
            this.portrait = portrait;

            this.mod = mod;
            this.flagship = flagship;
            this.flagshipName = flagshipName;
            this.personality = personality;

            this.qf = qf;
            this.level = level;
            this.cdrSkills = cdrSkills;
            this.cdrElites = cdrElites;
            this.officerLevel = officerLevel;
            this.officerElites = officerElites;
            this.officerCount = officerCount;
            this.maxPts = maxPts;
            this.opBonus = opBonus;
            this.sMods = sMods;
            this.ptsFromBoss = ptsFromBoss;
            this.ptsFromAdds = ptsFromAdds;
            this.reward = reward;
            this.fleetFaction = fleetFaction;

            this.banterText = banterText;
        }
    }

    public static class IBBInteractionConfigGen implements FIDConfigGen {

        @Override
        public FIDConfig createConfig() {
            FIDConfig config = new FIDConfig();
            config.showWarningDialogWhenNotHostile = false;
            config.impactsEnemyReputation = false;
            config.showFleetAttitude = false;
            config.pullInAllies = false;
            config.pullInEnemies = false;

            config.delegate = new FIDDelegate() {
                @Override
                public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
                    bcc.enemyDeployAll = true;
                }

                @Override
                public void notifyLeave(InteractionDialogAPI dialog) {
                }

                @Override
                public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context,
                        CargoAPI salvage) {
                    if (!(dialog.getInteractionTarget() instanceof CampaignFleetAPI)) {
                        return;
                    }

                    CampaignFleetAPI fleet = (CampaignFleetAPI) dialog.getInteractionTarget();
                    String stageName = (String) fleet.getMemoryWithoutUpdate().get("$stageName");

                    if (stageName.contentEquals(FamousBountyStage.STAGE_SPORESHIP.name())) {
                        DataForEncounterSide data = context.getDataFor(fleet);
                        List<FleetMemberAPI> losses = new ArrayList<>();
                        for (FleetMemberData fmd : data.getOwnCasualties()) {
                            losses.add(fmd.getMember());
                        }

                        List<DropData> dropRandom = new ArrayList<>();

                        int[] counts = new int[5];
                        String[] groups = new String[]{Drops.REM_FRIGATE, Drops.REM_DESTROYER,
                            Drops.REM_CRUISER, Drops.REM_CAPITAL, Drops.GUARANTEED_ALPHA};

                        for (FleetMemberAPI member : losses) {
                            if (SWP_Util.getNonDHullId(member.getHullSpec()).contentEquals("swp_boss_sporeship")) {
                                counts[4] += 2;
                                counts[3] += 1;
                            } else if (member.getHullSpec().hasTag("remnant")) {
                                if (member.isCapital()) {
                                    counts[3] += 1;
                                } else if (member.isCruiser()) {
                                    counts[2] += 1;
                                } else if (member.isDestroyer()) {
                                    counts[1] += 1;
                                } else if (member.isFrigate()) {
                                    counts[0] += 1;
                                }
                            }
                        }

                        for (int i = 0; i < counts.length; i++) {
                            int count = counts[i];
                            if (count <= 0) {
                                continue;
                            }

                            DropData d = new DropData();
                            d.group = groups[i];
                            d.chances = (int) Math.ceil(count * 1f);
                            dropRandom.add(d);
                        }

                        Random salvageRandom = new Random(Misc.getSalvageSeed(fleet));
                        CargoAPI extra = SalvageEntity.generateSalvage(salvageRandom, 1f, 1f, 1f, 1f, null, dropRandom);
                        for (CargoStackAPI stack : extra.getStacksCopy()) {
                            salvage.addFromStack(stack);
                        }
                    }

                    if (stageName.contentEquals(FamousBountyStage.STAGE_CANCER.name())) {
                        DataForEncounterSide data = context.getDataFor(fleet);
                        List<FleetMemberAPI> losses = new ArrayList<>();
                        for (FleetMemberData fmd : data.getOwnCasualties()) {
                            losses.add(fmd.getMember());
                        }

                        List<DropData> dropRandom = new ArrayList<>();

                        int[] counts = new int[5];
                        String[] groups = new String[]{Drops.REM_FRIGATE, Drops.REM_DESTROYER,
                            Drops.REM_CRUISER, Drops.REM_CAPITAL, Drops.GUARANTEED_ALPHA};

                        for (FleetMemberAPI member : losses) {
                            if (SWP_Util.SPECIAL_SHIPS.contains(SWP_Util.getNonDHullId(member.getHullSpec()))) {
                                if (member.isCapital()) {
                                    if (SWP_Util.getNonDHullId(member.getHullSpec()).contentEquals("uw_boss_cancer")) {
                                        counts[4] += 1;
                                    }
                                    counts[3] += 1;
                                    counts[2] += 1;
                                    counts[1] += 1;
                                    counts[0] += 1;
                                } else if (member.isCruiser()) {
                                    counts[2] += 1;
                                    counts[1] += 1;
                                    counts[0] += 1;
                                } else if (member.isDestroyer()) {
                                    counts[1] += 1;
                                    counts[0] += 1;
                                } else if (member.isFrigate()) {
                                    counts[0] += 1;
                                }
                            } else {
                                if (member.isCapital()) {
                                    counts[3] += 1;
                                } else if (member.isCruiser()) {
                                    counts[2] += 1;
                                } else if (member.isDestroyer()) {
                                    counts[1] += 1;
                                } else if (member.isFrigate()) {
                                    counts[0] += 1;
                                }
                            }
                        }

                        for (int i = 0; i < counts.length; i++) {
                            int count = counts[i];
                            if (count <= 0) {
                                continue;
                            }

                            DropData d = new DropData();
                            d.group = groups[i];
                            d.chances = (int) Math.ceil(count * 1f);
                            dropRandom.add(d);
                        }

                        Random salvageRandom = new Random(Misc.getSalvageSeed(fleet));
                        CargoAPI extra = SalvageEntity.generateSalvage(salvageRandom, 1f, 1f, 1f, 1f, null, dropRandom);
                        for (CargoStackAPI stack : extra.getStacksCopy()) {
                            salvage.addFromStack(stack);
                        }
                    }
                }
            };

            return config;
        }
    }
}
