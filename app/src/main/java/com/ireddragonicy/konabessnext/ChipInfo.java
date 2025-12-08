package com.ireddragonicy.konabessnext;

import android.app.Activity;
import java.util.*;

public class ChipInfo {
    public enum type {
        kona, kona_singleBin, msmnile, msmnile_singleBin, lahaina, lahaina_singleBin,
        lito_v1, lito_v2, lagoon, shima, yupik, waipio_singleBin, cape_singleBin,
        kalama, diwali, ukee_singleBin, pineapple, cliffs_singleBin, cliffs_7_singleBin,
        kalama_sg_singleBin, sun, canoe, tuna, unknown
    }

    public static type which;

    // Chip groups for common behaviors
    private static final Set<type> MAX_16_LEVELS = EnumSet.of(
        type.cape_singleBin, type.waipio_singleBin, type.kalama, type.diwali,
        type.ukee_singleBin, type.pineapple, type.cliffs_singleBin, type.cliffs_7_singleBin,
        type.kalama_sg_singleBin, type.sun, type.canoe, type.tuna
    );

    private static final Set<type> IGNORE_VOLT_TABLE = EnumSet.of(
        type.lahaina, type.lahaina_singleBin, type.shima, type.yupik,
        type.waipio_singleBin, type.cape_singleBin, type.kalama, type.diwali,
        type.ukee_singleBin, type.pineapple, type.cliffs_singleBin, type.cliffs_7_singleBin,
        type.kalama_sg_singleBin, type.sun, type.canoe, type.tuna
    );

    // Level configurations
    private static final Map<type, LevelConfig> LEVEL_CONFIGS = new HashMap<>();
    
    static {
        // Initialize configurations
        LevelConfig config416 = new LevelConfig(416, LevelTemplate.STANDARD);
        LevelConfig config464 = new LevelConfig(464, LevelTemplate.EXTENDED);
        LevelConfig config480 = new LevelConfig(480, LevelTemplate.FULL);
        LevelConfig config480Extended = new LevelConfig(480, LevelTemplate.FULL_EXTENDED);
        
        // Map chips to configurations
        LEVEL_CONFIGS.put(type.kona, config416);
        LEVEL_CONFIGS.put(type.kona_singleBin, config416);
        LEVEL_CONFIGS.put(type.msmnile, config416);
        LEVEL_CONFIGS.put(type.msmnile_singleBin, config416);
        LEVEL_CONFIGS.put(type.lahaina_singleBin, config416);
        LEVEL_CONFIGS.put(type.lito_v1, config416);
        LEVEL_CONFIGS.put(type.lito_v2, config416);
        LEVEL_CONFIGS.put(type.lagoon, config416);
        LEVEL_CONFIGS.put(type.shima, config416);
        LEVEL_CONFIGS.put(type.yupik, config416);
        LEVEL_CONFIGS.put(type.waipio_singleBin, config416);
        LEVEL_CONFIGS.put(type.cape_singleBin, config416);
        LEVEL_CONFIGS.put(type.diwali, config416);
        LEVEL_CONFIGS.put(type.ukee_singleBin, config416);
        
        LEVEL_CONFIGS.put(type.lahaina, config464);
        
        LEVEL_CONFIGS.put(type.kalama, config480);
        LEVEL_CONFIGS.put(type.kalama_sg_singleBin, config480);
        LEVEL_CONFIGS.put(type.pineapple, config480);
        LEVEL_CONFIGS.put(type.cliffs_singleBin, config480);
        LEVEL_CONFIGS.put(type.cliffs_7_singleBin, config480);
        LEVEL_CONFIGS.put(type.sun, config480Extended);
        LEVEL_CONFIGS.put(type.canoe, config480Extended);
        LEVEL_CONFIGS.put(type.tuna, config480Extended);
    }

    public static int getMaxTableLevels(type type) {
        return MAX_16_LEVELS.contains(type) ? 16 : 11;
    }

    public static boolean shouldIgnoreVoltTable(type type) {
        return IGNORE_VOLT_TABLE.contains(type);
    }

    public static boolean checkChipGeneral(type input) {
        type now = normalizeType(which);
        input = normalizeType(input);
        return input == now;
    }

    private static type normalizeType(type t) {
        return t == type.lito_v2 ? type.lito_v1 : t;
    }

    public static String name2chipdesc(String name, Activity activity) {
        return name2chipdesc(type.valueOf(name), activity);
    }

    public static String name2chipdesc(type t, Activity activity) {
        // Resource mapping
        Map<type, Integer> resourceMap = new HashMap<type, Integer>() {{
            put(type.kona, R.string.sdm865_series);
            put(type.kona_singleBin, R.string.sdm865_singlebin);
            put(type.msmnile, R.string.sdm855_series);
            put(type.msmnile_singleBin, R.string.sdm855_singlebin);
            put(type.lahaina, R.string.sdm888);
            put(type.lahaina_singleBin, R.string.sdm888_singlebin);
            put(type.lito_v1, R.string.lito_v1_series);
            put(type.lito_v2, R.string.lito_v2_series);
            put(type.lagoon, R.string.lagoon_series);
            put(type.shima, R.string.sd780g);
            put(type.yupik, R.string.sd778g);
            put(type.waipio_singleBin, R.string.sd8g1_singlebin);
            put(type.cape_singleBin, R.string.sd8g1p_singlebin);
            put(type.kalama, R.string.sd8g2);
            put(type.diwali, R.string.sd7g1);
            put(type.ukee_singleBin, R.string.sd7g2);
            put(type.pineapple, R.string.sd8g3);
            put(type.cliffs_singleBin, R.string.sd8sg3);
            put(type.cliffs_7_singleBin, R.string.sd7pg3);
            put(type.kalama_sg_singleBin, R.string.sdg3xg2);
            put(type.sun, R.string.sd8e);
            put(type.canoe, R.string.sd8e_gen5);
            put(type.tuna, R.string.sd8sg4);
        }};

        Integer resourceId = resourceMap.get(t);
        return resourceId != null ? activity.getResources().getString(resourceId) 
                                  : activity.getResources().getString(R.string.unknown);
    }

    public static class rpmh_levels {
        public static int[] levels() {
            LevelConfig config = LEVEL_CONFIGS.get(which);
            return config != null ? config.getLevels() : new int[0];
        }

        public static String[] level_str() {
            LevelConfig config = LEVEL_CONFIGS.get(which);
            return config != null ? config.getLevelStrings() : new String[0];
        }
    }

    // Level configuration class
    private static class LevelConfig {
        private final int[] levels;
        private final String[] levelStrings;

        LevelConfig(int size, LevelTemplate template) {
            this.levels = new int[size];
            this.levelStrings = new String[size];
            
            for (int i = 0; i < size; i++) {
                levels[i] = i + 1;
                levelStrings[i] = template.getLevelString(i, levels[i]);
            }
        }

        int[] getLevels() { return levels; }
        String[] getLevelStrings() { return levelStrings; }
    }

    // Template for level naming patterns
    private enum LevelTemplate {
        STANDARD {
            @Override
            String getLevelString(int index, int value) {
                return STANDARD_LABELS.getOrDefault(index, String.valueOf(value));
            }
        },
        EXTENDED {
            @Override
            String getLevelString(int index, int value) {
                String label = STANDARD_LABELS.get(index);
                if (label != null) return label;
                return EXTENDED_LABELS.getOrDefault(index, String.valueOf(value));
            }
        },
        FULL {
            @Override
            String getLevelString(int index, int value) {
                String label = STANDARD_LABELS.get(index);
                if (label != null) return label;
                label = EXTENDED_LABELS.get(index);
                if (label != null) return label;
                return FULL_LABELS.getOrDefault(index, String.valueOf(value));
            }
        },
        FULL_EXTENDED {
            @Override
            String getLevelString(int index, int value) {
                String label = STANDARD_LABELS.get(index);
                if (label != null) return label;
                label = EXTENDED_LABELS.get(index);
                if (label != null) return label;
                label = FULL_LABELS.get(index);
                if (label != null) return label;
                return FULL_EXTENDED_LABELS.getOrDefault(index, String.valueOf(value));
            }
        };

        abstract String getLevelString(int index, int value);

        // Common label mappings
        private static final Map<Integer, String> STANDARD_LABELS = new HashMap<Integer, String>() {{
            put(15, "16 - RETENTION");
            put(47, "48 - MIN_SVS");
            put(55, "56 - LOW_SVS_D1");
            put(63, "64 - LOW_SVS");
            put(79, "80 - LOW_SVS_L1");
            put(95, "96 - LOW_SVS_L2");
            put(127, "128 - SVS");
            put(143, "144 - SVS_L0");
            put(191, "192 - SVS_L1");
            put(223, "224 - SVS_L2");
            put(255, "256 - NOM");
            put(319, "320 - NOM_L1");
            put(335, "336 - NOM_L2");
            put(351, "352 - NOM_L3");
            put(383, "384 - TURBO");
            put(399, "400 - TURBO_L0");
            put(415, "416 - TURBO_L1");
        }};

        private static final Map<Integer, String> EXTENDED_LABELS = new HashMap<Integer, String>() {{
            put(431, "432 - TURBO_L2");
            put(447, "448 - SUPER_TURBO");
            put(463, "464 - SUPER_TURBO_NO_CPR");
        }};

        private static final Map<Integer, String> FULL_LABELS = new HashMap<Integer, String>() {{
            put(51, "52 - LOW_SVS_D2");
            put(59, "60 - LOW_SVS_D0");
            put(71, "72 - LOW_SVS_P1");
            put(287, "288 - NOM_L0");
            put(431, "432 - TURBO_L2");
            put(447, "448 - TURBO_L3");
            put(463, "464 - SUPER_TURBO");
            put(479, "480 - SUPER_TURBO_NO_CPR");
        }};

        private static final Map<Integer, String> FULL_EXTENDED_LABELS = new HashMap<Integer, String>() {{
            put(49, "50 - LOW_SVS_D3");
            put(50, "51 - LOW_SVS_D2_5");
            put(53, "54 - LOW_SVS_D1_5");
            put(451, "452 - TURBO_L4");
        }};
    }
}