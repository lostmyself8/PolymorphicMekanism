package com.jerry.polymek.client.recipe;

import com.illusivesoulworks.polymorph.api.client.base.AbstractRecipesWidget;
import com.illusivesoulworks.polymorph.api.client.widgets.children.OpenSelectionButton;
import com.illusivesoulworks.polymorph.platform.Services;
import com.jerry.polymek.PolymorphicMekanism;
import com.mojang.blaze3d.systems.RenderSystem;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.element.GuiElement.ButtonBackground;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Keeps Polymorph's picker interaction while rendering Mekanism's native button background.
 */
public final class MekanismOpenSelectionButton extends OpenSelectionButton {

    private static final ResourceLocation ICON = PolymorphicMekanism.rl("gui/button/craft_change.png");

    private final AbstractContainerScreen<?> containerScreen;
    private int relativeX;
    private int relativeY;

    public MekanismOpenSelectionButton(AbstractContainerScreen<?> screen, int x, int y, OnPress onPress) {
        super(screen, x, y, AbstractRecipesWidget.SELECTOR, onPress);
        containerScreen = screen;
        setOffsets(x, y);
    }

    /**
     * Updates the hit box immediately, including before a moved QIO window handles its next click.
     */
    @Override
    public void setOffsets(int x, int y) {
        super.setOffsets(x, y);
        relativeX = x;
        relativeY = y;
        synchronizePosition();
    }

    private void synchronizePosition() {
        setPosition(Services.CLIENT_PLATFORM.getScreenLeft(containerScreen) + relativeX,
                Services.CLIENT_PLATFORM.getScreenTop(containerScreen) + relativeY);
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        synchronizePosition();
        MekanismRenderer.resetColor(graphics);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        int state = !active ? 0 : isHovered() ? 2 : 1;
        GuiUtils.blitNineSlicedSized(graphics, ButtonBackground.DEFAULT.getTexture(), getX(), getY(), getWidth(), getHeight(),
                20, 4, 200, 20, 0, state * 20, 200, 60);
        graphics.blit(ICON, getX(), getY(), getWidth(), getHeight(), 0, 0, 16, 16, 16, 16);
    }
}
