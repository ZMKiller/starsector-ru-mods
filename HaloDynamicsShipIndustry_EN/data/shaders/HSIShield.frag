#version 110

uniform sampler2D shieldTex;
uniform sampler2D fxTex;
uniform float type;
uniform vec4 state;

varying vec2 fragUV;

vec2 recoverShieldUV(float size,vec2 uv,vec2 rel){
    return ((rel*256.0+(uv-0.5)*2.0*size)/256.0);
}

void main() {
    vec4 col = texture2D(shieldTex,fragUV);
    float alpha = 0.0;
    //type =>mode: 0 for normal band spread | 1 for hit | 2 for nothing-> stencil func
    if(type<=0.5){
        col.w = 0.5;   
        float life = state.y;
        float dist = length((fragUV - 0.5) * 2.0)*256.0;
        float bandSize = state.z/10.0*((life<0.1)?life*10.0:1.0);
        float combinedBaseAlpha = state.w;
        float startDist = life*state.z;
        alpha = sin(3.1415926*(dist-startDist)/bandSize);
        if(dist<startDist||dist>startDist+bandSize){
            alpha = 0.0;
        }
        alpha = alpha*combinedBaseAlpha;
        if(alpha>1.0) alpha = 1.0;
        //alpha=alpha*combinedBaseAlpha;
        //alpha*= (sin(3.1415926*life));
        //alpha*=0.8;
    }else if(type<= 2.5){
        float life = state.x;
        float size = state.y;//hit size
        vec2 relUV = state.zw;//shieldTex uv for hitpoint
        float combinedShieldAlphaBase = type-1.0;
        vec2 sampleUV = recoverShieldUV(size*0.5,fragUV,relUV);
        col = texture2D(shieldTex,sampleUV);
        vec4 col2 = texture2D(fxTex,fragUV);
        col = col*col2;
        float dist = length(fragUV - 0.5)*size;
        alpha = sin(3.1415926*(dist-0.5*life*size)/0.5*life*size);
        if(dist>size){
            alpha = 0.0;
        }
        if(dist<=0.5*life*size){
            alpha = 1.0;
        }
        alpha=alpha*combinedShieldAlphaBase;
        if(alpha>1.0) alpha = 1.0;
    }else{
        alpha = 1.0;
    }
    //vec4 alphaVec = (1.0,1.0,1.0,alpha);
    gl_FragColor = vec4(col.xyz,col.w*alpha);
}