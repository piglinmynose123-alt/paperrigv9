package com.rigmod;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.Generic3x3ContainerScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Builds a purely visual version of a dropper's contents while its screen is being drawn.
 * While something has really been taken out of the dropper, only the picked number looks EMPTY
 * and every other number looks present (even the one that really dropped). Once everything is
 * back in the dropper, it simply looks full again. Works for the host and the viewer dropper.
 * Active only during one screen render, never during clicks.
 */
public final class RigView {
    private static final Set<ItemStack> NORMAL = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final ItemStack[] SNAP_H = new ItemStack[9];
    private static final ItemStack[] SNAP_V = new ItemStack[9];
    private static ItemStack[] view;
    private static Inventory viewInv;

    private RigView() {}

    /** Stacks that belong to the dropper display and must be drawn exactly as they are. */
    public static boolean isNormal(ItemStack s) {
        return !NORMAL.isEmpty() && NORMAL.contains(s);
    }

    /** The last seen real item for this number, or null. */
    public static ItemStack snapshot(char side, int number) {
        if (number < 1 || number > 9) return null;
        ItemStack s = (side == 'V' ? SNAP_V : SNAP_H)[number - 1];
        return (s == null || s.isEmpty()) ? null : s;
    }

    public static void begin(Screen screen) {
        end();
        if (!(screen instanceof Generic3x3ContainerScreen dropper)) return;

        List<Slot> slots = dropper.getScreenHandler().slots;
        if (slots.size() < 9) return;

        Inventory inv = slots.get(0).inventory;
        ItemStack[] real = new ItemStack[9];
        RigLogic.Tag[] tags = new RigLogic.Tag[9];
        int hostCount = 0;
        int viewerCount = 0;
        boolean anyEmpty = false;
        ItemStack namedLike = null;
        for (int i = 0; i < 9; i++) {
            Slot s = slots.get(i);
            real[i] = s.inventory.getStack(s.getIndex());
            if (real[i].isEmpty()) anyEmpty = true;
            tags[i] = RigLogic.tag(real[i]);
            if (tags[i] != null) {
                if (tags[i].side() == 'H') hostCount++;
                else viewerCount++;
            }
            if (namedLike == null && RigLogic.nameTag(real[i]) != null) {
                namedLike = real[i];
            }
        }
        if (hostCount == 0 && viewerCount == 0) return; // not one of the game droppers
        char side = hostCount >= viewerCount ? 'H' : 'V';

        // Remember the intact dropper (all 9 present and in order), even while the rig is off,
        // so items can be shown again later.
        boolean intact = true;
        for (int i = 0; i < 9; i++) {
            if (tags[i] == null || tags[i].side() != side || tags[i].number() != i + 1) {
                intact = false;
                break;
            }
        }
        if (intact) {
            ItemStack[] snap = side == 'V' ? SNAP_V : SNAP_H;
            for (int i = 0; i < 9; i++) snap[i] = real[i].copy();
        }

        if (!RigState.enabled) return;

        int pick = RigState.pick(side);
        ItemStack[] out = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            if (anyEmpty && i + 1 == pick) {
                out[i] = ItemStack.EMPTY;   // something really dropped: this one looks dropped
            } else {
                out[i] = shown(i, side, real, tags[i], namedLike);
            }
        }

        view = out;
        viewInv = inv;
        for (ItemStack st : out) {
            if (!st.isEmpty()) NORMAL.add(st);
        }
    }

    public static void end() {
        view = null;
        viewInv = null;
        NORMAL.clear();
    }

    /** Used by the Slot mixin. Returns null when the real stack should be used. */
    public static ItemStack viewFor(Slot slot) {
        ItemStack[] v = view;
        if (v == null || slot.inventory != viewInv) return null;
        int i = slot.getIndex();
        if (i < 0 || i > 8) return null;
        return v[i];
    }

    /** What slot i should look like when it is NOT the "dropped" slot. */
    private static ItemStack shown(int i, char side, ItemStack[] realAll, RigLogic.Tag tag, ItemStack namedLike) {
        int n = i + 1;
        ItemStack real = realAll[i];

        // Genuine item for this slot (or something unrecognised): show it as it is.
        boolean genuine = !real.isEmpty() && (tag == null || (tag.side() == side && tag.number() == n));
        if (genuine) return real;

        // Empty (really dropped) or a different number put here: show the right item for this slot.
        ItemStack c;
        ItemStack snap = snapshot(side, n);
        if (snap != null) {
            c = snap.copy();
        } else {
            c = RigLogic.fromConfig(side, n);
            if (c != null) {
                if (namedLike != null) RigLogic.setName(c, n, side, namedLike);
            } else {
                // Name-based items and nothing remembered: copy a neighbour and rename it.
                for (ItemStack other : realAll) {
                    RigLogic.Tag t = RigLogic.tag(other);
                    if (t != null && t.side() == side) {
                        c = other.copy();
                        RigLogic.setName(c, n, side, other);
                        break;
                    }
                }
            }
        }
        if (c == null) return real;
        c.setCount(real.isEmpty() ? 1 : real.getCount());
        return c;
    }
}
