#version 110
//from https://www.shadertoy.com/view/Mlcczr @https://www.shadertoy.com/user/laserdog
//from @Shane
const vec2 s = vec2(1, 1.7320508); // 1.7320508 = sqrt(3)
const vec3 baseCol = vec3(0.05098, 0.25098, 0.2784);
const float borderThickness = 0.04;

uniform vec4 state;
uniform vec2 resolution;
//uniform sampler2D renderMap;
varying vec2 fragUV;

float calcHexDistance(vec2 p)
{
    p = abs(p);
    return max(dot(p, s * 0.5), p.x);
}

vec4 calcHexInfo(vec2 uv)
{
    vec4 hexCenter = round(vec4(uv, uv - vec2(0.5, 1.0)) / s.xyxy);
    vec4 offset = vec4(uv - hexCenter.xy * s, uv - (hexCenter.zw + 0.5) * s);
    return dot(offset.xy, offset.xy) < dot(offset.zw, offset.zw) ? vec4(offset.xy, hexCenter.xy) : vec4(offset.zw, hexCenter.zw);
}

float nsmoothstep(float edge0, float edge1, float x){
    float t;
    t = clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
    return (3.0 - 2.0 * t) * t * t;
}

void main()
{
    vec2 uv = 10.0 * (2.0 * fragUV - resolution.xy) / resolution.y;
    //vec2 uv = 10.0 * (fragUV);
    
    vec4 hexInfo = calcHexInfo(uv);
    float totalDist = calcHexDistance(hexInfo.xy) + borderThickness;
    vec4 fragColor = vec4(0.0,0.0,0.0,0.5);
    
    float angle = atan(hexInfo.y, hexInfo.x);
    vec3 isoline = baseCol*0.5;

    float alpha = 0.0;   
    float life = state.y;
    float dist = length((fragUV - 0.5) * 2.0)*256.0;
    float bandSize = state.z/10.0;
    float startDist = life*state.z*1.415;
    alpha = sin((dist-startDist)/bandSize);
    if(dist<startDist||dist>startDist+bandSize){
        alpha = 0.0;
    }
    float sinOffset = 1.0;
    float aa = 5.0 / resolution.y;
    fragColor.rgb= (nsmoothstep(0.51, 0.51 - aa, totalDist) + pow(1. - max(0.0, 0.5 - totalDist), 20.0) * 1.5)
        * (baseCol +vec3(0.0, 0.1, 0.09)) + isoline + baseCol * nsmoothstep(1.2 + sinOffset, 0.2 + sinOffset - aa, totalDist); 
    gl_FragColor = fragColor;
}