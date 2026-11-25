package com.rigmod.mixin;

import com.rigmod.RigLogic;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemModelManager.class)
public abstract class ItemModelManagerMixin {
    // Both entry points take exactly one ItemStack argument. Applying the swap twice is harmless.
    @ModifyVariable(method = {"clearAndUpdate", "update"}, at = @At("HEAD"), argsOnly = true)
    private ItemStack rigmod$disguise(ItemStack stack) {
        return RigLogic.disguise(stack);
    }
}
