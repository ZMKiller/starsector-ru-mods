package data.scripts.util;

import com.fs.starfarer.api.Global;

public class CFT_txt {   
    private static final String CFT="CFT";
    public static String txt(String id){
        return Global.getSettings().getString(CFT, id);
    }    
}