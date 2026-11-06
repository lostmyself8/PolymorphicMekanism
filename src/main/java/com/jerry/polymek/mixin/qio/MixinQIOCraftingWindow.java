package com.jerry.polymek.mixin.qio;

import com.jerry.polymek.PolymorphicMekanism;
import com.jerry.polymek.common.recipe.RecipeOption;
import com.jerry.polymek.common.recipe.RecipeOptions;
import com.jerry.polymek.compat.qio.QIOCraftingTarget;
import com.jerry.polymek.compat.qio.QIOSelectionStorage;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import java.util.List;
import java.util.stream.IntStream;

import mekanism.api.inventory.IInventorySlot;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.content.qio.QIOCraftingWindow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces only recipe resolution; Mekanism retains crafting, replacement, remainder and shift-click logic.
 */
@Mixin(value = QIOCraftingWindow.class, remap = false)
public abstract class MixinQIOCraftingWindow implements QIOCraftingTarget {

    @Shadow
    @Final
    private IQIOCraftingWindowHolder holder;
    @Shadow
    @Final
    private byte windowIndex;
    @Shadow
    @Final
    private IInventorySlot outputSlot;
    @Shadow
    @Nullable
    private RecipeHolder<CraftingRecipe> lastRecipe;
    @Shadow
    private boolean isCrafting;
    @Unique
    @Nullable
    private ResourceLocation polymek$selectedRecipe;
    @Unique
    private boolean polymek$invalidReported;
    @Unique
    private boolean polymek$selectionValidated;
    @Unique
    private boolean polymek$craftingCycle;

    @Invoker("asCraftingInput")
    public abstract CraftingInput.Positioned polymek$craftingInput();

    @Shadow
    public abstract void invalidateRecipe();

    @Override
    public List<ItemStack> polymek$inputStacks() {
        QIOCraftingWindow window = (QIOCraftingWindow) (Object) this;
        return IntStream.range(0, 9).mapToObj(index -> window.getInputSlot(index).getStack().copy()).toList();
    }

    @Override
    public List<RecipeOption> polymek$options() {
        Level level = holder.getLevel();
        return level == null ? List.of() : RecipeOptions.matching(level, RecipeType.CRAFTING, polymek$craftingInput().input());
    }

    @Override
    public @Nullable ResourceLocation polymek$selectedRecipe() {
        return polymek$selectedRecipe;
    }

    @Override
    public boolean polymek$canSelect() {
        return !isCrafting && holder.getLevel() != null;
    }

    @Override
    public void polymek$selectRecipe(ResourceLocation id) {
        if (!polymek$canSelect()) {
            throw new IllegalStateException("Cannot change a QIO recipe during crafting");
        }
        polymek$restoreRecipe(id);
        ((QIOSelectionStorage) holder).polymek$storeRecipe(windowIndex, id);
        invalidateRecipe();
    }

    @Override
    public void polymek$restoreRecipe(@Nullable ResourceLocation id) {
        polymek$selectedRecipe = id;
        polymek$invalidReported = false;
        polymek$selectionValidated = false;
        lastRecipe = null;
    }

    @Inject(method = "craftingStarted", at = @At("HEAD"))
    private void polymek$beginCrafting(Player player, CallbackInfo callback) {
        polymek$craftingCycle = true;
    }

    @Inject(method = "craftingFinished", at = @At("TAIL"))
    private void polymek$endCrafting(Level level, CallbackInfo callback) {
        polymek$craftingCycle = false;
    }

    @Inject(method = "onContentsChanged", at = @At("HEAD"))
    private void polymek$releaseChoiceForEditedMaterials(CallbackInfo callback) {
        if (polymek$selectedRecipe == null || !polymek$selectionValidated || isCrafting || polymek$craftingCycle) {
            return;
        }
        Level level = holder.getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        CraftingInput input = polymek$craftingInput().input();
        RecipeHolder<?> resolved = level.getRecipeManager().byKey(polymek$selectedRecipe).orElse(null);
        // A missing/reloaded recipe remains visibly locked; only an actual material edit releases a known recipe.
        if (!input.isEmpty() && resolved != null && resolved.value() instanceof CraftingRecipe recipe && !recipe.matches(input, level)) {
            polymek$restoreRecipe(null);
            ((QIOSelectionStorage) holder).polymek$storeRecipe(windowIndex, null);
        }
    }

    @WrapMethod(method = "updateOutputSlot")
    private void polymek$resolveSelectedRecipe(Level world, Operation<Void> original) {
        ResourceLocation selected = polymek$selectedRecipe;
        if (selected == null) {
            original.call(world);
            return;
        }
        CraftingInput input = polymek$craftingInput().input();
        if (input.isEmpty()) {
            lastRecipe = null;
            outputSlot.setEmpty();
            return;
        }
        RecipeHolder<?> resolved = world.getRecipeManager().byKey(selected).orElse(null);
        if (resolved != null && resolved.value() instanceof CraftingRecipe recipe && recipe.matches(input, world)) {
            lastRecipe = new RecipeHolder<>(resolved.id(), recipe);
            outputSlot.setStack(recipe.assemble(input, world.registryAccess()));
            polymek$invalidReported = false;
            polymek$selectionValidated = true;
        } else {
            lastRecipe = null;
            outputSlot.setEmpty();
            if (!polymek$invalidReported) {
                PolymorphicMekanism.LOGGER.warn("QIO workbench {} cannot craft selected recipe {}; choose a recipe matching its current inputs", windowIndex, selected);
                polymek$invalidReported = true;
            }
        }
    }
}
