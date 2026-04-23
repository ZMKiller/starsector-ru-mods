#version 110

uniform sampler2D tex;
uniform float facing;
uniform float scale;
uniform vec2 norm;
uniform float flip;
uniform vec2 arc;
uniform float attwidth;

vec2 coord = gl_TexCoord[0].xy;

float getShortestRotation(in float start, in float end) {
    float d = (end - start) + 3.1415927;
    d = (d / 6.2831853);
    return ((d - floor(d)) * 6.2831853) - 3.1415927;
}

void main(void) {
    vec4 col = texture2D(tex, coord);
    vec2 vc = (col.rg * 2.0) - 1.0;

    float cs = cos(facing * 0.0174533);
    float sn = sin(facing * 0.0174533);

    vc = vec2((vc.x * cs) - (vc.y * sn), (vc.x * sn) + (vc.y * cs)) * flip;

    float cutoff = 1.0;
    
    float angle = mod(atan(vc.y, vc.x), 6.2831853);
    if (angle < 0.0) {
        angle += 6.2831853;
    }

    if (((arc.x > arc.y) && ((angle < arc.x) && (angle > arc.y))) ||
        ((arc.x < arc.y) && ((angle < arc.x) || (angle > arc.y)))) {
        cutoff = 0.0;
    } else {
        float d = min(abs(getShortestRotation(arc.x, angle)), abs(getShortestRotation(arc.y, angle)));
        if (d < attwidth) {
            cutoff = d / attwidth;
        }
    }

    vc = (vc * 0.5) + 0.5;

    if (col.b < 0.005) {
        gl_FragColor = vec4(vc, ((col.b - norm.y) / norm.x) * scale * cutoff, 0.0);
    } else {
        gl_FragColor = vec4(vc, ((col.b - norm.y) / norm.x) * scale * cutoff, col.a * cutoff);
    }
}
