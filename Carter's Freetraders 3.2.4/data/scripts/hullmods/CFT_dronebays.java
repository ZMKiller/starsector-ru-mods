package data.scripts.hullmods;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.FighterOPCostModifier;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;

public class CFT_dronebays extends BaseHullMod 
{
   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      stats.removeListenerOfClass(CFT_dronebays.CftDronebaysListener.class);
      stats.addListener(new CFT_dronebays.CftDronebaysListener());
   }

   public boolean affectsOPCosts() {
      return true;
   }

   public static class CftDronebaysListener implements FighterOPCostModifier {
      public int getFighterOPCost(MutableShipStatsAPI stats, FighterWingSpecAPI fighter, int currCost) {
         return (fighter.hasTag("auto_fighter") || fighter.hasTag("drone")) && currCost <= 90 ? currCost : 99999;
      }
   }
}