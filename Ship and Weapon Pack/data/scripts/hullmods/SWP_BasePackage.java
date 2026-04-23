package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;

public abstract class SWP_BasePackage extends BaseHullMod {

    public static final String SWP_NO_PACKAGE = "swp_no_package";

    protected static final float PARA_PAD = 10f;
    protected static final float SECTION_PAD = 10f;
    protected static final float INTERNAL_PAD = 4f;
    protected static final float INTERNAL_PARA_PAD = 4f;
    protected static final float BULLET_PAD = 3f;

    protected abstract String getHullModId();

    protected abstract String getFlavorText();

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        if (getHullModId().contentEquals(SWP_NO_PACKAGE)) {
            LabelAPI label = tooltip.addPara("Imperial Packages cannot be installed on this hull.", 0f);
            label.setHighlightColors(Global.getSettings().getDesignTypeColor("Imperium"));
            label.setHighlight("Imperial Packages");

            if (getFlavorText() != null) {
                label = tooltip.addPara(getFlavorText(), Misc.getGrayColor(), PARA_PAD);
                label.setAlignment(Alignment.MID);
            }
        }
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if ((ship != null) && ship.getVariant().getHullMods().contains(SWP_NO_PACKAGE) && !getHullModId().contentEquals(SWP_NO_PACKAGE)) {
            return "Imperial Packages cannot be installed";
        }
        if ((ship != null) && !ship.getHullSpec().getHullId().startsWith("ii_")) {
            return "Must be installed on an Imperium ship";
        }
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship == null) {
            return false;
        }
        if (ship.getVariant().getHullMods().contains(SWP_NO_PACKAGE) && !getHullModId().contentEquals(SWP_NO_PACKAGE)) {
            return false;
        }
        return ship.getHullSpec().getHullId().startsWith("ii_");
    }

    @Override
    public Color getBorderColor() {
        return new Color(200, 200, 200);
    }

    @Override
    public Color getNameColor() {
        return new Color(200, 200, 200);
    }
}
