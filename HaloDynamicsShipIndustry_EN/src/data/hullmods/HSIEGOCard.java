package data.hullmods;

public class HSIEGOCard {
    public String id;
    public int lv;
    public boolean isSpecific = false;
    public String SpecificId = "";

    public HSIEGOCard(String id, int lv, boolean isSpecific, String SpecificId) {
        this.id = id;
        this.lv = lv;
        this.isSpecific = isSpecific;
        this.SpecificId = SpecificId;
    }
}
