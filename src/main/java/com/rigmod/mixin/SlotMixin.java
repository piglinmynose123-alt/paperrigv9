package com.rigmod.mixin;

import com.rigmod.RigView;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {
    @Inject(method = "getStack", at = @At("RETURN"), cancellable = true)
    private void rigmod$view(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack v = RigView.viewFor((Slot) (Object) this);
        if (v != null) cir.setReturnValue(v);
    }
}
