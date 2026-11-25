package com.rigmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.Generic3x3ContainerScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class RigMod implements ClientModInitializer {
    public static KeyBinding toggleKey;
    public static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        RigConfig.load();

        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("rigmod", "main"));

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.rigmod.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, category));
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.rigmod.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_X, category));

        // Keybinds are only polled when no screen is open...
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) toggle(client);
            while (menuKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new RigScreen());
            }
        });

        // ...so inside the dropper GUI we catch the toggle key ourselves.
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (isToggleableScreen(screen)) {
                ScreenKeyboardEvents.allowKeyPress(screen).register((s, input) -> {
                    if (matches(toggleKey, input)) {
                        toggle(client);
                        return false;
                    }
                    return true;
                });
            }
        });
    }

    private static boolean matches(KeyBinding binding, KeyInput input) {
        return !binding.isUnbound() && KeyBindingHelper.getBoundKeyOf(binding).getCode() == input.key();
    }

    private static boolean isToggleableScreen(Screen s) {
        return s instanceof Generic3x3ContainerScreen
            || s instanceof GenericContainerScreen
            || s instanceof InventoryScreen;
    }

    private static void toggle(MinecraftClient client) {
        RigState.setEnabled(!RigState.enabled);
    }
}
