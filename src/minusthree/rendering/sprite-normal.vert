#version 450
precision highp float;
in vec3 a_pos;
in vec2 a_uv;

uniform vec4 u_tint;
uniform vec4 u_pos_scale;
uniform vec2 u_res;

out vec2 uv;
out vec4 tint;

void main() {
  vec2 pixel_pos = (a_pos.xy * u_pos_scale.zw) + u_pos_scale.xy;
  vec2 ndc_pos = (pixel_pos / u_res);
  gl_Position = vec4(ndc_pos, 0.0, 1.0);
  uv = vec2(1.0 - a_uv.x, a_uv.y);
  tint = u_tint;
}
