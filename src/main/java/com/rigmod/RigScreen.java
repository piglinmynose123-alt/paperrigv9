package com.rigmod;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class RigScreen extends Screen {
    private final TextFieldWidget[] host = new TextFieldWidget[9];
    private final TextFieldWidget[] viewer = new TextFieldWidget[9];
    private TextFieldWidget hostMin, hostMax, viewerMin, viewerMax;
    private ButtonWidget rerollBtn;

    public RigScreen() {
        super(Text.literal("Rig Settings"));
    }

    private TextFieldWidget field(int x, int y, int w, String value, String hint) {
        TextFieldWidget f = new TextFieldWidget(textRenderer, x, y, w, 16, Text.literal(hint));
        f.setMaxLength(128);
        f.setText(value == null ? "" : value);
        f.setPlaceholder(Text.literal(hint));
        addDrawableChild(f);
        return f;
    }

    @Override
    protected void init() {
        RigConfig c = RigConfig.INSTANCE;
        int cx = width / 2;
        int x0 = cx - 150;
        int x1 = cx + 10;
        int w = 140;
        int y0 = 32;

        for (int i = 0; i < 9; i++) {
            final int idx = i;
            int y = y0 + i * 18;
            host[i]   = field(x0, y, 110, c.hostItems[i],   "minecraft:zombie_head");
            viewer[i] = field(x1, y, 110, c.viewerItems[i], "minecraft:skeleton_skull");
            addDrawableChild(ButtonWidget.builder(Text.literal("Hand"), b -> useHeld(host[idx]))
                .dimensions(x0 + 112, y, 28, 16).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Hand"), b -> useHeld(viewer[idx]))
                .dimensions(x1 + 112, y, 28, 16).build());
        }

        hostMin   = field(x0,      206, 66, String.valueOf(c.hostMin),   "7");
        hostMax   = field(x0 + 72, 206, 66, String.valueOf(c.hostMax),   "9");
        viewerMin = field(x1,      206, 66, String.valueOf(c.viewerMin), "1");
        viewerMax = field(x1 + 72, 206, 66, String.valueOf(c.viewerMax), "6");

        addDrawableChild(ButtonWidget.builder(enabledLabel(), b -> {
            RigState.setEnabled(!RigState.enabled);
            b.setMessage(enabledLabel());
            if (rerollBtn != null) rerollBtn.setMessage(rerollLabel());
        }).dimensions(x0, 228, w, 20).build());

        rerollBtn = addDrawableChild(ButtonWidget.builder(rerollLabel(), b -> {
            RigState.roll();
            b.setMessage(rerollLabel());
        }).dimensions(x1, 228, w, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
            .dimensions(cx - 70, 252, 140, 20).build());
    }

    /** Fills the row with the item currently held in the main hand. */
    private void useHeld(TextFieldWidget f) {
        if (client == null || client.player == null) return;
        ItemStack held = client.player.getMainHandStack();
        if (held.isEmpty()) return;
        f.setText(RigLogic.idOf(held).toString());
    }

    private Text rerollLabel() {
        return Text.literal("Re-roll (H" + RigState.pick('H') + " V" + RigState.pick('V') + ")");
    }

    private Text enabledLabel() {
        return Text.literal("Rig: " + (RigState.enabled ? "ON" : "OFF"));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        int cx = width / 2;
        int x0 = cx - 150;
        int x1 = cx + 10;
        ctx.drawCenteredTextWithShadow(textRenderer, title, cx, 6, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "HOST item for number 1-9", x0, 20, 0xFFFFFF55);
        ctx.drawTextWithShadow(textRenderer, "VIEWER item for number 1-9", x1, 20, 0xFFFFFF55);
        for (int i = 0; i < 9; i++) {
            int ty = 32 + i * 18 + 4;
            ctx.drawTextWithShadow(textRenderer, String.valueOf(i + 1), x0 - 9, ty, 0xFFAAAAAA);
            ctx.drawTextWithShadow(textRenderer, String.valueOf(i + 1), x1 - 9, ty, 0xFFAAAAAA);
        }
        ctx.drawTextWithShadow(textRenderer, "Host can drop (from - to)", x0, 195, 0xFFAAAAAA);
        ctx.drawTextWithShadow(textRenderer, "Viewer can drop (from - to)", x1, 195, 0xFFAAAAAA);
    }

    private static int parse(TextFieldWidget f, int fallback) {
        try {
            return Math.max(1, Math.min(9, Integer.parseInt(f.getText().trim())));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @Override
    public void close() {
        RigConfig c = RigConfig.INSTANCE;
        for (int i = 0; i < 9; i++) {
            c.hostItems[i] = host[i].getText().trim();
            c.viewerItems[i] = viewer[i].getText().trim();
        }
        c.hostMin = parse(hostMin, c.hostMin);
        c.hostMax = parse(hostMax, c.hostMax);
        c.viewerMin = parse(viewerMin, c.viewerMin);
        c.viewerMax = parse(viewerMax, c.viewerMax);
        RigConfig.normalize();
        RigConfig.save();
        super.close();
    }
}
