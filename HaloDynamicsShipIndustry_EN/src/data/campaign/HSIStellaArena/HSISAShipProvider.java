package data.campaign.HSIStellaArena;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.HSISAShop;
import com.fs.starfarer.api.impl.campaign.rulecmd.HSISAShopComm;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.kit.HSII18nUtil;

public class HSISAShipProvider extends BaseSpecialItemPlugin {

    public void performRightClickAction() {
        String param = stack.getSpecialDataIfSpecial().getData();
        if (Global.getSector() != null && Global.getSector().getPlayerFleet() != null) {
            final FleetMemberAPI ship = Global.getFactory().createFleetMember(FleetMemberType.SHIP,
                    Global.getSettings().createEmptyVariant(param + "_hull", Global.getSettings().getHullSpec(param)));
            Global.getSector().getPlayerFleet().getFleetData().addFleetMember(ship);
        }
    }

    public boolean hasRightClickAction() {
        return true;
    }

    @Override
    public void render(float x, float y, float w, float h, float alphaMult,
            float glowMult, SpecialItemRendererAPI renderer) {
        float cx = x + w / 2f;
        float cy = y + h / 2f;

        float blX = cx - 30f;
        float blY = cy - 15f;
        float tlX = cx - 20f;
        float tlY = cy + 26f;
        float trX = cx + 23f;
        float trY = cy + 26f;
        float brX = cx + 15f;
        float brY = cy - 18f;

        String hullId = stack.getSpecialDataIfSpecial().getData();

        boolean known = Global.getSector().getPlayerFaction().knowsShip(hullId);

        float mult = 1f;
        // if (known) mult = 0.5f;

        Color bgColor = Global.getSector().getPlayerFaction().getDarkUIColor();
        bgColor = Misc.setAlpha(bgColor, 255);

        // float b = Global.getSector().getCampaignUI().getSharedFader().getBrightness()
        // * 0.25f;
        renderer.renderBGWithCorners(bgColor, blX, blY, tlX, tlY, trX, trY, brX, brY,
                alphaMult * mult, glowMult * 0.5f * mult, false);
        renderer.renderShipWithCorners(hullId, null, blX, blY, tlX, tlY, trX, trY, brX, brY,
                alphaMult * mult, glowMult * 0.5f * mult, !known);

        SpriteAPI overlay = Global.getSettings().getSprite("ui", "bpOverlayShip");
        overlay.setColor(Color.green);
        overlay.setColor(Global.getSector().getPlayerFaction().getBrightUIColor());
        overlay.setAlphaMult(alphaMult);
        overlay.setNormalBlend();
        renderer.renderScanlinesWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY, alphaMult, false);

        if (known) {
            renderer.renderBGWithCorners(Color.black, blX, blY, tlX, tlY, trX, trY, brX, brY,
                    alphaMult * 0.5f, 0f, false);
        }

        overlay.renderWithCorners(blX, blY, tlX, tlY, trX, trY, brX, brY);
    }

    @Override
    public String getName() {
        ShipHullSpecAPI ship = Global.getSettings().getHullSpec(stack.getSpecialDataIfSpecial().getData());
        if (ship != null) {
            // return ship.getHullName() + " Blueprint";
            return ship.getNameWithDesignationWithDashClass() + super.getName();
        }
        return super.getName();
    }

    @Override
    public String getDesignType() {
        ShipHullSpecAPI ship = Global.getSettings().getHullSpec(stack.getSpecialDataIfSpecial().getData());
        if (ship != null) {
            return ship.getManufacturer();
        }
        return null;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, CargoTransferHandlerAPI transferHandler,
            Object stackSource) {
        ShipHullSpecAPI ship = Global.getSettings().getHullSpec(stack.getSpecialDataIfSpecial().getData());
        super.createTooltip(tooltip, expanded, transferHandler, stackSource);

        float pad = 3f;
        float opad = 10f;
        float small = 5f;
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color b = Misc.getButtonTextColor();
        b = Misc.getPositiveHighlightColor();

        String hullId = stack.getSpecialDataIfSpecial().getData();
        // boolean known = Global.getSector().getPlayerFaction().knowsShip(hullId);
        Description desc = Global.getSettings().getDescription(ship.getDescriptionId(), Type.SHIP);

        String prefix = "";
        if (ship.getDescriptionPrefix() != null) {
            prefix = ship.getDescriptionPrefix() + "\n\n";
        }
        tooltip.addPara(prefix + desc.getText1FirstPara(), opad);

        HSISAShopComm comm = HSISAShop.getComm(stack);

        float cost = 0;
        if(comm!=null) cost = comm.cost;

        tooltip.addPara(HSII18nUtil.getCampaignString("HSISAShipProviderCost")+String.format("%.1f", cost), opad);

        tooltip.addPara(HSII18nUtil.getCampaignString("HSISAShipProviderRightClick"), b, opad);
    }

}
