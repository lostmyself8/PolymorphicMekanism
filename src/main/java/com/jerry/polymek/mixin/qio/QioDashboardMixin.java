package com.jerry.polymek.mixin.qio;

import com.jerry.polymek.common.registries.PolymekDataComponents;
import com.jerry.polymek.compat.qio.QioCraftingTarget;
import com.jerry.polymek.compat.qio.QioSelectionStorage;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.tile.qio.TileEntityQIOComponent;
import mekanism.common.tile.qio.TileEntityQIODashboard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Persists block dashboard choices through normal saves and Mekanism's sustained block-item data.
 */
@Mixin(value = TileEntityQIODashboard.class, remap = false)
public abstract class QioDashboardMixin extends TileEntityQIOComponent implements QioSelectionStorage {

    @Unique
    private static final String polymek$KEY = "polymek_qio_recipes";

    @Shadow
    public abstract QIOCraftingWindow[] getCraftingWindows();

    protected QioDashboardMixin(Holder<Block> blockProvider, BlockPos position, BlockState state) {
        super(blockProvider, position, state);
    }

    @Override
    public void polymek$storeRecipe(int index, @Nullable ResourceLocation recipeId) {
        setChanged();
    }

    @Inject(method = "writeSustainedData", at = @At("TAIL"))
    private void polymek$saveChoices(HolderLookup.Provider provider, CompoundTag tag, CallbackInfo callback) {
        CompoundTag choices = new CompoundTag();
        QIOCraftingWindow[] windows = getCraftingWindows();
        for (int index = 0; index < windows.length; index++) {
            ResourceLocation selected = ((QioCraftingTarget) windows[index]).polymek$selectedRecipe();
            if (selected != null) {
                choices.putString(Integer.toString(index), selected.toString());
            }
        }
        if (!choices.isEmpty()) {
            tag.put(polymek$KEY, choices);
        }
    }

    @Inject(method = "readSustainedData", at = @At("TAIL"))
    private void polymek$loadChoices(HolderLookup.Provider provider, CompoundTag tag, CallbackInfo callback) {
        CompoundTag choices = tag.getCompound(polymek$KEY);
        QIOCraftingWindow[] windows = getCraftingWindows();
        for (int index = 0; index < windows.length; index++) {
            String key = Integer.toString(index);
            ResourceLocation selected = choices.contains(key) ? ResourceLocation.parse(choices.getString(key)) : null;
            ((QioCraftingTarget) windows[index]).polymek$restoreRecipe(selected);
        }
    }

    @Inject(method = "collectImplicitComponents", at = @At("TAIL"))
    private void polymek$collectChoices(DataComponentMap.Builder builder, CallbackInfo callback) {
        QIOCraftingWindow[] windows = getCraftingWindows();
        for (int index = 0; index < windows.length; index++) {
            ResourceLocation selected = ((QioCraftingTarget) windows[index]).polymek$selectedRecipe();
            if (selected != null) {
                builder.set(PolymekDataComponents.qioRecipeComponent(index), selected);
            }
        }
    }

    @Inject(method = "applyImplicitComponents", at = @At("TAIL"))
    private void polymek$applyChoices(BlockEntity.DataComponentInput input, CallbackInfo callback) {
        QIOCraftingWindow[] windows = getCraftingWindows();
        for (int index = 0; index < windows.length; index++) {
            ((QioCraftingTarget) windows[index]).polymek$restoreRecipe(input.get(PolymekDataComponents.qioRecipeComponent(index)));
            windows[index].invalidateRecipe();
        }
    }
}