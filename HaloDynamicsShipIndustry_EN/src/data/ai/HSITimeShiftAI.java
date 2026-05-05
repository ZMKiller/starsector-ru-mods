package data.ai;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;

import data.kit.HSITimeImage;
import data.kit.HSITimeImageListener;

public class HSITimeShiftAI implements ShipSystemAIScript {

	private ShipAPI ship;
	private CombatEngineAPI engine;
	private ShipwideAIFlags flags;
	private ShipSystemAPI system;
    private HSITimeImageListener images = null;
	
	
	public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
		this.ship = ship;
		this.flags = flags;
		this.engine = engine;
		this.system = system;
        if(ship.hasListenerOfClass(new HSITimeImageListener(ship).getClass())){
            images = ship.getListeners(new HSITimeImageListener(ship).getClass()).get(0);
        }
	}
	
	
	@SuppressWarnings("unchecked")
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
		if(system.isCoolingDown()) return;
        if(engine.isPaused()) return;
        if(images == null){
            if(ship.hasListenerOfClass(new HSITimeImageListener(ship).getClass())){
                images = ship.getListeners(new HSITimeImageListener(ship).getClass()).get(0);
            }
        }
        if(images == null) return;
        if(images.getSize()<1) return;
        HSITimeImage start = images.getExactStart();
        if(start == null) return;
        HSITimeImage curr = new HSITimeImage(ship);
        if(curr.getHitpoints()<start.getHitpoints()*0.95f) ship.useSystem();
        if((curr.gethardFlux()+curr.getsoftFlux())>(start.gethardFlux()+start.getsoftFlux())&&ship.getFluxLevel()>0.8f) ship.useSystem();
	}
}
