#version 110

uniform sampler2D tex;
uniform float degree;
uniform float time;

vec2 coord = gl_TexCoord[0].xy;

float rand(vec2 co){
    return fract(sin(dot(co.xy, vec2(12.9898,78.233))) * 43758.5453);
}

float hue2rgb(in vec3 pqt){
	if (pqt.z < 0.0) pqt.z += 1.0;
	if (pqt.z > 1.0) pqt.z -= 1.0;
	if (pqt.z < 0.1666667) return pqt.x + (pqt.y - pqt.x) * 6.0 * pqt.z;
	if (pqt.z < 0.5) return pqt.y;
	if (pqt.z < 0.6666667) return pqt.x + (pqt.y - pqt.x) * (0.6666667 - pqt.z) * 6.0;
	return pqt.x;
}

vec3 hslToRgb(in vec3 hsl) {
    vec3 rgb;

    if (hsl.y == 0.0) {
		rgb = vec3(hsl.z);
    } else {
        float q = hsl.z < 0.5 ? hsl.z * (1.0 + hsl.y) : hsl.z + hsl.y - hsl.z * hsl.y;
        float p = 2.0 * hsl.z - q;
        rgb.r = hue2rgb(vec3(p, q, hsl.x + 0.3333333));
        rgb.g = hue2rgb(vec3(p, q, hsl.x));
        rgb.b = hue2rgb(vec3(p, q, hsl.x - 0.3333333));
    }

    return rgb;
}

vec3 rgbToHsl(in vec3 rgb) {
    float mx = max(max(rgb.r, rgb.g), rgb.b);
	float mn = min(min(rgb.r, rgb.g), rgb.b);
    vec3 hsl = vec3(mx + mn / 2.0);

    if (mx == mn) {
        hsl.x = 0.0;
		hsl.y = 0.0;
    } else {
        float d = mx - mn;
        hsl.y = hsl.z > 0.5 ? d / (2.0 - mx - mn) : d / (mx + mn);
        if (mx == rgb.r) {
            hsl.x = (rgb.g - rgb.b) / d + (rgb.g < rgb.b ? 6.0 : 0.0);
		} else if (mx == rgb.g) {
            hsl.x = (rgb.b - rgb.r) / d + 2.0;
		} else {
            hsl.x = (rgb.r - rgb.g) / d + 4.0;
        }
        hsl.x /= 6.0;
    }

    return hsl;
}

void main() {
	vec4 color = texture2D(tex, coord);
	vec3 newcolor = rgbToHsl(color.rgb);
	newcolor.y += 0.3;
	newcolor.z *= newcolor.z;
	newcolor = hslToRgb(newcolor) * rand(coord * time);
	gl_FragColor = vec4(mix(color.rgb, newcolor, degree), 1.0);
}
