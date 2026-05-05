#version 110

//Changed Black Hole Kills Galaxy  by rakesh111989
//---  Galaxy --- Fabrice NEYRET  august 2013

const float RETICULATION = 3.;  
const float NB_ARMS = 1.;       
//const float ARM = 3.;         
const float COMPR = .1;         
const float SPEED = .12;
const float GALAXY_R = 1./2.;
const float BULB_R = 1./3.;
const vec3 GALAXY_COL = vec3(.5,.5,1.); 
const vec3 BULB_COL   = vec3(0.5,0.4,0.7);
const float BULB_BLACK_R = 1./5.5;
const vec3 BULB_BLACK_COL   = vec3(0,0,0);
const vec3 SKY_COL    = 0.*vec3(.1,.3,.5);

uniform sampler2D text0;
uniform sampler2D text1;
uniform float level;
uniform float t;

varying vec2 fragUV;
		
#define Pi 3.1415927

// --- base noise
float tex(vec2 uv) 
{
	float n = texture2D(text0,uv).r;
	return 1.-abs(2.*n-1.);
}


// --- perlin turbulent noise + rotation
float noise(vec2 uv)
{
	float v=0.;
	float a=-2.0*SPEED*t,
    co=cos(a),si=sin(a); 
	mat2 M = mat2(co,-si,si,co);
	const int L = 7;
	float s=1.;
	for (int i=0; i<L; i++)
	{
		uv = M*uv;
		float b = tex(uv*s);
		v += 1./s* pow(b,RETICULATION); 
		s *= 2.;
	}
	
    return v/2.;
}


void main() 
{
	vec2 uv = fragUV-vec2(1.0,1.0);
	vec3 col;
	
	float rho = length(uv); 
	float ang = atan(uv.y,uv.x);
	float shear = 2.*log(rho);
	float c = cos(shear), s=sin(shear);
	mat2 R = mat2(c,-s,s,c);

	float r; 
	r = rho/GALAXY_R; float dens = exp(-r*r);
	r = rho/BULB_R;	  float bulb = exp(-r*r);
	r = rho/BULB_BLACK_R; float bulb_black = exp(-r*r);
	float phase = NB_ARMS*(ang-shear);

	ang = ang-COMPR*cos(phase)+SPEED*t;
	uv = rho*vec2(cos(ang),sin(ang));

	float spires = 1.+NB_ARMS*COMPR*sin(phase);
	dens *= .7*spires;	
	
	float gaz = noise(.09*1.2*R*uv);
	float gaz_trsp = pow((1.-gaz*dens),2.);

	float ratio = 4.;
	float stars1 = texture2D(text1,ratio*uv+.5).r, // M*uv
	      stars2 = texture2D(text0,ratio*uv+.5).r,
		  stars = pow(1.-(1.-stars1)*(1.-stars2),5.);
	
	col = mix(SKY_COL,
			  gaz_trsp*(1.7*GALAXY_COL) + 1.2*stars, 
			  dens);
	col = mix(col, 2.*BULB_COL,1.2* bulb);

	col = mix(col, 1.2*BULB_BLACK_COL, 2.0*bulb_black);
	
	gl_FragColor = vec4(col,level);
}