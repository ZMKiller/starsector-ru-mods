#version 110

uniform vec4 color;
uniform float effectlevel;

varying vec2 fragUV;

const float PI = 3.14159265;


void main() {
    vec4 col = texture2D(shipTex,fragUV);
    //vec4 final = vec4(0.15,0.71,0.95,0.0);
    //vec4 final = vec4(1.0-col.x,1.0-col.y,1.0-col.z,0.0);
    vec4 final = vec4(0.15,0.71,0.95,0.0)*vec4(1.0-col.x,1.0-col.y,1.0-col.z,0.0);
    float lim = 1.5;
    if(col.w>0.1&&((col.x+col.y+col.z)<lim)){
        float alpha = ((lim-col.x-col.y-col.z)/lim);
        alpha = alpha*alpha;
        alpha = alpha*effectlevel*1.25*col.w;
        if(alpha>1.0) alpha = 1.0;
        final = vec4(final.x,final.y,final.z,alpha);
    }
    gl_FragColor = final;
}


vec2 hash2( vec2 p )
{
    // texture based white noise
    //	return texture( iChannel0, (p+0.5)/256.0, -100.0 ).xy;

    // procedural white noise
    return fract(sin(vec2(dot(p,vec2(127.1,311.7)),dot(p,vec2(269.5,183.3))))*244.098);
}

vec3 voronoi( in vec2 x )
{
    vec2 n = floor(x);
    vec2 f = fract(x);

    //----------------------------------
    // first pass: regular voronoi
    //----------------------------------
    vec2 mg, mr;

    float md = 8.0;
    for( int j=-1; j<=1; j++ )
    for( int i=-1; i<=1; i++ )
    {
        vec2 g = vec2(float(i),float(j));
        vec2 o = hash2( n + g );
        o = 0.5 + 0.5*sin(6.2831*o );
        vec2 r = g + o - f;
        float d = dot(r,r);

        if( d<md )
        {
            md = d;
            mr = r;
            mg = g;
        }
    }

    //----------------------------------
    // second pass: distance to borders
    //----------------------------------

    md = 8.0;
    for( int j=-2; j<=2; j++ )
    for( int i=-2; i<=2; i++ )
    {
        vec2 g = mg + vec2(float(i),float(j));
        vec2 o = hash2( n + g );
        //vec2 o = n+g;
        o = 0.5 + 0.5*sin( 6.2831*o );
        vec2 r = g + o - f;

        if( dot(mr-r,mr-r)>0.00001 )
        md = min( md, dot( 0.5*(mr+r), normalize(r-mr) ) );
    }


    return vec3( md, mr );
}


float sphere(float t, float k)
{
    float d = 1.0+t*t-t*t*k*k;
    if (d <= 0.0)
    return -1.0;
    float x = (k - sqrt(d))/(1.0 + t*t);
    return asin(x*t);
}


void mainImage( out vec4 fragColor, in vec2 fragCoord )
{

    vec2 uv = fragCoord.xy - 0.5*iResolution.xy;
    uv = vec2(uv.x*0.8,uv.y*1.5);
    float v = iResolution.x;
    //if (v > iResolution.y)
    //    v = iResolution.y;
    uv /= v;
    uv *= 2.8;
    float len = length(uv);
    float k = 1.0;
    float len2;

    len2 = sphere(len*k,sqrt(2.0))/sphere(1.0*k,sqrt(2.0));
    uv = uv * len2 * 0.5 / len;
    uv = uv + 0.5;
    /*if (len2 < 0.0 || uv.x < 0. || uv.y < 0. || uv.x > 1. || uv.y > 1.)
    {
        fragColor = texColor;
        return;
    }*/

    vec2 pos = uv;
    float t = iTime/2.0;
    //float t = 1.0;
    float scale1 = 40.0;
    float scale2 = 20.0;
    float val = 0.0;

    val += sin((pos.x*scale1 + t));
    val += sin((pos.y*scale1 + t)/2.0);
    val += sin((pos.x*scale2 + pos.y*scale2 + sin(t))/2.0);
    val += sin((pos.x*scale2 - pos.y*scale2 + t)/2.0);
    val /= 2.0;


    vec3 c = voronoi(80.0*pos );

    // isolines
    val += 2.0*sin(t)*c.x*(0.5 + 0.5*sin(64.0*c.x));

    float glow = 0.020 / (0.015 + distance(len, 1.0));

    val = (cos(PI*val) + 1.0) * 0.5;
    vec4 col2 = vec4(0.3, 0.7, 1.0, 1.0);

    fragColor = step(len, 1.0) * 0.5 * col2 * val + glow * col2;
}