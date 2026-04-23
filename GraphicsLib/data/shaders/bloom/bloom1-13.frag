#version 110

uniform sampler2D tex;
uniform vec2 screen;
uniform float hdr;
uniform float scale;

vec2 coord = gl_TexCoord[0].xy;
float pixelDistance = 1.0 / screen.x;
float scaledPixelDistance = scale * pixelDistance;
vec2 screenEdge = vec2(pixelDistance * 0.5, (pixelDistance * -0.5) + screen.y);
float hdrScale = 1.0 / hdr;

vec3 getFragment(in float position) {
	position = clamp(position, screenEdge.x, screenEdge.y);
	return texture2D(tex, vec2(position, coord.y)).rgb;
}

void main() {
	vec3 increasedColor = max(texture2D(tex, coord).rgb - hdrScale, 0.0) * 0.153493233;
	increasedColor += max(getFragment(coord.x + scaledPixelDistance) - hdrScale, 0.0) * 0.142740489;
	increasedColor += max(getFragment(coord.x - scaledPixelDistance) - hdrScale, 0.0) * 0.142740489;
	increasedColor += max(getFragment(coord.x + 2.0 * scaledPixelDistance) - hdrScale, 0.0) * 0.11482017;
	increasedColor += max(getFragment(coord.x - 2.0 * scaledPixelDistance) - hdrScale, 0.0) * 0.11482017;
	increasedColor += max(getFragment(coord.x + 3.0 * scaledPixelDistance) - hdrScale, 0.0) * 0.079960182;
	increasedColor += max(getFragment(coord.x - 3.0 * scaledPixelDistance) - hdrScale, 0.0) * 0.079960182;
	increasedColor += max(getFragment(coord.x + 4.0 * scaledPixelDistance) - hdrScale, 0.0) * 0.048316408;
	increasedColor += max(getFragment(coord.x - 4.0 * scaledPixelDistance) - hdrScale, 0.0) * 0.048316408;
	increasedColor += max(getFragment(coord.x + 5.0 * scaledPixelDistance) - hdrScale, 0.0) * 0.025484836;
	increasedColor += max(getFragment(coord.x - 5.0 * scaledPixelDistance) - hdrScale, 0.0) * 0.025484836;
	increasedColor += max(getFragment(coord.x + 6.0 * scaledPixelDistance) - hdrScale, 0.0) * 0.011931298;
	increasedColor += max(getFragment(coord.x - 6.0 * scaledPixelDistance) - hdrScale, 0.0) * 0.011931298;
	increasedColor *= hdr;
	gl_FragColor = vec4(increasedColor.r, increasedColor.g, increasedColor.b, 1.0);
}