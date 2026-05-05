package com.fs.starfarer.api.impl.campaign.rulecmd.salvage;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import data.campaign.HPSID.HSIHPSIDLevelIntel;
import data.campaign.HPSID.HSIHPSIDTurnedInFactor;
import data.campaign.HPSID.HSIHPSIDTurnedInFactor.Type;
import data.kit.AjimusUtils;
import data.kit.HSII18nUtil;
import data.kit.HSIIds;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HPSIDTurnIn extends BaseCommandPlugin {

	protected CampaignFleetAPI playerFleet;
	protected SectorEntityToken entity;
	protected FactionAPI playerFaction;
	protected FactionAPI entityFaction;
	protected TextPanelAPI text;
	protected OptionPanelAPI options;
	protected CargoAPI playerCargo;
	protected MemoryAPI memory;
	protected InteractionDialogAPI dialog;
	protected Map<String, MemoryAPI> memoryMap;
	protected PersonAPI person;
	protected FactionAPI faction;

	protected boolean buysAICores;
	protected float valueMult;
	protected float repMult;
	protected Type t;

	protected float surveyValueMult = 1f;
	protected float surveyRepMult = 0.5f;

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params,
			Map<String, MemoryAPI> memoryMap) {
		String type = params.get(0).getString(memoryMap);
		if (type == null)
			return false;
		Type t = Enum.valueOf(Type.class, type);
		this.dialog = dialog;
		this.memoryMap = memoryMap;

		String command = params.get(1).getString(memoryMap);
		if (command == null)
			return false;

		memory = getEntityMemory(memoryMap);

		entity = dialog.getInteractionTarget();
		text = dialog.getTextPanel();
		options = dialog.getOptionPanel();

		playerFleet = Global.getSector().getPlayerFleet();
		playerCargo = playerFleet.getCargo();

		playerFaction = Global.getSector().getPlayerFaction();
		entityFaction = entity.getFaction();

		person = dialog.getInteractionTarget().getActivePerson();
		faction = person.getFaction();

		valueMult = faction.getCustomFloat("AICoreValueMult");
		repMult = faction.getCustomFloat("AICoreRepMult");

		switch (t) {
			case AI_CORE:
				// if (playerHasCores()) {
				if (command.equals("selectCores")) {
					selectCores();
				}
				// }
				break;
			case COMBAT_DATA:
				break;
			case OMEGA_WEAPON:
				break;
			case SPECIAL_SHIP:
				if (command.equals("selectRareship")) {
					selectRareShip();
				}
				break;
			case SURVEY_DATA:
				if (command.equals("selectSurveyData")) {
					selectSurveyData();
				}
				break;
			default:
				break;

		}
		return true;
	}

	protected boolean playerHasCores() {
		for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
			CommoditySpecAPI spec = stack.getResourceIfResource();
			if (spec != null && spec.getDemandClass().equals(Commodities.AI_CORES)) {
				return true;
			}
		}
		return false;
	}

	protected void selectSurveyData() {
		CargoAPI copy = Global.getFactory().createCargo(false);
		// copy.addAll(cargo);
		// copy.setOrigSource(playerCargo);
		for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
			CommoditySpecAPI spec = stack.getResourceIfResource();
			if (spec != null && spec.getDemandClass().equals(Commodities.SURVEY_DATA)) {
				copy.addFromStack(stack);
			}
		}
		copy.sort();
		final Type t = Type.SURVEY_DATA;
		final float width = 310f;
		dialog.showCargoPickerDialog(HSII18nUtil.getCampaignString("HPSIDTurnInSD"),
				HSII18nUtil.getCampaignString("HPSIDConfirmText"), HSII18nUtil.getCampaignString("HPSIDCancelText"),
				true, width, copy,
				new CargoPickerListener() {
					public void pickedCargo(CargoAPI cargo) {
						if (cargo.isEmpty()) {
							cancelledCargoSelection();
							return;
						}
						cargo.sort();
						for (CargoStackAPI stack : cargo.getStacksCopy()) {
							playerCargo.removeItems(stack.getType(), stack.getData(), stack.getSize());
							if (stack.isCommodityStack()) { // should be always, but just in case
								int num = (int) stack.getSize();
								AddRemoveCommodity.addCommodityLossText(stack.getCommodityId(), num, text);

								HSIHPSIDLevelIntel.getInstance()
										.addFactor(new HSIHPSIDTurnedInFactor(t, stack.getCommodityId(),
												HSIHPSIDTurnedInFactor.calculatePoints(t, stack.getCommodityId()) * num,
												num));
							}
						}

						float bounty = computeSurveyDataCreditValue(cargo);
						float repChange = computeSurveyDataReputationValue(cargo);

						if (bounty > 0) {
							playerCargo.getCredits().add(bounty);
							AddRemoveCommodity.addCreditsGainText((int) bounty, text);
							AjimusUtils.setTraitorTrigger();
						}

						if (repChange >= 1f) {
							CustomRepImpact impact = new CustomRepImpact();
							impact.delta = repChange * 0.01f;
							Global.getSector().adjustPlayerReputation(
									new RepActionEnvelope(RepActions.CUSTOM, impact,
											null, text, true),
									faction.getId());

							impact.delta *= 0.25f;
							if (impact.delta >= 0.01f) {
								Global.getSector().adjustPlayerReputation(
										new RepActionEnvelope(RepActions.CUSTOM, impact,
												null, text, true),
										person);
							}
						}

						FireBest.fire(null, dialog, memoryMap, "HSISurveyDataTurnedIn");
					}

					public void cancelledCargoSelection() {
					}

					public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp,
							boolean pickedUpFromSource, CargoAPI combined) {

						float bounty = computeSurveyDataCreditValue(combined);
						float repChange = computeSurveyDataReputationValue(combined);

						float pad = 3f;
						float small = 5f;
						float opad = 10f;

						panel.setParaFontOrbitron();
						panel.addPara(Misc.ucFirst(faction.getDisplayName()), faction.getBaseUIColor(), 1f);
						// panel.addTitle(Misc.ucFirst(faction.getDisplayName()),
						// faction.getBaseUIColor());
						// panel.addPara(faction.getDisplayNameLong(), faction.getBaseUIColor(), opad);
						// panel.addPara(faction.getDisplayName() + " (" + entity.getMarket().getName()
						// + ")", faction.getBaseUIColor(), opad);
						panel.setParaFontDefault();

						panel.addImage(faction.getLogo(), width * 1f, 3f);

						// panel.setParaFontColor(Misc.getGrayColor());
						// panel.setParaSmallInsignia();
						// panel.setParaInsigniaLarge();
						panel.addPara(HSII18nUtil.getCampaignString("HPSIDTurnInSDBounty"),
								opad, Misc.getHighlightColor(), faction.getDisplayNameLongWithArticle());
						panel.beginGridFlipped(width, 1, 40f, 10f);
						// panel.beginGrid(150f, 1);
						panel.addToGrid(0, 0, HSII18nUtil.getCampaignString("HPSIDBountyValue"),
								"" + (int) (surveyValueMult * 100f) + "%");
						panel.addToGrid(0, 1, HSII18nUtil.getCampaignString("HPSIDRepValue"),
								"" + (int) (surveyRepMult * 100f) + "%");
						panel.addGrid(pad);

						panel.addPara(HSII18nUtil.getCampaignString("HPSIDTurnInSDReward"),
								opad * 1f, Misc.getHighlightColor(), faction.getDisplayNameWithArticle(),
								Misc.getWithDGS(bounty) + Strings.C,
								"" + (int) repChange);

						// panel.addPara("Bounty: %s", opad, Misc.getHighlightColor(),
						// Misc.getWithDGS(bounty) + Strings.C);
						// panel.addPara("Reputation: %s", pad, Misc.getHighlightColor(), "+12");
					}
				});
	}

	protected float computeSurveyDataCreditValue(CargoAPI cargo) {
		float bounty = 0;
		for (CargoStackAPI stack : cargo.getStacksCopy()) {
			CommoditySpecAPI spec = stack.getResourceIfResource();
			if (spec != null && spec.getDemandClass().equals(Commodities.SURVEY_DATA)) {
				bounty += spec.getBasePrice() * stack.getSize();
			}
		}
		bounty *= valueMult;
		return bounty;
	}

	protected float computeSurveyDataReputationValue(CargoAPI cargo) {
		float rep = 0;
		for (CargoStackAPI stack : cargo.getStacksCopy()) {
			CommoditySpecAPI spec = stack.getResourceIfResource();
			if (spec != null && spec.getDemandClass().equals(Commodities.SURVEY_DATA)) {
				rep += getBaseSDRepValue(spec.getId()) * stack.getSize();
			}
		}
		rep *= repMult;
		// if (rep < 1f) rep = 1f;
		return rep;
	}

	protected int getBaseSDRepValue(String id) {
		switch (id) {
			case Commodities.SURVEY_DATA_1:
				return 2;
			case Commodities.SURVEY_DATA_2:
				return 4;
			case Commodities.SURVEY_DATA_3:
				return 6;
			case Commodities.SURVEY_DATA_4:
				return 8;
			case Commodities.SURVEY_DATA_5:
				return 10;
			default:
				break;
		}
		return 0;
	}

	protected void selectCores() {
		CargoAPI copy = Global.getFactory().createCargo(false);
		// copy.addAll(cargo);
		// copy.setOrigSource(playerCargo);
		for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
			CommoditySpecAPI spec = stack.getResourceIfResource();
			if (spec != null && spec.getDemandClass().equals(Commodities.AI_CORES)) {
				copy.addFromStack(stack);
			}
		}
		copy.sort();

		final float width = 310f;
		dialog.showCargoPickerDialog(HSII18nUtil.getCampaignString("HPSIDTurnInCore"),
				HSII18nUtil.getCampaignString("HPSIDConfirmText"), HSII18nUtil.getCampaignString("HPSIDCancelText"),
				true, width, copy,
				new CargoPickerListener() {
					public void pickedCargo(CargoAPI cargo) {
						if (cargo.isEmpty()) {
							cancelledCargoSelection();
							return;
						}
						cargo.sort();
						final Type t = Type.AI_CORE;
						for (CargoStackAPI stack : cargo.getStacksCopy()) {
							playerCargo.removeItems(stack.getType(), stack.getData(), stack.getSize());
							if (stack.isCommodityStack()) { // should be always, but just in case
								int num = (int) stack.getSize();
								AddRemoveCommodity.addCommodityLossText(stack.getCommodityId(), num, text);

								String key = "$turnedIn_" + stack.getCommodityId();
								int turnedIn = faction.getMemoryWithoutUpdate().getInt(key);
								faction.getMemoryWithoutUpdate().set(key, turnedIn + num);

								// Also, total of all cores! -dgb
								String key2 = "$turnedIn_allCores";
								int turnedIn2 = faction.getMemoryWithoutUpdate().getInt(key2);
								faction.getMemoryWithoutUpdate().set(key2, turnedIn2 + num);

								HSIHPSIDLevelIntel.getInstance()
										.addFactor(new HSIHPSIDTurnedInFactor(t, stack.getCommodityId(),
												HSIHPSIDTurnedInFactor.calculatePoints(t, stack.getCommodityId()) * num,
												num));
							}
						}

						float bounty = computeCoreCreditValue(cargo);
						float repChange = computeCoreReputationValue(cargo);

						if (bounty > 0) {
							playerCargo.getCredits().add(bounty);
							AddRemoveCommodity.addCreditsGainText((int) bounty, text);
							AjimusUtils.setTraitorTrigger();
						}

						if (repChange >= 1f) {
							CustomRepImpact impact = new CustomRepImpact();
							impact.delta = repChange * 0.01f;
							Global.getSector().adjustPlayerReputation(
									new RepActionEnvelope(RepActions.CUSTOM, impact,
											null, text, true),
									faction.getId());

							impact.delta *= 0.25f;
							if (impact.delta >= 0.01f) {
								Global.getSector().adjustPlayerReputation(
										new RepActionEnvelope(RepActions.CUSTOM, impact,
												null, text, true),
										person);
							}
						}

						FireBest.fire(null, dialog, memoryMap, "HSIAICoresTurnedIn");
					}

					public void cancelledCargoSelection() {
					}

					public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp,
							boolean pickedUpFromSource, CargoAPI combined) {

						float bounty = computeCoreCreditValue(combined);
						float repChange = computeCoreReputationValue(combined);

						float pad = 3f;
						float small = 5f;
						float opad = 10f;

						panel.setParaFontOrbitron();
						panel.addPara(Misc.ucFirst(faction.getDisplayName()), faction.getBaseUIColor(), 1f);
						// panel.addTitle(Misc.ucFirst(faction.getDisplayName()),
						// faction.getBaseUIColor());
						// panel.addPara(faction.getDisplayNameLong(), faction.getBaseUIColor(), opad);
						// panel.addPara(faction.getDisplayName() + " (" + entity.getMarket().getName()
						// + ")", faction.getBaseUIColor(), opad);
						panel.setParaFontDefault();

						panel.addImage(faction.getLogo(), width * 1f, 3f);

						// panel.setParaFontColor(Misc.getGrayColor());
						// panel.setParaSmallInsignia();
						// panel.setParaInsigniaLarge();
						panel.addPara(HSII18nUtil.getCampaignString("HPSIDTurnInCoreBounty"),
								opad, Misc.getHighlightColor(), faction.getDisplayNameLongWithArticle());
						panel.beginGridFlipped(width, 1, 40f, 10f);
						// panel.beginGrid(150f, 1);
						panel.addToGrid(0, 0, HSII18nUtil.getCampaignString("HPSIDBountyValue"),
								"" + (int) (surveyValueMult * 100f) + "%");
						panel.addToGrid(0, 1, HSII18nUtil.getCampaignString("HPSIDRepValue"),
								"" + (int) (surveyRepMult * 100f) + "%");
						panel.addGrid(pad);

						panel.addPara(HSII18nUtil.getCampaignString("HPSIDTurnInCoreReward"),
								opad * 1f, Misc.getHighlightColor(), faction.getDisplayNameWithArticle(),
								Misc.getWithDGS(bounty) + Strings.C,
								"" + (int) repChange);

						// panel.addPara("Bounty: %s", opad, Misc.getHighlightColor(),
						// Misc.getWithDGS(bounty) + Strings.C);
						// panel.addPara("Reputation: %s", pad, Misc.getHighlightColor(), "+12");
					}
				});
	}

	protected float computeCoreCreditValue(CargoAPI cargo) {
		float bounty = 0;
		for (CargoStackAPI stack : cargo.getStacksCopy()) {
			CommoditySpecAPI spec = stack.getResourceIfResource();
			if (spec != null && spec.getDemandClass().equals(Commodities.AI_CORES)) {
				bounty += spec.getBasePrice() * stack.getSize();
			}
		}
		bounty *= valueMult;
		return bounty;
	}

	protected float computeCoreReputationValue(CargoAPI cargo) {
		float rep = 0;
		for (CargoStackAPI stack : cargo.getStacksCopy()) {
			CommoditySpecAPI spec = stack.getResourceIfResource();
			if (spec != null && spec.getDemandClass().equals(Commodities.AI_CORES)) {
				rep += getBaseCoreRepValue(spec.getId()) * stack.getSize();
			}
		}
		rep *= repMult;
		// if (rep < 1f) rep = 1f;
		return rep;
	}

	public static float getBaseCoreRepValue(String coreType) {
		if (Commodities.OMEGA_CORE.equals(coreType)) {
			return 30f;
		}
		if (Commodities.ALPHA_CORE.equals(coreType)) {
			return 12f;
		}
		if (Commodities.BETA_CORE.equals(coreType)) {
			return 7f;
		}
		if (Commodities.GAMMA_CORE.equals(coreType)) {
			return 2f;
		}
		return 1f;
	}

	protected void selectRareShip() {
		List<FleetMemberAPI> rareships = getRareShip();
		int total = rareships.size();
		int rows = (int) (total / 4) + 1;
		int cols = 4;
		dialog.showFleetMemberPickerDialog(HSII18nUtil.getCampaignString("HPSIDTurnInRS"),
				HSII18nUtil.getCampaignString("HPSIDConfirmText"), HSII18nUtil.getCampaignString("HPSIDCancelText"),
				rows, cols, 150f, true, true, rareships,
				new FleetMemberPickerListener() {
					public void pickedFleetMembers(List<FleetMemberAPI> members) {
						if (members.isEmpty())
							return;
						pickedShips(members);
						FireAll.fire(null, dialog, memoryMap, "HSITurnedInRareShip");
					}

					public void cancelledFleetMemberPicking() {

					}
				});
	}

	protected List<FleetMemberAPI> getRareShip() {
		List<FleetMemberAPI> rareships = new ArrayList<>(1);
		for (FleetMemberAPI m : playerFleet.getMembersWithFightersCopy()) {
			if (m.isFighterWing())
				continue;
			if (((m.getHullSpec().getHints().contains(ShipTypeHints.UNBOARDABLE)
					|| m.getHullSpec().getHints().contains(ShipTypeHints.HIDE_IN_CODEX))
					&&( m.getHullSpec().getTags().contains(Tags.RESTRICTED))||m.getHullSpec().hasTag(Tags.NO_SELL)||m.getHullSpec().hasTag(Tags.NO_DROP))
			||m.getHullSpec().hasTag(Tags.DWELLER)||m.getHullSpec().hasTag(Tags.OMEGA)||m.getHullSpec().hasTag(Tags.THREAT)||((m.getHullSpec().hasTag(Tags.CODEX_UNLOCKABLE))&&!m.getHullSpec().hasTag(Tags.AUTOMATED_RECOVERABLE)))
				rareships.add(m);
		}
		return rareships;
	}

	protected void pickedShips(List<FleetMemberAPI> members) {
		float bounty = 0;
		float repChange = 0;
		final Type t = Type.SPECIAL_SHIP;
		for (FleetMemberAPI m : members) {
			float price = m.getBaseValue();
			bounty += price;
			float rep = Math.min(m.getHullSpec().getHullSize().ordinal() * 10, price / 1000);
			repChange += rep;
			playerFleet.getFleetData().removeFleetMember(m);
			AddRemoveCommodity.addFleetMemberLossText(m, text);
			HSIHPSIDLevelIntel.getInstance()
					.addFactor(new HSIHPSIDTurnedInFactor(t, m.getHullSpec().getBaseHullId(),
							HSIHPSIDTurnedInFactor.calculatePoints(t, m.getHullSpec().getBaseHullId()), 1));
		}
		if (bounty > 0) {
			playerCargo.getCredits().add(bounty);
			AddRemoveCommodity.addCreditsGainText((int) bounty, text);
			AjimusUtils.setTraitorTrigger();
		}

		if (repChange >= 1f) {
			CustomRepImpact impact = new CustomRepImpact();
			impact.delta = repChange * 0.01f;
			Global.getSector().adjustPlayerReputation(
					new RepActionEnvelope(RepActions.CUSTOM, impact,
							null, text, true),
					faction.getId());

			impact.delta *= 0.25f;
			if (impact.delta >= 0.01f) {
				Global.getSector().adjustPlayerReputation(
						new RepActionEnvelope(RepActions.CUSTOM, impact,
								null, text, true),
						person);
			}
			PersonAPI commsioner = Global.getSector().getImportantPeople().getPerson(HSIIds.PERSON.THE_COMMISSIONER);
			impact.delta *= 0.5f;
			if (impact.delta >= 0.01f) {
				Global.getSector().adjustPlayerReputation(
						new RepActionEnvelope(RepActions.CUSTOM, impact,
								null, text, true),
						commsioner);
			}
		}

	}

}
