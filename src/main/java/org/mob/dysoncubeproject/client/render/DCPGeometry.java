package org.mob.dysoncubeproject.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public final class DCPGeometry {

    private DCPGeometry() {}

    public static float seconds(long gameTime, float partial) {
        return ((gameTime % 100000L) + partial) / 20.0f;
    }

    public static void vertex(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float z,
                              float time, float param, float r, float g, float b, float a) {
        vc.addVertex(pose, x, y, z).setUv(time, param).setColor(r, g, b, a);
    }

    public static void ribbon(VertexConsumer vc, PoseStack.Pose pose,
                              float ax, float ay, float az, float bx, float by, float bz,
                              float width, float time, float param, float r, float g, float b, float a) {
        float dx = bx - ax, dy = by - ay, dz = bz - az;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0e-5f) return;
        dx /= len; dy /= len; dz /= len;

        // any axis not parallel to the segment
        float ux = 0, uy = 1, uz = 0;
        if (Math.abs(dy) > 0.9f) { ux = 1; uy = 0; }

        // n1 = normalize(d x up) * width
        float n1x = dy * uz - dz * uy, n1y = dz * ux - dx * uz, n1z = dx * uy - dy * ux;
        float l1 = (float) Math.sqrt(n1x * n1x + n1y * n1y + n1z * n1z);
        n1x = n1x / l1 * width; n1y = n1y / l1 * width; n1z = n1z / l1 * width;

        // n2 = normalize(d x n1) * width
        float n2x = dy * n1z - dz * n1y, n2y = dz * n1x - dx * n1z, n2z = dx * n1y - dy * n1x;
        float l2 = (float) Math.sqrt(n2x * n2x + n2y * n2y + n2z * n2z);
        n2x = n2x / l2 * width; n2y = n2y / l2 * width; n2z = n2z / l2 * width;

        quad(vc, pose, ax - n1x, ay - n1y, az - n1z, bx - n1x, by - n1y, bz - n1z,
                bx + n1x, by + n1y, bz + n1z, ax + n1x, ay + n1y, az + n1z, time, param, r, g, b, a);
        quad(vc, pose, ax - n2x, ay - n2y, az - n2z, bx - n2x, by - n2y, bz - n2z,
                bx + n2x, by + n2y, bz + n2z, ax + n2x, ay + n2y, az + n2z, time, param, r, g, b, a);
    }

    private static void quad(VertexConsumer vc, PoseStack.Pose p,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4,
                             float t, float v, float r, float g, float b, float a) {
        vertex(vc, p, x1, y1, z1, t, v, r, g, b, a);
        vertex(vc, p, x2, y2, z2, t, v, r, g, b, a);
        vertex(vc, p, x3, y3, z3, t, v, r, g, b, a);
        vertex(vc, p, x4, y4, z4, t, v, r, g, b, a);
    }
}