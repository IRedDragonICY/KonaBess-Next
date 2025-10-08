package xzr.konabess;


import android.app.Activity;

public class ChipInfo {
    public enum type {
        kona,
        kona_singleBin,
        msmnile,
        msmnile_singleBin,
        lahaina,
        lahaina_singleBin,
        lito_v1, lito_v2,
        lagoon,
        shima,
        yupik,
        waipio_singleBin,
        cape_singleBin,
        kalama,
        diwali,
        ukee_singleBin,
        pineapple,
        cliffs_singleBin,
        cliffs_7_singleBin,
        kalama_sg_singleBin,
        sun,
        canoe,
        tuna,
        unknown
    }

    public static int getMaxTableLevels(type type) {
        if (type == ChipInfo.type.cape_singleBin || type == ChipInfo.type.waipio_singleBin
                || type == ChipInfo.type.kalama || type == ChipInfo.type.diwali
                || type == ChipInfo.type.ukee_singleBin || type == ChipInfo.type.pineapple
                || type == ChipInfo.type.cliffs_singleBin || type == ChipInfo.type.cliffs_7_singleBin
                || type == ChipInfo.type.kalama_sg_singleBin || type == ChipInfo.type.sun || type == ChipInfo.type.canoe || type == ChipInfo.type.tuna)
            return 16;
        return 11;
    }

    public static boolean shouldIgnoreVoltTable(type type) {
        return type == ChipInfo.type.lahaina || type == ChipInfo.type.lahaina_singleBin
                || type == ChipInfo.type.shima || type == ChipInfo.type.yupik
                || type == ChipInfo.type.waipio_singleBin || type == ChipInfo.type.cape_singleBin
                || type == ChipInfo.type.kalama || type == ChipInfo.type.diwali
                || type == ChipInfo.type.ukee_singleBin || type == ChipInfo.type.pineapple
                || type == ChipInfo.type.cliffs_singleBin || type == ChipInfo.type.cliffs_7_singleBin
                || type == ChipInfo.type.kalama_sg_singleBin || type == ChipInfo.type.sun || type == ChipInfo.type.canoe || type == ChipInfo.type.tuna;
    }

    public static boolean checkChipGeneral(type input) {
        type now = which;
        if (now == type.lito_v2)
            now = type.lito_v1;
        if (input == type.lito_v2)
            input = type.lito_v1;
        return input == now;
    }

    public static String name2chipdesc(String name, Activity activity) {
        type t = type.valueOf(name);
        return name2chipdesc(t, activity);
    }

    public static String name2chipdesc(type t, Activity activity) {
        switch (t) {
            case kona:
                return activity.getResources().getString(R.string.sdm865_series);
            case kona_singleBin:
                return activity.getResources().getString(R.string.sdm865_singlebin);
            case msmnile:
                return activity.getResources().getString(R.string.sdm855_series);
            case msmnile_singleBin:
                return activity.getResources().getString(R.string.sdm855_singlebin);
            case lahaina:
                return activity.getResources().getString(R.string.sdm888);
            case lahaina_singleBin:
                return activity.getResources().getString(R.string.sdm888_singlebin);
            case lito_v1:
                return activity.getResources().getString(R.string.lito_v1_series);
            case lito_v2:
                return activity.getResources().getString(R.string.lito_v2_series);
            case lagoon:
                return activity.getResources().getString(R.string.lagoon_series);
            case shima:
                return activity.getResources().getString(R.string.sd780g);
            case yupik:
                return activity.getResources().getString(R.string.sd778g);
            case waipio_singleBin:
                return activity.getResources().getString(R.string.sd8g1_singlebin);
            case cape_singleBin:
                return activity.getResources().getString(R.string.sd8g1p_singlebin);
            case kalama:
                return activity.getResources().getString(R.string.sd8g2);
            case diwali:
                return activity.getResources().getString(R.string.sd7g1);
            case ukee_singleBin:
                return activity.getResources().getString(R.string.sd7g2);
            case pineapple:
                return activity.getResources().getString(R.string.sd8g3);
            case cliffs_singleBin:
                return activity.getResources().getString(R.string.sd8sg3);
            case cliffs_7_singleBin:
                return activity.getResources().getString(R.string.sd7pg3);
            case kalama_sg_singleBin:
                return activity.getResources().getString(R.string.sdg3xg2);
            case sun:
                return activity.getResources().getString(R.string.sd8e);
            case canoe:
                return activity.getResources().getString(R.string.sd8e_gen5);
            case tuna:
                return activity.getResources().getString(R.string.sd8sg4);
        }
        return activity.getResources().getString(R.string.unknown);
    }

    public static type which;

    public static class rpmh_levels {
        public static int[] levels() {
            if (ChipInfo.which == type.kona || ChipInfo.which == type.kona_singleBin)
                return rpmh_levels_kona.levels;
            else if (ChipInfo.which == type.msmnile || ChipInfo.which == type.msmnile_singleBin)
                return rpmh_levels_msmnile.levels;
            else if (ChipInfo.which == type.lahaina)
                return rpmh_levels_lahaina.levels;
            else if (ChipInfo.which == type.lahaina_singleBin)
                return rpmh_levels_lahaina_singleBin.levels;
            else if (ChipInfo.which == type.lito_v1 || ChipInfo.which == type.lito_v2)
                return rpmh_levels_lito.levels;
            else if (ChipInfo.which == type.lagoon)
                return rpmh_levels_lagoon.levels;
            else if (ChipInfo.which == type.shima)
                return rpmh_levels_shima.levels;
            else if (ChipInfo.which == type.yupik)
                return rpmh_levels_yupik.levels;
            else if (ChipInfo.which == type.waipio_singleBin)
                return rpmh_levels_waipio.levels;
            else if (ChipInfo.which == type.cape_singleBin)
                return rpmh_levels_cape.levels;
            else if (ChipInfo.which == type.kalama)
                return rpmh_levels_kalama.levels;
            else if (ChipInfo.which == type.diwali)
                return rpmh_levels_diwali.levels;
            else if (ChipInfo.which == type.ukee_singleBin)
                return rpmh_levels_ukee.levels;
            else if (ChipInfo.which == type.pineapple)
                return rpmh_levels_pineapple.levels;
            else if (ChipInfo.which == type.cliffs_singleBin
                    || ChipInfo.which == type.cliffs_7_singleBin)
                return rpmh_levels_cliffs.levels;
            else if (ChipInfo.which == type.kalama_sg_singleBin)
                return rpmh_levels_kalama.levels;
            else if (ChipInfo.which == type.sun)
                return rpmh_levels_sun.levels;
            else if (ChipInfo.which == type.canoe)
                return rpmh_levels_canoe.levels;
            else if (ChipInfo.which == type.tuna)
                return rpmh_levels_tuna.levels;

            return new int[]{};
        }

        public static String[] level_str() {
            if (ChipInfo.which == type.kona || ChipInfo.which == type.kona_singleBin)
                return rpmh_levels_kona.level_str;
            else if (ChipInfo.which == type.msmnile || ChipInfo.which == type.msmnile_singleBin)
                return rpmh_levels_msmnile.level_str;
            else if (ChipInfo.which == type.lahaina)
                return rpmh_levels_lahaina.level_str;
            else if (ChipInfo.which == type.lahaina_singleBin)
                return rpmh_levels_lahaina_singleBin.level_str;
            else if (ChipInfo.which == type.lito_v1 || ChipInfo.which == type.lito_v2)
                return rpmh_levels_lito.level_str;
            else if (ChipInfo.which == type.lagoon)
                return rpmh_levels_lagoon.level_str;
            else if (ChipInfo.which == type.shima)
                return rpmh_levels_shima.level_str;
            else if (ChipInfo.which == type.yupik)
                return rpmh_levels_yupik.level_str;
            else if (ChipInfo.which == type.waipio_singleBin)
                return rpmh_levels_waipio.level_str;
            else if (ChipInfo.which == type.cape_singleBin)
                return rpmh_levels_cape.level_str;
            else if (ChipInfo.which == type.kalama)
                return rpmh_levels_kalama.level_str;
            else if (ChipInfo.which == type.diwali)
                return rpmh_levels_diwali.level_str;
            else if (ChipInfo.which == type.ukee_singleBin)
                return rpmh_levels_ukee.level_str;
            else if (ChipInfo.which == type.pineapple)
                return rpmh_levels_pineapple.level_str;
            else if (ChipInfo.which == type.cliffs_singleBin
                    || ChipInfo.which == type.cliffs_7_singleBin)
                return rpmh_levels_cliffs.level_str;
            else if (ChipInfo.which == type.kalama_sg_singleBin)
                return rpmh_levels_kalama.level_str;
            else if (ChipInfo.which == type.sun)
                return rpmh_levels_sun.level_str;
            else if (ChipInfo.which == type.canoe)
                return rpmh_levels_canoe.level_str;
            else if (ChipInfo.which == type.tuna)
                return rpmh_levels_tuna.level_str;

            return new String[]{};
        }
    }

    private static class rpmh_levels_kona {
        public static final int[] levels;
        public static final String[] level_str;

        static {
            levels = new int[416]; // 416 elements
            level_str = new String[levels.length];

            for (int i = 0; i < levels.length; i++) {
                levels[i] = i + 1; // Start from 1
                level_str[i] = String.valueOf(levels[i]);
                switch (i) {
                    case 15:
                        level_str[i] = "16 - RETENTION";
                        break;
                    case 47:
                        level_str[i] = "48 - MIN_SVS";
                        break;
                    case 55:
                        level_str[i] = "56 - LOW_SVS_D1";
                        break;
                    case 63:
                        level_str[i] = "64 - LOW_SVS";
                        break;
                    case 79:
                        level_str[i] = "80 - LOW_SVS_L1";
                        break;
                    case 95:
                        level_str[i] = "96 - LOW_SVS_L2";
                        break;
                    case 127:
                        level_str[i] = "128 - SVS";
                        break;
                    case 143:
                        level_str[i] = "144 - SVS_L0";
                        break;
                    case 191:
                        level_str[i] = "192 - SVS_L1";
                        break;
                    case 223:
                        level_str[i] = "224 - SVS_L2";
                        break;
                    case 255:
                        level_str[i] = "256 - NOM";
                        break;
                    case 319:
                        level_str[i] = "320 - NOM_L1";
                        break;
                    case 335:
                        level_str[i] = "336 - NOM_L2";
                        break;
                    case 351:
                        level_str[i] = "352 - NOM_L3";
                        break;
                    case 383:
                        level_str[i] = "384 - TURBO";
                        break;
                    case 399:
                        level_str[i] = "400 - TURBO_L0";
                        break;
                    case 415:
                        level_str[i] = "416 - TURBO_L1";
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static class rpmh_levels_msmnile {
        private static final int RPMH_REGULATOR_LEVEL_OFFSET = 1;
        public static final int[] levels;
        public static final String[] level_str;

        static {
            levels = new int[416];
            level_str = new String[levels.length];

            for (int i = 0; i < levels.length; i++) {
                levels[i] = i + RPMH_REGULATOR_LEVEL_OFFSET;
                level_str[i] = String.valueOf(levels[i]);
            }
        }
    }

    private static class rpmh_levels_lahaina {
        public static final int[] levels;
        public static final String[] level_str;

        static {
            levels = new int[464]; // 464 elements
            level_str = new String[levels.length];

            for (int i = 0; i < levels.length; i++) {
                levels[i] = i + 1; // Start from 1
                level_str[i] = String.valueOf(levels[i]);
                switch (i) {
                    case 15:
                        level_str[i] = "16 - RETENTION";
                        break;
                    case 47:
                        level_str[i] = "48 - MIN_SVS";
                        break;
                    case 55:
                        level_str[i] = "56 - LOW_SVS_D1";
                        break;
                    case 63:
                        level_str[i] = "64 - LOW_SVS";
                        break;
                    case 79:
                        level_str[i] = "80 - LOW_SVS_L1";
                        break;
                    case 95:
                        level_str[i] = "96 - LOW_SVS_L2";
                        break;
                    case 127:
                        level_str[i] = "128 - SVS";
                        break;
                    case 143:
                        level_str[i] = "144 - SVS_L0";
                        break;
                    case 191:
                        level_str[i] = "192 - SVS_L1";
                        break;
                    case 223:
                        level_str[i] = "224 - SVS_L2";
                        break;
                    case 255:
                        level_str[i] = "256 - NOM";
                        break;
                    case 319:
                        level_str[i] = "320 - NOM_L1";
                        break;
                    case 335:
                        level_str[i] = "336 - NOM_L2";
                        break;
                    case 351:
                        level_str[i] = "352 - NOM_L3";
                        break;
                    case 383:
                        level_str[i] = "384 - TURBO";
                        break;
                    case 399:
                        level_str[i] = "400 - TURBO_L0";
                        break;
                    case 415:
                        level_str[i] = "416 - TURBO_L1";
                        break;
                    case 431:
                        level_str[i] = "432 - TURBO_L2";
                        break;
                    case 447:
                        level_str[i] = "448 - SUPER_TURBO";
                        break;
                    case 463:
                        level_str[i] = "464 - SUPER_TURBO_NO_CPR";
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static class rpmh_levels_lahaina_singleBin {
        public static final int[] levels;
        public static final String[] level_str;

        static {
            levels = new int[416]; // 416 elements
            level_str = new String[levels.length];

            for (int i = 0; i < levels.length; i++) {
                levels[i] = i + 1; // Start from 1
                level_str[i] = String.valueOf(levels[i]);
                switch (i) {
                    case 15:
                        level_str[i] = "16 - RETENTION";
                        break;
                    case 47:
                        level_str[i] = "48 - MIN_SVS";
                        break;
                    case 55:
                        level_str[i] = "56 - LOW_SVS_D1";
                        break;
                    case 63:
                        level_str[i] = "64 - LOW_SVS";
                        break;
                    case 79:
                        level_str[i] = "80 - LOW_SVS_L1";
                        break;
                    case 95:
                        level_str[i] = "96 - LOW_SVS_L2";
                        break;
                    case 127:
                        level_str[i] = "128 - SVS";
                        break;
                    case 143:
                        level_str[i] = "144 - SVS_L0";
                        break;
                    case 191:
                        level_str[i] = "192 - SVS_L1";
                        break;
                    case 223:
                        level_str[i] = "224 - SVS_L2";
                        break;
                    case 255:
                        level_str[i] = "256 - NOM";
                        break;
                    case 319:
                        level_str[i] = "320 - NOM_L1";
                        break;
                    case 335:
                        level_str[i] = "336 - NOM_L2";
                        break;
                    case 351:
                        level_str[i] = "352 - NOM_L3";
                        break;
                    case 383:
                        level_str[i] = "384 - TURBO";
                        break;
                    case 399:
                        level_str[i] = "400 - TURBO_L0";
                        break;
                    case 415:
                        level_str[i] = "416 - TURBO_L1";
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static class rpmh_levels_lito {
        public static final int[] levels;
        public static final String[] level_str;

        static {
            levels = new int[416]; // 416 elements
            level_str = new String[levels.length];

            for (int i = 0; i < levels.length; i++) {
                levels[i] = i + 1; // Start from 1
                level_str[i] = String.valueOf(levels[i]);
                switch (i) {
                    case 15:
                        level_str[i] = "16 - RETENTION";
                        break;
                    case 47:
                        level_str[i] = "48 - MIN_SVS";
                        break;
                    case 55:
                        level_str[i] = "56 - LOW_SVS_D1";
                        break;
                    case 63:
                        level_str[i] = "64 - LOW_SVS";
                        break;
                    case 79:
                        level_str[i] = "80 - LOW_SVS_L1";
                        break;
                    case 95:
                        level_str[i] = "96 - LOW_SVS_L2";
                        break;
                    case 127:
                        level_str[i] = "128 - SVS";
                        break;
                    case 143:
                        level_str[i] = "144 - SVS_L0";
                        break;
                    case 191:
                        level_str[i] = "192 - SVS_L1";
                        break;
                    case 223:
                        level_str[i] = "224 - SVS_L2";
                        break;
                    case 255:
                        level_str[i] = "256 - NOM";
                        break;
                    case 319:
                        level_str[i] = "320 - NOM_L1";
                        break;
                    case 335:
                        level_str[i] = "336 - NOM_L2";
                        break;
                    case 351:
                        level_str[i] = "352 - NOM_L3";
                        break;
                    case 383:
                        level_str[i] = "384 - TURBO";
                        break;
                    case 399:
                        level_str[i] = "400 - TURBO_L0";
                        break;
                    case 415:
                        level_str[i] = "416 - TURBO_L1";
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static class rpmh_levels_lagoon {
        public static final int[] levels;
        public static final String[] level_str;

        static {
            levels = new int[416]; // 416 elements
            level_str = new String[levels.length];

            for (int i = 0; i < levels.length; i++) {
                levels[i] = i + 1; // Start from 1
                level_str[i] = String.valueOf(levels[i]);
                switch (i) {
                    case 15:
                        level_str[i] = "16 - RETENTION";
                        break;
                    case 47:
                        level_str[i] = "48 - MIN_SVS";
                        break;
                    case 55:
                        level_str[i] = "56 - LOW_SVS_D1";
                        break;
                    case 63:
                        level_str[i] = "64 - LOW_SVS";
                        break;
                    case 79:
                        level_str[i] = "80 - LOW_SVS_L1";
                        break;
                    case 95:
                        level_str[i] = "96 - LOW_SVS_L2";
                        break;
                    case 127:
                        level_str[i] = "128 - SVS";
                        break;
                    case 143:
                        level_str[i] = "144 - SVS_L0";
                        break;
                    case 191:
                        level_str[i] = "192 - SVS_L1";
                        break;
                    case 223:
                        level_str[i] = "224 - SVS_L2";
                        break;
                    case 255:
                        level_str[i] = "256 - NOM";
                        break;
                    case 319:
                        level_str[i] = "320 - NOM_L1";
                        break;
                    case 335:
                        level_str[i] = "336 - NOM_L2";
                        break;
                    case 351:
                        level_str[i] = "352 - NOM_L3";
                        break;
                    case 383:
                        level_str[i] = "384 - TURBO";
                        break;
                    case 399:
                        level_str[i] = "400 - TURBO_L0";
                        break;
                    case 415:
                        level_str[i] = "416 - TURBO_L1";
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static class rpmh_levels_shima {
        public static final int[] levels;
        public static final String[] level_str;

        static {
            levels = new int[416]; // 416 elements
            level_str = new String[levels.length];

            for (int i = 0; i < levels.length; i++) {
                levels[i] = i + 1; // Start from 1
                level_str[i] = String.valueOf(levels[i]);
                switch (i) {
                    case 15:
                        level_str[i] = "16 - RETENTION";
                        break;
                    case 47:
                        level_str[i] = "48 - MIN_SVS";
                        break;
                    case 55:
                        level_str[i] = "56 - LOW_SVS_D1";
                        break;
                    case 63:
                        level_str[i] = "64 - LOW_SVS";
                        break;
                    case 79:
                        level_str[i] = "80 - LOW_SVS_L1";
                        break;
                    case 95:
                        level_str[i] = "96 - LOW_SVS_L2";
                        break;
                    case 127:
                        level_str[i] = "128 - SVS";
                        break;
                    case 143:
                        level_str[i] = "144 - SVS_L0";
                        break;
                    case 191:
                        level_str[i] = "192 - SVS_L1";
                        break;
                    case 223:
                        level_str[i] = "224 - SVS_L2";
                        break;
                    case 255:
                        level_str[i] = "256 - NOM";
                        break;
                    case 319:
                        level_str[i] = "320 - NOM_L1";
                        break;
                    case 335:
                        level_str[i] = "336 - NOM_L2";
                        break;
                    case 351:
                        level_str[i] = "352 - NOM_L3";
                        break;
                    case 383:
                        level_str[i] = "384 - TURBO";
                        break;
                    case 399:
                        level_str[i] = "400 - TURBO_L0";
                        break;
                    case 415:
                        level_str[i] = "416 - TURBO_L1";
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static class rpmh_levels_yupik {
        public static final int[] levels;
        public static final String[] level_str;

        static {
            levels = new int[416]; // 416 elements
            level_str = new String[levels.length];

            for (int i = 0; i < levels.length; i++) {
                levels[i] = i + 1; // Start from 1
                level_str[i] = String.valueOf(levels[i]);
                switch (i) {
                    case 15:
                        level_str[i] = "16 - RETENTION";
                        break;
                    case 47:
                        level_str[i] = "48 - MIN_SVS";
                        break;
                    case 55:
                        level_str[i] = "56 - LOW_SVS_D1";
                        break;
                    case 63:
                        level_str[i] = "64 - LOW_SVS";
                        break;
                    case 79:
                        level_str[i] = "80 - LOW_SVS_L1";
                        break;
                    case 95:
                        level_str[i] = "96 - LOW_SVS_L2";
                        break;
                    case 127:
                        level_str[i] = "128 - SVS";
                        break;
                    case 143:
                        level_str[i] = "144 - SVS_L0";
                        break;
                    case 191:
                        level_str[i] = "192 - SVS_L1";
                        break;
                    case 223:
                        level_str[i] = "224 - SVS_L2";
                        break;
                    case 255:
                        level_str[i] = "256 - NOM";
                        break;
                    case 319:
                        level_str[i] = "320 - NOM_L1";
                        break;
                    case 335:
                        level_str[i] = "336 - NOM_L2";
                        break;
                    case 351:
                        level_str[i] = "352 - NOM_L3";
                        break;
                    case 383:
                        level_str[i] = "384 - TURBO";
                        break;
                    case 399:
                        level_str[i] = "400 - TURBO_L0";
                        break;
                    case 415:
                        level_str[i] = "416 - TURBO_L1";
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static class rpmh_levels_waipio {
        public static final int[] levels;
        public static final String[] level_str;

        static {
            levels = new int[416]; // 416 elements
            level_str = new String[levels.length];

            for (int i = 0; i < levels.length; i++) {
                levels[i] = i + 1; // Start from 1
                level_str[i] = String.valueOf(levels[i]);
                switch (i) {
                    case 15:
                        level_str[i] = "16 - RETENTION";
                        break;
                    case 47:
                        level_str[i] = "48 - MIN_SVS";
                        break;
                    case 55:
                        level_str[i] = "56 - LOW_SVS_D1";
                        break;
                    case 63:
                        level_str[i] = "64 - LOW_SVS";
                        break;
                    case 79:
                        level_str[i] = "80 - LOW_SVS_L1";
                        break;
                    case 95:
                        level_str[i] = "96 - LOW_SVS_L2";
                        break;
                    case 127:
                        level_str[i] = "128 - SVS";
                        break;
                    case 143:
                        level_str[i] = "144 - SVS_L0";
                        break;
                    case 191:
                        level_str[i] = "192 - SVS_L1";
                        break;
                    case 223:
                        level_str[i] = "224 - SVS_L2";
                        break;
                    case 255:
                        level_str[i] = "256 - NOM";
                        break;
                    case 319:
                        level_str[i] = "320 - NOM_L1";
                        break;
                    case 335:
                        level_str[i] = "336 - NOM_L2";
                        break;
                    case 351:
                        level_str[i] = "352 - NOM_L3";
                        break;
                    case 383:
                        level_str[i] = "384 - TURBO";
                        break;
                    case 399:
                        level_str[i] = "400 - TURBO_L0";
                        break;
                    case 415:
                        level_str[i] = "416 - TURBO_L1";
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static class rpmh_levels_cape {
        public static final int[] levels;
        public static final String[] level_str;

        static {
            levels = new int[416]; // 416 elements
            level_str = new String[levels.length];

            for (int i = 0; i < levels.length; i++) {
                levels[i] = i + 1; // Start from 1
                level_str[i] = String.valueOf(levels[i]);
                switch (i) {
                    case 15:
                        level_str[i] = "16 - RETENTION";
                        break;
                    case 47:
                        level_str[i] = "48 - MIN_SVS";
                        break;
                    case 55:
                        level_str[i] = "56 - LOW_SVS_D1";
                        break;
                    case 63:
                        level_str[i] = "64 - LOW_SVS";
                        break;
                    case 79:
                        level_str[i] = "80 - LOW_SVS_L1";
                        break;
                    case 95:
                        level_str[i] = "96 - LOW_SVS_L2";
                        break;
                    case 127:
                        level_str[i] = "128 - SVS";
                        break;
                    case 143:
                        level_str[i] = "144 - SVS_L0";
                        break;
                    case 191:
                        level_str[i] = "192 - SVS_L1";
                        break;
                    case 223:
                        level_str[i] = "224 - SVS_L2";
                        break;
                    case 255:
                        level_str[i] = "256 - NOM";
                        break;
                    case 319:
                        level_str[i] = "320 - NOM_L1";
                        break;
                    case 335:
                        level_str[i] = "336 - NOM_L2";
                        break;
                    case 351:
                        level_str[i] = "352 - NOM_L3";
                        break;
                    case 383:
                        level_str[i] = "384 - TURBO";
                        break;
                    case 399:
                        level_str[i] = "400 - TURBO_L0";
                        break;
                    case 415:
                        level_str[i] = "416 - TURBO_L1";
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static class rpmh_levels_diwali {
        public static final int[] levels;
        public static final String[] level_str;

        static {
            levels = new int[416]; // 416 elements
            level_str = new String[levels.length];

            for (int i = 0; i < levels.length; i++) {
                levels[i] = i + 1; // Start from 1
                level_str[i] = String.valueOf(levels[i]);
                switch (i) {
                    case 15:
                        level_str[i] = "16 - RETENTION";
                        break;
                    case 47:
                        level_str[i] = "48 - MIN_SVS";
                        break;
                    case 55:
                        level_str[i] = "56 - LOW_SVS_D1";
                        break;
                    case 63:
                        level_str[i] = "64 - LOW_SVS";
                        break;
                    case 79:
                        level_str[i] = "80 - LOW_SVS_L1";
                        break;
                    case 95:
                        level_str[i] = "96 - LOW_SVS_L2";
                        break;
                    case 127:
                        level_str[i] = "128 - SVS";
                        break;
                    case 143:
                        level_str[i] = "144 - SVS_L0";
                        break;
                    case 191:
                        level_str[i] = "192 - SVS_L1";
                        break;
                    case 223:
                        level_str[i] = "224 - SVS_L2";
                        break;
                    case 255:
                        level_str[i] = "256 - NOM";
                        break;
                    case 319:
                        level_str[i] = "320 - NOM_L1";
                        break;
                    case 335:
                        level_str[i] = "336 - NOM_L2";
                        break;
                    case 351:
                        level_str[i] = "352 - NOM_L3";
                        break;
                    case 383:
                        level_str[i] = "384 - TURBO";
                        break;
                    case 399:
                        level_str[i] = "400 - TURBO_L0";
                        break;
                    case 415:
                        level_str[i] = "416 - TURBO_L1";
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static class rpmh_levels_ukee {
        public static final int[] levels;
        public static final String[] level_str;

        static {
            levels = new int[416]; // 416 elements
            level_str = new String[levels.length];

            for (int i = 0; i < levels.length; i++) {
                levels[i] = i + 1; // Start from 1
                level_str[i] = String.valueOf(levels[i]);
                switch (i) {
                    case 15:
                        level_str[i] = "16 - RETENTION";
                        break;
                    case 47:
                        level_str[i] = "48 - MIN_SVS";
                        break;
                    case 55:
                        level_str[i] = "56 - LOW_SVS_D1";
                        break;
                    case 63:
                        level_str[i] = "64 - LOW_SVS";
                        break;
                    case 79:
                        level_str[i] = "80 - LOW_SVS_L1";
                        break;
                    case 95:
                        level_str[i] = "96 - LOW_SVS_L2";
                        break;
                    case 127:
                        level_str[i] = "128 - SVS";
                        break;
                    case 143:
                        level_str[i] = "144 - SVS_L0";
                        break;
                    case 191:
                        level_str[i] = "192 - SVS_L1";
                        break;
                    case 223:
                        level_str[i] = "224 - SVS_L2";
                        break;
                    case 255:
                        level_str[i] = "256 - NOM";
                        break;
                    case 319:
                        level_str[i] = "320 - NOM_L1";
                        break;
                    case 335:
                        level_str[i] = "336 - NOM_L2";
                        break;
                    case 351:
                        level_str[i] = "352 - NOM_L3";
                        break;
                    case 383:
                        level_str[i] = "384 - TURBO";
                        break;
                    case 399:
                        level_str[i] = "400 - TURBO_L0";
                        break;
                    case 415:
                        level_str[i] = "416 - TURBO_L1";
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static class rpmh_levels_kalama {
        public static final int[] levels;
        public static final String[] level_str;

        static {
            levels = new int[480]; // 480 elements
            level_str = new String[levels.length];

            for (int i = 0; i < levels.length; i++) {
                levels[i] = i + 1; // Start from 1
                level_str[i] = String.valueOf(levels[i]);
                switch (i) {
                    case 15:
                        level_str[i] = "16 - RETENTION";
                        break;
                    case 47:
                        level_str[i] = "48 - MIN_SVS";
                        break;
                    case 51:
                        level_str[i] = "52 - LOW_SVS_D2";
                        break;
                    case 55:
                        level_str[i] = "56 - LOW_SVS_D1";
                        break;
                    case 59:
                        level_str[i] = "60 - LOW_SVS_D0";
                        break;
                    case 63:
                        level_str[i] = "64 - LOW_SVS";
                        break;
                    case 71:
                        level_str[i] = "72 - LOW_SVS_P1";
                        break;
                    case 79:
                        level_str[i] = "80 - LOW_SVS_L1";
                        break;
                    case 95:
                        level_str[i] = "96 - LOW_SVS_L2";
                        break;
                    case 127:
                        level_str[i] = "128 - SVS";
                        break;
                    case 143:
                        level_str[i] = "144 - SVS_L0";
                        break;
                    case 191:
                        level_str[i] = "192 - SVS_L1";
                        break;
                    case 223:
                        level_str[i] = "224 - SVS_L2";
                        break;
                    case 255:
                        level_str[i] = "256 - NOM";
                        break;
                    case 287:
                        level_str[i] = "288 - NOM_L0";
                        break;
                    case 319:
                        level_str[i] = "320 - NOM_L1";
                        break;
                    case 335:
                        level_str[i] = "336 - NOM_L2";
                        break;
                    case 383:
                        level_str[i] = "384 - TURBO";
                        break;
                    case 399:
                        level_str[i] = "400 - TURBO_L0";
                        break;
                    case 415:
                        level_str[i] = "416 - TURBO_L1";
                        break;
                    case 431:
                        level_str[i] = "432 - TURBO_L2";
                        break;
                    case 447:
                        level_str[i] = "448 - TURBO_L3";
                        break;
                    case 463:
                        level_str[i] = "464 - SUPER_TURBO";
                        break;
                    case 479:
                        level_str[i] = "480 - SUPER_TURBO_NO_CPR";
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static class rpmh_levels_pineapple {
        public static final int[] levels;
        public static final String[] level_str;

        static {
            levels = new int[480]; // 480 elements
            level_str = new String[levels.length];

            for (int i = 0; i < levels.length; i++) {
                levels[i] = i + 1; // Start from 1
                level_str[i] = String.valueOf(levels[i]);
                switch (i) {
                    case 15:
                        level_str[i] = "16 - RETENTION";
                        break;
                    case 47:
                        level_str[i] = "48 - MIN_SVS";
                        break;
                    case 51:
                        level_str[i] = "52 - LOW_SVS_D2";
                        break;
                    case 55:
                        level_str[i] = "56 - LOW_SVS_D1";
                        break;
                    case 59:
                        level_str[i] = "60 - LOW_SVS_D0";
                        break;
                    case 63:
                        level_str[i] = "64 - LOW_SVS";
                        break;
                    case 71:
                        level_str[i] = "72 - LOW_SVS_P1";
                        break;
                    case 79:
                        level_str[i] = "80 - LOW_SVS_L1";
                        break;
                    case 95:
                        level_str[i] = "96 - LOW_SVS_L2";
                        break;
                    case 127:
                        level_str[i] = "128 - SVS";
                        break;
                    case 143:
                        level_str[i] = "144 - SVS_L0";
                        break;
                    case 191:
                        level_str[i] = "192 - SVS_L1";
                        break;
                    case 223:
                        level_str[i] = "224 - SVS_L2";
                        break;
                    case 255:
                        level_str[i] = "256 - NOM";
                        break;
                    case 287:
                        level_str[i] = "288 - NOM_L0";
                        break;
                    case 319:
                        level_str[i] = "320 - NOM_L1";
                        break;
                    case 335:
                        level_str[i] = "336 - NOM_L2";
                        break;
                    case 383:
                        level_str[i] = "384 - TURBO";
                        break;
                    case 399:
                        level_str[i] = "400 - TURBO_L0";
                        break;
                    case 415:
                        level_str[i] = "416 - TURBO_L1";
                        break;
                    case 431:
                        level_str[i] = "432 - TURBO_L2";
                        break;
                    case 447:
                        level_str[i] = "448 - TURBO_L3";
                        break;
                    case 463:
                        level_str[i] = "464 - SUPER_TURBO";
                        break;
                    case 479:
                        level_str[i] = "480 - SUPER_TURBO_NO_CPR";
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static class rpmh_levels_cliffs {
        public static final int[] levels;
        public static final String[] level_str;

        static {
            levels = new int[480]; // 480 elements
            level_str = new String[levels.length];

            for (int i = 0; i < levels.length; i++) {
                levels[i] = i + 1; // Start from 1
                level_str[i] = String.valueOf(levels[i]);
                switch (i) {
                    case 15:
                        level_str[i] = "16 - RETENTION";
                        break;
                    case 47:
                        level_str[i] = "48 - MIN_SVS";
                        break;
                    case 51:
                        level_str[i] = "52 - LOW_SVS_D2";
                        break;
                    case 55:
                        level_str[i] = "56 - LOW_SVS_D1";
                        break;
                    case 59:
                        level_str[i] = "60 - LOW_SVS_D0";
                        break;
                    case 63:
                        level_str[i] = "64 - LOW_SVS";
                        break;
                    case 71:
                        level_str[i] = "72 - LOW_SVS_P1";
                        break;
                    case 79:
                        level_str[i] = "80 - LOW_SVS_L1";
                        break;
                    case 95:
                        level_str[i] = "96 - LOW_SVS_L2";
                        break;
                    case 127:
                        level_str[i] = "128 - SVS";
                        break;
                    case 143:
                        level_str[i] = "144 - SVS_L0";
                        break;
                    case 191:
                        level_str[i] = "192 - SVS_L1";
                        break;
                    case 223:
                        level_str[i] = "224 - SVS_L2";
                        break;
                    case 255:
                        level_str[i] = "256 - NOM";
                        break;
                    case 287:
                        level_str[i] = "288 - NOM_L0";
                        break;
                    case 319:
                        level_str[i] = "320 - NOM_L1";
                        break;
                    case 335:
                        level_str[i] = "336 - NOM_L2";
                        break;
                    case 383:
                        level_str[i] = "384 - TURBO";
                        break;
                    case 399:
                        level_str[i] = "400 - TURBO_L0";
                        break;
                    case 415:
                        level_str[i] = "416 - TURBO_L1";
                        break;
                    case 431:
                        level_str[i] = "432 - TURBO_L2";
                        break;
                    case 447:
                        level_str[i] = "448 - TURBO_L3";
                        break;
                    case 463:
                        level_str[i] = "464 - SUPER_TURBO";
                        break;
                    case 479:
                        level_str[i] = "480 - SUPER_TURBO_NO_CPR";
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static class rpmh_levels_sun {
        public static final int[] levels;
        public static final String[] level_str;

        static {
            levels = new int[480]; // 480 elements
            level_str = new String[levels.length];

            for (int i = 0; i < levels.length; i++) {
                levels[i] = i + 1; // Start from 1
                level_str[i] = String.valueOf(levels[i]);
                switch (i) {
                    case 15:
                        level_str[i] = "16 - RETENTION";
                        break;
                    case 47:
                        level_str[i] = "48 - MIN_SVS";
                        break;
                    case 49:
                        level_str[i] = "50 - LOW_SVS_D3";
                        break;
                    case 51:
                        level_str[i] = "52 - LOW_SVS_D2";
                        break;
                    case 55:
                        level_str[i] = "56 - LOW_SVS_D1";
                        break;
                    case 59:
                        level_str[i] = "60 - LOW_SVS_D0";
                        break;
                    case 63:
                        level_str[i] = "64 - LOW_SVS";
                        break;
                    case 71:
                        level_str[i] = "72 - LOW_SVS_P1";
                        break;
                    case 79:
                        level_str[i] = "80 - LOW_SVS_L1";
                        break;
                    case 95:
                        level_str[i] = "96 - LOW_SVS_L2";
                        break;
                    case 127:
                        level_str[i] = "128 - SVS";
                        break;
                    case 143:
                        level_str[i] = "144 - SVS_L0";
                        break;
                    case 191:
                        level_str[i] = "192 - SVS_L1";
                        break;
                    case 223:
                        level_str[i] = "224 - SVS_L2";
                        break;
                    case 255:
                        level_str[i] = "256 - NOM";
                        break;
                    case 287:
                        level_str[i] = "288 - NOM_L0";
                        break;
                    case 319:
                        level_str[i] = "320 - NOM_L1";
                        break;
                    case 335:
                        level_str[i] = "336 - NOM_L2";
                        break;
                    case 383:
                        level_str[i] = "384 - TURBO";
                        break;
                    case 399:
                        level_str[i] = "400 - TURBO_L0";
                        break;
                    case 415:
                        level_str[i] = "416 - TURBO_L1";
                        break;
                    case 431:
                        level_str[i] = "432 - TURBO_L2";
                        break;
                    case 447:
                        level_str[i] = "448 - TURBO_L3";
                        break;
                    case 451:
                        level_str[i] = "452 - TURBO_L4";
                        break;
                    case 463:
                        level_str[i] = "464 - SUPER_TURBO";
                        break;
                    case 479:
                        level_str[i] = "480 - SUPER_TURBO_NO_CPR";
                        break;
                    default:
                        break;
                }
            }
        }
    }


    private static class rpmh_levels_canoe {
        public static final int[] levels;
        public static final String[] level_str;

        static {
            levels = new int[480]; // 480 elements
            level_str = new String[levels.length];

            for (int i = 0; i < levels.length; i++) {
                levels[i] = i + 1; // Start from 1
                level_str[i] = String.valueOf(levels[i]);
                switch (i) {
                    case 15:
                        level_str[i] = "16 - RETENTION";
                        break;
                    case 47:
                        level_str[i] = "48 - MIN_SVS";
                        break;
                    case 49:
                        level_str[i] = "50 - LOW_SVS_D3";
                        break;
                    case 50:
                        level_str[i] = "51 - LOW_SVS_D2_5"; // todo: Synchronize the actual name from regulator headers
                        break;
                    case 51:
                        level_str[i] = "52 - LOW_SVS_D2";
                        break;
                    case 53:
                        level_str[i] = "54 - LOW_SVS_D1_5"; // todo: Synchronize the actual name from regulator headers
                        break;
                    case 55:
                        level_str[i] = "56 - LOW_SVS_D1";
                        break;
                    case 59:
                        level_str[i] = "60 - LOW_SVS_D0";
                        break;
                    case 63:
                        level_str[i] = "64 - LOW_SVS";
                        break;
                    case 71:
                        level_str[i] = "72 - LOW_SVS_P1";
                        break;
                    case 79:
                        level_str[i] = "80 - LOW_SVS_L1";
                        break;
                    case 95:
                        level_str[i] = "96 - LOW_SVS_L2";
                        break;
                    case 127:
                        level_str[i] = "128 - SVS";
                        break;
                    case 143:
                        level_str[i] = "144 - SVS_L0";
                        break;
                    case 191:
                        level_str[i] = "192 - SVS_L1";
                        break;
                    case 223:
                        level_str[i] = "224 - SVS_L2";
                        break;
                    case 255:
                        level_str[i] = "256 - NOM";
                        break;
                    case 287:
                        level_str[i] = "288 - NOM_L0";
                        break;
                    case 319:
                        level_str[i] = "320 - NOM_L1";
                        break;
                    case 335:
                        level_str[i] = "336 - NOM_L2";
                        break;
                    case 383:
                        level_str[i] = "384 - TURBO";
                        break;
                    case 399:
                        level_str[i] = "400 - TURBO_L0";
                        break;
                    case 415:
                        level_str[i] = "416 - TURBO_L1";
                        break;
                    case 431:
                        level_str[i] = "432 - TURBO_L2";
                        break;
                    case 447:
                        level_str[i] = "448 - TURBO_L3";
                        break;
                    case 451:
                        level_str[i] = "452 - TURBO_L4";
                        break;
                    case 463:
                        level_str[i] = "464 - SUPER_TURBO";
                        break;
                    case 479:
                        level_str[i] = "480 - SUPER_TURBO_NO_CPR";
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static class rpmh_levels_tuna {
        public static final int[] levels;
        public static final String[] level_str;

        static {
            levels = new int[480]; // 480 elements
            level_str = new String[levels.length];

            for (int i = 0; i < levels.length; i++) {
                levels[i] = i + 1; // Start from 1
                level_str[i] = String.valueOf(levels[i]);
                switch (i) {
                    case 15:
                        level_str[i] = "16 - RETENTION";
                        break;
                    case 47:
                        level_str[i] = "48 - MIN_SVS";
                        break;
                    case 49:
                        level_str[i] = "50 - LOW_SVS_D3";
                        break;
                    case 50:
                        level_str[i] = "51 - LOW_SVS_D2_5"; // todo: Synchronize the actual name from regulator headers
                        break;
                    case 51:
                        level_str[i] = "52 - LOW_SVS_D2";
                        break;
                    case 53:
                        level_str[i] = "54 - LOW_SVS_D1_5"; // todo: Synchronize the actual name from regulator headers
                        break;
                    case 55:
                        level_str[i] = "56 - LOW_SVS_D1";
                        break;
                    case 59:
                        level_str[i] = "60 - LOW_SVS_D0";
                        break;
                    case 63:
                        level_str[i] = "64 - LOW_SVS";
                        break;
                    case 71:
                        level_str[i] = "72 - LOW_SVS_P1";
                        break;
                    case 79:
                        level_str[i] = "80 - LOW_SVS_L1";
                        break;
                    case 95:
                        level_str[i] = "96 - LOW_SVS_L2";
                        break;
                    case 127:
                        level_str[i] = "128 - SVS";
                        break;
                    case 143:
                        level_str[i] = "144 - SVS_L0";
                        break;
                    case 191:
                        level_str[i] = "192 - SVS_L1";
                        break;
                    case 223:
                        level_str[i] = "224 - SVS_L2";
                        break;
                    case 255:
                        level_str[i] = "256 - NOM";
                        break;
                    case 287:
                        level_str[i] = "288 - NOM_L0";
                        break;
                    case 319:
                        level_str[i] = "320 - NOM_L1";
                        break;
                    case 335:
                        level_str[i] = "336 - NOM_L2";
                        break;
                    case 383:
                        level_str[i] = "384 - TURBO";
                        break;
                    case 399:
                        level_str[i] = "400 - TURBO_L0";
                        break;
                    case 415:
                        level_str[i] = "416 - TURBO_L1";
                        break;
                    case 431:
                        level_str[i] = "432 - TURBO_L2";
                        break;
                    case 447:
                        level_str[i] = "448 - TURBO_L3";
                        break;
                    case 451:
                        level_str[i] = "452 - TURBO_L4";
                        break;
                    case 463:
                        level_str[i] = "464 - SUPER_TURBO";
                        break;
                    case 479:
                        level_str[i] = "480 - SUPER_TURBO_NO_CPR";
                        break;
                    default:
                        break;
                }
            }
        }
    }
}