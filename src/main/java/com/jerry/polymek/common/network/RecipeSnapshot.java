package com.jerry.polymek.common.network;

import com.jerry.polymek.common.recipe.RecipeOption;
import com.jerry.polymek.common.recipe.RecipeTargetKey;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** An authoritative response scoped to the exact open menu and independent recipe target. */
public record RecipeSnapshot(int containerId, RecipeTargetKey target, List<ItemStack> inputs, List<RecipeOption> options,
                             Optional<ResourceLocation> selected, boolean editable) {

    public RecipeSnapshot {
        inputs = inputs.stream().map(ItemStack::copy).toList();
        options = List.copyOf(options);
    }

    /** Returns input copies so consumers cannot mutate the server input identity retained by this response. */
    @Override
    public List<ItemStack> inputs() {
        return inputs.stream().map(ItemStack::copy).toList();
    }

    /** Rejects responses for another menu, target, slot layout, material, component state, or ingredient count. */
    public boolean matchesContext(int containerId, RecipeTargetKey target, List<ItemStack> inputs) {
        return this.containerId == containerId && this.target.equals(target) && ItemStack.listMatches(this.inputs, inputs);
    }

    /**
     * Keeps the last confirmed presentation during a count-only refresh, without treating its options as current
     * authority. Empty slots, material components, slot order, menu, and target must still match. New replies must
     * use {@link #matchesContext(int, RecipeTargetKey, List)} so count-sensitive recipes refresh against exact inputs.
     */
    public boolean matchesPresentationContext(int containerId, RecipeTargetKey target, List<ItemStack> inputs) {
        if (this.containerId != containerId || !this.target.equals(target) || this.inputs.size() != inputs.size()) {
            return false;
        }
        for (int slot = 0; slot < this.inputs.size(); slot++) {
            ItemStack previous = this.inputs.get(slot);
            ItemStack current = inputs.get(slot);
            if (previous.isEmpty() != current.isEmpty()
                    || !previous.isEmpty() && !ItemStack.isSameItemSameComponents(previous, current)) {
                return false;
            }
        }
        return true;
    }

    /** Keeps an unavailable selection visible for recovery only within the inputs represented by this snapshot. */
    public boolean hasMissingSelection() {
        return selected.isPresent() && options.stream().noneMatch(option -> option.id().equals(selected.get()));
    }

    /** Offers a picker for genuine conflicts or a missing explicit choice, never for an ordinary unique recipe. */
    public boolean requiresChoice() {
        return options.size() > 1 || hasMissingSelection();
    }
}
