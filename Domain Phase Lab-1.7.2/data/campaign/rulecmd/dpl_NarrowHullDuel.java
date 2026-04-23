package data.campaign.rulecmd;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEntityPickerListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.listeners.ListenerUtil;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.GateEntityPlugin;
import com.fs.starfarer.api.impl.campaign.GateExplosionScript;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

import data.scripts.campaign.dpl_relay_explosion_plugin;


public class dpl_NarrowHullDuel extends BaseCommandPlugin {
	
	protected MemoryAPI memory;
	protected InteractionDialogAPI dialog;
	protected TextPanelAPI text;
	protected Map<String, MemoryAPI> memoryMap;
	protected int YourPos;
	protected int YourHealth;
	protected int BossPos;
	protected int BossHealth;
	protected String CurrStageName;
	
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		
		if (dialog == null) return false;
	      
	    String action = params.get(0).getString(memoryMap);
			
		memory = memoryMap.get(MemKeys.LOCAL);
		if (memory == null) return false;
		
		text = dialog.getTextPanel();
		
		if ("initialize".equals(action)) {
			initialize();
		} else if ("showImage".equals(action)) {
			
			String category = "illustrations";
			String key = (String) memory.get("$dpl_NHDuel_CurrState");
			SpriteAPI sprite = Global.getSettings().getSprite(category, key);
			dialog.getVisualPanel().showImagePortion(category, key, sprite.getWidth(), sprite.getHeight(), 0, 0, 480, 300);
			return true;
			
		} else if ("check_won".equals(action)) {
			return check_won();
		} else if ("check_lost".equals(action)) {
			return check_lost();
		} else if ("can_move_fw".equals(action)) {
			return can_move_fw();
		} else if ("can_dash_fw".equals(action)) {
			return can_dash_fw();
		} else if ("can_move_bw".equals(action)) {
			return can_move_bw();
		} else if ("can_dash_bw".equals(action)) {
			return can_dash_bw();
		} else if ("can_use_pistol".equals(action)) {
			return can_use_pistol();
		} else if ("can_use_hammer".equals(action)) {
			return can_use_hammer();
		} else if ("can_power_dash".equals(action)) {
			return can_power_dash();
		} else if ("move_fw".equals(action)) {
			move_fw();
		} else if ("dash_fw".equals(action)) {
			dash_fw();
		} else if ("move_bw".equals(action)) {
			move_bw();
		} else if ("dash_bw".equals(action)) {
			dash_bw();
		} else if ("use_pistol".equals(action)) {
			use_pistol();
		} else if ("use_hammer".equals(action)) {
			use_hammer();
		} else if ("power_dash".equals(action)) {
			power_dash();
		} else if ("enemy_move".equals(action)) {
			enemy_move();
		}
		return false;
	}
	
	public void initialize() {
		YourPos = 0;
		BossPos = 4;
		YourHealth = 10;
		BossHealth = 12;
		CurrStageName = "dpl_NarrowHullDuel_04";
		memory.set("$dpl_NHDuel_CurrState",CurrStageName, 0);
		memory.set("$YourPos", YourPos, 0);
		memory.set("$BossPos", BossPos, 0);
		memory.set("$YourHealth", YourHealth, 0);
		memory.set("$BossHealth", BossHealth, 0);
	}
	
	//Whether you can win on the next move.
	public boolean check_won() {
		YourPos = memory.getInt("$YourPos");
		BossPos = memory.getInt("$BossPos");
		YourHealth = memory.getInt("$YourHealth");
		BossHealth = memory.getInt("$BossHealth");
		boolean WinByHammer = (BossPos-YourPos <= 2) && (BossHealth <= 2);
		boolean WinByDash = (BossPos-YourPos == 2) && (BossPos == 4);
		boolean WinByPistol = (BossHealth <= 1);
		boolean AlreadyLost = check_lost();
		if (!AlreadyLost) {
			return (WinByHammer || WinByDash || WinByPistol);
		}
		return false;
	}
	
	//Whether you are already lost at the current stage.
	public boolean check_lost() {
		YourPos = memory.getInt("$YourPos");
		YourHealth = memory.getInt("$YourHealth");
		boolean LostByHealth = (YourHealth < 0);
		boolean LostByPosition = (YourPos < 0);
		return (LostByHealth || LostByPosition);
	}
	
	//Whether you can move forward.
	public boolean can_move_fw() {
		YourPos = memory.getInt("$YourPos");
		BossPos = memory.getInt("$BossPos");
		boolean CanMoveFW = (BossPos-YourPos > 1);
		return (CanMoveFW);
	}
	
	//Whether you can dash forward.
	public boolean can_dash_fw() {
		YourPos = memory.getInt("$YourPos");
		BossPos = memory.getInt("$BossPos");
		boolean CanDashFW = (BossPos-YourPos > 2);
		return (CanDashFW);
	}
	
	//Whether you can move backward.
	public boolean can_move_bw() {
		YourPos = memory.getInt("$YourPos");
		BossPos = memory.getInt("$BossPos");
		boolean CanMoveBW = (YourPos >= 1);
		return (CanMoveBW);
	}
	
	//Whether you can dash backward.
	public boolean can_dash_bw() {
		YourPos = memory.getInt("$YourPos");
		BossPos = memory.getInt("$BossPos");
		boolean CanDashBW = (YourPos >= 2);
		return (CanDashBW);
	}
	
	//Whether you can use pistol.
	public boolean can_use_pistol() {
		return true;
	}
	
	//Whether you can use hammer.
	public boolean can_use_hammer() {
		YourPos = memory.getInt("$YourPos");
		BossPos = memory.getInt("$BossPos");
		boolean CanUseHammer = (BossPos-YourPos == 1);
		return (CanUseHammer);
	}
	
	//Whether you can use power dash.
	public boolean can_power_dash() {
		YourPos = memory.getInt("$YourPos");
		BossPos = memory.getInt("$BossPos");
		boolean CanPowerDash = (BossPos-YourPos == 2);
		return (CanPowerDash);
	}
	
	//Move Forward.
	public void move_fw() {
		YourPos = memory.getInt("$YourPos");
		YourPos = YourPos + 1;
		memory.set("$YourPos", YourPos, 0);
	}
	
	//Dash Forward.
	public void dash_fw() {
		YourPos = memory.getInt("$YourPos");
		YourPos = YourPos + 2;
		memory.set("$YourPos", YourPos, 0);
	}
	
	//Move Backward.
	public void move_bw() {
		YourPos = memory.getInt("$YourPos");
		YourPos = YourPos - 1;
		memory.set("$YourPos", YourPos, 0);
	}
	
	//Dash Backward.
	public void dash_bw() {
		YourPos = memory.getInt("$YourPos");
		YourPos = YourPos - 2;
		memory.set("$YourPos", YourPos, 0);
	}
	
	//Use Pistol.
	public void use_pistol() {
		BossHealth = memory.getInt("$BossHealth");
		BossHealth = BossHealth - 2;
		memory.set("$BossHealth", BossHealth, 0);
	}
	
	//Use Hammer.
	public void use_hammer() {
		BossHealth = memory.getInt("$BossHealth");
		BossHealth = BossHealth - 3;
		memory.set("$BossHealth", BossHealth, 0);
	}
	
	//Power Dash.
	public void power_dash() {
		YourPos = memory.getInt("$YourPos");
		BossPos = memory.getInt("$BossPos");
		BossHealth = memory.getInt("$BossHealth");
		YourPos = YourPos+2;
		BossPos = BossPos+1;
		BossHealth = BossHealth - 3;
		memory.set("$YourPos", YourPos, 0);
		memory.set("$BossPos", BossPos, 0);
		memory.set("$BossHealth", BossHealth, 0);
	}
	
	//Enemy does the move, and updates interaction dialogue.
	public void enemy_move() {
		YourPos = memory.getInt("$YourPos");
		BossPos = memory.getInt("$BossPos");
		YourHealth = memory.getInt("$YourHealth");
		BossHealth = memory.getInt("$BossHealth");
		
		boolean BossDoesDamage = (BossPos-YourPos == 1);
		BossPos = BossPos - 1;
		if (BossDoesDamage) {
			YourPos = YourPos - 1;
			YourHealth = YourHealth -5;
		}
		
		int ind0 = Math.max(YourPos, 0);
		int ind1 = Math.max(BossPos, 1);
		
		CurrStageName = "dpl_NarrowHullDuel_" + String.valueOf(ind0) + String.valueOf(ind1);
		memory.set("$dpl_NHDuel_CurrState",CurrStageName, 0);
		memory.set("$YourPos", YourPos, 0);
		memory.set("$BossPos", BossPos, 0);
		memory.set("$YourHealth", YourHealth, 0);
		memory.set("$BossHealth", BossHealth, 0);
		
		text.addPara("The Robot moves 1 meter ahead.");
		
		if (YourPos < 0) {
			text.addPara("You are smashed into wall.");
			YourHealth = -1;
		}
		
		if (YourHealth <= 10 && 5 < YourHealth) {
			text.addPara("You can take 3 more hits.");
		} else if (YourHealth <= 5 && 1 < YourHealth) {
			text.addPara("The robot hits you. You can take 2 more hits.");
		} else if (YourHealth <= 1 && 0 <= YourHealth) {
			text.addPara("The robot hits you. You can take 1 more hit.");
		} else if (YourHealth < 0) {
			text.addPara("You fall unconscious.");
		}
		
		if (BossHealth <= 12 && 10 < BossHealth) {
			text.addPara("The Robot needs damage of 6 more Gauss Pistol shots.");
		} else if (BossHealth <= 10 && 8 < BossHealth) {
			text.addPara("The Robot needs damage of 5 more Gauss Pistol shots.");
		} else if (BossHealth <= 8 && 6 < BossHealth) {
			text.addPara("The Robot needs damage of 4 more Gauss Pistol shots.");
		} else if (BossHealth <= 6 && 4 < BossHealth) {
			text.addPara("The Robot needs damage of 3 more Gauss Pistol shots.");
		} else if (BossHealth <= 4 && 2 < BossHealth) {
			text.addPara("The Robot needs damage of 2 more Gauss Pistol shots.");
		} else if (BossHealth <= 2 && 0 < BossHealth) {
			text.addPara("The Robot needs damage of 1 more Gauss Pistol shots, plus a slight damage to finish him.");
		} else if (BossHealth == 0) {
			text.addPara("You just need to do a slight more damage to finish the robot.");
		}
		
	}
	
}

