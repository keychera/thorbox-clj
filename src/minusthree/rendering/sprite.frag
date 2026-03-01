#version 450
precision mediump float;
in vec2 uv;
in vec4 tint;

uniform sampler2D u_tex;

out vec4 o_color;

void main() {
  vec4 tex = texture(u_tex, uv);
  vec3 color = mix(tex.rgb, tint.rgb, tint.a);
  o_color = vec4(color, tex.a);
}
