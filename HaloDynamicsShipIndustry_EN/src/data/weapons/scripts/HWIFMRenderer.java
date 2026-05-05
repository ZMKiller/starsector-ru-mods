package data.weapons.scripts;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import java.awt.Color;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Sphere;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;

public class HWIFMRenderer extends BaseCombatLayeredRenderingPlugin {
    private final SpriteAPI TEXTURE = Global.getSettings().getSprite("HSI_fx", "BlueGiant");
    private Sphere sphere = new Sphere();
    private static final Color STAR = new Color(245, 245, 245, 255);

    public HWIFMRenderer() {
        super();
        this.layer = CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER;
        sphere.setNormals(GLU.GLU_SMOOTH);
        sphere.setTextureFlag(true);
        sphere.setDrawStyle(GLU.GLU_FILL);
        sphere.setOrientation(GLU.GLU_OUTSIDE);
    }

    public float getRenderRadius() {
        return 10000f;
    }

    public static HWIFMRenderer getInstance() {
        if (Global.getCombatEngine().getCustomData().containsKey("HWIFMRenderer")) {
            return (HWIFMRenderer) Global.getCombatEngine().getCustomData().get("HWIFMRenderer");
        } else {
            HWIFMRenderer hwifmr = new HWIFMRenderer();
            Global.getCombatEngine().getCustomData().put("HWIFMRenderer", hwifmr);
            Global.getCombatEngine().addLayeredRenderingPlugin(hwifmr);
            return hwifmr;
        }
    }

    public void advance(float amount){
        if(Global.getCombatEngine().isPaused()) return;
        
    }

    public void Damage(MissileAPI m){
        Global.getCombatEngine().spawnDamagingExplosion(createExplosionSpec(), m.getSource(), m.getLocation());
    }

    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        /*for (HWIFMRenderData data : datas) {
            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, TEXTURE.getTextureId());
            // GL11.glEnable(GL11.GL_BLEND);
            // GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
            // GL11.glBegin(GL11.GL_QUADS);
            // GL11.glBegin(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glTranslatef(data.location.getX(), data.location.getY(), 0f);
            GL11.glRotatef(data.getAngle1(), 0f, 0f, 1f);
            GL11.glRotatef(data.getAngle2(), 1f, 0f, 0f);
            GL11.glRotatef(data.getAngle2(), 0f, 1f, 0f);
            GL11.glColor3ub((byte) STAR.getRed(), (byte) STAR.getGreen(), (byte) STAR.getGreen());
            sphere.draw(data.size*15f, 20, 20);
            // GL11.glEnd();
            GL11.glPopMatrix();
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);
        }*/
    }


    public DamagingExplosionSpec createExplosionSpec() {
		float damage = 50f;
		DamagingExplosionSpec spec = new DamagingExplosionSpec(
				0.05f, // duration
				400f, // radius
				200f, // coreRadius
				damage, // maxDamage
				damage, // minDamage
				CollisionClass.PROJECTILE_FF, // collisionClass
				CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter
				0f, // particleSizeMin
				0f, // particleSizeRange
				0f, // particleDuration
				0, // particleCount
				new Color(255,255,255,0), // particleColor
				new Color(255,100,100,0)  // explosionColor
		);

		spec.setDamageType(DamageType.ENERGY);
		spec.setUseDetailedExplosion(false);
		return spec;		
	}

}
