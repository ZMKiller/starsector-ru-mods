package data.scripts.HSIGenerator;

import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.ThemeGenContext;

public class HSIThemeGenerator extends BaseThemeGenerator{
    public static final String HSITC1ItemsKey = "HSI_TC1_Data";

    public void generateHSITC1Targets(){
        
    }

    @Override
    public void generateForSector(ThemeGenContext context, float allowedSectorFraction){
        
    }

    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    public String getThemeId() {
        return "HSI_ThemeGenerator";
    }
}
