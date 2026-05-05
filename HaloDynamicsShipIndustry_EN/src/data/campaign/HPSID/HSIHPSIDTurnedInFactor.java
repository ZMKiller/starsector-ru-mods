package data.campaign.HPSID;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import data.kit.HSII18nUtil;

public class HSIHPSIDTurnedInFactor extends BaseOneTimeFactor {
    public enum Type {
        OMEGA_WEAPON,
        COMBAT_DATA,
        SURVEY_DATA,
        AI_CORE,
        SPECIAL_SHIP,
        RESOURCE
    }

    private Type type;
    private String param;
    private int num;

    public HSIHPSIDTurnedInFactor(Type type, String param, int points, int num) {
        super(points);
        this.type = type;
        this.param = param;
        this.num = num;
    }

    protected String getBulletPointText(BaseEventIntel intel) {
        return getFactorString(type, param, num);
    }

    @Override
	public String getDesc(BaseEventIntel intel) {
		return getFactorString(type, param, num);
	}

	public TooltipCreator getMainRowTooltip(BaseEventIntel intel) {
		return new BaseFactorTooltip() {
			@Override
			public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
				tooltip.addPara(getFactorString(type, param, num)+ HSII18nUtil.getCampaignString("HSITurnedInBenefit"),
						0f);
			}
			
		};
	}
    public static int calculatePoints(Type type, String param) {
        int pt = 0;
        switch (type) {
            case AI_CORE:
                switch (param) {
                    case Commodities.ALPHA_CORE:
                        pt = 15;
                        break;
                    case Commodities.BETA_CORE:
                        pt = 8;
                        break;
                    case Commodities.GAMMA_CORE:
                        pt = 4;
                        break;
                    case Commodities.OMEGA_CORE:
                        pt = 40;
                        break;
                    default:
                        break;
                }
                break;
            case COMBAT_DATA:
                break;
            case OMEGA_WEAPON:
                break;
            case SPECIAL_SHIP:
                ShipHullSpecAPI spec = Global.getSettings().getHullSpec(param);
                if(spec!=null){
                    pt = spec.getHullSize().ordinal()*15;
                }
                break;
            case SURVEY_DATA:
                switch (param) {
                    case Commodities.SURVEY_DATA_1:
                        pt = 2;
                        break;
                    case Commodities.SURVEY_DATA_2:
                        pt = 4;
                        break;
                    case Commodities.SURVEY_DATA_3:
                        pt = 6;
                        break;
                    case Commodities.SURVEY_DATA_4:
                        pt = 7;
                        break;
                    case Commodities.SURVEY_DATA_5:
                        pt = 9;
                        break;
                    default:
                        break;
                }
                break;
            case RESOURCE:
                break;
            default:
                break;

        }

        return pt;
    }

    public static String getFactorString(Type type, String param, int num) {
        String str = "";
        str += HSII18nUtil.getCampaignString("HPSID_TurnInString");
        switch (type) {
            case AI_CORE:
                str += Global.getSettings().getCommoditySpec(param).getName();
                break;
            case COMBAT_DATA:

                break;
            case OMEGA_WEAPON:
                str += Global.getSettings().getWeaponSpec(param).getWeaponName();
                break;
            case SPECIAL_SHIP:
                str += Global.getSettings().getHullSpec(param).getNameWithDesignationWithDashClass();
                break;
            case SURVEY_DATA:
                str += Global.getSettings().getCommoditySpec(param).getName();
                break;
            default:
                break;

        }
        str += " *";
        str += ("" + num);
        return str;
    }
}
