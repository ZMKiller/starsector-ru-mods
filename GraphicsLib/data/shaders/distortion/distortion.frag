#version 110

uniform sampler2D tex;
uniform sampler2D distort;
uniform vec4 screen;
uniform vec2 norm;

vec2 coord = gl_TexCoord[0].xy;
float trans = screen.x / screen.y;
vec2 pixelDistance = vec2(1.0 / screen.x, 1.0 / screen.y);
vec4 screenEdge = vec4(pixelDistance.x * 0.5, pixelDistance.y * 0.5, (pixelDistance.x * -0.5) + screen.z, (pixelDistance.y * -0.5) + screen.w);

vec4 getDisplacedFragment(in vec2 displace, in sampler2D texture) {
	displace = clamp(displace, screenEdge.xy, screenEdge.zw);
	return texture2D(texture, displace);
}

void main(void) {
	vec3 col;
	vec4 dis = texture2D(distort, coord);
	dis += getDisplacedFragment(coord + vec2(pixelDistance.x, 0.0), distort);
	dis += getDisplacedFragment(coord - vec2(pixelDistance.x, 0.0), distort);
	dis += getDisplacedFragment(coord + vec2(0.0, pixelDistance.y), distort);
	dis += getDisplacedFragment(coord - vec2(0.0, pixelDistance.y), distort);
	dis *= 0.2;

	if (dis.a > 0.0 && dis.b > 0.0) {
		float scale = dis.b * norm.x + norm.y;

		vec2 displacement = ((dis.rg * 2.0) - 1.0) * scale;
		displacement.x /= trans;
		displacement += coord;

		col = getDisplacedFragment(displacement, tex).rgb;
	} else {
		col = texture2D(tex, coord).rgb;
	}

	gl_FragColor = vec4(col, 1.0);
}
