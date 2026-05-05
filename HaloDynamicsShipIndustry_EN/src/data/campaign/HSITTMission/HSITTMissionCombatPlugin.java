package data.campaign.HSITTMission;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class HSITTMissionCombatPlugin extends BaseEveryFrameCombatPlugin {
    private CombatEngineAPI engine ;
    public enum BATTLE_STAGE{
        STAGE_1,//战机，黄蜂x1 钩爪x1 利爪x3 雷霆x1 野狼x1
        STAGE_2,//战机+护卫舰 钩爪x1 长弓x2 匕首x1 野狼x3
        STAGE_3,//护卫舰小组 预兆x3 野狼x2 斗士ttx1
        STAGE_4,//高难度波次 亥博龙x1 长弓x1 利爪x4 斗士ttx2
        STAGE_5//撤离波次（待议
    }

    private BATTLE_STAGE currStage = BATTLE_STAGE.STAGE_1;

    private float width,height;
    private float timer = 0,max_time = 1;

    private List<ShipAPI> deployed = new ArrayList<>();

    private IntervalUtil switchTimer = new IntervalUtil(3f,4f);

    private boolean isSwitching = false;


    @Override
    public void init(CombatEngineAPI engine) {
        //engine.getFleetManager(FleetSide.ENEMY).setAdmiralAI(new HSITTMissionAdmiralAI());
        this.engine = engine;
        width = engine.getMapWidth();
        height = engine.getMapHeight();
        setStage(BATTLE_STAGE.STAGE_1);
    }
    private int spawned = 0;
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        super.advance(amount, events);
        if(this.engine == null){
            this.engine = Global.getCombatEngine();
        }
        if(currStage!=BATTLE_STAGE.STAGE_5){
            engine.setDoNotEndCombat(true);
            engine.setCombatNotOverFor(1f);
        }
        if(engine.getPlayerShip()!=null){
            if(!engine.getPlayerShip().getCustomData().containsKey("HSI_NoDeath")){
                engine.getPlayerShip().setCustomData("HSI_NoDeath",true);
                engine.getPlayerShip().addListener(new HSINoDeathListener(engine.getPlayerShip()));
                engine.getPlayerShip().getMutableStats().getCRLossPerSecondPercent().modifyMult("HSI_NoDeath",0f);
            }
        }
        if(!engine.isPaused())timer = Math.max(0,timer-amount);
        float size = Math.max(0,(1f-timer/max_time));
        SpriteAPI sprite = null;
        CombatEngineLayers layer = CombatEngineLayers.PLANET_LAYER;
        CombatFleetManagerAPI manager = engine.getFleetManager(FleetSide.ENEMY);
        if(engine.getFleetManager(FleetSide.ENEMY).getCurrStrength()<=0&&!isSwitching){
            deployed.clear();
            if(spawned == 4) setStage(BATTLE_STAGE.STAGE_5);
            if(spawned == 3){
                setStage(BATTLE_STAGE.STAGE_4);
                isSwitching = true;
            }
            if(spawned == 2){
                setStage(BATTLE_STAGE.STAGE_3);
                isSwitching = true;
            }
            if(spawned == 1){
                setStage(BATTLE_STAGE.STAGE_2);
                isSwitching = true;
            }

        }
        if(timer <=0&&!isSwitching&&currStage != BATTLE_STAGE.STAGE_4&&currStage!=BATTLE_STAGE.STAGE_5){
            if(spawned == 3) setStage(BATTLE_STAGE.STAGE_4);
            if(spawned == 2) setStage(BATTLE_STAGE.STAGE_3);
            if(spawned == 1) setStage(BATTLE_STAGE.STAGE_2);
            isSwitching = true;
        }

        switch (currStage) {
            case STAGE_1:
                sprite = Global.getSettings().getSprite("HSI_BG","bg1");
                break;
            case STAGE_2:
                sprite = Global.getSettings().getSprite("HSI_BG","bg2");
                break;
            case STAGE_3:
                sprite = Global.getSettings().getSprite("HSI_BG","bg3");
                break;
            case STAGE_4:
            case STAGE_5:
                sprite = Global.getSettings().getSprite("HSI_BG","bg4");
                break;
        }

        if(isSwitching){
            if(!engine.isPaused()) switchTimer.advance(amount);
            if(switchTimer.intervalElapsed()){
                isSwitching = false;
            }
        }

        if(!isSwitching){
            switch (currStage) {
                case STAGE_1:
                    if (spawned < 1) {
                        manager.spawnShipOrWing("wasp_wing", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f);
                        manager.spawnShipOrWing("claw_wing", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f);
                        manager.spawnShipOrWing("talon_wing", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f);
                        manager.spawnShipOrWing("talon_wing", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f);
                        manager.spawnShipOrWing("talon_wing", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f);
                        manager.spawnShipOrWing("thunder_wing", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f);
                        deployed.add(manager.spawnShipOrWing("wolf_Assault", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f));
                        timer = 120f;
                        max_time = 120f;
                        spawned++;
                    }
                    //sprite = Global.getSettings().getSprite("HSI_BG","bg1");
                    break;
                case STAGE_2:
                    if (spawned < 2) {
                        manager.spawnShipOrWing("claw_wing", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f);
                        manager.spawnShipOrWing("longbow_wing", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f);
                        manager.spawnShipOrWing("dagger_wing", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f);
                        manager.spawnShipOrWing("dagger_wing", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f);
                        manager.spawnShipOrWing("thunder_wing", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f);
                        deployed.add(manager.spawnShipOrWing("wolf_Assault", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f));
                        deployed.add(manager.spawnShipOrWing("wolf_Assault", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f));
                        deployed.add(manager.spawnShipOrWing("wolf_Assault", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f));
                        timer = 180f;
                        max_time = 180f;
                        spawned++;
                    }
                    //sprite = Global.getSettings().getSprite("HSI_BG","bg2");
                    break;
                case STAGE_3:
                    if (spawned < 3) {
                        deployed.add(manager.spawnShipOrWing("omen_HSISS_HVB2", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f));
                        deployed.add(manager.spawnShipOrWing("omen_HSISS_HVB2", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f));
                        deployed.add(manager.spawnShipOrWing("omen_HSISS_HVB2", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f));
                        deployed.add(manager.spawnShipOrWing("wolf_Assault", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f));
                        deployed.add(manager.spawnShipOrWing("wolf_Assault", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f));
                        deployed.add(manager.spawnShipOrWing("brawler_tritachyon_HSISS_HVB2", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f));
                        timer = 220f;
                        max_time = 220f;
                        spawned++;
                    }
                    //sprite = Global.getSettings().getSprite("HSI_BG","bg3");
                    break;
                case STAGE_4:
                    if (spawned < 4) {
                        manager.spawnShipOrWing("claw_wing", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f);
                        manager.spawnShipOrWing("longbow_wing", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f);
                        manager.spawnShipOrWing("dagger_wing", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f);
                        manager.spawnShipOrWing("dagger_wing", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f);
                        manager.spawnShipOrWing("thunder_wing", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f);
                        deployed.add(manager.spawnShipOrWing("brawler_tritachyon_HSISS_HVB2", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f));
                        deployed.add(manager.spawnShipOrWing("brawler_tritachyon_HSISS_HVB2", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f));
                        deployed.add(manager.spawnShipOrWing("brawler_tritachyon_HSISS_HVB2", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f));
                        deployed.add(manager.spawnShipOrWing("HSITT_hyperion_Strike", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f));
                        deployed.add(manager.spawnShipOrWing("HSITT_hyperion_Strike", new Vector2f(width / 4f + ((float) Math.random() - 0.5f) * 900f, height/2f), 2f));
                        timer = 300f;
                        max_time = 300f;
                        spawned++;
                    }
                    //sprite = Global.getSettings().getSprite("HSI_BG","bg4");
                    break;
                case STAGE_5:
                    //sprite = Global.getSettings().getSprite("HSI_BG","bg4");
                    break;
            }
        }
        if(isSwitching){
            SpriteAPI s1 = null,s2 = null;
            switch (currStage) {
                case STAGE_2:
                    s1 = Global.getSettings().getSprite("HSI_BG","bg1");
                    s2 = Global.getSettings().getSprite("HSI_BG","bg2");
                    break;
                case STAGE_3:
                    s1 = Global.getSettings().getSprite("HSI_BG","bg2");
                    s2 = Global.getSettings().getSprite("HSI_BG","bg3");
                    break;
                case STAGE_4:
                    s1 = Global.getSettings().getSprite("HSI_BG","bg3");
                    s2 = Global.getSettings().getSprite("HSI_BG","bg4");
                    break;
                default:
            }

            if(s1!=null&&s2!=null){
                float level = switchTimer.getElapsed()/switchTimer.getIntervalDuration();
                float size1 = (1f-level)*(1.5f - 0.5f * size)-(level);
                size1 = MathUtils.clamp(size1,0,1);
                float size2 = 3f-1.5f*level;
                MagicRender.screenspace(
                        s2,
                        MagicRender.positioning.CENTER,
                        new Vector2f(0, 0),
                        new Vector2f(0, 0),
                        new Vector2f(sprite.getWidth() * (size2), sprite.getHeight() * (size2)),
                        new Vector2f(0, 0),
                        90f,
                        0f,
                        new Color(1f, 1f, 1f, MathUtils.clamp(0.5f+0.5f*level,0,1)),
                        false,
                        0f,
                        0f,
                        0f,
                        0f,
                        0f,
                        0f,
                        amount,
                        0f,
                        layer
                );
                MagicRender.screenspace(
                        s1,
                        MagicRender.positioning.CENTER,
                        new Vector2f(0, 0),
                        new Vector2f(0, 0),
                        new Vector2f(sprite.getWidth() * (size1), sprite.getHeight() * (size1)),
                        new Vector2f(0, 0),
                        90f,
                        0f,
                        new Color(1f, 1f, 1f, size1),
                        false,
                        0f,
                        0f,
                        0f,
                        0f,
                        0f,
                        0f,
                        amount,
                        0f,
                        layer
                );
            }else{
                if(sprite!=null) {
                    MagicRender.screenspace(
                            sprite,
                            MagicRender.positioning.CENTER,
                            new Vector2f(0, 0),
                            new Vector2f(0, 0),
                            new Vector2f(sprite.getWidth() * (1.5f - 0.5f * size), sprite.getHeight() * (1.5f - 0.5f * size)),
                            new Vector2f(0, 0),
                            90f,
                            0f,
                            new Color(1f, 1f, 1f, 1f),
                            false,
                            0f,
                            0f,
                            0f,
                            0f,
                            0f,
                            0f,
                            amount,
                            0f,
                            layer
                    );
                }
            }

        }else {
            if (sprite != null) {
                MagicRender.screenspace(
                        sprite,
                        MagicRender.positioning.CENTER,
                        new Vector2f(0, 0),
                        new Vector2f(0, 0),
                        new Vector2f(sprite.getWidth() * (1.5f - 0.5f * size), sprite.getHeight() * (1.5f - 0.5f * size)),
                        new Vector2f(0, 0),
                        90f,
                        0f,
                        new Color(1f, 1f, 1f, 1f),
                        false,
                        0f,
                        0f,
                        0f,
                        0f,
                        0f,
                        0f,
                        amount,
                        0f,
                        layer
                );
            }
        }
    }

    protected void setStage(BATTLE_STAGE stage){
        currStage = stage;
        switch (currStage) {
            case STAGE_1:
                timer = 120f;
                max_time = 120f;
                break;
            case STAGE_2:
                timer = 180f;
                max_time = 180f;
                break;
            case STAGE_3:
                timer = 220f;
                max_time = 220f;
                break;
            case STAGE_4:
                timer = 300f;
                max_time = 300f;
            case STAGE_5:
                break;
        }
    }

    public static class HSINoDeathListener implements HullDamageAboutToBeTakenListener{

        private ShipAPI ship;

        public HSINoDeathListener(ShipAPI ship){
            this.ship = ship;
        }
        @Override
        public boolean notifyAboutToTakeHullDamage(Object o, ShipAPI target, Vector2f point, float amount) {
            return ship.getHitpoints() <= amount + 10f;
        }
    }
}
