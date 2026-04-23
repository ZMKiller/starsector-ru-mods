#version 110

uniform sampler2D tex;
uniform vec4 data;
// data.x = angle
// data.y = flatness
// data.z = flip (horizontal)
// data.a = flip (vertical)

vec2 coord = gl_TexCoord[0].xy;

void main(void) {
	vec4 col = texture2D(tex, coord);
	if (data.y > 1.0) {
		// Special case for sprites without normal maps
		gl_FragColor = vec4(0.5, 0.5, 0.5, col.a);
	} else if (data.y != 0.0) {
		vec3 vc = (col.rgb * 2.0) - 1.0;

		float cs = cos(data.x * -0.0174533);
		float sn = sin(data.x * -0.0174533);

		vc.xy *= data.zw;
		vc = vec3((vc.x * cs) - (vc.y * sn), (vc.x * sn) + (vc.y * cs), vc.z);
		vc.xy *= (1.0 - data.y);
		vc = normalize(vc);

		vc = (vc * 0.5) + 0.5;

		gl_FragColor = vec4(vc, col.a);
	} else {
		vec2 vc = (col.rg * 2.0) - 1.0;

		float cs = cos(data.x * -0.0174533);
		float sn = sin(data.x * -0.0174533);

		vc *= data.zw;
		vc = vec2((vc.x * cs) - (vc.y * sn), (vc.x * sn) + (vc.y * cs));

		vc = (vc * 0.5) + 0.5;

		gl_FragColor = vec4(vc, col.b, col.a);
	}
}
