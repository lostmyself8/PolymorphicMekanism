package com.jerry.polymek.client.robit;

import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.robit.GuiRobitSmelting;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

/**
 * Owns the Robit furnace's selector button in Mekanism's native rendering and input tree.
 */
public final class GuiRobitRecipePickerElement extends GuiElement {

    public GuiRobitRecipePickerElement(GuiRobitSmelting gui) {
        super(gui, RobitRecipesWidget.BUTTON_X, RobitRecipesWidget.BUTTON_Y, 16, 16);
    }

    private @Nullable RobitRecipesWidget widget() {
        return RobitClientIntegration.recipesWidget((GuiRobitSmelting) gui());
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        RobitRecipesWidget widget = widget();
        return widget != null && widget.isNativeButtonVisible() && super.isMouseOver(mouseX, mouseY);
    }

    @Override
    public void renderForeground(GuiGraphics graphics, int mouseX, int mouseY) {
        RobitRecipesWidget widget = widget();
        if (widget != null) {
            boolean windowCoversButton = gui().getWindowHovering(mouseX, mouseY) != null;
            graphics.pose().pushPose();
            graphics.pose().translate(-getGuiLeft(), -getGuiTop(), 0);
            widget.renderNativeButton(graphics, windowCoversButton ? Integer.MIN_VALUE : mouseX,
                    windowCoversButton ? Integer.MIN_VALUE : mouseY, 0);
            graphics.pose().popPose();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        RobitRecipesWidget widget = widget();
        return widget != null && widget.clickNativeButton(mouseX, mouseY, button);
    }
}
