package com.jerry.polymek.mixin.client.robit;

import com.jerry.polymek.client.robit.GuiRobitRecipePickerElement;
import mekanism.client.gui.robit.GuiRobit;
import mekanism.client.gui.robit.GuiRobitSmelting;
import mekanism.common.inventory.container.entity.robit.RobitContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Attaches the furnace selector whenever the native Robit GUI constructs or rebuilds its controls. */
@Mixin(value = GuiRobitSmelting.class, remap = false)
public abstract class MixinGuiRobitSmelting extends GuiRobit<RobitContainer> {

    protected MixinGuiRobitSmelting(RobitContainer container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Inject(method = "addGuiElements", at = @At("TAIL"))
    private void polymek$addRecipePicker(CallbackInfo callback) {
        addRenderableWidget(new GuiRobitRecipePickerElement((GuiRobitSmelting) (Object) this));
    }
}
