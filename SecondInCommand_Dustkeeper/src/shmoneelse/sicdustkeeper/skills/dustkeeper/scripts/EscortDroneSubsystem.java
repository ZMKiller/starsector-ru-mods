package shmoneelse.sicdustkeeper.skills.dustkeeper.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.combat.entities.Ship;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.subsystems.drones.MagicDroneSubsystem;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;
import second_in_command.specs.SCOfficer;
import com.fs.starfarer.api.characters.PersonAPI;

import java.util.List;

public class EscortDroneSubsystem extends MagicDroneSubsystem {
    int numDrones = 0;
    List<SCBaseSkillPlugin> activeSkills;
    SCData d;

    public EscortDroneSubsystem(ShipAPI ship, SCData data) {
        super(ship);
        d = data;

        /*List<SCOfficer> activeOfficers = data.getActiveOfficers();
        for(SCOfficer officer : activeOfficers)
        {
            if(officer != null && !officer.getActiveSkillPlugins().isEmpty())
                activeSkills.addAll(officer.getActiveSkillPlugins()); // Populate all the effects we'll have to apply on spawning the drone
        }*/
        activeSkills = d.getAllActiveSkillsPlugins();

        if(!ship.isStationModule() && !ship.isFighter()) {
            if(Misc.isAutomated(ship)) {
                if (ship.isFrigate())
                    numDrones = 1;
                else if (ship.isDestroyer())
                    numDrones = 3;
                else if (ship.isCruiser())
                    numDrones = 5;
                else if (ship.isCapital())
                    numDrones = 7;
                else if (ship.isStation())
                    numDrones = 9;
            }
            else
            {
                if (ship.isFrigate())
                    numDrones = 1;
                else if (ship.isDestroyer())
                    numDrones = 2;
                else if (ship.isCruiser())
                    numDrones = 3;
                else if (ship.isCapital())
                    numDrones = 4;
                else if (ship.isStation())
                    numDrones = 5;
            }
        }
        setDronesToSpawn(numDrones);
    }

    @Override
    public @NotNull ShipAPI spawnDrone()
    {
        ShipAPI fighter = super.spawnDrone();
        // Now, we need to apply the skills that should be applied. We know we're using SIC so we can just check those skill implementations.
        // A similar technique could be used to get hullmod effects off the parent ship as well, in theory.
        for(SCBaseSkillPlugin plugin : activeSkills)
            plugin.applyEffectsToFighterSpawnedByShip(d, fighter, ship, plugin.getId());

        return fighter;
    }

    @Override
    public boolean canAssignKey() {
        return false;
    }

    @Override
    public float getBaseActiveDuration() {
        return 0;
    }

    @Override
    public float getBaseCooldownDuration() {
        return 2f;
    }

    @Override
    public boolean shouldActivateAI(float amount) {
        return canActivate();
    }

    /*@Override
    public float getBaseChargeRechargeDuration() {
        return 0f;
    }*/ // Not needed, identical to super

    @Override
    public boolean canActivate() {
        return false;
    }

    @Override
    public String getDisplayText() {
        return "Escort Drones";
    }

    @Override
    public String getStateText() {
        return "";
    }

    @Override
    public float getBarFill() {
        float fill = 0f;
        if (charges < calcMaxCharges()) {
            fill = chargeInterval.getElapsed() / chargeInterval.getIntervalDuration();
        }

        return fill;
    }

    @Override
    public int getMaxCharges() {
        return 0;
    }

    @Override
    public int getMaxDeployedDrones() {
        return numDrones;
    }

    @Override
    public float  getDroneCreationTime() {
        return 20f;
    }

    @Override
    public boolean hasSeparateDroneCharges() {
        return true;
    }


    @Override
    public boolean usesChargesOnActivate() {
        return false;
    }

    @Override
    public @NotNull String getDroneVariant() {
        return "sotf_brattice_single";
    }

    @Override
    public void drawHUDBar(@NotNull ViewportAPI viewport, @NotNull Vector2f rootLoc, @NotNull Vector2f barLoc, boolean displayAdditionalInfo, float longestNameWidth) {
        // We don't need the subsystem information to be shown
    }

    /*@Override
    public @NotNull DroneFormation getDroneFormation()
    {
        return new HoveringFormation();
    }*/
}
