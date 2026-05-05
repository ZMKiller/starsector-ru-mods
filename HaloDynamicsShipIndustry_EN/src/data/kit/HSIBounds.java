package data.kit;

import com.fs.starfarer.api.combat.BoundsAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class HSIBounds implements BoundsAPI {

    public static class HSISegement implements SegmentAPI{

        private Vector2f P1;
        private Vector2f P2;

        public HSISegement(Vector2f P1,Vector2f P2){
            this.P1 = P1;
            this.P2 = P2;
        }

        @Override
        public Vector2f getP1() {
            return new Vector2f(P1);
        }

        @Override
        public Vector2f getP2() {
            return new Vector2f(P2);
        }

        @Override
        public void set(float v1, float v2, float v3, float v4) {
            this.P1 = new Vector2f(v1,v2);
            this.P2 = new Vector2f(v3,v4);
        }
    }

    private CombatEntityAPI anchor;

    private List<SegmentAPI> orig = new ArrayList<>();

    private List<SegmentAPI> updated = new ArrayList<>();

    public HSIBounds(CombatEntityAPI anchor){
        this.anchor = anchor;
    }
    @Override
    public void update(Vector2f location, float facing) {
        updated.clear();
        for(SegmentAPI segement:orig){
            HSISegement u = new HSISegement(AjimusUtils.getEngineCoordFromRelativeCoord(location,segement.getP1(),facing),AjimusUtils.getEngineCoordFromRelativeCoord(location,segement.getP2(),facing));
            updated.add(u);
        }
    }

    @Override
    public List<SegmentAPI> getSegments() {
        return updated;
    }

    @Override
    public void clear() {
        orig.clear();
        updated.clear();
    }

    @Override
    public void addSegment(float x1, float y1, float x2, float y2) {
        orig.add(new HSISegement(new Vector2f(x1,y1),new Vector2f(x2,y2)));
        //update(anchor.getLocation(),anchor.getFacing());
    }

    @Override
    public void addSegment(float x2, float y2) {
        if(orig.isEmpty()) return;
        int l = orig.size();
        SegmentAPI last = orig.get(l-1);
        orig.add(new HSISegement(new Vector2f(last.getP2()),new Vector2f(x2,y2)));
    }

    public void addSegment(Vector2f v1,Vector2f v2){
        orig.add(new HSISegement(new Vector2f(v1),new Vector2f(v2)));
        //updated(anchor.getLocation(),anchor.getFacing());
    }



    @Override
    public List<SegmentAPI> getOrigSegments() {
        return orig;
    }
}
