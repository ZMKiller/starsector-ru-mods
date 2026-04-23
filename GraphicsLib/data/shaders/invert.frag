#version 110

uniform sampler2D tex;

vec2 coord = gl_TexCoord[0].xy;

void main() {
	vec4 color = texture2D(tex, coord);
	//gl_FragColor = vec4(1.0 - color.r, 1.0 - color.g, 1.0 - color.b, 1.0);
	gl_FragColor = vec4(color.r, color.g, color.b, 1.0);
}