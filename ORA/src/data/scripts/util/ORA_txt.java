/*
By Tartiflette
 */
package data.scripts.util;

import com.fs.starfarer.api.Global;

public class ORA_txt {   
    private static final String ORA="ORA";
    public static String txt(String id){
        return Global.getSettings().getString(ORA, id);
    }    
}