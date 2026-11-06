package com.jerry.polymek.mixin.robit;

import com.jerry.polymek.PolymorphicMekanism;
import com.jerry.polymek.common.recipe.RecipeOption;
import com.jerry.polymek.common.registries.PolymekDataComponents;
import com.jerry.polymek.compat.robit.RobitSmeltingTarget;
import com.jerry.polymek.compat.robit.RobitSmeltingChoice;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import java.util.List;

import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.monitor.RecipeCacheLookupMonitor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps furnace selection on its owning entity while retaining Mekanism's complete processing path.
 */
@Mixin(value = EntityRobit.class, remap = false)
public abstract class MixinEntityRobit implements RobitSmeltingTarget {

    @Unique
    private static final String polymek$SMELTING_CHOICE_TAG = "polymek_smelting_choice";

    @Shadow
    @Final
    private InputInventorySlot smeltingInputSlot;
    @Shadow
    @Final
    private RecipeCacheLookupMonitor<ItemStackToItemStackRecipe> recipeCacheLookupMonitor;
    @Shadow
    private int progress;

    @Unique
    @Nullable
    private RobitSmeltingChoice polymek$choice;
    @Unique
    @Nullable
    private ResourceLocation polymek$reportedInvalidRecipe;
    // The default recipe has no explicit choice; remember the material behind its native progress.
    @Unique
    private ItemStack polymek$lastProcessingMaterial = ItemStack.EMPTY;

    @Override
    public @Nullable RobitSmeltingChoice polymek$smeltingChoice() {
        return polymek$choice;
    }

    @Override
    public void polymek$restoreSmeltingChoice(RobitSmeltingChoice choice) {
        polymek$choice = choice;
        polymek$resetProcessing();
    }

    @Override
    public List<ItemStack> polymek$inputStacks() {
        return List.of(smeltingInputSlot.getStack().copy());
    }

    @Override
    public List<RecipeOption> polymek$options() {
        polymek$normalizeMaterialContext();
        ItemStack input = smeltingInputSlot.getStack();
        return polymek$matchingRecipes().stream()
                .map(holder -> new RecipeOption(holder.id(), holder.value().getOutput(input)))
                .filter(option -> !option.output().isEmpty())
                .toList();
    }

    @Override
    public @Nullable ResourceLocation polymek$selectedRecipe() {
        polymek$normalizeMaterialContext();
        if (smeltingInputSlot.isEmpty()) {
            return null;
        }
        if (polymek$choice != null) {
            return polymek$choice.recipe();
        }
        EntityRobit robit = (EntityRobit) (Object) this;
        ItemStackToItemStackRecipe active = robit.getRecipe(0);
        return polymek$matchingRecipes().stream()
                .filter(holder -> holder.value() == active)
                .map(RecipeHolder::id)
                .findFirst().orElse(null);
    }

    @Override
    public boolean polymek$canSelect() {
        return !smeltingInputSlot.isEmpty();
    }

    @Override
    public void polymek$selectRecipe(ResourceLocation id) {
        if (polymek$matchingRecipes().stream().noneMatch(holder -> holder.id().equals(id))) {
            throw new IllegalArgumentException("Recipe does not match the Robit furnace: " + id);
        }
        ItemStack input = smeltingInputSlot.getStack();
        boolean materialChanged = polymek$choice == null
                ? !ItemStack.isSameItemSameComponents(polymek$lastProcessingMaterial, input)
                : !polymek$choice.matchesMaterial(input);
        if (polymek$choice == null || !polymek$choice.recipe().equals(id)
                || materialChanged) {
            polymek$choice = new RobitSmeltingChoice(id, input);
            polymek$reportedInvalidRecipe = null;
            if (materialChanged) {
                polymek$resetProcessing();
            } else {
                polymek$invalidateProcessing();
            }
        }
    }

    @Unique
    private List<RecipeHolder<ItemStackToItemStackRecipe>> polymek$matchingRecipes() {
        EntityRobit robit = (EntityRobit) (Object) this;
        ItemStack input = smeltingInputSlot.getStack();
        if (input.isEmpty()) {
            return List.of();
        }
        return MekanismRecipeType.SMELTING.getRecipes(robit.level()).stream()
                .filter(holder -> !holder.value().isIncomplete() && holder.value().test(input))
                .toList();
    }

    @Unique
    private void polymek$resetProcessing() {
        progress = 0;
        polymek$invalidateProcessing();
    }

    @Unique
    private void polymek$invalidateProcessing() {
        MixinRobitRecipeCacheAccess cache = (MixinRobitRecipeCacheAccess) recipeCacheLookupMonitor;
        cache.polymek$setCachedRecipe(null);
        cache.polymek$setHasNoRecipe(false);
        cache.polymek$setShouldUnpause(true);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void polymek$updateMaterialContext(CallbackInfo callback) {
        EntityRobit robit = (EntityRobit) (Object) this;
        if (!robit.level().isClientSide) {
            polymek$normalizeMaterialContext();
            polymek$lastProcessingMaterial = smeltingInputSlot.getStack().copyWithCount(1);
        }
    }

    @Unique
    private void polymek$normalizeMaterialContext() {
        EntityRobit robit = (EntityRobit) (Object) this;
        ItemStack input = smeltingInputSlot.getStack();
        if (!robit.level().isClientSide && polymek$choice != null && !input.isEmpty() && !polymek$choice.matchesMaterial(input)) {
            polymek$choice = null;
            polymek$reportedInvalidRecipe = null;
            polymek$resetProcessing();
        }
    }

    @WrapMethod(method = "getRecipe(I)Lmekanism/api/recipes/ItemStackToItemStackRecipe;")
    private ItemStackToItemStackRecipe polymek$useSelectedSmeltingRecipe(
            int cacheIndex, Operation<ItemStackToItemStackRecipe> original) {
        polymek$normalizeMaterialContext();
        if (polymek$choice == null) {
            return original.call(cacheIndex);
        }
        ItemStackToItemStackRecipe recipe = polymek$matchingRecipes().stream()
                .filter(holder -> holder.id().equals(polymek$choice.recipe()))
                .map(RecipeHolder::value).findFirst().orElse(null);
        if (recipe != null) {
            polymek$reportedInvalidRecipe = null;
        }
        if (recipe == null && !smeltingInputSlot.isEmpty()
                && !polymek$choice.recipe().equals(polymek$reportedInvalidRecipe)) {
            polymek$reportedInvalidRecipe = polymek$choice.recipe();
            PolymorphicMekanism.LOGGER.warn("Robit {} stopped: selected smelting recipe {} no longer matches its material",
                    ((EntityRobit) (Object) this).getUUID(), polymek$choice.recipe());
        }
        return recipe;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void polymek$saveSmeltingChoice(CompoundTag tag, CallbackInfo callback) {
        if (polymek$choice != null) {
            EntityRobit robit = (EntityRobit) (Object) this;
            RobitSmeltingChoice.CODEC.encodeStart(robit.registryAccess().createSerializationContext(NbtOps.INSTANCE), polymek$choice)
                    .resultOrPartial(error -> PolymorphicMekanism.LOGGER.error("Cannot save Robit smelting choice: {}", error))
                    .ifPresent(value -> tag.put(polymek$SMELTING_CHOICE_TAG, value));
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void polymek$loadSmeltingChoice(CompoundTag tag, CallbackInfo callback) {
        polymek$choice = null;
        polymek$reportedInvalidRecipe = null;
        polymek$lastProcessingMaterial = smeltingInputSlot.getStack().copyWithCount(1);
        if (tag.contains(polymek$SMELTING_CHOICE_TAG)) {
            EntityRobit robit = (EntityRobit) (Object) this;
            RobitSmeltingChoice.CODEC.parse(robit.registryAccess().createSerializationContext(NbtOps.INSTANCE), tag.get(polymek$SMELTING_CHOICE_TAG))
                    .resultOrPartial(error -> PolymorphicMekanism.LOGGER.error("Cannot load Robit smelting choice: {}", error))
                    .ifPresent(choice -> polymek$choice = choice);
        }
    }

    @ModifyReturnValue(method = "getItemVariant", at = @At("RETURN"))
    private ItemStack polymek$savePickedSmeltingChoice(ItemStack stack) {
        if (polymek$choice != null) {
            stack.set(PolymekDataComponents.ROBIT_SMELTING_CHOICE.get(), polymek$choice);
        }
        return stack;
    }
}
