package data.scripts.HSIRenderer;

import java.awt.Color;

import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.ShipAPI;

public class HSIShieldHitData {
    protected Vector2f loc;//relative
    protected Color c;
    protected float radius;
    protected float time;
    protected ShipAPI source;

    public HSIShieldHitData(ShipAPI source,Vector2f loc,Color c,float radius,float time){
        //absoluteLoc
        this.source = source;
        Vector2f rel = Vector2f.sub(loc, source.getLocation(), null);
        float facing = source.getFacing();
        this.loc = new Vector2f(rel);
        this.loc = VectorUtils.rotate(rel, -facing, this.loc);
        this.c = c;
        this.radius = radius;
        this.time = time;
    }
}
