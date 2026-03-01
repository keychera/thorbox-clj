#version 450
precision highp float;
in vec3 a_pos;
in vec2 a_uv;
in vec4 i_pos_scale;
in vec4 i_tint;
in vec4 i_crop;

uniform vec2 u_res;

out vec2 uv;
out vec4 tint;

void main() {
  vec2 pixel_pos = (a_pos.xy * i_pos_scale.zw) + i_pos_scale.xy;
  vec2 ndc_pos = (pixel_pos / u_res);
  gl_Position = vec4(ndc_pos, 0.0, 1.0);
  vec2 cropped_uv = mix(i_crop.xy, i_crop.zw, a_uv);
  uv = cropped_uv;
  tint = i_tint;
}
