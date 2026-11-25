#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vColor;
out vec3 vWorldPos;

void main() {
    // Each receiver face supplies local pattern coordinates in UV0.
    // Present them as an XY plane to the shared hex fragment shader.
    vWorldPos = vec3(UV0, 0.0);
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vColor = Color;
}
