package data.scripts.HSIRenderer;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;
import data.kit.AjimusUtils;
import data.shipsystems.scripts.HSITSControl;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.subsystems.MagicSubsystem;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HSITSControlFxObject extends HSIBaseCombatRendererObject{

    private List<HSITSControlData> data = new ArrayList<>();

    public static Color inner = new Color(105, 175, 255, 225);

    private ShipAPI ship;

    private MagicSubsystem subsystem;

    public HSITSControlFxObject(ShipAPI ship, MagicSubsystem system){
        this.subsystem = system;
        this.ship = ship;
    }
    @Override
    public void render() {
        for(HSITSControlData d:data){
            d.render();
        }
    }

    @Override
    public boolean shouldRender() {
        return !data.isEmpty();
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
        Iterator<HSITSControlData> d = data.iterator();
        List<HSITSControlData> toR = new ArrayList<>();
        while (d.hasNext()){
            HSITSControlData dd = d.next();
            dd.advance(amount);
            if(dd.isExpired()) d.remove();
            if(!subsystem.isActive()&&dd.getBeam()!=null){
                toR.add(dd);
            }
        }
        data.removeAll(toR);

        //Global.getLogger(this.getClass()).info("DataSize:"+data.size());
    }

    @Override
    public CombatEngineLayers getLayer() {
        return CombatEngineLayers.BELOW_SHIPS_LAYER;
    }

    public void addData(HSITSControlData d){
        this.data.add(d);
    }

    @Override
    public boolean isExpired() {
        return !ship.isAlive();
    }

    public static class HSITSControlData{
        private final FaderUtil fader;
        private float inAngle = 90 ,outAngle = 90,size = 0;
        private Vector2f inLoc = null,outLoc = null;

        private BeamAPI beam = null;

        private float elapsed = 0;
        public HSITSControlData(float time, Vector2f inLoc, float inAngle , Vector2f outLoc, float outAngle , float size, BeamAPI beam){
            this.fader = new FaderUtil(1f,time);
            this.inAngle = inAngle;
            this.outAngle = outAngle;
            this.inLoc = inLoc;
            this.outLoc = outLoc;
            this.size = size;
            this.beam = beam;
            fader.fadeOut();
        }
        public void advance(float amount){
            elapsed+=amount;
            if(beam == null) {
                fader.advance(amount);
            }else{
                inLoc = beam.getRayEndPrevFrame();
                inAngle = VectorUtils.getAngle(beam.getFrom(),beam.getRayEndPrevFrame())+90f;
            }
        }

        public void render(){
            if(inLoc!=null){
                createPortalSimple(inLoc,inAngle,size);
            }
            if(outLoc!=null){
                createPortalSimple(outLoc,outAngle,size);
            }
        }

        private void createPortalSimple(Vector2f center,float angle,float size){
            GL11.glPushMatrix();
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            //GL11.glBegin(GL11.GL_TRIANGLE_FAN);
            GL11.glLineWidth(4f);
            GL11.glBegin(GL11.GL_LINE_STRIP);
            Vector2f dx = (Vector2f) Misc.getUnitVectorAtDegreeAngle(angle+90f).scale(size*getBrightness());
            Vector2f dy= (Vector2f) Misc.getUnitVectorAtDegreeAngle(angle).scale(size*getBrightness()/2f);
            Misc.setColor(inner, AjimusUtils.EnsurePositive(getBrightness()));
            GL11.glVertex2f(center.x+dx.x,center.y+dx.y);
            Misc.setColor(inner, AjimusUtils.EnsurePositive(getBrightness()));
            GL11.glVertex2f(center.x+dy.x,center.y+dy.y);
            Misc.setColor(inner, AjimusUtils.EnsurePositive(getBrightness()));
            GL11.glVertex2f(center.x-dx.x,center.y-dx.y);
            Misc.setColor(inner, AjimusUtils.EnsurePositive(getBrightness()));
            GL11.glVertex2f(center.x-dy.x,center.y-dy.y);
            Misc.setColor(inner, AjimusUtils.EnsurePositive(getBrightness()));
            GL11.glVertex2f(center.x+dx.x,center.y+dx.y);
            Misc.setColor(inner, AjimusUtils.EnsurePositive(getBrightness()));
            GL11.glEnd();
            GL11.glPopMatrix();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }

        public float getBrightness(){
            return MathUtils.clamp((beam==null)?fader.getBrightness():beam.getBrightness(),0,1);
        }

        public FaderUtil getFader() {
            return fader;
        }

        public float getInAngle() {
            return inAngle;
        }

        public float getOutAngle() {
            return outAngle;
        }

        public float getSize() {
            return size;
        }

        public Vector2f getInLoc() {
            return inLoc;
        }

        public Vector2f getOutLoc() {
            return outLoc;
        }

        public float getElapsed() {
            return elapsed;
        }

        public BeamAPI getBeam() {
            return beam;
        }

        public boolean isExpired(){
            return (beam!=null&&beam.getBrightness()<=0.05f)||(beam==null&&getBrightness()<=0);
        }
    }
}
