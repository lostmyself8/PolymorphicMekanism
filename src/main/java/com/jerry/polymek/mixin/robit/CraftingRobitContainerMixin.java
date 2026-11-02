package com.jerry.polymek.mixin.robit;

import com.illusivesoulworks.polymorph.mixin.core.AccessorCraftingMenu;
import com.jerry.polymek.common.recipe.RecipeOption;
import com.jerry.polymek.compat.robit.RobitCraftingAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import mekanism.common.inventory.container.entity.robit.CraftingRobitContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Selects a workbench recipe independently for each Robit menu and its own actual materials.
 */
@Mixin(value = CraftingRobitContainer.class, remap = false)
public abstract class CraftingRobitContainerMixin implements RobitCraftingAccess {

    @Unique
    private static final Logger POLYMEK_LOG = LoggerFactory.getLogger("polymek.robit");
    @Unique
    @Nullable
    private ResourceLocation polymek$selection;
    @Unique
    private List<ItemStack> polymek$selectionInput = List.of();
    @Unique
    private boolean polymek$reportedInvalidRecipe;

    @Override
    public List<RecipeOption> polymek$options() {
        AccessorCraftingMenu access = (AccessorCraftingMenu) this;
        Level level = access.getPlayer().level();
        CraftingInput input = access.getCraftSlots().asCraftInput();
        return level.getRecipeManager().getRecipesFor(RecipeType.CRAFTING, input, level).stream()
                .map(holder -> new RecipeOption(holder.id(), holder.value().assemble(input, level.registryAccess())))
                .filter(option -> !option.output().isEmpty())
                .toList();
    }

    @Override
    public @Nullable ResourceLocation polymek$selectedRecipe() {
        AccessorCraftingMenu access = (AccessorCraftingMenu) this;
        CraftingInput input = access.getCraftSlots().asCraftInput();
        if (input.isEmpty()) {
            return null;
        }
        return polymek$craftingRecipe(input, access.getPlayer().level())
                .map(RecipeHolder::id).orElse(polymek$selection);
    }

    @Override
    public boolean polymek$canSelect() {
        return !((AccessorCraftingMenu) this).getCraftSlots().asCraftInput().isEmpty();
    }

    @Override
    public void polymek$selectRecipe(ResourceLocation id) {
        if (polymek$options().stream().noneMatch(option -> option.id().equals(id))) {
            throw new IllegalArgumentException("Recipe does not match this Robit workbench: " + id);
        }
        AccessorCraftingMenu access = (AccessorCraftingMenu) this;
        polymek$selection = id;
        polymek$selectionInput = polymek$copyInput(access.getCraftSlots());
        polymek$reportedInvalidRecipe = false;
        ((CraftingRobitContainer) (Object) this).slotsChanged(access.getCraftSlots());
    }

    @Override
    public Optional<RecipeHolder<CraftingRecipe>> polymek$craftingRecipe(CraftingInput input, Level level) {
        List<RecipeHolder<CraftingRecipe>> recipes = level.getRecipeManager().getRecipesFor(RecipeType.CRAFTING, input, level);
        if (polymek$selection != null) {
            Optional<RecipeHolder<CraftingRecipe>> selected = recipes.stream()
                    .filter(holder -> holder.id().equals(polymek$selection)).findFirst();
            if (selected.isPresent()) {
                polymek$selectionInput = polymek$copyInput(((AccessorCraftingMenu) this).getCraftSlots());
                polymek$reportedInvalidRecipe = false;
                return selected;
            }
            List<ItemStack> current = polymek$copyInput(((AccessorCraftingMenu) this).getCraftSlots());
            if (polymek$sameInput(current, polymek$selectionInput)) {
                if (!polymek$reportedInvalidRecipe) {
                    polymek$reportedInvalidRecipe = true;
                    POLYMEK_LOG.warn("Robit workbench stopped: selected recipe {} no longer matches its material", polymek$selection);
                }
                return Optional.empty();
            }
            if (recipes.isEmpty()) {
                return Optional.empty();
            }
            // Another complete material pattern starts a new workbench selection context.
            polymek$selection = null;
            polymek$selectionInput = List.of();
            polymek$reportedInvalidRecipe = false;
        }
        return recipes.stream().findFirst();
    }

    @Unique
    private static List<ItemStack> polymek$copyInput(CraftingContainer container) {
        List<ItemStack> input = new ArrayList<>(container.getContainerSize());
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            input.add(stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
        }
        return input;
    }

    @Unique
    private static boolean polymek$sameInput(List<ItemStack> first, List<ItemStack> second) {
        if (first.size() != second.size()) {
            return false;
        }
        for (int slot = 0; slot < first.size(); slot++) {
            if (!ItemStack.isSameItemSameComponents(first.get(slot), second.get(slot))) {
                return false;
            }
        }
        return true;
    }
}