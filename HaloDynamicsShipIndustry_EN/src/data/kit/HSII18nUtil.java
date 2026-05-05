package data.kit;

import com.fs.starfarer.api.Global;

public class HSII18nUtil {
    private static final String SHIP_SYSTEM_SCRIPT = "shipSystem";
    private static final String HULL_MOD_SCRIPT = "hullMod";
    private static final String ECON_SCRIPT = "econ";
    private static final String CAMPAIGN_SCRIPT = "campaign";
    private static final String WEAPON_SCRIPT = "weapon";

    public static String getString(String category, String id) {
        return Global.getSettings().getString(category, id);
    }

    public static String getShipSystemString(String id) {
        return getString(SHIP_SYSTEM_SCRIPT, id);
    }

    public static String getHullModString(String id) {
        return getString(HULL_MOD_SCRIPT, id);
    }

    public static String getEconString(String id){
        return getString(ECON_SCRIPT,id);
    }

    public static String getCampaignString(String id) {
        return getString(CAMPAIGN_SCRIPT, id);
    }

    public static String getWeaponString(String id){
        return getString(WEAPON_SCRIPT, id);
    }
}
