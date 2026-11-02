package com.jerry.polymek.client.qio;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiCraftingWindow;
import net.minecraft.client.gui.GuiGraphics;

/** Bridges native QIO window elements to the screen's single Polymorph/JEI and snapshot coordinator. */
public interface QioNativeRecipeView {

    /** Identifies the live GUI owner so a transferred window never keeps using its previous screen's coordinator. */
    boolean isFor(IGuiWrapper gui);

    /** Includes the expanded recipe row in native window hit testing, including the portion outside the window body. */
    boolean contains(GuiCraftingWindow owner, int relativeX, int relativeY, double mouseX, double mouseY);

    /** Draws the focused window's picker once at the native element's current position and existing foreground depth. */
    void renderInWindow(GuiCraftingWindow owner, int relativeX, int relativeY, GuiGraphics graphics,
          int mouseX, int mouseY, float partialTick);

    /** Handles one native click after synchronizing hit boxes and establishes window focus before any selection packet. */
    boolean mouseClickedInWindow(GuiCraftingWindow owner, int relativeX, int relativeY, double mouseX, double mouseY, int button);

    /** Dismisses only the given window's transient picker on focus loss or native window close. */
    void closeInWindow(GuiCraftingWindow owner);
}