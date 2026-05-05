//Noise animation - Electric
//by nimitz (stormoid.com) (twitter: @stormoid)
//modified to look like a portal by Pleh
//fbm tweaks by foxes

//The domain is displaced by two fbm calls one for each axis.
//Turbulent fbm (aka ridged) is used for better effect.

#version 110

uniform vec2 ta;
uniform sampler2D noise;

varying vec2 fragUV;

mat2 makem2(float theta){float c = cos(theta);float s = sin(theta);return mat2(c,-s,s,c);}
float noise(vec2 x ){return texture2D(noise.x*0.01,noise.y*0.01).x;}

float fbm(vec2 p)
{
    time = 0.15f*ta.x;
    vec4 tt=fract(vec4(time)+vec4(0.0,0.25,0.5,0.75));
    vec2 p1=p-normalize(p)*tt.x;
    vec2 p2=vec2(1.0)+p-normalize(p)*tt.y;
    vec2 p3=vec2(2.0)+p-normalize(p)*tt.z;
    vec2 p4=vec2(3.0)+p-normalize(p)*tt.w;
    vec4 tr=vec4(1.0)-abs(tt-vec4(0.5))*2.0;//*vec4(0.0,1.0,0.0,1.0);
    float z=2.;
    vec4 rz = vec4(0.);
    for (float i= 1.;i < 4.;i++)
    {
        rz+= abs((vec4(noise(p1),noise(p2),noise(p3),noise(p4))-vec4(0.5))*2.)/z;
        z = z*2.;
        p1 = p1*2.;
        p2 = p2*2.;
        p3 = p3*2.;
        p4 = p4*2.;
    }
    return dot(rz,tr)*0.25;
}
float dualfbm(vec2 p)
{
    //get two rotated fbm calls and displace the domain
    vec2 p2 = p*0.7;
    vec2 basis = vec2(fbm(p2-time*1.6),fbm(p2+time*1.7));
    basis = (basis-.5)*0.2;
    p += basis;

    //coloring
    return fbm(p); //*makem2(time*2.0));
}

float circ(vec2 p)
{
    float r = length(p);
    r = log(sqrt(r));
    return abs(mod(r*2.,6.2831853)-4.54)*3.+.5;

}

void main() {
{
    //setup system
    vec2 p = fragUV-0.5;

    float rz = dualfbm(p);

    //rings
    //p /= 7.0; //exp(mod(time*10.,3.14159));
    //rz *= pow(abs((0.0-circ(p))),.99);

    rz *= abs((-circ(vec2(p.x / 20.0*ta.y, p.y / 10.0*ta.y))));
    rz *= abs((-circ(vec2(p.x / 20.0*ta.y, p.y / 10.0*ta.y))));
    //rz *= abs((-circ(vec2(p.x / 9.0, p.y / 4.0))));
    //rz *= abs((-circ(vec2(p.x / 9.0, p.y / 4.0))));

    //final color
    vec3 col = vec3(.05,0.1,0.3)/rz;
    col=pow(abs(col),vec3(.93));
    gl_FragColor = vec4(col,1.0*ta.y);
}