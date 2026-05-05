package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Skills;


public class JYDmodPlugin extends BaseModPlugin {

   public void onApplicationLoad() {
        {
            boolean hasLazyLib = Global.getSettings().getModManager().isModEnabled("lw_lazylib");
            if (!hasLazyLib) {
                throw new RuntimeException("Junk Yard Dogs LazyLib!"
                        + "\nGet it at http://fractalsoftworks.com/forum/index.php?topic=5444");
            }

            boolean hasMagicLib = Global.getSettings().getModManager().isModEnabled("MagicLib");
            if (!hasMagicLib) {
                throw new RuntimeException("Junk Yard Dogs requires MagicLib!" + "\nGet it at http://fractalsoftworks.com/forum/index.php?topic=13718");
            }
        }
    }
	
}
	

