#version 110

// Noise animation - Electric
// by nimitz (stormoid.com) (twitter: @stormoid)
// License Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
// Contact the author for other licensing options

//The domain is displaced by two fbm calls one for each axis.
//Turbulent fbm (aka ridged) is used for better effect.

#define tau 6.2831853

uniform sampler2D text0;
uniform float time;
uniform float alpha;

varying vec2 fragUV;

mat2 makem2(float theta){float c = cos(theta);float s = sin(theta);return mat2(c,-s,s,c);}


float noise(vec2 x){
    return 2.*texture2D(text0, x).x;
}

float fbm(vec2 p)
{
    float z=2.;
    float rz = 0.;
    vec2 bp = p;
    for (float i= 1.;i < 6.;i++)
    {
        rz+= abs((noise(p)-0.5)*2.)/z;
        z = z*2.;
        p = p*2.;
    }
    return rz;
}

float dualfbm(vec2 p)
{
    //get two rotated fbm calls and displace the domain
    vec2 p2 = p*.7;
    vec2 basis = vec2(fbm(p2-time*1.6),fbm(p2+time*1.7));
    basis = (basis-.5)*.2;
    p += basis;

    //coloring
    return fbm(p*makem2(time*0.02));
}

float circ(vec2 p)
{
    float r = length(p);
    r = log(sqrt(r));
    return abs(mod(r*4.,tau)-3.14)*3.+.2;

}

void main()
{
    //setup system
    vec2 p = fragUV-0.5;
    p*=0.7;

    float rz = dualfbm(p);

    //final color
    vec3 col = vec3(107.0, 148.0, 196.0)/255.0/rz;
    p /= exp(mod(time*10.,3.14159));
    col=pow(abs(col),vec3(.99));
    float ALPHA = 0.8;
    if(2.0*length(fragUV-0.5)>0.5) ALPHA = ALPHA*(1.0-((2.0*length(fragUV-0.5)-0.5)/0.5));
    gl_FragColor = vec4(col,1.0)*ALPHA*alpha;
}