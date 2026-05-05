package progsmod.data.campaign.rulecmd.util;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.*;

public class HullModButtonData {
    public String id;
    public String name;
    public String spriteName;
    public String defaultDescription;
    public String tooltipDescription;
    public HullModEffect hullModEffect;
    public HullSize hullSize;
    public int cost;
    public boolean isEnhanceOnly;
    public boolean isBuiltIn = false;

    public HullModButtonData(String id, String name, String spriteName, String defaultDescription, String tooltipDescription, HullModEffect hullModEffect, HullSize hullSize, int cost, boolean isEnhanceOnly) {
        this.id = id;
        this.name = name;
        this.spriteName = spriteName;
        this.defaultDescription = defaultDescription;
        this.tooltipDescription = tooltipDescription;
        this.hullModEffect = hullModEffect;
        this.hullSize = hullSize;
        this.cost = cost;
        this.isEnhanceOnly = isEnhanceOnly;
    }

    public HullModButtonData(String id, String name, String spriteName, String defaultDescription,
                             String tooltipDescription, HullModEffect hullModEffect,
                             HullSize hullSize, int cost, boolean isEnhanceOnly, boolean isBuiltIn) {
        this.id = id;
        this.name = name;
        this.spriteName = spriteName;
        this.defaultDescription = defaultDescription;
        this.tooltipDescription = tooltipDescription;
        this.hullModEffect = hullModEffect;
        this.hullSize = hullSize;
        this.cost = cost;
        this.isEnhanceOnly = isEnhanceOnly;
        this.isBuiltIn = isBuiltIn;
    }
}
