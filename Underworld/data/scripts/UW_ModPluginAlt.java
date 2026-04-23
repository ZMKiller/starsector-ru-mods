package data.scripts;

import com.fs.starfarer.api.Global;
import data.scripts.world.underworld.UW_Styx;
import exerelin.campaign.SectorManager;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;

public class UW_ModPluginAlt {

    static void initGraphicsLib() {
        ShaderLib.init();

        if (ShaderLib.areShadersAllowed() && ShaderLib.areBuffersAllowed()) {
            LightData.readLightDataCSV("data/lights/uw_light_data.csv");
            TextureData.readTextureDataCSV("data/lights/uw_texture_data.csv");
        }
    }

    static void initUW() {
        new UW_Styx().generate(Global.getSector());
        if (UnderworldModPlugin.isExerelin && !SectorManager.getManager().isCorvusMode()) {
            return;
        }
    }
}
