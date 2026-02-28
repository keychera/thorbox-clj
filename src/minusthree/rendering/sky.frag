#version 450
precision mediump float;
in vec2 uv;
out vec4 o_color;

void main() {
  o_color = vec4(uv.x, uv.y, 1.0, 1.0);
}