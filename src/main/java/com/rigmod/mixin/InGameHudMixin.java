package com.rigmod.mixin;

import com.rigmod.RigLogic;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * The item name that pops up above the hotbar when you select an item.
 * Every ItemStack.getName() call inside the HUD is routed through the rig, so the popup shows
 * the same fake name as everywhere else. Render-only; the real item is untouched.
 * (require = 0 so a future change in the HUD code can never stop the game from starting.)
 */
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Redirect(
        method = "/.*/",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getName()Lnet/minecraft/text/Text;"),
        require = 0
    )
    private Text rigmod$heldItemName(ItemStack stack) {
        return RigLogic.disguise(stack).getName();
    }
}
