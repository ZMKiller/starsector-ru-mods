package data.campaign.HSITC1Items;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc.Token;

import data.kit.HSII18nUtil;

public class HSITC1Intel extends BaseIntelPlugin {

	protected PlanetAPI planet;

	public HSITC1Intel() {
		this.planet = HSITC1Generator.getPlanet();
	}

	@Override
	protected void notifyEnded() {
		super.notifyEnded();
		Global.getSector().removeScript(this);
	}

	@Override
	public boolean callEvent(String ruleId, InteractionDialogAPI dialog,
			List<Token> params, Map<String, MemoryAPI> memoryMap) {
		return true;
	}

	@Override
	protected void notifyEnding() {
		super.notifyEnding();
	}

	@Override
	public boolean isEnded() {
		return Global.getSector().getMemoryWithoutUpdate().contains("$HSITC1Finished");
	}

	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {

		float pad = 3f;
		float opad = 10f;

		float initPad = pad;
		if (mode == ListInfoMode.IN_DESC)
			initPad = opad;

		Color tc = getBulletColorForMode(mode);

		bullet(info);

		info.addPara(HSII18nUtil.getCampaignString("HSITC1_ShortDesc2"), tc, initPad);

		initPad = 0f;

		unindent(info);
	}

	@Override
	public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
		Color c = getTitleColor(mode);
		info.setParaSmallInsignia();
		info.addPara(getName(), c, 0f);
		info.setParaFontDefault();
		addBulletPoints(info, mode);

	}

	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		info.addPara(HSII18nUtil.getCampaignString("HSITC1_ShortDesc1"), new Color(255, 140, 200, 255), opad);

		addBulletPoints(info, ListInfoMode.IN_DESC);

	}

	@Override
	public String getIcon() {
		return Global.getSettings().getSpriteName("intel", "red_planet");
	}

	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_STORY);
		tags.add(Tags.INTEL_EXPLORATION);
		tags.add(Tags.INTEL_ACCEPTED);
		tags.add(Tags.INTEL_MISSIONS);
		return tags;
	}

	@Override
	public IntelSortTier getSortTier() {
		return IntelSortTier.TIER_2;
	}

	public String getSortString() {
		return HSII18nUtil.getCampaignString("HSITC1_NameBefore");
	}

	public String getName() {
		if (isEnded() || isEnding()) {
			return HSII18nUtil.getCampaignString("HSITC1_NameFinished");
		}
		return HSII18nUtil.getCampaignString("HSITC1_NameBefore");
	}

	@Override
	public FactionAPI getFactionForUIColors() {
		return super.getFactionForUIColors();
	}

	public String getSmallDescriptionTitle() {
		return getName();
	}

	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {
		return planet;
	}

	@Override
	public boolean shouldRemoveIntel() {
		return super.shouldRemoveIntel();
	}

	@Override
	public String getCommMessageSound() {
		return getSoundMajorPosting();
	}

}
