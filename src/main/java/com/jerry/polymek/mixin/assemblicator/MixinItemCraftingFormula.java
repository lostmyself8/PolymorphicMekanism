package com.jerry.polymek.mixin.assemblicator;

import com.jerry.polymek.common.PolymekLang;
import com.jerry.polymek.common.registries.PolymekDataComponents;

import java.util.List;

import mekanism.api.text.EnumColor;
import mekanism.common.item.ItemCraftingFormula;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Clearing the native formula also clears the exact recipe identity encoded by this integration.
 */
@Mixin(value = ItemCraftingFormula.class, remap = false)
public abstract class MixinItemCraftingFormula {

    @Inject(method = "use", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/item/ItemStack;set(Ljava/util/function/Supplier;Ljava/lang/Object;)Ljava/lang/Object;"))
    private void polymek$clearSelection(@NotNull Level world, Player player, @NotNull InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        player.getItemInHand(hand).remove(PolymekDataComponents.FORMULA_RECIPE.get());
    }

    @Inject(method = "appendHoverText", at = @At("TAIL"))
    private void polymek$describeSelection(ItemStack itemStack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag, CallbackInfo callback) {
        ResourceLocation selected = itemStack.get(PolymekDataComponents.FORMULA_RECIPE.get());
        if (selected != null) {
            tooltip.add(PolymekLang.RECIPE_SOURCE.translateColored(EnumColor.GRAY));
            tooltip.add(PolymekLang.RECIPE_ENCODED.translateColored(EnumColor.GRAY, selected));
        }
    }
}