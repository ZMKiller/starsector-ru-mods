package data.econ.HSIGF;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.HSIGenerator.HSIDreadnought;

public class HSIGuardFleetFactory {

    public enum Type{
        CARRIER,COMBAT,HEAVY
    }

    public static WeightedRandomPicker<Type> typePicker = new WeightedRandomPicker<>();
    static {
        typePicker.add(Type.COMBAT);
        typePicker.add(Type.HEAVY);
        typePicker.add(Type.CARRIER);
    }


    public static CampaignFleetAPI createGuardFleet(float combat,float tanker,float freighter){
        Type type = typePicker.pick();
        FactionAPI faction = Global.getSector().getFaction("GF3");
        CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet("HSI",faction.getFleetTypeName(FleetTypes.PATROL_LARGE),true);
        PersonAPI admiral = createAdmiral();
        fleet.setCommander(admiral);
        if(type == Type.COMBAT){
            fleet.getMemoryWithoutUpdate().set("$HSI_FullAssault",true);
        }
        switch (type) {
            case CARRIER:
            {
                FleetMemberAPI f = fleet.getFleetData().addFleetMember("HSI_UkiyoB_Elite");
                ShipVariantAPI v = f.getVariant().clone();
                v.setSource(VariantSource.REFIT);
                v.addPermaMod(HullMods.AUTOREPAIR,true);
                v.addPermaMod(HullMods.TURRETGYROS,true);
                f.setVariant(v,false,false);
                f.updateStats();
                f.setCaptain(admiral);
                f.setFlagship(true);
                float combatDiv1 = combat * 0.3f;
                while (combatDiv1 > 0) {
                    FleetMemberAPI fy = fleet.getFleetData().addFleetMember("HSI_FengYue_GF3_Support");
                    fy.setCaptain(createHigherCaptain());
                    ShipVariantAPI vfy = fy.getVariant().clone();
                    vfy.setSource(VariantSource.REFIT);
                    vfy.addPermaMod(HullMods.AUTOREPAIR,true);
                    fy.setVariant(vfy,false,false);
                    fy.updateStats();
                    combatDiv1 -= fy.getFleetPointCost();
                }
                float combatDiv2 = combat * 0.5f;
                while (combatDiv2 > 0) {
                    FleetMemberAPI sf = fleet.getFleetData().addFleetMember("HSI_Xianfeng_GF3_Strike");
                    sf.setCaptain(createLowerCaptain());
                    ShipVariantAPI vsf = sf.getVariant().clone();
                    vsf.setSource(VariantSource.REFIT);
                    vsf.addPermaMod(HullMods.AUTOREPAIR,true);
                    sf.setVariant(vsf,false,false);
                    sf.updateStats();
                    combatDiv2 -= sf.getFleetPointCost();
                }
                float combatDiv3 = combat * 0.3f;
                while (combatDiv3 > 0) {
                    FleetMemberAPI sf = fleet.getFleetData().addFleetMember("HSI_Qiming_GF3_Strike");
                    sf.setCaptain(HSIDreadnought.createJuniorCaptain());
                    sf.updateStats();
                    combatDiv3 -= sf.getFleetPointCost();
                }
            }
                break;
            case COMBAT:
            {
                FleetMemberAPI f = fleet.getFleetData().addFleetMember("HSI_UkiyoA_Elite");
                ShipVariantAPI v = f.getVariant().clone();
                v.setSource(VariantSource.REFIT);
                v.addPermaMod(HullMods.AUTOREPAIR,true);
                v.addPermaMod(HullMods.TURRETGYROS,true);
                f.setVariant(v,false,false);
                f.setCaptain(admiral);
                f.setFlagship(true);
                f.updateStats();
                float combatDiv1 = combat * 0.2f;
                while (combatDiv1 > 0) {
                    FleetMemberAPI fy = fleet.getFleetData().addFleetMember( "HSI_ShanYu_Assault");
                    fy.setCaptain(createHigherCaptain());
                    ShipVariantAPI vfy = fy.getVariant().clone();
                    vfy.setSource(VariantSource.REFIT);
                    vfy.addPermaMod(HullMods.AUTOREPAIR,true);
                    fy.setVariant(vfy,false,false);
                    fy.updateStats();
                    combatDiv1 -= fy.getFleetPointCost();
                }
                float combatDiv6= combat * 0.2f;
                while (combatDiv6 > 0) {
                    FleetMemberAPI sf = fleet.getFleetData().addFleetMember("HSI_Eagle_GF3_Support");
                    sf.setCaptain(createHigherCaptain());
                    ShipVariantAPI vsf = sf.getVariant().clone();
                    vsf.setSource(VariantSource.REFIT);
                    vsf.addPermaMod(HullMods.AUTOREPAIR,true);
                    sf.setVariant(vsf,false,false);
                    sf.updateStats();
                    combatDiv6 -= sf.getFleetPointCost();
                }
                float combatDiv2 = combat * 0.2f;
                while (combatDiv2 > 0) {
                    FleetMemberAPI sf = fleet.getFleetData().addFleetMember("HSI_YunYan_GF3_Assault");
                    sf.setCaptain(HSIDreadnought.createJuniorCaptain());
                    ShipVariantAPI vsf = sf.getVariant().clone();
                    vsf.setSource(VariantSource.REFIT);
                    vsf.addPermaMod(HullMods.AUTOREPAIR,true);
                    sf.setVariant(vsf,false,false);
                    sf.updateStats();
                    combatDiv2 -= sf.getFleetPointCost();
                }
                float combatDiv4 = combat * 0.1f;
                while (combatDiv4 > 0) {
                    FleetMemberAPI sf = fleet.getFleetData().addFleetMember("HSI_Scarab_Assault");
                    sf.setCaptain(HSIDreadnought.createJuniorCaptain());
                    ShipVariantAPI vsf = sf.getVariant().clone();
                    vsf.setSource(VariantSource.REFIT);
                    vsf.addPermaMod(HullMods.AUTOREPAIR,true);
                    sf.setVariant(vsf,false,false);
                    sf.updateStats();
                    combatDiv4 -= sf.getFleetPointCost();
                }
                float combatDiv3 = combat * 0.1f;
                while (combatDiv3 > 0) {
                    FleetMemberAPI sf = fleet.getFleetData().addFleetMember("HSI_ZhiYuan_Assault");
                    sf.setCaptain(HSIDreadnought.createJuniorCaptain());
                    ShipVariantAPI vsf = sf.getVariant().clone();
                    vsf.setSource(VariantSource.REFIT);
                    vsf.addPermaMod(HullMods.AUTOREPAIR,true);
                    sf.setVariant(vsf,false,false);
                    sf.updateStats();
                    combatDiv3 -= sf.getFleetPointCost();
                }
                float combatDiv5 = combat * 0.2f;
                while (combatDiv5 > 0) {
                    FleetMemberAPI sf = fleet.getFleetData().addFleetMember("HSI_TianGuang_Dash");
                    sf.setCaptain(createLowerCaptain());
                    ShipVariantAPI vsf = sf.getVariant().clone();
                    vsf.setSource(VariantSource.REFIT);
                    vsf.addPermaMod(HullMods.AUTOREPAIR,true);
                    sf.setVariant(vsf,false,false);
                    sf.updateStats();
                    combatDiv5 -= sf.getFleetPointCost();
                }
            }
                break;
            case HEAVY:
            {
                FleetMemberAPI f = fleet.getFleetData().addFleetMember("HSI_UkiyoC_Elite");
                ShipVariantAPI v = f.getVariant().clone();
                v.setSource(VariantSource.REFIT);
                v.addPermaMod(HullMods.AUTOREPAIR,true);
                v.addPermaMod(HullMods.TURRETGYROS,true);
                f.setVariant(v,false,false);
                f.setCaptain(admiral);
                f.setFlagship(true);
                f.updateStats();
                float combatDiv1 = combat * 0.3f;
                while (combatDiv1 > 0) {
                    FleetMemberAPI fy = fleet.getFleetData().addFleetMember( "HSI_Oath_GF3_Defense");
                    fy.setCaptain(createHigherCaptain());
                    ShipVariantAPI vfy = fy.getVariant().clone();
                    vfy.setSource(VariantSource.REFIT);
                    vfy.addPermaMod(HullMods.AUTOREPAIR,true);
                    fy.setVariant(vfy,false,false);
                    fy.updateStats();
                    combatDiv1 -= fy.getFleetPointCost();
                }
                float combatDiv2 = combat * 0.3f;
                while (combatDiv2 > 0) {
                    FleetMemberAPI sf = fleet.getFleetData().addFleetMember("HSI_Promise_GF3_Support");
                    sf.setCaptain(createLowerCaptain());
                    ShipVariantAPI vsf = sf.getVariant().clone();
                    vsf.setSource(VariantSource.REFIT);
                    vsf.addPermaMod(HullMods.AUTOREPAIR,true);
                    sf.setVariant(vsf,false,false);
                    sf.updateStats();
                    combatDiv2 -= sf.getFleetPointCost();
                }
                float combatDiv4 = combat * 0.3f;
                while (combatDiv4 > 0) {
                    FleetMemberAPI sf = fleet.getFleetData().addFleetMember("HSI_Weaver_GF3_Strike");
                    sf.setCaptain(createLowerCaptain());
                    ShipVariantAPI vsf = sf.getVariant().clone();
                    vsf.setSource(VariantSource.REFIT);
                    vsf.addPermaMod(HullMods.AUTOREPAIR,true);
                    sf.setVariant(vsf,false,false);
                    sf.updateStats();
                    combatDiv4 -= sf.getFleetPointCost();
                }
                float combatDiv3 = combat * 0.1f;
                while (combatDiv3 > 0) {
                    FleetMemberAPI sf = fleet.getFleetData().addFleetMember("HSI_ZhiYuan_Assault");
                    sf.setCaptain(HSIDreadnought.createJuniorCaptain());
                    ShipVariantAPI vsf = sf.getVariant().clone();
                    vsf.setSource(VariantSource.REFIT);
                    vsf.addPermaMod(HullMods.AUTOREPAIR,true);
                    sf.setVariant(vsf,false,false);
                    sf.updateStats();
                    combatDiv3 -= sf.getFleetPointCost();
                }
            }
                break;
        }
        FleetMemberAPI elegy = fleet.getFleetData().addFleetMember("HSI_Elegy_Support");
        elegy.setCaptain(createHigherCaptain());
        elegy.updateStats();

        elegy = fleet.getFleetData().addFleetMember("HSI_Elegy_Support");
        elegy.setCaptain(createHigherCaptain());
        elegy.updateStats();


        float t = tanker;
        while (t>0){
            FleetMemberAPI f = fleet.getFleetData().addFleetMember("HSI_DaoHe_Supply");
            t-=f.getFleetPointCost();
        }

        float f = freighter;
        while (f>0){
            FleetMemberAPI m = fleet.getFleetData().addFleetMember("HSI_DaoHe_Supply");
            f-=m.getFleetPointCost();
        }

        fleet.getMemoryWithoutUpdate().set("$HSI_GuardFleet",true);

        return fleet;
    }

    public static PersonAPI createAdmiral() {
        PersonAPI person = Global.getFactory().createPerson();
        person.setPersonality(Personalities.AGGRESSIVE);
        person.setPortraitSprite(Global.getSector().getFaction("HSI").getPortraits(person.getGender()).pick());
        person.getStats().setSkipRefresh(true);

        person.getStats().setLevel(12);
        person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        person.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
        person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
        person.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
        person.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE,2);
        person.getStats().setSkillLevel(Skills.POINT_DEFENSE,2);
        person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
        person.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);

        person.getStats().setSkillLevel(Skills.NAVIGATION, 1);
        person.getStats().setSkillLevel(Skills.CREW_TRAINING,1);
        person.getStats().setSkillLevel(Skills.FLUX_REGULATION,1);
        person.getStats().setSkillLevel(Skills.CYBERNETIC_AUGMENTATION,1);

        person.getStats().setSkipRefresh(false);

        return person;
    }

    public static PersonAPI createHigherCaptain() {
        PersonAPI person = Global.getSector().getFaction("HSI").createRandomPerson();
        //person.setId(Misc.genUID());
        person.setPersonality(Personalities.AGGRESSIVE);
        person.getStats().setSkipRefresh(true);

        person.getStats().setLevel(8);
        person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        person.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
        person.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
        person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
        person.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
        person.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
        person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
        person.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
        person.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE,2);
        person.getStats().setSkillLevel(Skills.POINT_DEFENSE,2);


        person.getStats().setSkipRefresh(false);

        return person;
    }

    public static PersonAPI createLowerCaptain() {
        PersonAPI person = Global.getSector().getFaction("HSI").createRandomPerson();
        person.setPersonality(Personalities.AGGRESSIVE);
        person.getStats().setSkipRefresh(true);
        //person.setId(Misc.genUID());

        person.getStats().setLevel(5);
        person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        person.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
        person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
        person.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE,2);
        person.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);


        person.getStats().setSkipRefresh(false);

        return person;
    }
}
