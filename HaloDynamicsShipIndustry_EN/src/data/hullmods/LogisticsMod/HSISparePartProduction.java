package data.hullmods.LogisticsMod;

import java.util.List;

import com.fs.starfarer.api.campaign.BuffManagerAPI.Buff;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.TimeoutTracker;

public class HSISparePartProduction extends HSIBaseLogisticsMod {
	protected static final String KEY = "HSI_SparePartProduction";

	public static class HSISparePartProductionBuff implements Buff {
		// public boolean wasApplied = false;
		private String buffId = KEY;
		private int frames = 0;
		private TimeoutTracker<String> applyedSource = new TimeoutTracker<>();
		private float elapsed = 0;

		public boolean isExpired() {
			return frames >= 2;
		}

		public String getId() {
			return KEY;
		}

		public void apply(FleetMemberAPI member) {
			member.getStats().getCRPerDeploymentPercent().modifyMult(buffId, getTotal());
		}

		protected float getTotal(){
			return 1f-(Math.min(applyedSource.getItems().size()*0.2f,0.5f));
		}

		public void advance(float days) {
			frames++;
			applyedSource.advance(days);
			elapsed += days;
			if (elapsed >= 1) {
				elapsed -= 1;
			}
		}

		public void applyToBuff(FleetMemberAPI member) {
			if (applyedSource.contains(member.getId())) {
				applyedSource.set(member.getId(), 2);
			} else {
				applyedSource.add(member.getId(), 2);
			}
			frames = 0;
		}
	};

	@Override
	public void advanceInCampaign(FleetMemberAPI member, float amount) {
		if(member!=null&&member.getFleetData()!=null){
			for(FleetMemberAPI m:member.getFleetData().getMembersListCopy()){
				if(m.isFighterWing()) continue;
				if(m.isMothballed()) continue;
				if(m.getBuffManager()!=null){
					if(m.getBuffManager().getBuff(KEY)!=null){
						((HSISparePartProductionBuff)m.getBuffManager().getBuff(KEY)).applyToBuff(member);
					}else{
						HSISparePartProductionBuff b = new HSISparePartProductionBuff();
						b.applyToBuff(member);
						m.getBuffManager().addBuff(b);
					}
				}
			}
		}
	}

	@Override
	public int getNumForLimitedMod(ShipHullSpecAPI spec, String mod) {
		if (mod.contentEquals("HSI_CombatAutomateRepairSystem")) {
			return 2;
		}
		return super.getNumForLimitedMod(spec, mod);
	}

	@Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) {
			if (hullSize == HullSize.CAPITAL_SHIP) {
				return "20%";
			}
			return "10%";
		}
		if (index == 1) {
			return "50%";
		}
		if (index == 2) {
			if (hullSize == HullSize.CAPITAL_SHIP) {
				return "+2";
			}
			return "+1";
		}
		return null;
	}
}
