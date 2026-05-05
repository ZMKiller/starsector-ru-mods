package data.hullmods;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;

public class HSIFloatingWeaponModule {

    protected static final float DEBUFF_ROF = 20f;

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.addListener(new HSIFloatingModuleListener(ship));
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0)
            return "" + (int) (DEBUFF_ROF) + "%";
        return null;
    }

    public class HSIFloatingModuleListener implements AdvanceableListener {
        protected static final float rep = 20f;
        protected static final float maxSpd = 5f;
        protected static final float maxAcc = 2f;
        protected static final float maxRound = 25f;
        protected ShipAPI ship;
        protected List<HSIFloatingModuleListenerBindedSlot> slots = new ArrayList<>();

        public class HSIFloatingModuleListenerBindedSlot {
            protected ShipAPI module;
            protected Vector2f baseLocation;
            protected float baseDist;
            protected Vector2f vel = new Vector2f();
            protected float xfactor = (float) (-0.8f - 0.2f * Math.random());
            protected float yfactor = (float) (0.8f + 0.2f * Math.random());

            public HSIFloatingModuleListenerBindedSlot(ShipAPI module, Vector2f baseLocation) {
                this.module = module;
                module.ensureClonedStationSlotSpec();
                this.baseLocation = new Vector2f(baseLocation);
                baseDist = baseLocation.length();
                if (baseLocation.y < 0)
                    yfactor *= -1f;
            }

            public void advance(float amount) {
                if(module.getParentStation()==null) return;
                float limSpd = module.getParentStation().getVelocity().length() + maxSpd;
                Vector2f diff = new Vector2f(Vector2f.sub(baseLocation, module.getStationSlot().getLocation(), null));
                Vector2f shipvel = new Vector2f(xfactor * ship.getVelocity().y * amount,
                        yfactor * ship.getVelocity().x * amount);
                if (diff.length() > rep) {
                    Vector2f.add(vel, (Vector2f) diff.scale(maxAcc * amount / diff.length()), vel);
                }else{
                    float f = 0.99f;
                    if(vel.length()<shipvel.length()*0.95f){
                        f = 1;
                    }
                    vel.scale(f);
                }
                if (vel.length() > limSpd) {
                    vel.scale(limSpd / vel.length());
                }
                Vector2f toAdd = Vector2f.add(shipvel, vel, null);
                module.getStationSlot().getLocation().set(Vector2f.add(module.getStationSlot().getLocation(), toAdd, null));
                //wpn.getSlot().getLocation().set(Vector2f.add(wpn.getSlot().getLocation(), toAdd, null));
                //deco.getSlot().getLocation().set(wpn.getSlot().getLocation());
                /*if(wpn.getSprite()!=null){
                    wpn.getSprite().setCenter(wpn.getSprite().getCenterX()+toAdd.x, wpn.getSprite().getCenterY()+toAdd.y);
                    if(wpn.getSlot().isHardpoint()){
                        for(int i = 0;i<wpn.getSpec().getHardpointFireOffsets().size();i++){
                            wpn.getSpec().getHardpointFireOffsets().set(i, Vector2f.add(wpn.getSpec().getHardpointFireOffsets().get(i), toAdd, null));
                        }
                    }else{
                        for(int i = 0;i<wpn.getSpec().getTurretFireOffsets().size();i++){
                            wpn.getSpec().getTurretFireOffsets().set(i, Vector2f.add(wpn.getSpec().getTurretFireOffsets().get(i), toAdd, null));
                        }
                    }
                }
                deco.getSprite().setCenter(deco.getSprite().getCenterX()+toAdd.x,deco.getSprite().getCenterY()+toAdd.y);
                if(wpn.getBarrelSpriteAPI()!=null){
                    wpn.getBarrelSpriteAPI().setCenter(wpn.getBarrelSpriteAPI().getCenterX()+toAdd.x, wpn.getBarrelSpriteAPI().getCenterY()+toAdd.y);
                }*/
                // Global.getLogger(this.getClass()).info(toAdd.toString()+"-"+ship.getVelocity()+"-"+wpn.getSlot().getLocation());
            }

        }

        public HSIFloatingModuleListener(ShipAPI ship) {
            this.ship = ship;
            if(ship.getChildModulesCopy()!=null){
                for(ShipAPI m:ship.getChildModulesCopy()){
                    if(m.getHullSpec().hasTag("HSI_FloatingModule")){
                        ship.getMutableStats().getBallisticRoFMult().modifyMult("HSI_FloatingModule", (1 - DEBUFF_ROF / 100f));
                        ship.getMutableStats().getEnergyRoFMult().modifyMult("HSI_FloatingModule", (1 - DEBUFF_ROF / 100f));
                        ship.getMutableStats().getMissileRoFMult().modifyMult("HSI_FloatingModule", (1 - DEBUFF_ROF / 100f));
                        slots.add(new HSIFloatingModuleListenerBindedSlot(m, new Vector2f(m.getStationSlot().getLocation())));
                    }
                }
            }
        }

        @Override
        public void advance(float amount) {
            if (Global.getCombatEngine().isPaused())
                return;
            if (Global.getCurrentState().equals(GameState.CAMPAIGN))
                return;
            for (HSIFloatingModuleListenerBindedSlot s : slots) {
                s.advance(amount);
            }
        }

    }
}
