package SOTFECO.combatObj;

import com.fs.starfarer.campaign.fleet.FleetMember;
import com.fs.starfarer.combat.CombatFleetManager;
import com.fs.starfarer.combat.entities.Ship;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class SOTFECO_theOneJavaLine {
    public static MethodHandle getConstructor() throws NoSuchMethodException, IllegalAccessException {
        return MethodHandles.lookup().findConstructor(CombatFleetManager.class.getClasses()[2], MethodType.methodType(void.class, FleetMember.class, Ship.class));
    }
}
