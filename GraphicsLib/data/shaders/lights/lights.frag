#version 110

uniform sampler2D tex;
uniform sampler2D buf;
uniform sampler1D data;
uniform float trans;
uniform int size;
uniform vec4 norm1;
uniform vec4 norm2;
uniform vec4 norm3;
uniform float hdr;
uniform float specmult;

vec2 coord = gl_TexCoord[0].xy;

// [POINT] x = position.x (0)  |  y = position.y (1)  |  z = size (5)  |  w = type (7)
// [LINE] x = position.x (0)  |  y = position.y (1)  |  z = size (5)  |  w = type (7)
// [CONE] x = position.x (0)  |  y = position.y (1)  |  z = size (5)  |  w = type (7)
// [DIRECTION] x = direction.x (0)  |  y = direction.y (1)  |  z = specularIntensity (5)  |  w = type (7)
vec4 getLightData1(in float index) {
    // This is equivalent to: float texCoord = (11.0 * index + 0.5) / 4096.0;
    float texCoord = (0.002685546875 * index) + 0.0001220703125;
    return vec4((texture1D(data, texCoord).r * norm1.x) + norm1.y, (texture1D(data, texCoord + 0.000244140625).r * norm1.z) + norm1.w, texture1D(data, texCoord + 0.001220703125).r * norm2.x, texture1D(data, texCoord + 0.001708984375).r);
}

// [ALL] x = color.r (2)  |  y = color.g (3)  |  z = color.b (4)  |  w = intensity (6)
vec4 getLightData2(in float index) {
    // This is equivalent to: float texCoord = (11.0 * index + 0.5) / 4096.0;
    float texCoord = (0.002685546875 * index) + 0.0001220703125;
    return vec4(texture1D(data, texCoord + 0.00048828125).r, texture1D(data, texCoord + 0.000732421875).r, texture1D(data, texCoord + 0.0009765625).r, texture1D(data, texCoord + 0.00146484375).r * norm2.y);
}

// [POINT] x = specularIntensity (8)  |  y = unused (9)  |  z = height (10)
// [LINE] x = position2.x (8)  |  y = position2.y (9)  |  z = height (10)
// [CONE] x = arcStart (8)  |  y = arcEnd (9)  |  z = height (10)
// [DIRECTION] x = direction.z (8)  |  y = unused (9)  |  z = unused (10)
vec2 getLightData3(in float index) {
    // This is equivalent to: float texCoord = (11.0 * index + 0.5) / 4096.0;
    float texCoord = (0.002685546875 * index) + 0.0001220703125;
    return vec2((texture1D(data, texCoord + 0.001953125).r * norm3.x) + norm3.y, (texture1D(data, texCoord + 0.002197265625).r * norm3.z) + norm3.w);
}

float lineSquareDistance(in vec2 v, in vec2 w) {
    vec2 e = w - v;
    e.x *= trans;
    float l2 = dot(e, e);
    vec2 d = coord - v;
    d.x *= trans;
    if (l2 == 0.0) {
        return dot(d, d);
    }
    float t = dot(d, e) / l2;
    if (t < 0.0) {
        return dot(d, d);
    }
    if (t > 1.0) {
        d = coord - w;
        d.x *= trans;
        return dot(d, d);
    }
    e.x /= trans;
    d = v + t * e;
    e = coord - d;
    e.x *= trans;
    return dot(e, e);
}

float getShortestRotation(in float start, in float end) {
    float d = (end - start) + 3.1415927;
    d = (d / 6.2831853);
    return ((d - floor(d)) * 6.2831853) - 3.1415927;
}

float coneCutoff(in vec2 arc, in vec2 d) {
    float angle = mod(atan(d.y, d.x), 6.2831853);
    if (angle < 0.0) {
        angle += 6.2831853;
    }
    float cutoff = 1.0;
    if (((arc.x > arc.y) && ((angle < arc.x) && (angle > arc.y))) ||
        ((arc.x < arc.y) && ((angle < arc.x) || (angle > arc.y)))) {
        cutoff = 0.0;
    } else {
        float width = (arc.y - arc.x) * 0.5;
        if (width < 0.0) {
            width += 3.1415927;
        }
        float d = min(abs(getShortestRotation(arc.x, angle)), abs(getShortestRotation(arc.y, angle)));
        if (d < width) {
            cutoff = d / width;
        }
    }
    return cutoff;
}

void main() {
    vec3 color = texture2D(tex, coord).rgb;
    vec4 color2 = texture2D(buf, coord);

    float alpha = color2.a;
    if (alpha > 0.0) {
        vec3 illum = vec3(0.0);

        vec4 lightData1;
        vec4 lightData2;
        vec2 lightData3;
        for (int i = 0; i < size; i++) {
            lightData1 = getLightData1(float(i));

            if (lightData1.w < 0.25) {
                // Point source light
                vec2 d = coord - lightData1.xy;
                d.x *= trans;
                float distance = dot(d, d);

                if (distance < lightData1.z * lightData1.z) {
                    lightData2 = getLightData2(float(i));
                    lightData3 = getLightData3(float(i));
                    float specularFactor = specmult * dot(color2.rgb, vec3(0.3333333)) * lightData3.x;
                    float magnitude = ((specularFactor * 0.75 + lightData2.w) * 0.571428571429) * (lightData1.z - sqrt(distance)) / lightData1.z;
                    illum += magnitude * lightData2.rgb;
                }
            } else if (lightData1.w < 0.5) {
                // Line source light
                lightData3 = getLightData3(float(i));
                float distance = lineSquareDistance(lightData1.xy, lightData3.xy);

                if (distance < lightData1.z * lightData1.z) {
                    lightData2 = getLightData2(float(i));
                    float magnitude = lightData2.w * (lightData1.z - sqrt(distance)) / lightData1.z;
                    illum += magnitude * lightData2.rgb;
                }
            } else if (lightData1.w < 0.75) {
                // Cone source light
                vec2 d = coord - lightData1.xy;
                d.x *= trans;
                float distance = dot(d, d);

                if (distance < lightData1.z * lightData1.z) {
                    lightData3 = getLightData3(float(i));
                    float cutoff = coneCutoff(lightData3.xy, d);

                    if (cutoff > 0.0) {
                        lightData2 = getLightData2(float(i));
                        float magnitude = cutoff * lightData2.w * (lightData1.z - sqrt(distance)) / lightData1.z;
                        illum += magnitude * lightData2.rgb;
                    }
                }
            } else {
                // Directional source light
                lightData2 = getLightData2(float(i));
                lightData3 = getLightData3(float(i));
                float specularFactor = specmult * dot(color2.rgb, vec3(0.3333333)) * lightData1.z;
                illum += (specularFactor * 0.75 + lightData2.w) * lightData2.rgb * clamp(lightData3.x * -0.75 + 0.25, 0.0, 1.0);
            }
        }

        illum *= alpha;
        color += min(color, color2.rgb) * illum;
    }

    color *= hdr;
    clamp(color, 0.0, 1.0);
    gl_FragColor = vec4(color, 1.0);
}
