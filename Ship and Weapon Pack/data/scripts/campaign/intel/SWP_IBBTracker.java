package data.scripts.campaign.intel;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.campaign.BaseScript;
import data.scripts.campaign.intel.SWP_IBBIntel.FamousBountyStage;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/* Solve this once and for all... */
public class SWP_IBBTracker extends BaseScript {

    public static FamousBountyStage getStage(int stageNum) {
        if (stageNum < FamousBountyStage.values().length && stageNum >= 0) {
            return FamousBountyStage.values()[stageNum];
        }

        return null;
    }

    public static SWP_IBBTracker getTracker() {
        for (EveryFrameScript script : Global.getSector().getScripts()) {
            if (script instanceof SWP_IBBTracker sWP_IBBTracker) {
                return sWP_IBBTracker;
            }
        }

        return null;
    }

    private final Set<String> stagesAllowed = new HashSet<>(FamousBountyStage.values().length);
    private final Set<String> stagesBegan = new HashSet<>(FamousBountyStage.values().length);
    private final Set<String> stagesCompleted = new HashSet<>(FamousBountyStage.values().length);
    private final Set<String> stagesPosted = new HashSet<>(FamousBountyStage.values().length);
    private final Set<String> stagesRepicked = new HashSet<>(FamousBountyStage.values().length);

    @Override
    public void advance(float amount) {
    }

    public boolean allStagesComplete() {
        return stagesCompleted.size() >= stagesAllowed.size();
    }

    public FamousBountyStage getLowestIncompleteNonRepickedStage() {
        FamousBountyStage lowestStage = null;
        for (String stageName : stagesAllowed) {
            FamousBountyStage stage = FamousBountyStage.valueOf(stageName);
            if (stage == null) {
                continue;
            }
            if (isStageComplete(stage)) {
                continue;
            }
            if (isStageRepicked(stage)) {
                continue;
            }

            if ((lowestStage == null) || (stage.ordinal() < lowestStage.ordinal())) {
                lowestStage = stage;
            }
        }

        return lowestStage;
    }

    public FamousBountyStage getLowestIncompleteStage() {
        FamousBountyStage lowestStage = null;
        for (String stageName : stagesAllowed) {
            FamousBountyStage stage = FamousBountyStage.valueOf(stageName);
            if (stage == null) {
                continue;
            }
            if (isStageComplete(stage)) {
                continue;
            }

            if ((lowestStage == null) || (stage.ordinal() < lowestStage.ordinal())) {
                lowestStage = stage;
            }
        }

        return lowestStage;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    public boolean isStageAllowed(FamousBountyStage stage) {
        if (stage == null) {
            return false;
        }
        return stagesAllowed.contains(stage.name());
    }

    public boolean isStageAvailable(FamousBountyStage stage) {
        if (stage == null) {
            return false;
        }
        if (!isStageAllowed(stage)) {
            return false;
        }
//        if (isStagePosted(stage)) {
//            return false;
//        }
        if (isStageBegun(stage)) {
            return false;
        }
        return !isStageComplete(stage);
    }

    public boolean isStageBegun(FamousBountyStage stage) {
        if (stage == null) {
            return false;
        }
        return stagesBegan.contains(stage.name());
    }

    public boolean isStageComplete(FamousBountyStage stage) {
        if (stage == null) {
            return false;
        }
        return stagesCompleted.contains(stage.name());
    }

    public boolean isStagePosted(FamousBountyStage stage) {
        if (stage == null) {
            return false;
        }
        return stagesPosted.contains(stage.name());
    }

    public boolean isStageRepicked(FamousBountyStage stage) {
        if (stage == null) {
            return false;
        }
        return stagesRepicked.contains(stage.name());
    }

    public boolean noStagesAvailable() {
        for (String stageName : stagesAllowed) {
            FamousBountyStage stage = FamousBountyStage.valueOf(stageName);
            if (stage == null) {
                continue;
            }

            if (isStageAvailable(stage)) {
                return false;
            }
        }

        return true;
    }

    public boolean noStagesComplete() {
        return stagesCompleted.size() <= 0;
    }

    public int getNumStagesBegun() {
        return stagesBegan.size();
    }

    public int getNumCompletedStages() {
        return stagesCompleted.size();
    }

    public int getPendingStages() {
        return stagesBegan.size() - stagesCompleted.size();
    }

    public void refresh() {
        stagesAllowed.clear();
        for (FamousBountyStage stage : FamousBountyStage.values()) {
            if (stage.mod.isLoaded()) {
                stagesAllowed.add(stage.name());
            }
        }

        Iterator<String> iter = stagesPosted.iterator();
        while (iter.hasNext()) {
            String stageName = iter.next();
            FamousBountyStage stage;
            try {
                stage = FamousBountyStage.valueOf(stageName);
            } catch (IllegalArgumentException ex) {
                stage = null;
            }
            if (stage == null) {
                iter.remove();
            }
        }

        iter = stagesRepicked.iterator();
        while (iter.hasNext()) {
            String stageName = iter.next();
            FamousBountyStage stage;
            try {
                stage = FamousBountyStage.valueOf(stageName);
            } catch (IllegalArgumentException ex) {
                stage = null;
            }
            if (stage == null) {
                iter.remove();
            }
        }

        iter = stagesBegan.iterator();
        while (iter.hasNext()) {
            String stageName = iter.next();
            FamousBountyStage stage;
            try {
                stage = FamousBountyStage.valueOf(stageName);
            } catch (IllegalArgumentException ex) {
                stage = null;
            }
            if (stage == null) {
                iter.remove();
            }
        }

        iter = stagesCompleted.iterator();
        while (iter.hasNext()) {
            String stageName = iter.next();
            FamousBountyStage stage;
            try {
                stage = FamousBountyStage.valueOf(stageName);
            } catch (IllegalArgumentException ex) {
                stage = null;
            }
            if (stage == null) {
                iter.remove();
            }
        }
    }

    public void reportStageBegan(FamousBountyStage stage) {
        if (stage == null) {
            return;
        }

        stagesBegan.add(stage.name());
        stagesPosted.remove(stage.name());
        stagesRepicked.remove(stage.name());
    }

    public void reportStageCompleted(FamousBountyStage stage) {
        if (stage == null) {
            return;
        }

        stagesBegan.add(stage.name());
        stagesCompleted.add(stage.name());
        stagesPosted.remove(stage.name());
        stagesRepicked.remove(stage.name());
    }

    public void reportStageExpired(FamousBountyStage stage) {
        if (stage == null) {
            return;
        }

        stagesBegan.remove(stage.name());
        stagesCompleted.remove(stage.name());
        stagesPosted.remove(stage.name());
        stagesRepicked.remove(stage.name());
    }

    public void reportStagePosted(FamousBountyStage stage) {
        if (stage == null) {
            return;
        }

        stagesPosted.add(stage.name());
    }

    public void reportStageRepicked(FamousBountyStage stage) {
        if (stage == null) {
            return;
        }

        stagesRepicked.add(stage.name());
        stagesPosted.remove(stage.name());
    }

    public void reset() {
        stagesBegan.clear();
        stagesCompleted.clear();
        stagesPosted.clear();
        stagesRepicked.clear();
    }
}
