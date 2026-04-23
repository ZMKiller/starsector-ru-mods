#version 110

uniform sampler2D tex;
uniform vec2 screen;
uniform float intensity;
uniform float scale;

vec2 coord = gl_TexCoord[0].xy;
float pixelDistance = 1.0 / screen.x;
float scaledPixelDistance = scale * pixelDistance;
vec2 screenEdge = vec2(pixelDistance * 0.5, (pixelDistance * -0.5) + screen.y);

vec3 getFragment(in float position) {
	position = clamp(position, screenEdge.x, screenEdge.y);
	return texture2D(tex, vec2(coord.x, position)).rgb;
}

void main() {
	vec3 increasedColor = texture2D(tex, coord).rgb * 0.176956307;
	increasedColor += getFragment(coord.y + scaledPixelDistance) * 0.160736935;
	increasedColor += getFragment(coord.y - scaledPixelDistance) * 0.160736935;
	increasedColor += getFragment(coord.y + 2.0 * scaledPixelDistance) * 0.120525658;
	increasedColor += getFragment(coord.y - 2.0 * scaledPixelDistance) * 0.120525658;
	increasedColor += getFragment(coord.y + 3.0 * scaledPixelDistance) * 0.074756096;
	increasedColor += getFragment(coord.y - 3.0 * scaledPixelDistance) * 0.074756096;
	increasedColor += getFragment(coord.y + 4.0 * scaledPixelDistance) * 0.038593269;
	increasedColor += getFragment(coord.y - 4.0 * scaledPixelDistance) * 0.038593269;
	increasedColor += getFragment(coord.y + 5.0 * scaledPixelDistance) * 0.016909888;
	increasedColor += getFragment(coord.y - 5.0 * scaledPixelDistance) * 0.016909888;
	increasedColor *= intensity;
	gl_FragColor = vec4(increasedColor.r, increasedColor.g, increasedColor.b, 1.0);
}