package org.dark.graphics.util;

import com.fs.starfarer.api.combat.BoundsAPI;
import com.fs.starfarer.api.combat.BoundsAPI.SegmentAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.GLUtessellatorCallbackAdapter;
import org.lwjgl.util.glu.tessellation.GLUtessellatorImpl;
import org.lwjgl.util.vector.Vector2f;

public class Tessellate {

    private static final Map<ShipAPI, TessData> tessMap = new WeakHashMap<>(1000);

    public static void clearCache() {
        tessMap.clear();
    }

    public static void render(BoundsAPI bounds, float r, float g, float b, ShipAPI ship) {
        TessData tessData = tessMap.get(ship);
        double checksum = 0.0;
        if ((tessData != null) && !tessData.vertices.isEmpty()) {
            final List<SegmentAPI> segments = bounds.getOrigSegments();
            for (SegmentAPI segment : segments) {
                checksum += segment.getP1().x;
                checksum += segment.getP1().y;
            }
            if (tessData.checksum != checksum) {
                tessMap.remove(ship);
                tessData = null;
            }
        }
        if ((tessData == null) || (tessData.vertices.isEmpty())) {
            if (tessData != null) {
                tessMap.remove(ship);
            }
            tessData = new TessData(ship.getLocation(), ship.getFacing(), checksum);

            GLUtessellatorImpl tesselator = (GLUtessellatorImpl) GLU.gluNewTess();
            TessCallbackV2 callback = new TessCallbackV2(tessData);
            tesselator.gluTessCallback(GLU.GLU_TESS_VERTEX, callback);
            tesselator.gluTessCallback(GLU.GLU_TESS_BEGIN, callback);
            tesselator.gluTessCallback(GLU.GLU_TESS_END, callback);
            tesselator.gluTessCallback(GLU.GLU_TESS_COMBINE, callback);
            tesselator.gluTessCallback(GLU.GLU_TESS_EDGE_FLAG, callback);

            tesselator.gluTessProperty(GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_ODD);
            tesselator.gluTessBeginPolygon(null);
            tesselator.gluTessBeginContour();

            bounds.update(ship.getLocation(), ship.getFacing());
            final List<SegmentAPI> segments = bounds.getSegments();
            final List<Vector2f> points = new ArrayList<>(segments.size());
            for (SegmentAPI segment : segments) {
                points.add(segment.getP1());
            }
            double[][] data = new double[points.size()][6];
            for (int i = 0; i < points.size(); i++) {
                Vector2f v = points.get(i);
                data[i][0] = v.x;
                data[i][1] = v.y;
                data[i][2] = 0f;
                data[i][3] = r;
                data[i][4] = g;
                data[i][5] = b;
            }

            for (int i = 0; i < points.size(); i++) {
                tesselator.gluTessVertex(data[i], 0, new VertexDataV2(data[i])); //store the vertex
            }

            tesselator.gluTessEndContour();
            tesselator.gluTessEndPolygon();
            tesselator.gluDeleteTess();

            tessMap.put(ship, tessData);
        } else {
            GL11.glBegin(tessData.glType);
            GL11.glColor3f(1f, 1f, 1f);

            for (VertexDataV2 vertex : tessData.vertices) {
                Vector2f vertexVec = new Vector2f((float) vertex.data[0], (float) vertex.data[1]);
                VectorUtils.rotate(vertexVec, ship.getFacing());
                Vector2f.add(vertexVec, ship.getLocation(), vertexVec);

                GL11.glVertex2f(vertexVec.x, vertexVec.y);
            }

            GL11.glEnd();
        }
    }

    public static class TessCallbackV2 extends GLUtessellatorCallbackAdapter {

        private final WeakReference<TessData> tessRef;

        TessCallbackV2(TessData tessData) {
            this.tessRef = new WeakReference<>(tessData);
        }

        @Override
        public void begin(int type) {
            GL11.glBegin(type);

            TessData tessData = tessRef.get();
            if (tessData != null) {
                tessData.glType = type;
            }
        }

        @Override
        public void combine(double[] coords, Object[] data, float[] weight, Object[] outData) {
            for (int i = 0; i < outData.length; i++) {
                double[] combined = new double[6];
                combined[0] = coords[0];
                combined[1] = coords[1];
                combined[2] = coords[2];
                combined[3] = 1;
                combined[4] = 1;
                combined[5] = 1;

                outData[i] = new VertexDataV2(combined);
            }
        }

        @Override
        public void edgeFlag(boolean boundaryEdge) {
            GL11.glEdgeFlag(boundaryEdge);
        }

        @Override
        public void end() {
            GL11.glEnd();
        }

        @Override
        public void vertex(Object vertexData) {
            VertexDataV2 vertex = (VertexDataV2) vertexData;

            GL11.glVertex3d(vertex.data[0], vertex.data[1], vertex.data[2]);
            GL11.glColor3d(vertex.data[3], vertex.data[4], vertex.data[5]);

            TessData tessData = tessRef.get();
            if (tessData != null) {
                Vector2f vertexVec = new Vector2f((float) vertex.data[0], (float) vertex.data[1]);
                Vector2f.sub(vertexVec, tessData.initShipLocation, vertexVec);
                VectorUtils.rotate(vertexVec, -tessData.initShipFacing);

                double[] unUpdated = new double[6];
                unUpdated[0] = vertexVec.x;
                unUpdated[1] = vertexVec.y;
                unUpdated[2] = vertex.data[2];
                unUpdated[3] = vertex.data[3];
                unUpdated[4] = vertex.data[4];
                unUpdated[5] = vertex.data[5];
                tessData.vertices.add(new VertexDataV2(unUpdated));
            }
        }
    }

    public static class VertexDataV2 {

        public double[] data;

        VertexDataV2(double[] data) {
            this.data = data;
        }
    }

    public static class TessData {

        public List<VertexDataV2> vertices = new ArrayList<>();
        public Vector2f initShipLocation;
        public float initShipFacing;
        public int glType;
        public double checksum;

        TessData(Vector2f shipLocation, float shipFacing, double checksum) {
            initShipLocation = new Vector2f(shipLocation);
            initShipFacing = shipFacing;
            this.checksum = checksum;
        }
    }
}
