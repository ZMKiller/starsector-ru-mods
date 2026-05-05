package data.hullmods;

public class HSICombatStyle {

    public static enum HSICombatStyleType {
        COURAGE, STRIKE, SCOUT, SNIPE, FORT, SUPPORT, NONE;
    }

    public static final String COURAGE_ID = "HSI_CourageStyle";
    public static final String STRIKE_ID = "HSI_StrikeStyle";

    public static final String SCOUT_ID = "HSI_ScoutStyle";
    public static final String SNIPE_ID = "HSI_SnipeStyle";

    public static final String FORT_ID = "HSI_FortStyle";
    public static final String SUPPORT_ID = "HSI_SupportStyle";

    public static String getTypeModId(HSICombatStyleType style) {
        switch (style) {
            case COURAGE:
                return COURAGE_ID;
            case STRIKE:
                return STRIKE_ID;
            case SCOUT:
                return SCOUT_ID;
            case SNIPE:
                return SNIPE_ID;
            case FORT:
                return FORT_ID;
            case SUPPORT:
                return SUPPORT_ID;
            default:
                return null;
        }
    }
}
