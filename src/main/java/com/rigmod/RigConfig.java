package com.rigmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.nio.file.Files;
import java.nio.file.Path;

public class RigConfig {
    /** Numbers the host side may "drop" while the rig is on (one is picked at random). */
    public int hostMin = 7;
    public int hostMax = 9;

    /** Numbers the viewer side may "drop" while the rig is on. */
    public int viewerMin = 1;
    public int viewerMax = 6;

    /**
     * Item id (or resource-pack item model id) that counts as number 1..9 for each side,
     * e.g. "minecraft:zombie_head". Blank = fall back to matching items NAMED "N H" / "N V".
     */
    public String[] hostItems = new String[9];
    public String[] viewerItems = new String[9];

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static RigConfig INSTANCE = new RigConfig();
    private static Identifier[][] parsed = new Identifier[2][9];

    static {
        normalize();
    }

    private static int clamp(int v) {
        return Math.max(1, Math.min(9, v));
    }

    /** {lo, hi} of the numbers that may be picked for this side ('H' or 'V'). */
    public static int[] range(char side) {
        int lo = clamp(side == 'V' ? INSTANCE.viewerMin : INSTANCE.hostMin);
        int hi = clamp(side == 'V' ? INSTANCE.viewerMax : INSTANCE.hostMax);
        if (lo > hi) {
            int t = lo;
            lo = hi;
            hi = t;
        }
        return new int[] {lo, hi};
    }

    /** Configured id for number n (1-9) on this side, or null if blank. */
    public static Identifier id(char side, int n) {
        if (n < 1 || n > 9) return null;
        return parsed[side == 'V' ? 1 : 0][n - 1];
    }

    private static String[] fix(String[] a) {
        String[] o = new String[9];
        for (int i = 0; i < 9; i++) {
            o[i] = (a != null && i < a.length && a[i] != null) ? a[i].trim() : "";
        }
        return o;
    }

    private static Identifier parse(String s) {
        if (s == null || s.isBlank()) return null;
        return Identifier.tryParse(s.trim());
    }

    /** Pads the arrays to 9 entries and re-parses the ids. Call after changing the config. */
    public static void normalize() {
        INSTANCE.hostItems = fix(INSTANCE.hostItems);
        INSTANCE.viewerItems = fix(INSTANCE.viewerItems);
        Identifier[][] p = new Identifier[2][9];
        for (int i = 0; i < 9; i++) {
            p[0][i] = parse(INSTANCE.hostItems[i]);
            p[1][i] = parse(INSTANCE.viewerItems[i]);
        }
        parsed = p;
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("rigmod.json");
    }

    public static void load() {
        try {
            Path p = path();
            if (Files.exists(p)) {
                RigConfig c = GSON.fromJson(Files.readString(p), RigConfig.class);
                if (c != null) INSTANCE = c;
            } else {
                save();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        normalize();
    }

    public static void save() {
        try {
            Files.writeString(path(), GSON.toJson(INSTANCE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
