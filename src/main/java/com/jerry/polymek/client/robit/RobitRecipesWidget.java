package com.jerry.polymek.client.robit;

import com.jerry.polymek.client.recipe.ContextualRecipesWidget;
import com.jerry.polymek.common.recipe.RecipeTargetKey;
import com.jerry.polymek.compat.robit.RobitIntegration;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

/** Reuses Polymorph's selector with an explicit workbench or furnace context for this Robit screen. */
public final class RobitRecipesWidget extends ContextualRecipesWidget {

    private final boolean crafting;
    private final Slot output;
    private final List<Slot> inputs;

    public RobitRecipesWidget(AbstractContainerScreen<?> screen, boolean crafting) {
        super(screen);
        this.crafting = crafting;
        if (crafting) {
            output = screen.getMenu().getSlot(0);
            inputs = List.copyOf(screen.getMenu().slots.subList(1, 10));
        } else {
            output = screen.getMenu().slots.stream().filter(slot -> slot.x == 116 && slot.y == 35)
                  .findFirst().orElseThrow(() -> new IllegalStateException("Robit furnace has no native output slot"));
            Slot input = screen.getMenu().slots.stream().filter(slot -> slot.x == 51 && slot.y == 35)
                  .findFirst().orElseThrow(() -> new IllegalStateException("Robit furnace has no native input slot"));
            inputs = List.of(input);
        }
    }

    @Override
    protected RecipeTargetKey targetKey() {
        return new RecipeTargetKey(crafting ? RobitIntegration.CRAFTING : RobitIntegration.SMELTING, 0);
    }

    @Override
    protected List<ItemStack> inputStacks() {
        return inputs.stream().map(Slot::getItem).toList();
    }

    @Override
    public Slot getOutputSlot() {
        return output;
    }

    @Override
    public void selectRecipe(ResourceLocation recipe) {
        if (!crafting && Minecraft.getInstance().level != null) {
            boolean vanillaSmelting = Minecraft.getInstance().level.getRecipeManager().byKey(recipe)
                  .map(holder -> holder.value().getType() == RecipeType.SMELTING).orElse(false);
            if (vanillaSmelting) {
                // Mekanism imports vanilla furnace recipes under this exact generated ID.
                recipe = recipe.withPrefix("/mekanism_generated/");
            }
        }
        super.selectRecipe(recipe);
    }
}
