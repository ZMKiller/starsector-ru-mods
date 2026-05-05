package data.weapons.scripts;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;

public class HWITestWeaponEffect implements EveryFrameWeaponEffectPlugin {
    private boolean init = true;
    private WeaponAPI weapon;
    private HWITestWeaponRender render;

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (init) {
            init(weapon);
        }
    }

    private void init(WeaponAPI weapon) {
        this.weapon = weapon;
        render = new HWITestWeaponRender(this);
        Global.getCombatEngine().addLayeredRenderingPlugin(render);
        init = false;
    }

    public class HWITestWeaponRender extends BaseCombatLayeredRenderingPlugin {
        private HWITestWeaponEffect effect;
        private SpriteAPI p = Global.getSettings().getSprite("HSI_fx", "Magic");
        private Vector2f renderLoc;

        public HWITestWeaponRender(HWITestWeaponEffect effect) {
            this.effect = effect;
            this.layer = CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER;
            p.setSize(200f, 200f);
            renderLoc = effect.weapon.getLocation();
        }

        public void render(CombatEngineLayers layer, ViewportAPI viewport) {
            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, p.getTextureId());
            // GL11.glEnable(GL11.GL_BLEND);
            // GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
            // GL11.glBegin(GL11.GL_QUADS);
            // GL11.glBegin(GL11.GL_CULL_FACE);
            // GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glTranslatef(renderLoc.getX(), renderLoc.getY(), 0.1f);
            GL11.glRotatef(75f, 1f, 0f, 0f);
            GL11.glTexCoord2f(0f, 1f);
            GL11.glVertex3f(-100f, 100f, 0f);
            
            GL11.glTexCoord2f(1f, 1f);
            GL11.glVertex3f(100f, 100f, 0f);
            
            GL11.glTexCoord2f(1f, -1f);
            GL11.glVertex3f(100f, -100f, 0f);
            
            GL11.glTexCoord2f(0f, -1f);
            GL11.glVertex3f(-100f, -100f, 0f);
            // p.renderAtCenter(0, 0);
            // GL11.glRotatef(STAR_ANGLE, 0f, 0f, 1f);
            // GL11.glRotatef(STAR_ANGLE_1, 1f, 0f, 0f);
            // GL11.glColor3ub((byte) STAR.getRed(), (byte) STAR.getGreen(), (byte)
            // STAR.getGreen());
            // GL11.glEnd();
            GL11.glPopMatrix();
            //GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);
            // Global.getCombatEngine().maintainStatusForPlayerShip(this, null,
            // AADSRENDERPLUGIN_KEY, "render", false);
        }

        public void advance(float amount) {
            if (Global.getCombatEngine().isPaused())
                return;
            renderLoc = effect.weapon.getLocation();
            p.setAngle(p.getAngle() + amount * 12f);
        }

        public float getRenderRadius() {
            return 20000f;
        }

        public boolean isExpired() {
            return !(effect.weapon == null) && (effect.weapon.getShip() == null || !effect.weapon.getShip().isAlive()
                    || !Global.getCombatEngine().isEntityInPlay(effect.weapon.getShip()));
        }
    }

}
