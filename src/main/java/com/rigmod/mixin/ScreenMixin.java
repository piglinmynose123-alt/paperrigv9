package com.rigmod.mixin;

import com.rigmod.RigLogic;
import com.rigmod.RigView;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @ModifyVariable(
        method = "getTooltipFromItem(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/item/ItemStack;)Ljava/util/List;",
        at = @At("HEAD"),
        argsOnly = true
    )
    private static ItemStack rigmod$disguiseTooltip(ItemStack stack) {
        return RigLogic.disguise(stack);
    }

    // Covers the whole draw of a screen including its tooltip, and nothing else (never clicks).
    @Inject(method = "renderWithTooltip", at = @At("HEAD"))
    private void rigmod$beginView(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        RigView.begin((Screen) (Object) this);
    }

    @Inject(method = "renderWithTooltip", at = @At("RETURN"))
    private void rigmod$endView(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        RigView.end();
    }
}
