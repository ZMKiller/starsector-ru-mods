package data.weapons.scripts.HWIMaid;

import com.fs.starfarer.api.util.FaderUtil;

public class HWIMaidTaskData {
    public enum TaskType{
        ARMOR,HITPOINT,WEAPON;
    }

    public TaskType type;
    public Object param;
    //vector2i for armor grid location
    //weaponapi for weapon repair
    public FaderUtil progress = new FaderUtil(0f,4f);
    //if progress is finished it will repair something

    public void advance(float amount){
        progress.fadeIn();
        progress.advance(amount);
    }

    public boolean isFinished(){
        return progress.isFadedIn();
    }

}
