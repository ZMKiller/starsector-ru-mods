#version 110

uniform sampler2D tex;
uniform sampler2D glow;
uniform float hdr;

vec2 coord = gl_TexCoord[0].xy;

void main() {
	vec3 color = texture2D(tex, coord).xyz * hdr;
	vec3 increasedColor = texture2D(glow, coord).xyz;
	color += max(1.0 - color, 0.0) * increasedColor;
	gl_FragColor = vec4(color.r, color.g, color.b, 1.0);
}