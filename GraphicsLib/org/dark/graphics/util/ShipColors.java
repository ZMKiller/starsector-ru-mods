package org.dark.graphics.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModSpecAPI;
import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ShipColors {

    public static final Map<String, Color> EXPLOSION_COLORS = new HashMap<>(40);
    public static final Map<String, Color> SMOKE_COLORS = new HashMap<>(40);

    static {
        EXPLOSION_COLORS.put("LOW_TECH", new Color(255, 125, 25, 255));
        EXPLOSION_COLORS.put("MIDLINE", new Color(255, 145, 75, 255));
        EXPLOSION_COLORS.put("HIGH_TECH", new Color(100, 165, 255, 255));
    }

    static {
        SMOKE_COLORS.put("LOW_TECH", new Color(255, 125, 25, 255));
        SMOKE_COLORS.put("MIDLINE", new Color(255, 145, 75, 255));
        SMOKE_COLORS.put("HIGH_TECH", new Color(100, 165, 255, 255));
    }

    public static int clamp255(int x) {
        return Math.max(0, Math.min(255, x));
    }

    public static Color colorBlend(Color a, Color b, float amount) {
        float conjAmount = 1f - amount;
        return new Color(clamp255((int) (a.getRed() * conjAmount + b.getRed() * amount)),
                clamp255((int) (a.getGreen() * conjAmount + b.getGreen() * amount)),
                clamp255((int) (a.getBlue() * conjAmount + b.getBlue() * amount)),
                clamp255((int) (a.getAlpha() * conjAmount + b.getAlpha() * amount)));
    }

    public static Color colorJitter(Color color, float amount) {
        return new Color(clamp255((int) (color.getRed() + (int) (((float) Math.random() - 0.5f) * amount))),
                clamp255((int) (color.getGreen() + (int) (((float) Math.random() - 0.5f) * amount))),
                clamp255((int) (color.getBlue() + (int) (((float) Math.random() - 0.5f) * amount))),
                color.getAlpha());
    }

    public static void init() throws IOException, JSONException {
        for (ModSpecAPI mod : Global.getSettings().getModManager().getEnabledModsCopy()) {
            JSONObject head;
            try {
                head = Global.getSettings().loadJSON("data/config/hull_styles.json", mod.getId());
            } catch (RuntimeException e) {
                continue;
            }

            Iterator iter = head.keys();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                JSONObject hullSpec = head.getJSONObject(key);
                JSONArray explosionColorArray = hullSpec.optJSONArray("baseCombatExplosionColor");
                JSONArray smokeColorArray = hullSpec.optJSONArray("weaponDisabledExplosionColor");
                if (explosionColorArray != null) {
                    int red, green, blue, alpha;
                    red = explosionColorArray.optInt(0, 255);
                    green = explosionColorArray.optInt(1, 255);
                    blue = explosionColorArray.optInt(2, 255);
                    alpha = explosionColorArray.optInt(3, 255);
                    Color color = new Color(clamp255((int) red), clamp255((int) green), clamp255((int) blue), clamp255((int) alpha));
                    EXPLOSION_COLORS.put(key, color);
                    Global.getLogger(ShipColors.class).info("Loaded explosion color for " + key + ": " + color);
                }
                if (smokeColorArray != null) {
                    int red, green, blue, alpha;
                    red = smokeColorArray.optInt(0, 0);
                    green = smokeColorArray.optInt(1, 0);
                    blue = smokeColorArray.optInt(2, 0);
                    alpha = smokeColorArray.optInt(3, 255);
                    Color color = new Color(clamp255((int) red), clamp255((int) green), clamp255((int) blue), clamp255((int) alpha));
                    SMOKE_COLORS.put(key, color);
                    Global.getLogger(ShipColors.class).info("Loaded smoke color for " + key + ": " + color);
                }
            }
        }
    }
}
