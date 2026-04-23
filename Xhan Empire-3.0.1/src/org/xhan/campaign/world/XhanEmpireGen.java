package org.xhan.campaign.world;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import org.xhan.campaign.world.systems.PamedSystem;
import org.xhan.campaign.world.systems.XhanSystem;

import static com.fs.starfarer.api.impl.campaign.ids.Factions.*;
import static com.fs.starfarer.api.impl.campaign.shared.SharedData.getData;
import static org.xhan.XhanEmpireModPlugin.PAMED_FACTION_ID;
import static org.xhan.XhanEmpireModPlugin.XHAN_FACTION_ID;

public class XhanEmpireGen implements SectorGeneratorPlugin {

    @Override
    public void generate(SectorAPI sector) {

        new XhanSystem().generate(sector);
        new PamedSystem().generate(sector);

        getData().getPersonBountyEventData().addParticipatingFaction(XHAN_FACTION_ID);

        FactionAPI xhan = sector.getFaction(XHAN_FACTION_ID);
        FactionAPI pamed = sector.getFaction(PAMED_FACTION_ID);

        // yes it's a huge fuckin drag but these should match your exerelin reps... so you'll need to update both at the same time when you change them

        // Vanilla factions
        xhan.setRelationship(TRITACHYON, 0f);

        xhan.setRelationship(REMNANTS, -0.6f);
        xhan.setRelationship(PIRATES, -0.6f);
        xhan.setRelationship(PERSEAN, -0.6f);
        xhan.setRelationship(HEGEMONY, -0.6f);
        xhan.setRelationship(LIONS_GUARD, -0.6f);
        xhan.setRelationship(DIKTAT, -0.6f);
        xhan.setRelationship(LUDDIC_PATH, -0.8f);
        xhan.setRelationship(INDEPENDENT, -0.15f);
        xhan.setRelationship(PLAYER, -0.15f);

        //Pamed Vanilla
        pamed.setRelationship(TRITACHYON, 0.6f);

        pamed.setRelationship(REMNANTS, -0.6f);
        pamed.setRelationship(PIRATES, -0.25f);
        pamed.setRelationship(PERSEAN, -0.20f);
        pamed.setRelationship(HEGEMONY, 0.25f);
        pamed.setRelationship(LIONS_GUARD, 0.35f);
        pamed.setRelationship(DIKTAT, 0.35f);
        pamed.setRelationship(LUDDIC_PATH, -0.45f);
        pamed.setRelationship(INDEPENDENT, 0.35f);
        pamed.setRelationship(PLAYER, 0f);

        // Mod factions
        xhan.setRelationship("sylphon", 0f);

        xhan.setRelationship("Coalition", -0.15f);
        xhan.setRelationship("tiandong", -0.15f);
        xhan.setRelationship("kadur_remnant", -0.6f);
        xhan.setRelationship("blackrock_driveyards", -0.6f);
        xhan.setRelationship("interstellarimperium", -0.6f);

        xhan.setRelationship("united_security", -0.35f);
        xhan.setRelationship("exigency", -0.35f);
        xhan.setRelationship("mess", -0.35f);
        xhan.setRelationship("HMI", -0.6f);
        xhan.setRelationship("pack", -0.35f);
        xhan.setRelationship("syndicate_asp", -0.35f);
        xhan.setRelationship("syndicate_asp_familia", -0.35f);
        xhan.setRelationship("al_ars", -0.35f);
        xhan.setRelationship("OCI", -0.35f);
        xhan.setRelationship("GKSec", -0.35f);
        xhan.setRelationship("mayorate", -0.35f);
        xhan.setRelationship("metelson", -0.35f);
        xhan.setRelationship("SCY", -0.6f);
        xhan.setRelationship("shadow_industry", -0.6f);
        xhan.setRelationship("roider", -0.6f);

        xhan.setRelationship("blade_breakers", -0.6f);
        xhan.setRelationship("dassault_mikoyan", -0.6f);
        xhan.setRelationship("diableavionics", -0.6f);
        xhan.setRelationship("exipirated", -0.6f);
        xhan.setRelationship("gmda", -0.6f);
        xhan.setRelationship("gmda_patrol", -0.6f);
        xhan.setRelationship("draco", -0.6f);
        xhan.setRelationship("fang", -0.6f);
        xhan.setRelationship("junk_pirates", -0.6f);
        xhan.setRelationship("junk_pirates_hounds", -0.6f);
        xhan.setRelationship("junk_pirates_junkboys", -0.6f);
        xhan.setRelationship("junk_pirates_technicians", -0.6f);
        xhan.setRelationship("ORA", -0.6f);
        xhan.setRelationship("the_cartel", -0.6f);
        xhan.setRelationship("nullorder", -0.6f);
        xhan.setRelationship("templars", -0.6f);
        xhan.setRelationship("crystanite_pir", -0.6f);
        xhan.setRelationship("infected", -0.6f);
        xhan.setRelationship("new_galactic_order", -0.6f);
        xhan.setRelationship("TF7070_D3C4", -0.6f);
        xhan.setRelationship("minor_pirate_1", -0.6f);
        xhan.setRelationship("minor_pirate_2", -0.6f);
        xhan.setRelationship("minor_pirate_3", -0.6f);
        xhan.setRelationship("minor_pirate_4", -0.6f);
        xhan.setRelationship("minor_pirate_5", -0.6f);
        xhan.setRelationship("minor_pirate_6", -0.6f);

        xhan.setRelationship("cabal", -0.8f);

        //Pamed relations mod
        pamed.setRelationship("sylphon", 0.35f);
        pamed.setRelationship("xhanempire", -0.20f);
        pamed.setRelationship("Coalition", 0.15f);
        pamed.setRelationship("tiandong", 0.6f);
        pamed.setRelationship("kadur_remnant", 0.15f);
        pamed.setRelationship("blackrock_driveyards", 0.35f);
        pamed.setRelationship("interstellarimperium", 0.15f);

        pamed.setRelationship("united_security", 0.35f);
        pamed.setRelationship("exigency", -0.15f);
        pamed.setRelationship("mess", -0.6f);
        pamed.setRelationship("HMI", 0.35f);
        pamed.setRelationship("pack", 0.35f);
        pamed.setRelationship("syndicate_asp", 0.35f);
        pamed.setRelationship("syndicate_asp_familia", 0.35f);
        pamed.setRelationship("al_ars", 0.35f);
        pamed.setRelationship("OCI", 0.35f);
        pamed.setRelationship("GKSec", 0.35f);
        pamed.setRelationship("mayorate", 0.35f);
        pamed.setRelationship("metelson", 0.35f);
        pamed.setRelationship("SCY", 0.35f);
        pamed.setRelationship("shadow_industry", 0.6f);
        pamed.setRelationship("roider", 0.35f);

        pamed.setRelationship("blade_breakers", -0.6f);
        pamed.setRelationship("dassault_mikoyan", 0.6f);
        pamed.setRelationship("diableavionics", -0.15f);
        pamed.setRelationship("exipirated", 0.35f);
        pamed.setRelationship("gmda", 0.15f);
        pamed.setRelationship("gmda_patrol", 0.15f);
        pamed.setRelationship("draco", -0.6f);
        pamed.setRelationship("fang", -0.6f);
        pamed.setRelationship("junk_pirates", -0.15f);
        pamed.setRelationship("junk_pirates_hounds", -0.15f);
        pamed.setRelationship("junk_pirates_junkboys", -0.15f);
        pamed.setRelationship("junk_pirates_technicians", -0.15f);
        pamed.setRelationship("ORA", 0.6f);
        pamed.setRelationship("the_cartel", 0.35f);
        pamed.setRelationship("nullorder", 0.35f);
        pamed.setRelationship("templars", -0.6f);
        pamed.setRelationship("crystanite_pir", -0.6f);
        pamed.setRelationship("infected", -0.6f);
        pamed.setRelationship("new_galactic_order", -0.35f);
        pamed.setRelationship("TF7070_D3C4", -0.6f);
        pamed.setRelationship("minor_pirate_1", -0.15f);
        pamed.setRelationship("minor_pirate_2", -0.15f);
        pamed.setRelationship("minor_pirate_3", -0.15f);
        pamed.setRelationship("minor_pirate_4", -0.15f);
        pamed.setRelationship("minor_pirate_5", -0.15f);
        pamed.setRelationship("minor_pirate_6", -0.15f);

        pamed.setRelationship("cabal", 0.35f);
    }
}
