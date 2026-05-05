package data.kit;

import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.util.Misc;

import java.util.ArrayList;
import java.util.List;

public class HSIMathUtil {
    public static float getVectorAngle(Vector2f vector){
        float angle = Misc.getAngleInDegrees(vector);
        if(angle<0f) angle+=360f;
        return angle;
    }

    public static float getNormalizedAngle(float angle){
        float ang = angle;
        if(ang<0) ang+=360f;
        return ang%360f;
    }

    public static List<Vector2f> getCollidePointOnEllipse(float a,float b,float k,float m){
        float ra = a;
        float rb = b;

        if(ra<rb){
            rb = a;
            ra = b;
        }

        float x1 = 0;
        float y1 = 0;
        float x2 = 0;
        float y2 = 0;
        float a1 = b * b + a * a * k * k;
        float b1 = 2 * k * a * a * m;
        float c1 = a * a * (m * m - b * b);
        float delta = b1 * b1 - 4 * a1 * c1;
        List<Vector2f> result = new ArrayList<>();
        if (delta >= 0) {
            // 求两点坐标
            // x = (-b ± sqrt(delta)) / (2a)
            x1 = (float) (-b1 + Math.sqrt(delta)) / (2 * a1);
            x2 = (float)(-b1 - Math.sqrt(delta)) / (2 * a1);
            y1 = k * x1 + m;
            y2 = k * x2 + m;
            result.add(new Vector2f(x1,y1));
            result.add(new Vector2f(x2,y2));
        }
        return result;
    }

    /*public static boolean isStringLandedInEllipse(float a,float b,Vector2f pivot,float facing,Vector2f end){
        float toR = 90f-facing;
        Vector2f p1 = new Vector2f();
        VectorUtils.rotateAroundPivot(new Vector2f(end),pivot,toR,p1);
        float c = Math.sqrt()
    }*/

    //将任意两点转换为标准坐标系中的直线公式 y = kx + m;
    //.x = k,.y = m
    public static Vector2f getStandardLineParma(Vector2f pivot,float facing,Vector2f start,Vector2f end){
        float toR = 90f-facing;
        Vector2f p1 = new Vector2f(),p2 = new Vector2f();
        VectorUtils.rotateAroundPivot(new Vector2f(start),pivot,toR,p1);
        VectorUtils.rotateAroundPivot(new Vector2f(end),pivot,toR,p2);
        p1 = new Vector2f(p1.x-pivot.x,p1.y-pivot.y);
        p2 = new Vector2f(p2.x-pivot.x,p2.y-pivot.y);
        float k = (p1.x == p2.x)?Float.MAX_VALUE:(p2.y-p1.y)/(p2.x-p1.x);
        float m = (p1.x == p2.x)?0:(p1.y-p1.x*k);
        return new Vector2f(k,m);
    }


}
