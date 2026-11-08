package com.jerry.polymek.mixin.client.assemblicator;

import com.jerry.polymek.client.assemblicator.GuiAssemblicatorRecipePickerElement;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.machine.GuiFormulaicAssemblicator;
import mekanism.common.inventory.container.tile.FormulaicAssemblicatorContainer;
import mekanism.common.tile.machine.TileEntityFormulaicAssemblicator;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds the recipe picker as a native control immediately after Mekanism's auto-mode button.
 */
@Mixin(value = GuiFormulaicAssemblicator.class, remap = false)
public abstract class MixinGuiFormulaicAssemblicator extends GuiConfigurableTile<TileEntityFormulaicAssemblicator, FormulaicAssemblicatorContainer> {

    protected MixinGuiFormulaicAssemblicator(FormulaicAssemblicatorContainer container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Inject(method = "addGuiElements", at = @At("TAIL"))
    private void polymek$addRecipePicker(CallbackInfo callback) {
        GuiFormulaicAssemblicator screen = (GuiFormulaicAssemblicator) (Object) this;
        addRenderableWidget(new GuiAssemblicatorRecipePickerElement(screen));
    }
}
