#version 450
precision mediump float;
in vec2 uv;
out vec4 o_color;

void main() {
  vec3 colorA = vec3(0.65, 0.87, 0.90);
  vec3 colorB = vec3(0.91, 0.98, 0.96);

  vec3 sky = mix(colorA, colorB, uv.y * 2.0);
  
  o_color = vec4(sky, 1.0);
}