package data.scripts.HSIContrail;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;

public class HSIContrail {
    private List<HSIContrailMP> midpoints = new ArrayList<>(60);
    private WeaponAPI sourceWeapon;
    private ShipAPI sourceShip;
    public static final Color CONTRAIL = new Color(180, 180, 225, 200);
    protected static final float BASEVEL = 50f;
    protected final SpriteAPI sprite;
    protected final float WIDTH;
    private float alphamult = 0.2f;
    private float textStart = 0;

    public HSIContrail(ShipAPI ship,WeaponAPI weapon,float width) {
        sprite = Global.getSettings().getSprite("HSI_fx", "ContrailWeave");
        sourceShip = ship;
        sourceWeapon = weapon;
        this.WIDTH = width;
    }

    public void advance(float amount) {
        float f = 1;
        if (!sourceShip.isAlive()) {
            f = 3;
        }
        List<HSIContrailMP> toRemove = new ArrayList<>();
        for (HSIContrailMP mp : midpoints) {
            mp.advance(f * amount);
            if (mp.isExpired()) {
                toRemove.add(mp);
            }
        }
        midpoints.removeAll(toRemove);
        toRemove.clear();
        if (f < 3) {
            midpoints.add(new HSIContrailMP(sourceWeapon.getLocation(), Vector2f
                    .add((Vector2f) (Misc.getUnitVectorAtDegreeAngle(
                            sourceWeapon.getCurrAngle() + (float) (Math.random() - 0.5f) * 5f).scale(BASEVEL)),
                            sourceShip.getVelocity(), null),
                    0.8f, CONTRAIL));
        }
        alphamult = 0.4f;
        if(sourceShip.getSystem()!=null&&sourceShip.getSystem().isOn()){
            alphamult = 0.4f+sourceShip.getSystem().getEffectLevel()*0.6f;
        }
        if(sourceShip.getPhaseCloak()!=null&&sourceShip.getPhaseCloak().isOn()){
            alphamult = 0.4f+sourceShip.getPhaseCloak().getEffectLevel()*0.6f;
        }
        alphamult*=sourceShip.getVelocity().length()/(sourceShip.getMaxSpeed()+1);
        alphamult = MathUtils.clamp(alphamult, 0, 1);
        textStart=textStart+(sourceShip.getVelocity().length()*(amount*sourceShip.getMutableStats().getTimeMult().getModifiedValue())/sprite.getWidth());
    }

    public void render() {
        int mpsize = midpoints.size();
        if (mpsize > 0) {
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            sprite.bindTexture();
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glBegin(GL11.GL_QUAD_STRIP);
            float textLength = sprite.getWidth();
            float textProgress = textStart;
            for(int i = 0;i<mpsize-1;i++){
                Vector2f mp0; 
                Vector2f mp1;
                if(i<mpsize-2){
                    mp0 = midpoints.get(i).getLocation();
                    mp1 = midpoints.get(i+1).getLocation();   
                }else{
                    mp0 = midpoints.get(i).getLocation();
                    mp1 = sourceWeapon.getLocation();
                }
                float linkAngle = VectorUtils.getAngle(mp0, mp1); 
                //float width = WIDTH*midpoints.get(i).getOpacity()*0.5f;
                float width = WIDTH*(1f+midpoints.get(i).getOpacity()*0.2f);
                float length = MathUtils.getDistance(mp0, mp1);
                Vector2f v0 = MathUtils.getPointOnCircumference(mp0, width, linkAngle+90f);
                Vector2f v1 = MathUtils.getPointOnCircumference(mp0, width, linkAngle-90f);
                GL11.glColor4ub((byte)CONTRAIL.getRed(), (byte)CONTRAIL.getGreen(), (byte)CONTRAIL.getBlue(), (byte)(CONTRAIL.getAlpha()*midpoints.get(i).getOpacity()*alphamult));
                GL11.glTexCoord2f(textProgress,0f );
                GL11.glVertex2f(v0.x, v0.y);
                GL11.glTexCoord2f(textProgress, 1f);
                GL11.glVertex2f(v1.x, v1.y);
                textProgress+=length/textLength;
            }
            GL11.glEnd();
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }
}
