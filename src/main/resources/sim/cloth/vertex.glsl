#version 120

attribute vec4 vertex;
attribute vec4 normal;
varying vec3 N;
varying vec3 v;
varying vec3 frag_color;
uniform mat4 mvp[2];

void main(void) {
  gl_Position = mvp[0] * mvp[1] * vertex;
  v = vec3(vertex);
  N = vec3(normal);
  frag_color = vec3(normal);
}