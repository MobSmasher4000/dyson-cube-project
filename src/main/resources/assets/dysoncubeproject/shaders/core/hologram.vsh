#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;

out vec4 vColor;
out vec3 vWorldPos;
out float vTime;
out float vParam;

void main() {
    vWorldPos = Position;
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vColor = Color;
    vTime = UV0.x;
    vParam = UV0.y;
}
