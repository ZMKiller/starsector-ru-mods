#version 110

uniform sampler2D shipTex;
uniform float effectlevel;

varying vec2 fragUV;


void main() {
    vec4 col = texture2D(shipTex,fragUV);
    vec4 final = vec4(0.1,0.67,0.88,0.0);
    float lim = 0.8;   
    if(col.w>0.1&&((col.x+col.y+col.z)<lim)){
        float alpha = ((lim-col.x-col.y-col.z)/lim);
        alpha = alpha*alpha;
        alpha = alpha*effectlevel*1.25*col.w;
        if(alpha>1.0) alpha = 1.0;
        final = vec4(final.x,final.y,final.z,alpha);
    }
    gl_FragColor = final;
}