#version 120

varying vec3 N;
varying vec3 v;
varying vec4 frag_color;

void main(void) {
  vec3 L = vec3(0.0, -1.0, 0.0);
  float diff = max(dot(N,L), 0.0) + 0.1;
  diff = clamp(diff, 0.0, 1.0);

  gl_FragColor = diff * vec4(frag_color);
}