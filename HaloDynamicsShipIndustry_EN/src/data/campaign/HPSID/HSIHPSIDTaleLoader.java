package data.campaign.HPSID;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.rulecmd.HSISAAdvLoader;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class HSIHPSIDTaleLoader {

    public static class HSIHPSIDTale{
        private String id = "";
        private String title = "";
        private List<String> desc = new ArrayList<>();
        private String ill = "";

        public HSIHPSIDTale(String id,String title,List<String> desc,String ill){
            this.id = id;
            this.title = title;
            this.desc = desc;
            this.ill = ill;
        }

        public String getId() {
            return id;
        }

        public List<String> getDesc() {
            return desc;
        }

        public String getIll() {
            return ill;
        }

        public String getTitle() {
            return title;
        }
    }

    public static Map<String,HSIHPSIDTale> TALES = new HashMap<>();

    public static Logger LOG = Global.getLogger(HSIHPSIDTaleLoader.class);
    public static void load(){
        TALES.clear();
        JSONObject taleData = null;
        try {
            taleData = Global.getSettings().getMergedJSON("data/campaign/HPSIDTales.json");
        }catch (Exception e){
            LOG.fatal("Fail to load data/campaign/HPSIDTales.json", e);
        }
        if(taleData == null) return;
        Iterator<String> iter = taleData.keys();
        while (iter.hasNext()) {
            String id = iter.next();
            boolean missingMod = false;
            List<String> requiredMods = getStringList(id, "requiredMod", taleData);
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
            HSIHPSIDTale spec = new HSIHPSIDTale(id,getString(id,"title",taleData),getStringList(id,"desc",taleData),getString(id,"illustration",taleData));
            LOG.info(id+ "||" +getString(id,"title",taleData)+ "||" +getStringList(id,"desc",taleData)+ "||" +getString(id,"illustration",taleData));
            //if(isStringValid(spec.getName())&&isStringValid(spec.getSprite())){
            TALES.put(id, spec);
            LOG.info("Loaded tale:" + id);
        }
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



}
