package data.ai.HSIAutoFirePlugins;

import java.util.Comparator;

import com.fs.starfarer.api.combat.CombatEntityAPI;

public class HSIAutoFireTargetingUnitData {
    private CombatEntityAPI target;
    private float score;

    private float dist;

    public HSIAutoFireTargetingUnitData(CombatEntityAPI target,float score,float dist){
        setTarget(target);
        setScore(score);
        setDist(dist);
    }

    public CombatEntityAPI getTarget() {
        return target;
    }

    public void setTarget(CombatEntityAPI target) {
        this.target = target;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public float getDist(){
        return dist;
    }

    public void setDist(float dist){
        this.dist = dist;
    }

    public static class HSIAutoFireTargetingUnitDataScoreComparator implements Comparator<HSIAutoFireTargetingUnitData>{
        @Override
        public int compare(HSIAutoFireTargetingUnitData o1, HSIAutoFireTargetingUnitData o2) {
            return (int)(Math.ceil(o2.getScore()-o1.getScore()));
        }
    }

    public static class HSIAutoFireTargetingUnitDataRangeComparator implements Comparator<HSIAutoFireTargetingUnitData>{
        @Override
        public int compare(HSIAutoFireTargetingUnitData o1, HSIAutoFireTargetingUnitData o2) {
            return (int)(Math.ceil(o2.getScore()-o1.getScore()));
        }
    }
}
