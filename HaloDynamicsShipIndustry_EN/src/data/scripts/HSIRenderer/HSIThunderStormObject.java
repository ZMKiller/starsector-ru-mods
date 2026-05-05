package data.scripts.HSIRenderer;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import data.kit.HSIIds;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class HSIThunderStormObject extends HSIBaseCombatRendererObject{
    private final int shader;
    private final int uniform;

    private final int alpha;
    private float effectLevel = 0;
    private Vector2f translate = new Vector2f();

    private ShipAPI source;
    private int cost = 0;

    private float damage = 0;

    private float empDamage = 0;
    private DamageType damageType;

    private float interval = 0;

    private float elapsed = 0;

    private final float maxElapsed;
    private IntervalUtil damageInterval;

    private Vector2f location;

    private float facing;

    private float Ssize;

    private float fadein;
    private float fadeout;

    private final int text;


    public HSIThunderStormObject(ShipAPI source, Vector2f location,int cost, float damage, float empDamage,DamageType damageType,float interval,float facing) {
        this.source = source;
        this.cost = cost;
        this.damage = damage;
        this.empDamage = empDamage;
        this.damageType = damageType;
        this.interval = interval;
        this.location = new Vector2f(location);
        this.facing = facing;
        Ssize = cost*33f;
        damageInterval = new IntervalUtil(interval,interval);
        fadein = interval;
        fadeout = cost*interval-interval;
        this.maxElapsed = cost*interval;
        shader = HSIInitPlugin.ShaderManager.getShader(HSIInitPlugin.thunderstormKey);
        GL20.glUseProgram(shader);
        uniform = GL20.glGetUniformLocation(shader, "time");
        alpha = GL20.glGetUniformLocation(shader, "alpha");
        text = GL20.glGetUniformLocation(shader,"text0");
        GL20.glUniform1i(text,0);
        GL20.glUseProgram(0);
        setLayer(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
        createThunderStormDamage();
        //Global.getLogger(this.getClass()).info("Cost"+cost);
    }

    @Override
    public void advance(float amount) {
        damageInterval.advance(amount);
        elapsed+=amount;
        if(damageInterval.intervalElapsed()&&cost>0){
            cost--;
            createThunderStormDamage();
        }
    }

    private void createThunderStormDamage(){
        Global.getCombatEngine().spawnDamagingExplosion(createExplosionSpec(),source,location).getDamage().setFluxComponent(empDamage);
    }

    public DamagingExplosionSpec createExplosionSpec() {
        DamagingExplosionSpec spec = new DamagingExplosionSpec(
                0.1f, // duration
                Ssize, // radius
                Ssize*0.7f, // coreRadius
                damage, // maxDamage
                damage / 2f, // minDamage
                CollisionClass.PROJECTILE_NO_FF, // collisionClass
                CollisionClass.PROJECTILE_FIGHTER, // collisionClassByFighter
                0f, // particleSizeMin
                0f, // particleSizeRange
                0f, // particleDuration
                0, // particleCount
                new Color(255, 255, 255, 0), // particleColor
                new Color(107, 148, 196, 75) // explosionColor
        );

        spec.setDamageType(damageType);
        spec.setUseDetailedExplosion(false);
        return spec;
    }

    @Override
    public void render() {
        GL20.glUseProgram(shader);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        //GL11.glEnable(GL11.GL_BLEND);
        GL11.glPushMatrix();
        GL20.glUniform1f(uniform, elapsed);
        float a = 1.0f;
        if(elapsed<fadein){
            a = (elapsed/fadein);
        }else if(elapsed>fadeout){
            a = 1-(elapsed-fadeout)/interval;
        }
        if(a<0) a = 0.0f;
        if(a>1) a = 1.0f;
        GL20.glUniform1f(alpha, a);
        SpriteAPI grey1 = Global.getSettings().getSprite(HSIIds.SPRITE.GREY_NOISE_64PX);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, grey1.getTextureId());
        Vector2f uv = new Vector2f(1f,1f);
        Vector2f size = new Vector2f(Ssize,Ssize);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslatef(location.x,location.y, 0.0f);
        GL11.glRotatef(facing-90f, 0.0f, 0.0f, 1.0f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4b((byte) 0,(byte) 0,(byte) 0,(byte) 0);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex2f(-size.x, -size.y);
        GL11.glTexCoord2f(0.0f, uv.y);
        GL11.glVertex2f(-size.x, size.y);
        GL11.glTexCoord2f(uv.x, uv.y);
        GL11.glVertex2f(size.x, size.y);
        GL11.glTexCoord2f(uv.x, 0.0f);
        GL11.glVertex2f(size.x, -size.y);
        GL11.glEnd();
        //GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glPopAttrib();
        GL20.glUseProgram(0);
    }

    @Override
    public boolean isExpired() {
        return elapsed>=maxElapsed;
    }

    @Override
    public CombatEntityAPI getAnchor() {
        return source;
    }

    @Override
    public float getRadius() {
        return 10000f;
    }
}
