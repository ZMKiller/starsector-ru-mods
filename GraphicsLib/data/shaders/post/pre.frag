#version 110

uniform sampler2D screen;
uniform float time;
uniform float hueshift;
uniform float saturation;
uniform bool expdesaturation;
uniform float lightness;
uniform bool expdarkness;
uniform vec3 redhsl;
uniform vec3 yellowhsl;
uniform vec3 greenhsl;
uniform vec3 tealhsl;
uniform vec3 bluehsl;
uniform vec3 magentahsl;
uniform float contrast;
uniform float noise;
uniform float scanlines;
uniform float scanint;
uniform float scanwidth;

vec2 coord = gl_TexCoord[0].xy;

float rand(in vec2 co) {
    return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

float hue2rgb(in vec3 pqt) {
    vec3 tPQT = pqt;
    if (tPQT.z < 0.0) {
        tPQT.z += 1.0;
    }
    if (tPQT.z > 1.0) {
        tPQT.z -= 1.0;
    }
    if (tPQT.z < 0.1666667) {
        return tPQT.x + ((tPQT.y - tPQT.x) * 6.0 * tPQT.z);
    } else if (tPQT.z < 0.5) {
        return tPQT.y;
    } else if (tPQT.z < 0.6666667) {
        return tPQT.x + ((tPQT.y - tPQT.x) * (0.6666667 - tPQT.z) * 6.0);
    } else {
        return tPQT.x;
    }
}

vec3 hsl2rgb(in vec3 hsl) {
    vec3 rgb;

    if (hsl.y == 0.0) {
        rgb = vec3(hsl.z);
    } else {
        float q;
        if (hsl.z < 0.5) {
            q = hsl.z * (1.0 + hsl.y);
        } else {
            q = hsl.z + hsl.y - (hsl.z * hsl.y);
        }
        float p = (2.0 * hsl.z) - q;
        rgb.r = hue2rgb(vec3(p, q, hsl.x + 0.3333333));
        rgb.g = hue2rgb(vec3(p, q, hsl.x));
        rgb.b = hue2rgb(vec3(p, q, hsl.x - 0.3333333));
    }

    return clamp(rgb, vec3(0.0), vec3(1.0));
}

vec3 rgb2hsl(in vec3 rgb) {
    float mx = max(max(rgb.r, rgb.g), rgb.b);
    float mn = min(min(rgb.r, rgb.g), rgb.b);
    vec3 hsl = vec3((mx + mn) / 2.0);

    if (abs(mx - mn) <= 0.000001) {
        hsl.x = 0.0;
        hsl.y = 0.0;
    } else {
        float d = mx - mn;
        if (hsl.z > 0.5) {
            hsl.y = d / ((2.0 - mx) - mn);
        } else {
            hsl.y = d / (mx + mn);
        }
        if (abs(rgb.r - mx) <= 0.000001) {
            hsl.x = (rgb.g - rgb.b) / d;
            if (rgb.g < rgb.b) {
                hsl.x += 6.0;
            }
        } else if (abs(rgb.g - mx) <= 0.000001) {
            hsl.x = ((rgb.b - rgb.r) / d) + 2.0;
        } else {
            hsl.x = ((rgb.r - rgb.g) / d) + 4.0;
        }
        hsl.x /= 6.0;
    }

    return clamp(hsl, vec3(0.0), vec3(1.0));
}

float angularDistance(in float src, in float tgt) {
    float a = tgt - src;
    return mod(a + 0.5, 1.0) - 0.5;
}

void main() {
    vec4 color = texture2D(screen, coord);
    vec3 newcolor = color.rgb;
    vec3 hsl = rgb2hsl(color.rgb);

    float hueshift_t = hueshift;
    float saturation_t = saturation;
    bool expdesaturation_t = expdesaturation;
    float lightness_t = lightness;
    bool expdarkness_t = expdarkness;
    vec3 redhsl_t = redhsl;
    vec3 yellowhsl_t = yellowhsl;
    vec3 greenhsl_t = greenhsl;
    vec3 tealhsl_t = tealhsl;
    vec3 bluehsl_t = bluehsl;
    vec3 magentahsl_t = magentahsl;
    float contrast_t = contrast;
    float noise_t = noise;
    float scanlines_t = scanlines;
    float scanint_t = scanint;
    float scanwidth_t = scanwidth;

    /* TESTBENCH CODE - UNCOMMENT IF NEEDED */ /*
    hueshift_t        = 0.0;
    saturation_t      = 1.0;
    expdesaturation_t = false;
    lightness_t       = 1.0;
    expdarkness_t     = true;
    redhsl_t          = vec3(0.0, 1.0, 1.0);
    yellowhsl_t       = vec3(0.0, 1.0, 1.0);
    greenhsl_t        = vec3(0.0, 1.0, 1.0);
    tealhsl_t         = vec3(0.0, 1.0, 1.0);
    bluehsl_t         = vec3(0.0, 1.0, 1.0);
    magentahsl_t      = vec3(0.0, 1.0, 1.0);
    contrast_t        = 1.0;
    noise_t           = 0.0;
    scanlines_t       = 0.0;
    scanint_t         = scanint;
    scanwidth_t       = scanwidth;
    */ /* **** HIT F10 TO RELOAD SHADERS ***** */

    /* red */
    float factor = max(0.1666667 - abs(angularDistance(hsl.x, 0.0)), 0.0) * 6.0;
    vec3 hslchange = redhsl_t * factor;

    /* yellow */
    factor = max(0.1666667 - abs(angularDistance(hsl.x, 0.1666667)), 0.0) * 6.0;
    hslchange += yellowhsl_t * factor;

    /* green */
    factor = max(0.1666667 - abs(angularDistance(hsl.x, 0.3333333)), 0.0) * 6.0;
    hslchange += greenhsl_t * factor;

    /* teal */
    factor = max(0.1666667 - abs(angularDistance(hsl.x, 0.5)), 0.0) * 6.0;
    hslchange += tealhsl_t * factor;

    /* blue */
    factor = max(0.1666667 - abs(angularDistance(hsl.x, 0.6666667)), 0.0) * 6.0;
    hslchange += bluehsl_t * factor;

    /* magenta */
    factor = max(0.1666667 - abs(angularDistance(hsl.x, 0.8333333)), 0.0) * 6.0;
    hslchange += magentahsl_t * factor;

    hslchange = max(hslchange + vec3(hueshift_t, saturation_t - 1.0, lightness_t - 1.0), vec3(-1.0, 0.0, 0.0));

    /* hue */
    hsl.x += hslchange.x;
    hsl.x = mod(hsl.x, 1.0);

    /* saturation */
    if (hslchange.y <= 0.0) {
        hsl.y = 0.0;
    } else {
        hsl.y = (hslchange.y <= 1.0 && !expdesaturation_t) ? hsl.y * hslchange.y : 1.0 - pow(1.0 - hsl.y, hslchange.y);
    }

    /* lightness */
    if (hslchange.z <= 0.0) {
        hsl.z = 0.0;
    } else {
        hsl.z = (hslchange.z <= 1.0 && !expdarkness_t) ? hsl.z * hslchange.z : 1.0 - pow(1.0 - hsl.z, hslchange.z);
    }

    newcolor = hsl2rgb(hsl);

    /* contrast */
    newcolor = (newcolor - 0.5) * contrast_t + 0.5;

    /* noise */
    newcolor *= mix(1.0, rand(coord * time), noise_t);

    /* scanlines */
    newcolor *= (mod(coord.y, scanint_t) < scanwidth_t) ? 1.0 - scanlines_t : 1.0;

    gl_FragColor = vec4(clamp(newcolor, vec3(0.0), vec3(1.0)), color.a);
}
