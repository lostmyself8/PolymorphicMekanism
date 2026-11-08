package com.jerry.polymek.client.assemblicator;

import com.jerry.polymek.client.recipe.ContextualRecipesWidget;
import com.jerry.polymek.common.recipe.RecipeTargetKey;
import com.jerry.polymek.compat.assemblicator.AssemblicatorIntegration;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

import mekanism.common.inventory.container.tile.FormulaicAssemblicatorContainer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Places a contextual Polymorph picker after the formulaic assemblicator's native auto-mode button.
 */
public final class AssemblicatorRecipesWidget extends ContextualRecipesWidget {

    public static final int BUTTON_X = 130;
    public static final int BUTTON_Y = 75;

    private final FormulaicAssemblicatorContainer menu;

    public AssemblicatorRecipesWidget(AbstractContainerScreen<?> screen, FormulaicAssemblicatorContainer menu) {
        super(screen);
        this.menu = menu;
    }

    @Override
    protected RecipeTargetKey targetKey() {
        return new RecipeTargetKey(AssemblicatorIntegration.KIND, 0);
    }

    public void renderNativeButton(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderNativePickerButton(graphics, mouseX, mouseY, partialTick);
    }

    public boolean isNativeButtonVisible() {
        return hasVisiblePickerButton();
    }

    public boolean clickNativeButton(double mouseX, double mouseY, int button) {
        return clickNativePickerButton(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return clickPickerSelectionOnly(mouseX, mouseY, button);
    }

    @Override
    protected List<ItemStack> inputStacks() {
        List<ItemStack> input = new ArrayList<>();
        // Includes the formula component and native crafting inputs, so encoding and clearing are observable.
        for (Slot slot : menu.slots) {
            if (slot.x == 6 && slot.y == 26 || slot.x >= 26 && slot.x <= 62 && slot.y >= 17 && slot.y <= 53) {
                input.add(slot.getItem());
            }
        }
        return input;
    }

    @Override
    public Slot getOutputSlot() {
        // Formulaic assemblicators have six storage outputs and no vanilla ResultSlot.
        return menu.slots.getFirst();
    }

    @Override
    public int getXPos() {
        return BUTTON_X;
    }

    @Override
    public int getYPos() {
        return BUTTON_Y;
    }
}
