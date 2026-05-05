package data.campaign.HPSID;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class HSIHPSIDMonthlyRewards {

    public static WeightedRandomPicker<CargoStackAPI> getTierPicker(int tier) {
        WeightedRandomPicker<CargoStackAPI> pool = new WeightedRandomPicker<>();
        switch (tier) {
            case 0:
                addToPool(CargoItemType.RESOURCES, Commodities.SUPPLIES, pool, 40).setSize(10);
                addToPool(CargoItemType.RESOURCES, Commodities.FUEL, pool, 10).setSize(10);
                // addToPool(CargoItemType.RESOURCES, Commodities.FUEL, pool);
                break;
            case 1:
                addToPool(CargoItemType.WEAPONS, "HWI_MassDriverS", pool, 15).setSize(3);
                addToPool(CargoItemType.WEAPONS, "HWI_MassDriverM", pool);
                addToPool(CargoItemType.FIGHTER_CHIP, "HSI_Maid_wing", pool);
                addToPool(CargoItemType.WEAPONS, "HWI_Stella", pool, 15).setSize(2);
                addToPool(CargoItemType.WEAPONS, "HWI_Duality", pool, 5);
                addToPool(CargoItemType.WEAPONS, "HWI_IonLance", pool).setSize(2);
                addToPool(CargoItemType.WEAPONS, "reaper", pool, 10).setSize(2);
                addToPool(CargoItemType.WEAPONS, "HWI_TurbocannonM", pool, 3);
                addToPool(CargoItemType.WEAPONS, "HWI_TurbocannonS", pool, 3);
                break;
            case 2:
                addToPool(CargoItemType.FIGHTER_CHIP, "HSI_Flanker_wing", pool);
                addToPool(CargoItemType.FIGHTER_CHIP, "HSI_Mayfly_wing", pool);
                addToPool(CargoItemType.FIGHTER_CHIP, "HSI_Apocalyse_wing", pool, 1);
                addToPool(CargoItemType.FIGHTER_CHIP, "HSI_Thor_wing", pool, 2);
                addToPool(CargoItemType.WEAPONS, "HWI_MassDriverL", pool, 5);
                addToPool(CargoItemType.WEAPONS, "HWI_TurbocannonL", pool, 3);
                addToPool(CargoItemType.WEAPONS, "HWI_IonLanceM", pool, 7);
                addToPool(CargoItemType.WEAPONS, "HWI_Arbiter", pool, 3);
                addToPool(CargoItemType.WEAPONS, "HWI_FocusLance", pool, 2);
                break;
            case 3:
                addToPool(CargoItemType.SPECIAL, new SpecialItemData(Items.CORRUPTED_NANOFORGE, null), pool, 3);
                addToPool(CargoItemType.SPECIAL, new SpecialItemData(Items.PRISTINE_NANOFORGE, null), pool, 1);
                addToPool(CargoItemType.SPECIAL, new SpecialItemData(Items.SOIL_NANITES, null), pool,1);
                addToPool(CargoItemType.SPECIAL, new SpecialItemData(Items.BIOFACTORY_EMBRYO, null), pool,1);
                addToPool(CargoItemType.SPECIAL, new SpecialItemData(Items.CATALYTIC_CORE, null), pool,2);
                addToPool(CargoItemType.SPECIAL, new SpecialItemData(Items.SYNCHROTRON, null), pool,3);
                addToPool(CargoItemType.SPECIAL, new SpecialItemData(Items.ORBITAL_FUSION_LAMP, null), pool,6);
                addToPool(CargoItemType.SPECIAL, new SpecialItemData(Items.PLASMA_DYNAMO, null), pool,1);
                break;
            default:
                break;
        }
        return pool;
    }

    public static CargoStackAPI addToPool(CargoItemType type, Object data, WeightedRandomPicker<CargoStackAPI> pool) {
        return addToPool(type, data, pool, 10);
    }

    public static CargoStackAPI addToPool(CargoItemType type, Object data, WeightedRandomPicker<CargoStackAPI> pool,
            float weight) {
        CargoStackAPI stack = Global.getFactory().createCargoStack(type, data, null);
        stack.setSize(1);
        pool.add(stack, weight);
        return stack;
    }

}
