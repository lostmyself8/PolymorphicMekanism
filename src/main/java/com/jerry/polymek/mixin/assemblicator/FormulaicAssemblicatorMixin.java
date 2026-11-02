package com.jerry.polymek.mixin.assemblicator;

import com.jerry.polymek.common.registries.PolymekDataComponents;
import com.jerry.polymek.common.recipe.RecipeOption;
import com.jerry.polymek.common.recipe.RecipeOptions;
import com.jerry.polymek.common.recipe.RecipeTarget;
import com.mojang.logging.LogUtils;

import java.util.List;
import java.util.Optional;

import mekanism.api.inventory.IInventorySlot;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.attachments.FormulaAttachment;
import mekanism.common.content.assemblicator.RecipeFormula;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.machine.TileEntityFormulaicAssemblicator;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Preserves native crafting, energy, and remainder handling while selecting exact recipe identities.
 */
@Mixin(value = TileEntityFormulaicAssemblicator.class, remap = false)
public abstract class FormulaicAssemblicatorMixin implements RecipeTarget {

    @Unique
    private static final Logger POLYMEK_LOG = LogUtils.getLogger();
    @Unique
    private static final String POLYMEK_RECIPE_TAG = "polymek:selected_recipe";
    @Unique
    private @Nullable ResourceLocation polymek$selected;
    @Unique
    private List<ItemStack> polymek$selectedInput = List.of();
    @Unique
    private @Nullable ResourceLocation polymek$encoding;
    @Unique
    private @Nullable ResourceLocation polymek$lastRejected;
    @Shadow
    private List<IInventorySlot> craftingGridSlots;
    @Shadow
    private RecipeHolder<CraftingRecipe> cachedRecipe;
    @Shadow
    private ItemStack lastOutputStack;
    @Shadow
    private NonNullList<ItemStack> lastRemainingItems;
    @Shadow
    private boolean isRecipe;
    @Shadow
    private boolean needsOrganize;
    @Shadow
    private boolean canTryToMove;
    @Shadow
    private int operatingTicks;
    @Shadow
    public RecipeFormula formula;
    @Shadow
    BasicInventorySlot formulaSlot;

    @Shadow
    private void recalculateRecipe() {
        throw new AssertionError("Mixin shadow");
    }

    @Shadow
    private void checkFormula() {
        throw new AssertionError("Mixin shadow");
    }

    @Unique
    private TileEntityFormulaicAssemblicator polymek$tile() {
        return (TileEntityFormulaicAssemblicator) (Object) this;
    }

    @Unique
    private List<ItemStack> polymek$copyInput() {
        return craftingGridSlots.stream().map(slot -> slot.getStack().copy()).toList();
    }

    @Unique
    private CraftingInput.Positioned polymek$template() {
        FormulaAttachment attachment = formulaSlot.getStack().getOrDefault(MekanismDataComponents.FORMULA_HOLDER, FormulaAttachment.EMPTY);
        return attachment.isEmpty() ? MekanismUtils.getCraftingInputSlots(3, 3, craftingGridSlots, true)
                : MekanismUtils.getCraftingInput(3, 3, attachment.inventory(), true);
    }

    @Unique
    private @Nullable RecipeHolder<CraftingRecipe> polymek$exact(Level level, CraftingInput input, ResourceLocation selected) {
        return level.getRecipeManager().getRecipesFor(RecipeType.CRAFTING, input, level).stream()
                .filter(holder -> holder.id().equals(selected) && (holder.value().isSpecial() || !holder.value().isIncomplete()))
                .findFirst().orElse(null);
    }

    @Override
    public List<RecipeOption> polymek$options() {
        Level level = polymek$tile().getLevel();
        return level == null ? List.of() : RecipeOptions.matching(level, RecipeType.CRAFTING, polymek$template().input());
    }

    @Override
    public @Nullable ResourceLocation polymek$selectedRecipe() {
        ItemStack stack = formulaSlot.getStack();
        FormulaAttachment attachment = stack.getOrDefault(MekanismDataComponents.FORMULA_HOLDER, FormulaAttachment.EMPTY);
        if (!attachment.isEmpty()) {
            ResourceLocation encoded = stack.get(PolymekDataComponents.FORMULA_RECIPE.get());
            return encoded == null && formula.recipe() != null ? formula.recipe().id() : encoded;
        }
        if (MekanismUtils.getCraftingInputSlots(3, 3, craftingGridSlots, true).input().isEmpty()) {
            return null;
        }
        return polymek$selected == null && cachedRecipe != null ? cachedRecipe.id() : polymek$selected;
    }

    @Override
    public boolean polymek$canSelect() {
        return !polymek$tile().getAutoMode();
    }

    @Override
    public void polymek$selectRecipe(ResourceLocation id) {
        Level level = polymek$tile().getLevel();
        RecipeHolder<CraftingRecipe> recipe = level == null ? null : polymek$exact(level, polymek$template().input(), id);
        if (!polymek$canSelect() || recipe == null) {
            throw new IllegalArgumentException("Assemblicator recipe is locked or no longer matches: " + id);
        }
        polymek$selected = id;
        polymek$selectedInput = polymek$copyInput();
        polymek$lastRejected = null;
        cachedRecipe = recipe;
        operatingTicks = 0;
        needsOrganize = true;
        canTryToMove = true;
        ItemStack stack = formulaSlot.getStack();
        FormulaAttachment attachment = stack.getOrDefault(MekanismDataComponents.FORMULA_HOLDER, FormulaAttachment.EMPTY);
        if (!attachment.isEmpty()) {
            ItemStack updated = stack.copy();
            updated.set(PolymekDataComponents.FORMULA_RECIPE.get(), id);
            updated.set(MekanismDataComponents.FORMULA_HOLDER, new FormulaAttachment(attachment.inventory(), false));
            formula = new RecipeFormula(polymek$template(), recipe);
            formulaSlot.setStack(updated);
            checkFormula();
        }
        recalculateRecipe();
        polymek$tile().markForSave();
    }

    @Inject(method = "recalculateRecipe", at = @At("HEAD"), cancellable = true)
    private void polymek$validateLockedFormula(CallbackInfo callback) {
        Level level = polymek$tile().getLevel();
        if (level == null || level.isClientSide || formulaSlot == null || craftingGridSlots == null) {
            return;
        }
        if (CommonWorldTickHandler.flushTagAndRecipeCaches) {
            cachedRecipe = null;
        }
        ItemStack stack = formulaSlot.getStack();
        FormulaAttachment attachment = stack.getOrDefault(MekanismDataComponents.FORMULA_HOLDER, FormulaAttachment.EMPTY);
        ResourceLocation selected = stack.get(PolymekDataComponents.FORMULA_RECIPE.get());
        if (!attachment.isEmpty() && selected != null) {
            RecipeHolder<CraftingRecipe> current = polymek$exact(level, polymek$template().input(), selected);
            if (current == null || attachment.invalid()) {
                cachedRecipe = null;
                isRecipe = false;
                lastOutputStack = ItemStack.EMPTY;
                lastRemainingItems = NonNullList.create();
                operatingTicks = 0;
                formula = RecipeFormula.EMPTY;
                if (!selected.equals(polymek$lastRejected)) {
                    POLYMEK_LOG.warn("Assemblicator {} stopped: encoded recipe {} is missing or does not match its formula", polymek$tile().getBlockPos(), selected);
                    polymek$lastRejected = selected;
                }
                callback.cancel();
            } else {
                formula = new RecipeFormula(polymek$template(), current);
            }
        } else if (attachment.isEmpty() && polymek$selected != null) {
            CraftingInput input = MekanismUtils.getCraftingInputSlots(3, 3, craftingGridSlots, true).input();
            if (polymek$exact(level, input, polymek$selected) == null && !ItemStack.listMatches(polymek$selectedInput, polymek$copyInput())) {
                POLYMEK_LOG.debug("Assemblicator {} cleared manual recipe {} because it no longer matches its input",
                        polymek$tile().getBlockPos(), polymek$selected);
                polymek$selected = null;
                polymek$selectedInput = List.of();
                polymek$lastRejected = null;
                cachedRecipe = null;
            } else if (cachedRecipe == null || !polymek$selected.equals(cachedRecipe.id())) {
                cachedRecipe = null;
            }
        }
    }

    @Redirect(method = "recalculateRecipe", at = @At(value = "INVOKE", target = "Lmekanism/common/recipe/MekanismRecipeType;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;"))
    private Optional<RecipeHolder<CraftingRecipe>> polymek$resolveManualRecipe(RecipeType<CraftingRecipe> type, RecipeInput input, Level level) {
        CraftingInput craftingInput = (CraftingInput) input;
        if (polymek$selected == null) {
            return MekanismRecipeType.getRecipeFor(type, craftingInput, level);
        }
        return Optional.ofNullable(polymek$exact(level, craftingInput, polymek$selected));
    }

    @Redirect(method = "loadFormula", at = @At(value = "INVOKE", target = "Lmekanism/common/content/assemblicator/RecipeFormula;create(Lnet/minecraft/world/level/Level;Lmekanism/common/attachments/FormulaAttachment;)Lmekanism/common/content/assemblicator/RecipeFormula;"))
    private RecipeFormula polymek$loadEncodedRecipe(Level world, FormulaAttachment attachment, ItemStack formulaStack, FormulaAttachment originalAttachment) {
        ResourceLocation selected = formulaStack.get(PolymekDataComponents.FORMULA_RECIPE.get());
        if (selected == null) {
            return RecipeFormula.create(world, attachment);
        }
        CraftingInput.Positioned input = MekanismUtils.getCraftingInput(3, 3, attachment.inventory(), true);
        return new RecipeFormula(input, polymek$exact(world, input.input(), selected));
    }

    @Redirect(method = "encodeFormula", at = @At(value = "INVOKE", target = "Lmekanism/common/content/assemblicator/RecipeFormula;create(Lnet/minecraft/world/level/Level;Ljava/util/List;)Lmekanism/common/content/assemblicator/RecipeFormula;"))
    private RecipeFormula polymek$encodeSelectedRecipe(Level level, List<IInventorySlot> slots) {
        CraftingInput.Positioned input = MekanismUtils.getCraftingInputSlots(3, 3, slots, true);
        RecipeHolder<CraftingRecipe> recipe = polymek$selected == null
                ? MekanismRecipeType.getRecipeFor(RecipeType.CRAFTING, input.input(), level).orElse(null)
                : polymek$exact(level, input.input(), polymek$selected);
        polymek$encoding = recipe == null ? null : recipe.id();
        return new RecipeFormula(input, recipe);
    }

    @ModifyArg(method = "encodeFormula", at = @At(value = "INVOKE", target = "Lmekanism/common/inventory/slot/BasicInventorySlot;setStack(Lnet/minecraft/world/item/ItemStack;)V"), index = 0)
    private ItemStack polymek$storeEncodedRecipe(ItemStack stack) {
        if (polymek$encoding == null) {
            throw new IllegalStateException("An encoded formula must have a matching selected recipe");
        }
        stack.set(PolymekDataComponents.FORMULA_RECIPE.get(), polymek$encoding);
        return stack;
    }

    @Inject(method = "doSingleCraft", at = @At("HEAD"), cancellable = true)
    private void polymek$validateBeforeCraft(CallbackInfoReturnable<Boolean> callback) {
        // A craft packet may arrive immediately after /reload, before the next native machine tick.
        cachedRecipe = null;
        recalculateRecipe();
        if (!isRecipe) {
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void polymek$readSelection(CompoundTag nbt, HolderLookup.Provider provider, CallbackInfo callback) {
        polymek$selected = nbt.contains(POLYMEK_RECIPE_TAG) ? ResourceLocation.parse(nbt.getString(POLYMEK_RECIPE_TAG)) : null;
        polymek$selectedInput = polymek$selected == null ? List.of() : polymek$copyInput();
    }

    @Inject(method = "onLoad", at = @At("TAIL"))
    private void polymek$rememberLoadedInput(CallbackInfo callback) {
        if (polymek$selected != null) {
            polymek$selectedInput = polymek$copyInput();
        }
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void polymek$writeSelection(CompoundTag nbt, HolderLookup.Provider provider, CallbackInfo callback) {
        if (polymek$selected != null) {
            nbt.putString(POLYMEK_RECIPE_TAG, polymek$selected.toString());
        }
    }
}