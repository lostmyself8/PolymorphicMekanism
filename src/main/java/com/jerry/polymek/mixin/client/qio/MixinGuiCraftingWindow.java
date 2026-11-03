package com.jerry.polymek.mixin.client.qio;

import com.jerry.polymek.client.qio.GuiQIORecipePickerElement;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiCraftingWindow;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.inventory.container.SelectedWindowData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Attaches the selector to each native client window instead of a tick-positioned full-screen overlay.
 */
@Mixin(value = GuiCraftingWindow.class, remap = false)
public abstract class MixinGuiCraftingWindow extends GuiWindow {

    @Unique
    private GuiQIORecipePickerElement polymek$picker;

    protected MixinGuiCraftingWindow(IGuiWrapper gui, int x, int y, int width, int height, SelectedWindowData data) {
        super(gui, x, y, width, height, data);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void polymek$attachPicker(IGuiWrapper gui, int x, int y, QIOItemViewerContainer container,
                                      SelectedWindowData windowData, CallbackInfo callback) {
        polymek$picker = addChild(new GuiQIORecipePickerElement((GuiCraftingWindow) (Object) this));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (polymek$picker.isMouseOver(mouseX, mouseY)) {
            // An expanded choice may be above the window body; do not let GuiWindow turn this click into title dragging.
            boolean handled = polymek$picker.mouseClicked(mouseX, mouseY, button);
            if (handled) {
                setFocused(polymek$picker);
            }
            return handled;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onFocusLost() {
        polymek$picker.closePicker();
        super.onFocusLost();
    }
}