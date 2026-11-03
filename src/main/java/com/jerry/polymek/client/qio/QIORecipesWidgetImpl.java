package com.jerry.polymek.client.qio;

import com.jerry.polymek.client.recipe.ContextualRecipesWidget;
import com.jerry.polymek.common.recipe.RecipeTargetKey;
import com.jerry.polymek.compat.qio.QIOIntegration;
import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiCraftingWindow;
import mekanism.client.gui.qio.GuiQIOItemViewer;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.inventory.container.slot.VirtualInventoryContainerSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Routes the selector and JEI recipe choice to the focused, live QIO workbench, including moved windows. */
public final class QIORecipesWidgetImpl extends ContextualRecipesWidget implements QIORecipesWidget {

    private final GuiQIOItemViewer<?> screen;
    private @Nullable GuiCraftingWindow presentedWindow;

    public QIORecipesWidgetImpl(GuiQIOItemViewer<?> screen) {
        super(screen);
        this.screen = screen;
    }

    @Override
    protected @Nullable RecipeTargetKey targetKey() {
        byte index = screen.getMenu().getSelectedCraftingGrid();
        if (index < 0 || index >= 3) {
            return null;
        }
        VirtualInventoryContainerSlot output = screen.getMenu().getCraftingWindowSlot(index, 9);
        if (!(output.getLinkedWindow() instanceof GuiCraftingWindow window) || window.getIndex() != index
              || !screen.getWindows().contains(window)) {
            return null;
        }
        return new RecipeTargetKey(QIOIntegration.KIND, index);
    }

    @Override
    protected List<ItemStack> inputStacks() {
        RecipeTargetKey key = targetKey();
        if (key == null) {
            return List.of();
        }
        List<ItemStack> inputs = new ArrayList<>(9);
        QIOItemViewerContainer menu = screen.getMenu();
        for (int slot = 0; slot < 9; slot++) {
            inputs.add(menu.getCraftingWindowSlot((byte) key.index(), slot).getItem());
        }
        return inputs;
    }

    @Override
    protected boolean mayPresentPicker() {
        return inputStacks().stream().anyMatch(stack -> !stack.isEmpty());
    }

    @Override
    public Slot getOutputSlot() {
        byte selected = screen.getMenu().getSelectedCraftingGrid();
        return screen.getMenu().getCraftingWindowSlot(selected >= 0 && selected < 3 ? selected : (byte) 0, 9);
    }

    @Override
    public int getXPos() {
        return ((VirtualInventoryContainerSlot) getOutputSlot()).getActualX() + BUTTON_X_OFFSET;
    }

    @Override
    public int getYPos() {
        // The native button is below the title drag region rather than overlapping its bottom three pixels.
        return ((VirtualInventoryContainerSlot) getOutputSlot()).getActualY() + BUTTON_Y_OFFSET + 4;
    }

    @Override
    public boolean isFor(IGuiWrapper gui) {
        return gui == screen;
    }

    private boolean ownsNativeContext(GuiCraftingWindow owner) {
        RecipeTargetKey key = targetKey();
        return owner.gui() == screen && key != null && key.index() == owner.getIndex()
              && getOutputSlot() instanceof VirtualInventoryContainerSlot output && output.getLinkedWindow() == owner
              && hasPickerContext(key) && enforcePickerPresentation();
    }

    private void positionInWindow(GuiCraftingWindow owner, int relativeX, int relativeY) {
        synchronizePickerPosition(relativeX, relativeY);
        presentedWindow = owner;
    }

    private boolean pickerContains(double mouseX, double mouseY) {
        if (openButton.visible && openButton.isMouseOver(mouseX, mouseY)) {
            return true;
        }
        if (selectionWidget.isActive()) {
            return selectionWidget.getOutputWidgets().stream().anyMatch(output -> output.visible && output.isMouseOver(mouseX, mouseY));
        }
        return false;
    }

    @Override
    public boolean contains(GuiCraftingWindow owner, int relativeX, int relativeY, double mouseX, double mouseY) {
        if (!ownsNativeContext(owner)) {
            return false;
        }
        positionInWindow(owner, relativeX, relativeY);
        return pickerContains(mouseX, mouseY);
    }

    @Override
    public void renderInWindow(GuiCraftingWindow owner, int relativeX, int relativeY, GuiGraphics graphics,
          int mouseX, int mouseY, float partialTick) {
        if (ownsNativeContext(owner)) {
            positionInWindow(owner, relativeX, relativeY);
            boolean hoverBelongsToOwner = screen.getWindowHovering(mouseX, mouseY) == owner;
            renderPickerContents(graphics, hoverBelongsToOwner ? mouseX : Integer.MIN_VALUE,
                  hoverBelongsToOwner ? mouseY : Integer.MIN_VALUE, partialTick);
        }
    }

    @Override
    public boolean mouseClickedInWindow(GuiCraftingWindow owner, int relativeX, int relativeY,
          double mouseX, double mouseY, int button) {
        if (!ownsNativeContext(owner)) {
            return false;
        }
        positionInWindow(owner, relativeX, relativeY);
        if (!pickerContains(mouseX, mouseY)) {
            selectionWidget.setActive(false);
            return false;
        }
        // Native windows normally focus after dispatching children; establish the correct recipe context before sending.
        owner.onFocused();
        clickPickerContents(mouseX, mouseY, button);
        return true;
    }

    @Override
    public void closeInWindow(GuiCraftingWindow owner) {
        if (presentedWindow == owner) {
            selectionWidget.setActive(false);
            presentedWindow = null;
        }
    }

    /** Screen-event rendering is disabled because the native window child is the exclusive visual owner. */
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Polymorph retains this object for ticking, JEI, and snapshots; drawing belongs to renderInWindow.
    }

    /** Screen-event input must fall through so Mekanism dispatches the picker exactly once through its native window. */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }
}
