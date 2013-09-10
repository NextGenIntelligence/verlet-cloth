#version 120

attribute vec4 vertex;
varying vec3 frag_color;
uniform mat4 mvp[2];

void main(void) {
  gl_Position = mvp[0] * mvp[1] * vertex;
  frag_color = vec3(0.0, 0.5, 0.2);
}