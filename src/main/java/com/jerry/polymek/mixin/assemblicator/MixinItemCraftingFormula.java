package com.jerry.polymek.mixin.assemblicator;

import com.jerry.polymek.common.PolymekLang;
import com.jerry.polymek.common.registries.PolymekDataComponents;

import java.util.List;
import java.util.Optional;

import mekanism.api.text.EnumColor;
import mekanism.common.item.ItemCraftingFormula;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Clearing the native formula also clears the exact recipe identity encoded by this integration.
 */
@Mixin(value = ItemCraftingFormula.class, remap = false)
public abstract class MixinItemCraftingFormula {

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;<init>(Lnet/minecraft/world/item/Item$Properties;)V"), index = 0)
    private static Properties polymek$defaultRecipe(Properties properties) {
        return properties.component(PolymekDataComponents.FORMULA_RECIPE, Optional.empty());
    }

    @Inject(method = "use", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/item/ItemStack;set(Ljava/util/function/Supplier;Ljava/lang/Object;)Ljava/lang/Object;"))
    private void polymek$clearSelection(@NotNull Level world, Player player, @NotNull InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        player.getItemInHand(hand).set(PolymekDataComponents.FORMULA_RECIPE, Optional.empty());
    }

    @Inject(method = "appendHoverText", at = @At("TAIL"))
    private void polymek$describeSelection(ItemStack itemStack, TooltipContext context, List<Component> tooltip, TooltipFlag flag, CallbackInfo callback) {
        ResourceLocation selected = (ResourceLocation) itemStack.getOrDefault(PolymekDataComponents.FORMULA_RECIPE, Optional.empty()).orElse(null);
        if (selected != null) {
            tooltip.add(PolymekLang.RECIPE_SOURCE.translateColored(EnumColor.GRAY));
            tooltip.add(PolymekLang.RECIPE_ENCODED.translateColored(EnumColor.GRAY, selected));
        }
    }
}