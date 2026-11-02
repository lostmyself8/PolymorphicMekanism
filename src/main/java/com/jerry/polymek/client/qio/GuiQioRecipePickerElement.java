package com.jerry.polymek.client.qio;

import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.window.GuiCraftingWindow;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

/** Makes the Polymorph picker participate in QIO's native move, resize, transfer, hit-testing, and close lifecycle. */
public final class GuiQioRecipePickerElement extends GuiElement {

    /** Aligns the button with the output slot while leaving the window's top eighteen-pixel drag area clear. */
    public static final int BUTTON_X = 101;
    public static final int BUTTON_Y = 19;
    private final GuiCraftingWindow owner;

    public GuiQioRecipePickerElement(GuiCraftingWindow owner) {
        super(owner.gui(), owner.getRelativeX() + BUTTON_X, owner.getRelativeY() + BUTTON_Y, 16, 16);
        this.owner = owner;
    }

    private @Nullable QioNativeRecipeView view() {
        return QioClientIntegration.nativeView(gui());
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        QioNativeRecipeView view = view();
        return view != null && view.contains(owner, relativeX, relativeY, mouseX, mouseY);
    }

    @Override
    public void renderForeground(GuiGraphics graphics, int mouseX, int mouseY) {
        QioNativeRecipeView view = view();
        if (view != null) {
            // Native foregrounds already carry the GUI origin; Polymorph renders with absolute screen coordinates.
            graphics.pose().pushPose();
            graphics.pose().translate(-getGuiLeft(), -getGuiTop(), 0);
            view.renderInWindow(owner, relativeX, relativeY, graphics, mouseX, mouseY, 0);
            graphics.pose().popPose();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        QioNativeRecipeView view = view();
        return view != null && view.mouseClickedInWindow(owner, relativeX, relativeY, mouseX, mouseY, button);
    }

    /** Ends this window's transient interaction without retaining the coordinator across screen transfers. */
    public void closePicker() {
        QioNativeRecipeView view = view();
        if (view != null) {
            view.closeInWindow(owner);
        }
    }

    @Override
    public void onWindowClose() {
        closePicker();
        super.onWindowClose();
    }
}