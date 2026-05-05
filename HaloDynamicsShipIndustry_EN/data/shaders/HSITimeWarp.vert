#version 110

varying vec2 fragUV;

void main() {
    gl_Position = ftransform();
    fragUV = gl_MultiTexCoord0.xy;
}
