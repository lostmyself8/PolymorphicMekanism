package com.jerry.polymek.client.assemblicator;

import com.jerry.polymek.client.recipe.ContextualRecipesWidget;
import com.jerry.polymek.common.recipe.RecipeTargetKey;
import com.jerry.polymek.compat.assemblicator.AssemblicatorIntegration;
import java.util.ArrayList;
import java.util.List;
import mekanism.common.inventory.container.tile.FormulaicAssemblicatorContainer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Places a contextual Polymorph picker after the formulaic assemblicator's native auto-mode button. */
public final class AssemblicatorRecipesWidget extends ContextualRecipesWidget {

    private final FormulaicAssemblicatorContainer menu;

    public AssemblicatorRecipesWidget(AbstractContainerScreen<?> screen, FormulaicAssemblicatorContainer menu) {
        super(screen);
        this.menu = menu;
    }

    @Override
    protected RecipeTargetKey targetKey() {
        return new RecipeTargetKey(AssemblicatorIntegration.KIND, 0);
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
        return 130;
    }

    @Override
    public int getYPos() {
        return 75;
    }
}
