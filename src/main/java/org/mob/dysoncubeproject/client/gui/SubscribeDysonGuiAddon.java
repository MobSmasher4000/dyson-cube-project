package org.mob.dysoncubeproject.client.gui;

import com.hrznstudio.titanium.api.client.AssetTypes;
import com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.hrznstudio.titanium.util.AssetUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import org.mob.dysoncubeproject.DysonCubeProject;
import org.mob.dysoncubeproject.network.ClientSetSpherePasswordMessage;
import org.mob.dysoncubeproject.network.ClientSubscribeSphereMessage;
import org.mob.dysoncubeproject.world.ClientDysonSphere;
import org.mob.dysoncubeproject.world.DysonSphereStructure;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = DysonCubeProject.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class SubscribeDysonGuiAddon extends BasicScreenAddon {

    private static String activeModalDysonId = null;
    private static boolean isOwnerConfigMode = false;
    private static String inputBuffer = "";
    private static boolean showPlainText = false;

    private final String dysonID;
    private int guiX, guiY;
    private int screenWidth, screenHeight;

    public SubscribeDysonGuiAddon(String dysonID, int posX, int posY) {
        super(posX, posY);
        this.dysonID = dysonID;
        closeModal();
    }

    public static boolean isModalOpen() {
        return activeModalDysonId != null;
    }

    @SubscribeEvent
    public static void onRenderTooltip(RenderTooltipEvent.Pre event) {
        if (isModalOpen()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onScreenClose(ScreenEvent.Closing event) {
        if (isModalOpen()) {
            closeModal();
        }
    }

    @Override
    public int getXSize() {
        return 16;
    }

    @Override
    public int getYSize() {
        return 16;
    }

    private String getLocalUUID() {
        var player = Minecraft.getInstance().player;
        return player != null ? player.getStringUUID() : "";
    }

    private boolean isOwnSphere() {
        return dysonID != null && dysonID.equals(getLocalUUID());
    }

    private boolean isCurrentlySubscribed() {
        String sub = ClientDysonSphere.DYSON_SPHERE_PROGRESS.getSubscribedFor(getLocalUUID());
        return dysonID != null && dysonID.equals(sub);
    }

    private boolean isLockedForLocalPlayer() {
        if (isOwnSphere()) return false;
        DysonSphereStructure sphere = ClientDysonSphere.DYSON_SPHERE_PROGRESS.getSpheres().get(dysonID);
        return sphere != null && sphere.hasPassword();
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider assets, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        this.guiX = guiX;
        this.guiY = guiY;
        this.screenWidth = screen.width;
        this.screenHeight = screen.height;

        int x = guiX + getPosX();
        int y = guiY + getPosY();
        AssetUtil.drawAsset(guiGraphics, screen, assets.getAsset(AssetTypes.BUTTON_SIDENESS_PULL), x, y);

        // Draw active/lock status badge on the corner of the button
        DysonSphereStructure sphere = ClientDysonSphere.DYSON_SPHERE_PROGRESS.getSpheres().get(dysonID);
        boolean hasPass = sphere != null && sphere.hasPassword();

        if (isCurrentlySubscribed()) {
            // Green connected dot (top-left)
            guiGraphics.fill(x + 2, y + 2, x + 5, y + 5, 0xFF33FF55);
        }
        if (hasPass) {
            // Gold lock icon indicator (bottom-right)
            guiGraphics.fill(x + 10, y + 10, x + 15, y + 14, 0xFFFFAA00);
            guiGraphics.fill(x + 11, y + 8, x + 14, y + 10, 0xFFFFDD55);
        }
    }

    @Override
    public void drawForegroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider iAssetProvider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        if (!dysonID.equals(activeModalDysonId)) return;

        Font font = Minecraft.getInstance().font;
        int boxW = 156;
        int boxH = 74;
        int boxX = (screen.width - boxW) / 2 - guiX;
        int boxY = (screen.height - boxH) / 2 - guiY;

        guiGraphics.pose().pushPose();
        // Above item models (250), item stack counts (300), and dragged items (432)
        guiGraphics.pose().translate(0, 0, 500);

        // Dim backdrop
        guiGraphics.fill(-guiX, -guiY, screen.width - guiX, screen.height - guiY, 0x88000000);

        guiGraphics.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0xFF14181E);
        guiGraphics.renderOutline(boxX, boxY, boxW, boxH, 0xFF00D8FF);

        // Header
        Component title = isOwnerConfigMode
                ? Component.translatable("tooltip.dysoncubeproject.set_password").withStyle(ChatFormatting.AQUA)
                : Component.translatable("tooltip.dysoncubeproject.enter_password").withStyle(ChatFormatting.GOLD);
        guiGraphics.drawString(font, title, boxX + 8, boxY + 7, 0xFFFFFF, false);

        // Password Input Field
        int fieldX = boxX + 8;
        int fieldY = boxY + 22;
        int fieldW = boxW - 34;
        int fieldH = 16;
        guiGraphics.fill(fieldX, fieldY, fieldX + fieldW, fieldY + fieldH, 0xFF0A0C10);
        guiGraphics.renderOutline(fieldX, fieldY, fieldW, fieldH, 0xFF55FFFF);

        String displayStr = showPlainText ? inputBuffer : "•".repeat(inputBuffer.length());
        boolean cursorBlink = (System.currentTimeMillis() / 500) % 2 == 0;
        if (cursorBlink) displayStr += "_";
        guiGraphics.drawString(font, displayStr, fieldX + 4, fieldY + 4, 0xFFFFFF, false);

        // Show/Hide toggle button [o]
        int eyeX = fieldX + fieldW + 3;
        boolean hoverEye = isInside(mouseX - guiX, mouseY - guiY, eyeX, fieldY, 15, fieldH);
        guiGraphics.fill(eyeX, fieldY, eyeX + 15, fieldY + fieldH, hoverEye ? 0xFF2A3545 : 0xFF1C232E);
        guiGraphics.renderOutline(eyeX, fieldY, 15, fieldH, 0xFF4A607A);
        guiGraphics.drawCenteredString(font, showPlainText ? "ab" : "**", eyeX + 8, fieldY + 4, 0xCCCCCC);

        // Confirm Button
        int btnW = 64;
        int btnH = 16;
        int confirmX = boxX + 8;
        int btnY = boxY + 48;
        boolean hoverConfirm = isInside(mouseX - guiX, mouseY - guiY, confirmX, btnY, btnW, btnH);
        guiGraphics.fill(confirmX, btnY, confirmX + btnW, btnY + btnH, hoverConfirm ? 0xFF1B5E20 : 0xFF134217);
        guiGraphics.renderOutline(confirmX, btnY, btnW, btnH, 0xFF4CAF50);
        guiGraphics.drawCenteredString(font, isOwnerConfigMode ? Component.translatable("tooltip.dysoncubeproject.save") : Component.translatable("tooltip.dysoncubeproject.connect"), confirmX + btnW / 2, btnY + 4, 0xFFFFFF);

        // Cancel Button
        int cancelX = boxX + boxW - btnW - 8;
        boolean hoverCancel = isInside(mouseX - guiX, mouseY - guiY, cancelX, btnY, btnW, btnH);
        guiGraphics.fill(cancelX, btnY, cancelX + btnW, btnY + btnH, hoverCancel ? 0xFF6E1E1E : 0xFF4A1414);
        guiGraphics.renderOutline(cancelX, btnY, btnW, btnH, 0xFFE53935);
        guiGraphics.drawCenteredString(font, Component.translatable("tooltip.dysoncubeproject.cancel"), cancelX + btnW / 2, btnY + 4, 0xFFFFFF);

        guiGraphics.pose().popPose();
    }

    @Override
    public List<Component> getTooltipLines() {
        if (isModalOpen()) return List.of();
        List<Component> lines = new ArrayList<>();
        DysonSphereStructure sphere = ClientDysonSphere.DYSON_SPHERE_PROGRESS.getSpheres().get(dysonID);
        boolean hasPass = sphere != null && sphere.hasPassword();

        if (isCurrentlySubscribed()) {
            lines.add(Component.translatable("tooltip.dysoncubeproject.connected").withStyle(ChatFormatting.GREEN));
            if (!isOwnSphere()) {
                lines.add(Component.translatable("tooltip.dysoncubeproject.disconnect").withStyle(ChatFormatting.GRAY));
            }
        } else if (isLockedForLocalPlayer()) {
            lines.add(Component.translatable("tooltip.dysoncubeproject.locked").withStyle(ChatFormatting.GOLD));
            lines.add(Component.translatable("tooltip.dysoncubeproject.enter_left_password").withStyle(ChatFormatting.GRAY));
        } else {
            lines.add(Component.translatable("gui.dysoncubeproject.subscribe").withStyle(ChatFormatting.AQUA));
        }

        if (isOwnSphere()) {
            lines.add(Component.translatable(hasPass ? "tooltip.dysoncubeproject.status_protected" : "tooltip.dysoncubeproject.status_public").withStyle(hasPass ? ChatFormatting.GOLD : ChatFormatting.GREEN));
            lines.add(Component.translatable("tooltip.dysoncubeproject.configure_password").withStyle(ChatFormatting.YELLOW));
        }
        return lines;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // If a modal is currently open intercept clicks
        if (activeModalDysonId != null) {
            if (!dysonID.equals(activeModalDysonId)) {
                return true; // Block clicks on other rows while modal is active
            }

            int boxW = 156;
            int boxH = 74;
            int boxX = (screenWidth - boxW) / 2;
            int boxY = (screenHeight - boxH) / 2;

            int fieldX = boxX + 8;
            int fieldY = boxY + 22;
            int fieldW = boxW - 34;
            int eyeX = fieldX + fieldW + 3;

            int btnW = 64;
            int btnH = 16;
            int confirmX = boxX + 8;
            int cancelX = boxX + boxW - btnW - 8;
            int btnY = boxY + 48;

            if (isInside(mouseX, mouseY, eyeX, fieldY, 15, 16)) {
                showPlainText = !showPlainText;
                return true;
            }
            if (isInside(mouseX, mouseY, confirmX, btnY, btnW, btnH)) {
                submitModal();
                return true;
            }
            if (isInside(mouseX, mouseY, cancelX, btnY, btnW, btnH)) {
                closeModal();
                return true;
            }
            // Clicking outside the modal closes it
            if (!isInside(mouseX, mouseY, boxX, boxY, boxW, boxH)) {
                closeModal();
            }
            return true;
        }

        // Normal button click handling
        if (isMouseOver(mouseX - this.guiX, mouseY - this.guiY)) {
            if (button == 1 && isOwnSphere()) {
                // Right-click own sphere then Set/Clear Password modal
                openModal(dysonID, true);
                return true;
            }

            if (button == 0) {
                if (isCurrentlySubscribed() && !isOwnSphere()) {
                    // Disconnect back to own sphere
                    DysonCubeProject.NETWORK.get().sendToServer(new ClientSubscribeSphereMessage("-", ""));
                } else if (isLockedForLocalPlayer()) {
                    // Locked
                    openModal(dysonID, false);
                } else {
                    // Public or own sphere then connect immediately
                    DysonCubeProject.NETWORK.get().sendToServer(new ClientSubscribeSphereMessage(dysonID, ""));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!dysonID.equals(activeModalDysonId)) return false;

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            closeModal();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            submitModal();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !inputBuffer.isEmpty()) {
            inputBuffer = inputBuffer.substring(0, inputBuffer.length() - 1);
            return true;
        }
        return true; // Consume keys so inventory doesn't close while typing
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!dysonID.equals(activeModalDysonId)) return false;

        if (!Character.isISOControl(codePoint) && inputBuffer.length() < 24) {
            inputBuffer += codePoint;
            return true;
        }
        return true;
    }

    private static void openModal(String targetId, boolean ownerConfig) {
        activeModalDysonId = targetId;
        isOwnerConfigMode = ownerConfig;
        inputBuffer = "";
        showPlainText = false;
    }

    private static void closeModal() {
        activeModalDysonId = null;
        isOwnerConfigMode = false;
        inputBuffer = "";
    }

    private void submitModal() {
        if (isOwnerConfigMode) {
            DysonCubeProject.NETWORK.get().sendToServer(new ClientSetSpherePasswordMessage(inputBuffer));
        } else {
            DysonCubeProject.NETWORK.get().sendToServer(new ClientSubscribeSphereMessage(dysonID, inputBuffer));
        }
        closeModal();
    }

    private static boolean isInside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}