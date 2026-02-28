#version 450
precision mediump float;
in vec3 a_pos;
in vec2 a_uv;
in vec4 i_crop;

out vec2 uv;

void main() {
  gl_Position = vec4(a_pos, 1.0);
  vec2 cropped_uv = mix(i_crop.xy, i_crop.zw, a_uv);
  uv = cropped_uv;
}
