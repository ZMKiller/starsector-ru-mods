package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.campaign.HSIStellaArena.Buff.HSISABuff;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

public class HSISAAdvLoader {
    private static final Logger LOG = Global.getLogger(HSISAAdvLoader.class);

    private static JSONObject enemyData = null;
    public static Map<String,HSIAdvEnemySpec> enemies = new HashMap<>();

    private static JSONObject buffData = null;

    public static Map<String,HSIAdvBuffSpec> buffs = new HashMap<>();

    public HSISAAdvLoader(){

    }

    public static void load(){
        enemies.clear();
        try {
            enemyData = Global.getSettings().getMergedJSON("data/config/modFiles/HSIStellaArena/HSIStellaArenaAdventureModeEnemies.json");
        }catch (Exception e){
            LOG.fatal("Fail to load data/config/modFiles/HSIStellaArena/HSIStellaArenaAdventureModeEnemies.json", e);
        }
        if(enemyData == null) return;
        Iterator<String> iter = enemyData.keys();
        while (iter.hasNext()) {
            String id = iter.next();
            boolean missingMod = false;
            List<String> requiredMods = getStringList(id, "requiredMod",enemyData);
            if (!requiredMods.isEmpty()) {
                for (String mod : requiredMods) {
                    if (!Global.getSettings().getModManager().isModEnabled(id)) {
                            LOG.info("Skipping enemy for missing mod: " + mod);
                        missingMod = true;
                    }
                }
            }
            if (missingMod) {
                continue;
            }
            HSIAdvEnemySpec spec = new HSIAdvEnemySpec(id,getString(id,"name",enemyData), getString(id,"short",enemyData),
                    getString(id,"sprite",enemyData),getInt(id,"level",enemyData),getString(id,"plugin",enemyData));
            LOG.info(id+"||"+getString(id,"name",enemyData)+"||"+getString(id,"short",enemyData)+"||"+
                    getString(id,"sprite",enemyData)+"||"+getInt(id,"level",enemyData)+"||"+getString(id,"plugin",enemyData));
            //if(isStringValid(spec.getName())&&isStringValid(spec.getSprite())){
                enemies.put(id,spec);
                LOG.info("Loaded enemy:"+id);
            //}else{
            //    LOG.info("Fail load enemy "+id);
            //}
        }

        buffs.clear();
        try {
            buffData = Global.getSettings().getMergedJSON("data/config/modFiles/HSIStellaArena/HSIStellaArenaAdventureModeBuffs.json");
        }catch (Exception e){
            LOG.fatal("Fail to load data/config/modFiles/HSIStellaArena/HSIStellaArenaAdventureModeBuffs.json", e);
        }
        if(buffData == null) return;
        Iterator<String> iter2 = buffData.keys();
        while (iter2.hasNext()) {
            String id = iter2.next();
            boolean missingMod = false;
            List<String> requiredMods = getStringList(id, "requiredMod",buffData);
            if (!requiredMods.isEmpty()) {
                for (String mod : requiredMods) {
                    if (!Global.getSettings().getModManager().isModEnabled(id)) {
                        LOG.info("Skipping buff for missing mod: " + mod);
                        missingMod = true;
                    }
                }
            }
            if (missingMod) {
                continue;
            }
            HSIAdvBuffSpec spec = new HSIAdvBuffSpec(id,getString(id,"name",buffData),getString(id,"sprite",buffData),
                    getStringList(id,"tags",buffData),getString(id,"plugin",buffData),getString(id,"desc",buffData));
            LOG.info(id+"||"+getString(id,"name",buffData)+"||"+getString(id,"sprite",buffData)+"||"+
                    getStringList(id,"tags",buffData)+"||"+getString(id,"plugin",buffData)+"||"+getString(id,"desc",buffData));
            //if(isStringValid(spec.getEffect())&&isStringValid(spec.getName())&&isStringValid(spec.getSprite())&&isStringValid(spec.getId())) {
            buffs.put(id,spec);
            LOG.info("Loaded buff:"+id);
            //}else{
            //    LOG.info("Fail load buff "+id);
            //}
        }

        LOG.info("Finished load with "+enemies.keySet()+"||"+buffs.keySet());
    }

    private static String getString(String enemyId, String key,JSONObject json) {
        return getString(enemyId, key, null,json);
    }

    private static String getString(String enemyId, String key, String defaultValue,JSONObject json) {
        String value = defaultValue;

        try {
            JSONObject reqSettings = json.getJSONObject(enemyId);
            if (reqSettings.has(key)) {
                value = reqSettings.getString(key);
            }
        } catch (JSONException ex) {
        }

        return value;
    }

    private static Integer getInt(String enemyId, String key,JSONObject json) {
        return getInt(enemyId, key, -1,json);
    }

    private static Integer getInt(String enemyId, String key, int defaultValue,JSONObject json) {
        int value = defaultValue;

        try {
            JSONObject reqSettings = json.getJSONObject(enemyId);
            if (reqSettings.has(key)) {
                value = reqSettings.getInt(key);
            }
        } catch (JSONException ex) {
        }

        return value;
    }

    private static List<String> getStringList(String enemyId, String key,JSONObject json) {
        List<String> value = new ArrayList<>();

        try {
            JSONObject reqSettings = json.getJSONObject(enemyId);
            if (reqSettings.has(key)) {
                JSONArray list = reqSettings.getJSONArray(key);
                if (list.length() > 0) {
                    for (int i = 0; i < list.length(); i++) {
                        value.add(list.getString(i));
                    }
                }
            }
        } catch (JSONException ex) {
        }

        return value;
    }

    public static HSIAdvEnemySpec getRandomEnemyByLevel(int level){
        WeightedRandomPicker<HSIAdvEnemySpec> picker = new WeightedRandomPicker<>();
        for (String key : enemies.keySet()) {
            HSIAdvEnemySpec e = enemies.get(key);
            if(e.getLevel() == level){
                picker.add(e);
            }
        }
        return picker.pick();
    }




    public static class HSIAdvEnemySpec{
        private String id = "";
        private String name = "";

        private String sprite = "";

        private String desc = "";
        //private List<String> requiredMod = new ArrayList<>();

        private int level = -1;

        private String plugin = "";

        public HSIAdvEnemySpec(String id,String name,String desc,String sprite,int level,String plugin){
            this.id = id;
            this.name = name;
            this.desc = desc;
            this.sprite = sprite;
            this.level = level;
            this.plugin = plugin;
        }

        public int getLevel() {
            return level;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getPlugin() {
            return plugin;
        }

        public String getSprite() {
            return sprite;
        }

        public String getDesc() {
            return desc;
        }

        public PersonAPI createPersonForDisplay(){
            PersonAPI p = Global.getFactory().createPerson();
            p.setName(new FullName("",name, FullName.Gender.ANY));
            p.setPortraitSprite(sprite);
            p.setId(id);
            return p;
        }
    }

    public static class HSIAdvBuffSpec{
        private String id = "";
        private List<String> tags = new ArrayList<>();

        private String name = "";
        private String sprite = "";

        private String effect = "";

        private String desc = "";
        public HSIAdvBuffSpec(String id,String name,String sprite,List<String> tags,String effect,String desc){
            this.id = id;
            this.name = name;
            this.sprite = sprite;
            this.tags.addAll(tags);
            this.effect = effect;
            this.desc = desc;
        }

        public String getId() {
            return id;
        }

        public List<String> getTags() {
            return tags;
        }

        public String getEffect() {
            return effect;
        }

        public String getSprite() {
            return sprite;
        }

        public String getDesc() {
            return desc;
        }

        public String getName() {
            return name;
        }
        @Nullable
        public HSISABuff createBuff(){
            HSISABuff buff = null;
            try {
                Object test = Class.forName(getEffect()).newInstance();
                if(test instanceof HSISABuff){
                    buff = (HSISABuff) test;
                }
            }catch (Exception ex){
                Global.getLogger(this.getClass()).error(ex);
            }
            return buff;
        }
    }

    protected static final String PATH = "data/config/modFiles/HSIStellaArena/";

    public static CampaignFleetAPI createFleet(HSIAdvEnemySpec enemy,int level) {

        CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(Factions.SCAVENGERS,"HSISA-A-" + level+"||"+enemy.getName(),true);


        String Path = PATH + enemy.getId() + "_fleet.csv";
        try {
            JSONArray playerFleet = Global.getSettings().getMergedSpreadsheetDataForMod("rowNumber", Path, "aibattles");
            for (int i = 0; i < playerFleet.length(); i++) {
                JSONObject row = playerFleet.getJSONObject(i);
                if (row.getString("rowNumber").isEmpty()) continue;

                String variant = row.getString("variant");
                ShipVariantAPI v = Global.getSettings().getVariant(variant);
                if(v == null){
                    Global.getLogger(HSISAAdvLoader.class).info("Fail to load "+variant);
                    continue;
                }
                v.addTag(Tags.TAG_NO_AUTOFIT);
                v.addTag("ADVLEVEL_"+level);
                v.addPermaMod("HSI_AdventureEnemy");


                FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, v);
                fleet.getFleetData().addFleetMember(member);
                //fleet.fleet.getFleetData().ensureHasFlagship();

                String name = row.optString("name", member.getHullSpec().getHullName());
                if (!isStringValid(name)) name = member.getHullSpec().getHullName();
                member.setShipName(name);
                member.getCrewComposition().setCrew(member.getMaxCrew());
                member.getRepairTracker().setCR(0.7f);
                member.getRepairTracker().setCR(Math.max(member.getRepairTracker().getCR(), member.getRepairTracker().getMaxCR()));
                member.getRepairTracker().setMothballed(false);
                member.getRepairTracker().setCrashMothballed(false);

                String personality = row.optString("personality", Personalities.STEADY);
                if (!isStringValid(personality)) personality = Personalities.STEADY;
                PersonAPI person = Global.getFactory().createPerson();
                member.setCaptain(person);
                person.getStats().setLevel(0);
                person.setPersonality(personality);

                String skills = row.optString("skills", "");
                if (isStringValid(skills)) {

                    for (String skill : skills.split(",")) {
                        skill = skill.trim();

                        if (!person.getStats().hasSkill(skill)) {
                            person.getStats().setLevel(person.getStats().getLevel() + 1);
                        }

                        person.getStats().increaseSkill(skill);
                    }

                    person.setPortraitSprite("graphics/portraits/portrait_generic.png");
                }

                String portrait = row.optString("portrait", "graphics/portraits/portrait_generic.png");
                if (isStringValid(portrait) && person.getStats().getLevel() > 0) {
                    person.setPortraitSprite(portrait);
                }
            }
        } catch (IOException | JSONException ex) {
            Global.getLogger(HSISAAdvLoader.class).error("Fail to load " + enemy.getId() + "_fleet.csv");
            Global.getLogger(HSISAAdvLoader.class).error(ex);
        }

        return fleet;
    }

    protected static boolean isStringValid(String string) {
        if (string == null) return false;
        String toCheck = string.trim();
        if (toCheck.isEmpty()) return false;
        if (toCheck.contentEquals("")) return false;
        return true;
    }
}
