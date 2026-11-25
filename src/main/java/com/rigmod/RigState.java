package com.rigmod;

import java.util.Random;

public final class RigState {
    public static volatile boolean enabled = false;

    /** The number that "dropped" for each side while the rig is on (chosen when the rig is turned on). */
    public static int hostPick = 0;
    public static int viewerPick = 0;

    private static final Random RNG = new Random();

    private RigState() {}

    public static void setEnabled(boolean on) {
        enabled = on;
        if (on) roll();
    }

    /** Picks a new random number inside each side's allowed range. */
    public static void roll() {
        hostPick = rollIn('H');
        viewerPick = rollIn('V');
    }

    private static int rollIn(char side) {
        int[] r = RigConfig.range(side);
        return r[0] + RNG.nextInt(r[1] - r[0] + 1);
    }

    /** Current pick for a side ('H' or 'V'); re-rolls if it is missing or outside the range. */
    public static int pick(char side) {
        int[] r = RigConfig.range(side);
        int p = side == 'V' ? viewerPick : hostPick;
        if (p < r[0] || p > r[1]) {
            p = rollIn(side);
            if (side == 'V') viewerPick = p;
            else hostPick = p;
        }
        return p;
    }
}
