/*
By Tartiflette
 */
package data.scripts.util;

import com.fs.starfarer.api.Global;

public class HIVER_txt {   
    private static final String HIVER="HIVER";
    public static String txt(String id){
        return Global.getSettings().getString(HIVER, id);
    }    
}