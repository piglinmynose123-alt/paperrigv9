package com.rigmod;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RigLogic {
    // "1 H", "1H", "1-H", "#1 H" ... and also "H 1", "H1", "V-3"
    private static final Pattern NUM_FIRST =
        Pattern.compile("(?<![\\p{L}\\p{N}])([1-9])\\s*[-_#.]?\\s*([HhVv])(?![\\p{L}\\p{N}])");
    private static final Pattern SIDE_FIRST =
        Pattern.compile("(?<![\\p{L}\\p{N}])([HhVv])\\s*[-_#.]?\\s*([1-9])(?![\\p{L}\\p{N}])");

    /** side is 'H' (host) or 'V' (viewer). */
    public record Tag(char side, int number) {}

    private RigLogic() {}

    /** The item model id of a stack (the item id unless a resource pack/command changed it). */
    public static Identifier idOf(ItemStack stack) {
        Identifier model = stack.get(DataComponentTypes.ITEM_MODEL);
        return model != null ? model : Registries.ITEM.getId(stack.getItem());
    }

    /** Reads the number and side from the item's displayed name ("1 H", "V 3", ...). Null if none. */
    public static Tag nameTag(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        String name = stack.getName().getString();
        Matcher m = NUM_FIRST.matcher(name);
        if (m.find()) {
            return new Tag(Character.toUpperCase(m.group(2).charAt(0)), m.group(1).charAt(0) - '0');
        }
        m = SIDE_FIRST.matcher(name);
        if (m.find()) {
            return new Tag(Character.toUpperCase(m.group(1).charAt(0)), m.group(2).charAt(0) - '0');
        }
        return null;
    }

    /**
     * Which number/side a stack is. The NAME comes first, because the host and viewer droppers
     * may use the very same items and only differ by name. If the name says nothing, the item ids
     * from the menu are used.
     */
    public static Tag tag(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        Tag byName = nameTag(stack);
        if (byName != null) return byName;

        Identifier id = idOf(stack);
        char[] sides = {'H', 'V'};
        for (char side : sides) {
            for (int n = 1; n <= 9; n++) {
                Identifier want = RigConfig.id(side, n);
                if (want != null && want.equals(id)) return new Tag(side, n);
            }
        }
        return null;
    }

    /** Names a fake stack "<number> <side>", in the same way (and style) the real item is named. */
    public static void setName(ItemStack out, int number, char side, ItemStack like) {
        Text name = Text.literal(number + " " + side).setStyle(like.getName().getStyle());
        if (like.contains(DataComponentTypes.CUSTOM_NAME)) {
            out.set(DataComponentTypes.CUSTOM_NAME, name);
        } else {
            out.set(DataComponentTypes.ITEM_NAME, name);
        }
    }

    /** A fresh stack of the item configured in the menu for this number, or null if blank. */
    public static ItemStack fromConfig(char side, int number) {
        Identifier id = RigConfig.id(side, number);
        if (id == null) return null;
        Item item = Registries.ITEM.get(id);
        if (item == Items.AIR) {
            // Not a normal item id: treat it as a resource-pack item model.
            ItemStack out = new ItemStack(Items.PAPER);
            out.set(DataComponentTypes.ITEM_MODEL, id);
            return out;
        }
        return new ItemStack(item);
    }

    /**
     * RENDER-ONLY. Any recognised item that is NOT part of the dropper slot display
     * (on the ground, in hand, on the cursor, in the inventory...) is drawn as the number that
     * "dropped" for its side (picked when the rig was turned on). The real item is never touched.
     */
    public static ItemStack disguise(ItemStack stack) {
        if (!RigState.enabled || stack == null || stack.isEmpty()) return stack;
        if (RigView.isNormal(stack)) return stack;
        Tag t = tag(stack);
        if (t == null) return stack;
        int shown = RigState.pick(t.side());
        if (shown == t.number()) return stack;
        return lookFor(t.side(), shown, stack);
    }

    private static ItemStack lookFor(char side, int number, ItemStack real) {
        ItemStack out;
        ItemStack snap = RigView.snapshot(side, number);
        if (snap != null) {
            out = snap.copy();
        } else {
            out = fromConfig(side, number);
            if (out == null) out = new ItemStack(Items.PAPER);
            if (nameTag(real) != null) setName(out, number, side, real);
        }
        out.setCount(real.getCount());
        return out;
    }
}
