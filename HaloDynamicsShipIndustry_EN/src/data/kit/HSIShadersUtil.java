package data.kit;

import java.io.IOException;

import com.fs.starfarer.api.Global;

public class HSIShadersUtil {
    public class HSIShield {
        public static final String vert = "#version 110\n" +
                "varying vec2 fragUV;\n" +
                "void main() {\n" +
                "gl_Position = ftransform();\n" +
                "fragUV = gl_MultiTexCoord0.xy;\n" +
                "}";
        public static final String frag = "#version 110\n" +

                "uniform sampler2D shieldTex;\n" +
                "uniform sampler2D fxTex;\n" +
                "uniform vec4 state;\n" +

                "varying vec2 fragUV;\n" +

                "void main() {\n" +
                "vec4 col = texture2D(shieldTex,fragUV);\n" +
                "float alpha = 0;  \n" +
                "float life = state.y;\n" +
                "float dist = length((fragUV - 0.5) * 2.0)*256.0;\n" +
                "float bandSize = state.z/10.0;\n" +
                "float startDist = life*state.z*1.415;\n" +
                "if(dist<startDist){\n" +
                "alpha = 0;\n" +
                "}else if(dist>startDist+bandSize){\n" +
                "alpha = 0;\n" +
                "}else{\n" +
                "alpha = sin((dist-startDist)/bandSize*180.0);\n" +
                "}\n" +
                "gl_FragColor = col*alpha;\n" +
                "}\n";
    }

    public static String getShader(String path) {
        String shader = "";
        try {
            shader = Global.getSettings().loadText(path);
        } catch (IOException ex) {
            return shader;
        }
        return shader;
    }
}
