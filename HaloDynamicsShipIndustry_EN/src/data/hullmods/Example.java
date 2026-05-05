package data.hullmods;// =路径

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;

public class Example extends BaseHullMod {//hullmod是较为特殊的类。每个携带此船插的船都会共用同一个对象，因此最好不要在这个类中存储变量
    private static float TRANSFER_RATIO = 0.35f;
    private static float COOLDOWN = 60f;

    private static float RANGE = 2000f;
    private static String KEY = "Example";

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {//战斗中每帧
        if(Global.getCombatEngine().isPaused()) return;
        TimeoutTracker<ShipAPI> tracker = new TimeoutTracker<>();
        if(ship.getCustomData().containsKey(KEY)){//尝试从customdata里获得指定key对应的计时器-这里使用原版自带的TimeoutTracker
            tracker = (TimeoutTracker<ShipAPI>) ship.getCustomData().get(KEY);
        }else{//如果没有计时器那就放进去一个
            ship.setCustomData(KEY,tracker);
        }
        tracker.advance(amount);
        for(ShipAPI ally: Global.getCombatEngine().getShips()){//遍历战场上的船
            if(!ally.isAlive()||ally.isFighter()||ally.getOwner()!=ship.getOwner()|| Misc.getDistance(ship.getLocation(),ally.getLocation())>RANGE) continue;//continue会不运行下面的代码直接进入到下个循环 在这里把非法目标都排除掉
            //owner = 0 , 1, 100 分别代表友军 敌人 中立-一般指尸体 这个可以在api里某处注释查到


            if(ally.getFluxTracker().isOverloaded()&&!tracker.contains(ally)){//检查目标是否在过载和是否处于此效果的冷却中
                ally.getFluxTracker().stopOverload();//取消过载
                float flux = ally.getFluxTracker().getCurrFlux()*TRANSFER_RATIO;//获得需要转移的幅能
                flux = Math.min(ship.getMaxFlux()*(1f-ship.getFluxLevel()),flux);//取 自身空余幅能 和 转移幅能的较小者 <--上限在这里控制
                flux--;
                ally.getFluxTracker().decreaseFlux(flux);
                ship.getFluxTracker().increaseFlux(flux,false);
                tracker.set(ally,COOLDOWN);//在COOLDOWN走完后它会自动移除ally
            }

        }
    }
}
