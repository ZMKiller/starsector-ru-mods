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
	vec3 increasedColor = texture2D(tex, coord).xyz * 0.153493233;
	increasedColor += getFragment(coord.y + scaledPixelDistance) * 0.142740489;
	increasedColor += getFragment(coord.y - scaledPixelDistance) * 0.142740489;
	increasedColor += getFragment(coord.y + 2.0 * scaledPixelDistance) * 0.11482017;
	increasedColor += getFragment(coord.y - 2.0 * scaledPixelDistance) * 0.11482017;
	increasedColor += getFragment(coord.y + 3.0 * scaledPixelDistance) * 0.079960182;
	increasedColor += getFragment(coord.y - 3.0 * scaledPixelDistance) * 0.079960182;
	increasedColor += getFragment(coord.y + 4.0 * scaledPixelDistance) * 0.048316408;
	increasedColor += getFragment(coord.y - 4.0 * scaledPixelDistance) * 0.048316408;
	increasedColor += getFragment(coord.y + 5.0 * scaledPixelDistance) * 0.025484836;
	increasedColor += getFragment(coord.y - 5.0 * scaledPixelDistance) * 0.025484836;
	increasedColor += getFragment(coord.y + 6.0 * scaledPixelDistance) * 0.011931298;
	increasedColor += getFragment(coord.y - 6.0 * scaledPixelDistance) * 0.011931298;
	increasedColor *= intensity;
	gl_FragColor = vec4(increasedColor.r, increasedColor.g, increasedColor.b, 1.0);
}