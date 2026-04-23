package shmoneelse.sicdustkeeper.skills.dustkeeper;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.plugins.OfficerLevelupPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.rpg.Person;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;
import org.magiclib.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CyberwarfareTraining extends SCBaseSkillPlugin{

    @Override
    public String getAffectsString() {
        return "all AI cores";
    }

    /*@Override
    public void applyEffectsAfterShipCreation(SCData data, ShipAPI ship, ShipVariantAPI variant, String id) { // Adds listener for our version of Cyberwarfare
        if (ship.getCaptain() == null) return;
        if (!ship.getCaptain().isAICore()) return;
        if (ship.getCaptain().getStats() == null) return;
        if (ship.getCaptain().getStats().hasSkill("sotf_cyberwarfare")) return; // Don't do anything if they already have the skill, that's covered under advance
        if (ship.isFighter()) return;
        if (ship.isStationModule()) return; // Doesn't apply to submodules or fighters either.

        ship.addListener(new SotfCyberwarfareNerfed.SotfCyberwarfareShipHackScriptNerfed(ship));
    } */

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltipMakerAPI) {
        tooltipMakerAPI.addPara("Grants AI cores of Level 5 or higher an inferior version of the Cyberwarfare Protocols skill*", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("Increases the level of AI cores that already have Cyberwarfare Protocols by 1", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("   - If this officer is unassigned, the level is reduced back to the default and the skill is unlearned", 0f, Misc.getTextColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("   - If the core has more skills than possible at that level, it removes them automatically.", 0f, Misc.getTextColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addSpacer(10f);
        tooltipMakerAPI.addPara("* The Cyberwarfare Training skill has reduced intrusion effectiveness and a longer cooldown than normal.", 0f, Misc.getGrayColor(), Misc.getHighlightColor());

    }

    @Override
    public void advance(SCData data, Float amount)
    {
        if (data.isNPC()) return;
        if (Global.getSector() == null || Global.getSector().getPlayerFleet() == null) return;

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        List<FleetMemberAPI> fleetList = fleet.getMembersWithFightersCopy();

        for (FleetMemberAPI member : fleetList) {
            if(member.getCaptain() == null) continue;
            if(!member.getCaptain().isAICore()) continue;
            if(member.getCaptain().getMemoryWithoutUpdate().getBoolean("$sicdk_cyberwarfare_training")) continue;

            if(member.getCaptain().getStats().hasSkill("sotf_cyberwarfare"))
            {
                member.getCaptain().getStats().setLevel(member.getCaptain().getStats().getLevel()+1);
            }

            if(member.getCaptain().getStats().getLevel() < 5) continue; // Moved level check to later to accomodate cores w/ Cyberwarfare but at lower levels

            if(!member.getCaptain().getStats().hasSkill("sotf_cyberwarfare")) // Awkward position but else of two if statements
            {
                member.getCaptain().getStats().setLevel(member.getCaptain().getStats().getLevel()+1); // Accomodate the bonus skill
                member.getCaptain().getStats().setSkillLevel("sotf_cyberwarfare_nerfed", 1f);
            }

            member.getCaptain().getMemoryWithoutUpdate().set("$sicdk_cyberwarfare_training", true);
        }
    }

    @Override
    public void onActivation(SCData data) {
        if(data.isNPC()) { // NPCs only
            for (FleetMemberAPI fleet : data.getFleet().getFleetData().getMembersListCopy()) {
                PersonAPI core = fleet.getCaptain();

                if (core == null) continue;
                if (!core.isAICore()) continue; // AI cores only
                if (Global.getSector().getImportantPeople().containsPerson(core)) continue; // Also not IBB bounties
                if (core.getStats().getLevel() < 5) continue;
                if (core.hasTag("sicdk_cyberwarfare_update")) continue; // Don't duplicate

                core.addTag("sicdk_cyberwarfare_update");

                if (core.getStats().hasSkill("sotf_cyberwarfare"))
                {
                    core.getStats().setLevel(core.getStats().getLevel() + 1);
                    OfficerLevelupPlugin plugin = (OfficerLevelupPlugin) Global.getSettings().getPlugin("officerLevelUp");

                    Random rand = new Random();
                    List<String> pickList = plugin.pickLevelupSkills(core, rand);
                    if(!pickList.isEmpty())
                    {
                        String pick = pickList.get(rand.nextInt(pickList.size()));
                        core.getStats().setSkillLevel(pick, 2f);
                    }
                }
                else
                    core.getStats().setSkillLevel("sotf_cyberwarfare_nerfed", 1f);
            }
        }
    }

    @Override
    public void onDeactivation(SCData data) {
        if (Global.getSector() == null || Global.getSector().getPlayerFleet() == null) return;
        if (data.isNPC()) return;

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        List<FleetMemberAPI> fleetList = fleet.getMembersWithFightersCopy();

        for (FleetMemberAPI member : fleetList) {
            if(member.getCaptain() == null) continue;
            if(!member.getCaptain().isAICore()) continue;

            if(!member.getCaptain().getMemoryWithoutUpdate().getBoolean("$sicdk_cyberwarfare_training")) continue;

            if(member.getCaptain().getStats().hasSkill("sotf_cyberwarfare"))
            {
                member.getCaptain().getStats().setLevel(member.getCaptain().getStats().getLevel()-1);
            }
            else
            {
                member.getCaptain().getStats().setSkillLevel("sotf_cyberwarfare_nerfed", 0f);
                member.getCaptain().getStats().setLevel(member.getCaptain().getStats().getLevel()-1);
            }

            member.getCaptain().getMemoryWithoutUpdate().set("$sicdk_cyberwarfare_training", false);

            List<MutableCharacterStatsAPI.SkillLevelAPI> skills = member.getCaptain().getStats().getSkillsCopy();
            for(MutableCharacterStatsAPI.SkillLevelAPI skill : new ArrayList<>(skills))
            {
                if(skill.getSkill().isAptitudeEffect() || member.getCaptain().getStats().getSkillLevel(skill.getSkill().getName()) == 0f)
                    skills.remove(skill);
            }

            if(skills.size() <= member.getCaptain().getStats().getLevel()) continue;

            for(MutableCharacterStatsAPI.SkillLevelAPI skill : new ArrayList<>(skills))
            {
                if(skill.getSkill().hasTag("npc_only") || skill.getSkill().hasTag("player_only") || skill.getSkill().hasTag("ai_core_only"))
                    skills.remove(skill);
            }

            member.getCaptain().getStats().setSkillLevel(skills.get(skills.size()-1).getSkill().getId(), 0f);

        }
    }

}
