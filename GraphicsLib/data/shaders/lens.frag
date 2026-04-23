#version 110

uniform sampler2D tex;
uniform sampler1D data;
uniform float trans;
uniform float intensity;
uniform int size;
uniform vec4 normalization;
uniform float sizeNormalization;

vec2 coord = gl_TexCoord[0].xy;

vec3 getLensInfo(in float index) {
	// This is equivalent to: float texCoord = (3.0 * index + 0.5) / maxSize;
	float texCoord = 0.046875 * index + 0.0078125;
	vec3 texData = vec3((texture1D(data, texCoord).r * normalization.x) + normalization.y, (texture1D(data, texCoord + 0.015625).r * normalization.z) + normalization.w, texture1D(data, texCoord + 0.03125).r * sizeNormalization);
	return texData;
}

void main(void) {
	vec3 lensInfo;
	vec3 temp;
	vec2 m;
	vec2 d;
	float min = -1.0;
	float rSquared;

	for (int i = 0; i < size; i++) {
		temp = getLensInfo(float(i));

		m = temp.xy;
		d = coord - m;
		d.x = d.x * trans;

		rSquared = dot(d, d);
		if (rSquared < temp.z * temp.z) {
			if (min < 0.0 || rSquared < min) {
				min = rSquared;
				lensInfo = temp;
			}
		}
	}

	vec3 col;
	if (min < 0.0) {
		col = texture2D(tex, coord).xyz;
	} else {
		m = lensInfo.xy;
		d = coord - m;
		d.x = d.x * trans;
		float r = dot(d, d);

		vec2 uv = normalize(d) * r * intensity / lensInfo.z;
		uv.x = uv.x / trans;
		uv = uv + m;
		col = texture2D(tex, vec2(uv.x, uv.y)).xyz;
	}
	gl_FragColor = vec4(col, 1.0);
}

		// SQUAREXY:
		// vec2 uv = m + vec2(d.x * abs(d.x), d.y * abs(d.y)) / lensInfo.z;
		// vec2 uv = m + normalize(d) * r * r / lensInfo.z;
		// vec2 uv = m + d * r / lensInfo.z;
		//float rz = r;
		//if (r > lensInfo.z / 2.0) {
		//	rz = r + r - lensInfo.z;
		//} else {
		//	rz = 
		//}
		//vec2 uv = normalize(d) * r * r * intensity / lensInfo.z;