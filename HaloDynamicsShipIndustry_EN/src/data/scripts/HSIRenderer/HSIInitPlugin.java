package data.scripts.HSIRenderer;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import data.kit.HSIShadersUtil;

public class HSIInitPlugin extends BaseEveryFrameCombatPlugin {

    public static boolean GlobalInit = false;

    public static HSICombatRendererV2.HSICombatRendererV2ShaderManager ShaderManager = new HSICombatRendererV2.HSICombatRendererV2ShaderManager();

    private static final String ShieldVertKey = "data/shaders/HSIShield.vert";
    private static final String ShieldFragKey = "data/shaders/HSIShield.frag";
    private static final String ShieldShaderKey  = "HSI_Shield";

    private static final String HSIDisplacerVertKey = "data/shaders/HSIEdgeEffect.vert";
    private static final String HSIDisplacerFragKey = "data/shaders/HSIEdgeEffect.frag";
    public static final String HSIDisplacerKey = "HSI_Displacer";

    private static final String HSITimeWarpVertKey = "data/shaders/HSITimeWarp.vert";
    private static final String HSITimeWarpFragKey = "data/shaders/HSITimeWarp.frag";
    private static final String HSITimeWarpKey = "HSI_TimeWarp";

    private static final String ventVert = "data/shaders/HSIGalaxyVent.vert";
    private static final String ventFrag = "data/shaders/HSIGalaxyVent.frag";
    public static final String SPECIAL_VENT_SHADER = "HSISpecialVentShader";

    public static final String thunderstormVert = "data/shaders/HSIThunderStorm.vert";
    public static final String thunderstormFrag = "data/shaders/HSIThunderStorm.frag";
    public static final String thunderstormKey = "HSIThunderStormShader";

    @Override
    public void init(CombatEngineAPI engine) {
        if(!GlobalInit){
            GlobalInit = true;
            ShaderManager.createShader(HSIShadersUtil.getShader(ShieldVertKey), HSIShadersUtil.getShader(ShieldFragKey),ShieldShaderKey);
            ShaderManager.createShader(
                    HSIShadersUtil.getShader(HSIDisplacerVertKey), HSIShadersUtil.getShader(HSIDisplacerFragKey),
                    HSIDisplacerKey);
            ShaderManager.createShader(
                    HSIShadersUtil.getShader(HSITimeWarpVertKey), HSIShadersUtil.getShader(HSITimeWarpFragKey),
                    HSITimeWarpKey);
            ShaderManager.createShader(HSIShadersUtil.getShader(ventVert), HSIShadersUtil.getShader(ventFrag), SPECIAL_VENT_SHADER);
           // ShaderManager.createShader(HSIShadersUtil.getShader(thunderstormVert), HSIShadersUtil.getShader(thunderstormFrag), thunderstormKey);
        }
    }
}
