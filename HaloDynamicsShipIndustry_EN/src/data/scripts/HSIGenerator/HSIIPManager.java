package data.scripts.HSIGenerator;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;

import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import data.kit.HSIIds;

public class HSIIPManager {
    public static List<String> PortraitsCheck = new ArrayList<>();
    static{
        PortraitsCheck.add(HSIIds.PERSON.THE_COMMISSIONER);
    }

    public static void createCommisioner(SectorAPI sector) {
        ImportantPeopleAPI ip = sector.getImportantPeople();
        MarketAPI spaceBridge = Global.getSector().getEconomy().getMarket("HSI_SpaceBridge_Market");
        if (spaceBridge != null) {
            for (PersonAPI p : spaceBridge.getPeopleCopy()) {
                spaceBridge.removePerson(p);
                ip.removePerson(p);
                spaceBridge.getCommDirectory().removePerson(p);
            }
            PersonAPI TheCommisioner = spaceBridge.getFaction().createRandomPerson();

            TheCommisioner.setId(HSIIds.PERSON.THE_COMMISSIONER);
            TheCommisioner.setPostId("HSI_TheCommisioner");
            TheCommisioner.setRankId("HSI_TheCommisioner");
            TheCommisioner.setGender(FullName.Gender.FEMALE);
            TheCommisioner.getName().setFirst("Tamamo");
            TheCommisioner.getName().setLast("?");

            TheCommisioner.setPortraitSprite("graphics/portraits/IP/TheCommisioner.png");
            
            TheCommisioner.setImportance(PersonImportance.VERY_HIGH);
            TheCommisioner.setVoice(Voices.OFFICIAL);
            ip.addPerson(TheCommisioner);
            ip.getData(TheCommisioner).getLocation().setMarket(spaceBridge);
            ip.checkOutPerson(TheCommisioner, "permanent_staff");
            TheCommisioner.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);

            spaceBridge.setAdmin(TheCommisioner);
            spaceBridge.getCommDirectory().addPerson(TheCommisioner, 0);
            spaceBridge.addPerson(TheCommisioner);
        }
    }

    public static void createKnight(SectorAPI sector) {
        ImportantPeopleAPI ip = sector.getImportantPeople();
            PersonAPI TheKnight = sector.getPlayerFaction().createRandomPerson();

            TheKnight.setId(HSIIds.PERSON.THE_KNIGHT);
            TheKnight.setPostId(Ranks.CITIZEN);
            TheKnight.setRankId(Ranks.CITIZEN);
            TheKnight.setGender(FullName.Gender.ANY);

            TheKnight.setPortraitSprite("graphics/portraits/IP/HSI_Knight.png");
            
            TheKnight.setImportance(PersonImportance.VERY_HIGH);
            TheKnight.setVoice(Voices.OFFICIAL);
            ip.addPerson(TheKnight);
            ip.checkOutPerson(TheKnight, "permanent_staff");
    }

    public static void createHPSIDOperator(MarketAPI market){
        if(market!=null){
            PersonAPI HPSIDOperator = market.getFaction().createRandomPerson();
            HPSIDOperator.setPortraitSprite("graphics/portraits/IP/HSI_HPSID_Operator.png");
            HPSIDOperator.setPostId("HPSIDOperator");
            HPSIDOperator.setRankId("agent");
            HPSIDOperator.addTag("HPSID");
            market.addPerson(HPSIDOperator);
            market.getCommDirectory().addPerson(HPSIDOperator);
        }
    }

    public static void createStalkerContact(MarketAPI market){
        if(market!=null){
            ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
            PersonAPI SC = market.getFaction().createRandomPerson();
            SC.setPortraitSprite("graphics/portraits/IP/HSI_Stalker_Officer.png");
            SC.setRankId("agent");
            SC.addTag("HSISS");
            //SC.addTag(Tags.CONTACT_MILITARY);
            SC.setImportanceAndVoice(PersonImportance.HIGH, StarSystemGenerator.random);
            market.addPerson(SC);
            market.getCommDirectory().addPerson(SC);
            ip.addPerson(SC);
            ip.checkOutPerson(SC, "permanent_staff");
            ContactIntel.addPotentialContact(SC,market,null);
        }
    }



    public static PersonAPI getSkye(){
        PersonAPI skye = Global.getSector().getFaction(Factions.PLAYER).createRandomPerson();
        skye.setPortraitSprite("graphics/portraits/IP/HSI_Scythe.png");
        skye.setGender(FullName.Gender.FEMALE);
        skye.setName(new FullName("Skye","", FullName.Gender.FEMALE));
        skye.setPostId(Ranks.POST_CITIZEN);
        skye.setRankId(Ranks.CITIZEN);
        return skye;
    }

    public static void createHSIContact(MarketAPI market){
        if(market!=null){
            ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
            PersonAPI SC = market.getFaction().createRandomPerson();
            SC.setRankId("citizen");
            SC.setPostId("HSI_ForgeProvider");
            SC.addTag("HSIForge");
            SC.setImportanceAndVoice(PersonImportance.HIGH, StarSystemGenerator.random);
            SC.setPortraitSprite("graphics/portraits/IP/HSI_ForgeProvider_1.png");
            market.addPerson(SC);
            market.getCommDirectory().addPerson(SC);
            ip.addPerson(SC);
            ip.checkOutPerson(SC, "permanent_staff");
            ContactIntel.addPotentialContact(SC,market,null);
        }
    }

    public static PersonAPI getSalvation(){
        PersonAPI Salvation = Global.getSector().getFaction(Factions.DERELICT).createRandomPerson();

        Salvation.setId("HSI_Salvation");
        Salvation.setName(new FullName("Omeghal","Duke", FullName.Gender.MALE));
        Salvation.setPostId(Ranks.POST_ACADEMICIAN);
        Salvation.setRankId(Ranks.CITIZEN);

        Salvation.setPortraitSprite("graphics/portraits/IP/HSI_Salvation.png");
        return Salvation;
    }
}
