package org.xhan.campaign.world;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import org.apache.log4j.Logger;

import java.util.List;

import static com.fs.starfarer.api.Global.getLogger;
import static com.fs.starfarer.api.Global.getSector;
import static com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel.FULL;
import static com.fs.starfarer.api.impl.campaign.ids.Ranks.POST_FACTION_LEADER;
import static com.fs.starfarer.api.impl.campaign.ids.Skills.INDUSTRIAL_PLANNING;
import static com.fs.starfarer.api.util.Misc.getFactionMarkets;
import static org.xhan.XhanEmpireModPlugin.*;

public class XHAN_EmperorAndMegastructureAdder implements EveryFrameScript {

    public static final String EMPEROR_PORTRAIT = "graphics/portraits/DivineEmperor.png";
    public static final String GENERALISSIMO_PORTRAIT = "graphics/portraits/PamedGeneralissimo.png";
    public static final String MEGASTRUCTURE = "xhan_planetary_megastructure";

    public static Logger log = getLogger(XHAN_EmperorAndMegastructureAdder.class);
    private boolean done = false;

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if (!done) {
            doThing();
            doThingPamed();
            log.info("doing the thing");
            done = true;
        }
    }

    private void doThing() {
        List<MarketAPI> markets = getFactionMarkets(getSector().getFaction(XHAN_FACTION_ID));
        MarketAPI picked = null;
        int size = 0;
        for (MarketAPI market : markets) {
            int thisSize = market.getSize();
            if (thisSize > size) {
                size = thisSize;
                picked = market;
            }
        }

        if (picked != null) {
            PersonAPI divine = createAdmin(picked);
            divine.getStats().setSkillLevel(INDUSTRIAL_PLANNING, 1);
            divine.setPortraitSprite(EMPEROR_PORTRAIT);
            divine.setPostId(POST_FACTION_LEADER);
            divine.setRankId(POST_FACTION_LEADER);
            divine.getName().setFirst("Okkatrid");
            divine.getName().setLast("Zetnon");

            picked.addCondition(MEGASTRUCTURE);
            picked.getFirstCondition(MEGASTRUCTURE).setSurveyed(true);
            picked.setSurveyLevel(FULL);
        }
    }

    private void doThingPamed() {
        List<MarketAPI> markets = getFactionMarkets(getSector().getFaction(PAMED_FACTION_ID));
        MarketAPI picked = null;
        int size = 0;
        for (MarketAPI market : markets) {
            int thisSize = market.getSize();
            if (thisSize > size) {
                size = thisSize;
                picked = market;
            }
        }

        if (picked != null) {
            PersonAPI pamedg = createAdmin(picked);
            pamedg.getStats().setSkillLevel(INDUSTRIAL_PLANNING, 1);
            pamedg.setPortraitSprite(GENERALISSIMO_PORTRAIT);
            pamedg.setPostId(POST_FACTION_LEADER);
            pamedg.setRankId(POST_FACTION_LEADER);
            pamedg.getName().setFirst("Sisi");
            pamedg.getName().setLast("De Luca");

            picked.setSurveyLevel(FULL);
        }
    }
}
