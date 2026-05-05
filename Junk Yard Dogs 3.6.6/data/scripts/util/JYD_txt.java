/*
By Tartiflette
 */
package data.scripts.util;

import com.fs.starfarer.api.Global;

public class JYD_txt {   
    private static final String JYD="JYD";
    public static String txt(String id){
        return Global.getSettings().getString(JYD, id);
    }    
}