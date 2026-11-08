package com.jerry.polymek.client.assemblicator;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.jetbrains.annotations.Nullable;

/**
 * Makes the formulaic assemblicator selector button part of Mekanism's native GUI element tree.
 */
public final class GuiAssemblicatorRecipePickerElement extends GuiElement {

    public static final int BUTTON_X = 125;
    public static final int BUTTON_Y = 75;

    public GuiAssemblicatorRecipePickerElement(IGuiWrapper gui) {
        super(gui, BUTTON_X, BUTTON_Y, 16, 16);
    }

    private @Nullable AssemblicatorRecipesWidget widget() {
        if (!(gui() instanceof AbstractContainerScreen<?> screen)) {
            return null;
        }
        AssemblicatorRecipesWidget widget = AssemblicatorClientIntegration.recipesWidget(screen);
        if (widget != null) {
            // Follow the widget's live offset without changing the user's chosen visible button position.
            move(widget.getXPos() - relativeX, widget.getYPos() - relativeY);
        }
        return widget;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        AssemblicatorRecipesWidget widget = widget();
        return widget != null && widget.isNativeButtonVisible() && super.isMouseOver(mouseX, mouseY);
    }

    @Override
    public void renderForeground(GuiGraphics graphics, int mouseX, int mouseY) {
        AssemblicatorRecipesWidget widget = widget();
        if (widget != null) {
            graphics.pose().pushPose();
            graphics.pose().translate(-getGuiLeft(), -getGuiTop(), 0);
            widget.renderNativeButton(graphics, mouseX, mouseY, 0);
            graphics.pose().popPose();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        AssemblicatorRecipesWidget widget = widget();
        return widget != null && widget.clickNativeButton(mouseX, mouseY, button);
    }
}
