package data.character.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.AfterShipCreationSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import data.kit.HSII18nUtil;

public class HSIKnightSkill {
	public static final float DAMAGE_SP = 5f;
	public static final float DAMAGE = 2.5f;
	public static final float DAMAGE_REDUCE_SP = 5f;
	public static final float DAMAGE_REDUCE = 2.5f;
	public static final float AFTER_PLAYER_LOSS_SP = 10f;
	public static final float AFTER_PLAYER_LOSS = 20f;

	public static class Level1 extends BaseSkillEffectDescription implements AfterShipCreationSkillEffect {
		@Override
		public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
			if (!ship.hasListenerOfClass(HSIKnightSkillScript.class)) {
				ship.addListener(new HSIKnightSkillScript(ship,false));
			}
		}

		@Override
		public void unapplyEffectsAfterShipCreation(ShipAPI ship, String id) {
			ship.removeListenerOfClass(HSIKnightSkillScript.class);
		}

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {

		}

		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {

		}

		public String getEffectDescription(float level) {
			return null;
		}

		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill,
				TooltipMakerAPI info, float width) {
			init(stats, skill);

			info.addPara(HSII18nUtil.getCampaignString("HSIKnight_SkillDesc0"), 0f, hc, hc,""+DAMAGE+"%",""+DAMAGE_REDUCE+"%");
			info.addPara(HSII18nUtil.getCampaignString("HSIKnight_SkillDesc1"), 0f, hc, hc,""+AFTER_PLAYER_LOSS+"%");
			info.addPara(HSII18nUtil.getCampaignString("HSIKnight_SkillDesc2"), 0f, hc, hc,"100%");
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}

	public static class Level2 extends BaseSkillEffectDescription implements AfterShipCreationSkillEffect {

		@Override
		public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
			if (!ship.hasListenerOfClass(HSIKnightSkillScript.class)) {
				ship.addListener(new HSIKnightSkillScript(ship,true));
			}
		}

		@Override
		public void unapplyEffectsAfterShipCreation(ShipAPI ship, String id) {
			ship.removeListenerOfClass(HSIKnightSkillScript.class);
		}


		public String getEffectDescription(float level) {
			return null;
		}

		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill,
				TooltipMakerAPI info, float width) {
			init(stats, skill);

			info.addPara(HSII18nUtil.getCampaignString("HSIKnight_SkillDesc0"), 0f, hc, hc,""+DAMAGE_SP+"%",""+DAMAGE_REDUCE_SP+"%");
			info.addPara(HSII18nUtil.getCampaignString("HSIKnight_SkillDesc1"), 0f, hc, hc,""+AFTER_PLAYER_LOSS_SP+"%");
			info.addPara(HSII18nUtil.getCampaignString("HSIKnight_SkillDesc2"), 0f, hc, hc,"100%");
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}

		@Override
		public void apply(MutableShipStatsAPI mutableShipStatsAPI, HullSize hullSize, String s, float v) {

		}

		@Override
		public void unapply(MutableShipStatsAPI mutableShipStatsAPI, HullSize hullSize, String s) {

		}
	}

	public static class Level3 extends BaseSkillEffectDescription implements AfterShipCreationSkillEffect {

		@Override
		public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
			Global.getCombatEngine().getListenerManager().addListener(new HSIKnightSkillEliteScript(ship));
		}

		@Override
		public void unapplyEffectsAfterShipCreation(ShipAPI ship, String id) {
		}

		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {

		}

		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {

		}

		public String getEffectDescription(float level) {
			return null;
		}

		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill,
											TooltipMakerAPI info, float width) {
			init(stats, skill);

			info.addPara(HSII18nUtil.getCampaignString("HSIKnight_SkillDesc3"), 0f, hc, hc,""+10+"%");
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}

	}
}
