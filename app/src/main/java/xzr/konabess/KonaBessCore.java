package xzr.konabess;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.SystemProperties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import xzr.konabess.utils.AssetsUtil;
import xzr.konabess.utils.RootHelper;

public class KonaBessCore {
    public static String dts_path;
    private static int dtb_num;
    public static String boot_name;
    private static boolean prepared = false;

    private enum dtb_types {
        dtb,
        kernel_dtb,
        both
    }

    private static dtb_types dtb_type;

    public static void cleanEnv(Context context) throws IOException {
        prepared = false;
        dts_path = null;
        dtbs = null;
        boot_name = null;
        if (!RootHelper.execShForOutput("rm -rf " + context.getFilesDir().getAbsolutePath() + "/*").isEmpty()) {
            // Command executed successfully (output doesn't matter)
        }
    }

    private static String[] fileList = {"dtc", "magiskboot"};

    public static void setupEnv(Context context) throws IOException {
        for (String s : fileList) {
            AssetsUtil.exportFiles(context, s, context.getFilesDir().getAbsolutePath() + "/" + s);
            File file = new File(context.getFilesDir().getAbsolutePath() + "/" + s);
            file.setExecutable(true);
            if (!file.canExecute())
                throw new IOException();
        }
    }

    public static void reboot() throws IOException {
        if (!RootHelper.execAndCheck("svc power reboot")) {
            throw new IOException("Failed to reboot");
        }
    }

    static String getCurrent(String name) {
        switch (name.toLowerCase()) {
            case "brand":
                return SystemProperties.get("ro.product.brand", "");
            case "name":
                return SystemProperties.get("ro.product.name", "");
            case "model":
                return SystemProperties.get("ro.product.model", "");
            case "board":
                return SystemProperties.get("ro.product.board", "");
            case "id":
                return SystemProperties.get("ro.product.build.id", "");
            case "version":
                return SystemProperties.get("ro.product.build.version.release", "");
            case "fingerprint":
                return SystemProperties.get("ro.product.build.fingerprint", "");
            case "manufacturer":
                return SystemProperties.get("ro.product.manufacturer", "");
            case "device":
                return SystemProperties.get("ro.product.device", "");
            case "slot":
                return SystemProperties.get("ro.boot.slot_suffix", "");
            default:
                return null;
        }
    }

    public static void getBootImage(Context context) throws IOException {
        try {
            getVendorBootImage(context);
            boot_name = "vendor_boot";
        } catch (Exception e) {
            getRealBootImage(context);
            boot_name = "boot";
        }
    }

    private static void getRealBootImage(Context context) throws IOException {
        String bootImgPath = context.getFilesDir().getAbsolutePath() + "/boot.img";
        if (!RootHelper.execAndCheck(
                "dd if=/dev/block/bootdevice/by-name/boot" + getCurrent("slot") + " of=" + bootImgPath,
                "chmod 644 " + bootImgPath)) {
            throw new IOException("Failed to get boot image");
        }

        File target = new File(bootImgPath);
        if (!target.exists() || !target.canRead()) {
            target.delete();
            throw new IOException();
        }
    }

    private static void getVendorBootImage(Context context) throws IOException {
        String bootImgPath = context.getFilesDir().getAbsolutePath() + "/boot.img";
        if (!RootHelper.execAndCheck(
                "dd if=/dev/block/bootdevice/by-name/vendor_boot" + getCurrent("slot") + " of=" + bootImgPath,
                "chmod 644 " + bootImgPath)) {
            throw new IOException("Failed to get vendor boot image");
        }

        File target = new File(bootImgPath);
        if (!target.exists() || !target.canRead()) {
            target.delete();
            throw new IOException();
        }
    }

    public static void writeBootImage(Context context) throws IOException {
        if (!RootHelper.execAndCheck(
                "dd if=" + context.getFilesDir().getAbsolutePath() + "/boot_new.img of=/dev/block/bootdevice/by-name/" + boot_name + getCurrent("slot"))) {
            throw new IOException("Failed to write boot image");
        }
    }

    public static void backupBootImage(Context context) throws IOException {
        if (!RootHelper.execShForOutput("cp -f " + context.getFilesDir().getAbsolutePath() + "/boot.img " +
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + boot_name + ".img").isEmpty()) {
            // Backup executed successfully
        }
    }

    static class dtb {
        int id;
        ChipInfo.type type;
    }

    public static ArrayList<dtb> dtbs;
    private static dtb currentDtb;

    public static dtb getCurrentDtb() {
        return currentDtb;
    }

    public static void checkDevice(Context context) throws IOException {
        dtbs = new ArrayList<>();
        for (int i = 0; i < dtb_num; i++) {
            if (checkChip(context, i, "kona v2.1")
                    || KonaBessCore.getCurrent("device").equals("OP4A79") && checkChip(context, i
                    , "kona v2")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = checkSingleBin(context, i) ? ChipInfo.type.kona_singleBin :
                        ChipInfo.type.kona;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "SM8150 v2")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = checkSingleBin(context, i) ? ChipInfo.type.msmnile_singleBin :
                        ChipInfo.type.msmnile;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Lahaina V2.1")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = checkSingleBin(context, i) ? ChipInfo.type.lahaina_singleBin :
                        ChipInfo.type.lahaina;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Lahaina v2.1")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = checkSingleBin(context, i) ? ChipInfo.type.lahaina_singleBin :
                        ChipInfo.type.lahaina;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Lito")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.lito_v1;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Lito v2")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.lito_v2;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Lagoon")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.lagoon;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Shima")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.shima;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Yupik")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.yupik;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Waipio")
                    || checkChip(context, i, "Waipio v2")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.waipio_singleBin;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Cape")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.cape_singleBin;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Kalama v2")
                    || checkChip(context, i, "KalamaP v2")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.kalama;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Diwali")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.diwali;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Ukee")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.ukee_singleBin;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Pineapple v2")
                    || checkChip(context, i, "PineappleP v2")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.pineapple;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Cliffs SoC")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.cliffs_singleBin;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Cliffs 7 SoC")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.cliffs_7_singleBin;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "KalamaP SG SoC")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.kalama_sg_singleBin;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Sun v2 SoC")
                    || checkChip(context, i, "Sun Alt. Thermal Profile v2 SoC")
                    || checkChip(context, i, "SunP v2 SoC")
                    || checkChip(context, i, "SunP v2 Alt. Thermal Profile SoC")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.sun;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Canoe v2 SoC")
                    || checkChip(context, i, "CanoeP v2 SoC")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.canoe;
                dtbs.add(dtb);
            } else if (checkChip(context, i, "Tuna 7 SoC") || checkChip(context, i, "Tuna SoC")) {
                dtb dtb = new dtb();
                dtb.id = i;
                dtb.type = ChipInfo.type.tuna;
                dtbs.add(dtb);
            }
        }
    }

    public static void chooseTarget(dtb dtb, Activity activity) {
        dts_path = activity.getFilesDir().getAbsolutePath() + "/" + dtb.id + ".dts";
        ChipInfo.which = dtb.type;
        currentDtb = dtb;
        prepared = true;
    }

    public static boolean isPrepared() {
        return prepared && dts_path != null && new File(dts_path).exists()
                && ChipInfo.which != ChipInfo.type.unknown;
    }

    private static boolean checkSingleBin(Context context, int index) throws IOException {
        List<String> output = RootHelper.execShForOutput(
                "cat " + context.getFilesDir().getAbsolutePath() + "/" + index + ".dts | grep 'qcom,gpu-pwrlevels {'");
        return !output.isEmpty();
    }

    private static boolean checkChip(Context context, int index, String chip) throws IOException {
        List<String> output = RootHelper.execShForOutput(
                "cat " + context.getFilesDir().getAbsolutePath() + "/" + index + ".dts | grep model | grep '" + chip + "'");
        return !output.isEmpty();
    }

    private static void unpackBootImage(Context context) throws IOException {
        List<String> output = RootHelper.execShForOutput(
                "cd " + context.getFilesDir().getAbsolutePath(),
                "./magiskboot unpack boot.img");
        
        File kdtb_file = new File(context.getFilesDir().getAbsolutePath() + "/kernel_dtb");
        File dtb_file = new File(context.getFilesDir().getAbsolutePath() + "/dtb");

        if (kdtb_file.exists() && dtb_file.exists()) {
            dtb_type = dtb_types.both;
            return;
        }

        if (kdtb_file.exists()) {
            dtb_type = dtb_types.kernel_dtb;
            return;
        }

        if (dtb_file.exists()) {
            dtb_type = dtb_types.dtb;
            return;
        }

        throw new IOException("Failed to unpack boot image");
    }

    private static void dtb2dts(Context context, int index) throws IOException {
        String filesDir = context.getFilesDir().getAbsolutePath();
        List<String> output = RootHelper.execShForOutput(
                "cd " + filesDir,
                "./dtc -I dtb -O dts " + index + ".dtb -o " + index + ".dts",
                "rm -f " + index + ".dtb");
        
        if (!new File(filesDir + "/" + index + ".dts").exists()) {
            StringBuilder log = new StringBuilder();
            for (String line : output) {
                log.append(line).append("\n");
            }
            throw new IOException(log.toString());
        }
    }

    public static void bootImage2dts(Context context) throws IOException {
        unpackBootImage(context);
        dtb_num = dtb_split(context);
        for (int i = 0; i < dtb_num; i++) {
            dtb2dts(context, i);
        }
    }

    private static int toUnsignedByte(byte in) {
        return (int) in & 0xFF;
    }

    public static int dtb_split(Context context) throws IOException {
        File dtb = null;
        if (dtb_type == dtb_types.dtb)
            dtb = new File(context.getFilesDir().getAbsolutePath() + "/dtb");
        else if (dtb_type == dtb_types.kernel_dtb)
            dtb = new File(context.getFilesDir().getAbsolutePath() + "/kernel_dtb");
        else if (dtb_type == dtb_types.both) {
            dtb = new File(context.getFilesDir().getAbsolutePath() + "/dtb");
            if (!new File(context.getFilesDir().getAbsolutePath() + "/kernel_dtb").delete())
                throw new IOException();
        } else {
            throw new IOException();
        }

        byte[] dtb_bytes = new byte[(int) dtb.length()];
        FileInputStream fileInputStream = new FileInputStream(dtb);
        if (fileInputStream.read(dtb_bytes) != dtb.length())
            throw new IOException();
        fileInputStream.close();

        int i = 0;
        ArrayList<Integer> cut = new ArrayList<>();
        while (i + 8 < dtb.length()) {
            if (dtb_bytes[i] == (byte) 0xD0 && dtb_bytes[i + 1] == (byte) 0x0D
                    && dtb_bytes[i + 2] == (byte) 0xFE && dtb_bytes[i + 3] == (byte) 0xED) {
                cut.add(i);
                int size = (int) (toUnsignedByte(dtb_bytes[i + 4]) * Math.pow(256, 3)
                        + toUnsignedByte(dtb_bytes[i + 5]) * Math.pow(256, 2)
                        + toUnsignedByte(dtb_bytes[i + 6]) * Math.pow(256, 1)
                        + toUnsignedByte(dtb_bytes[i + 7]));
                i += size > 0 ? size : 1;
                continue;
            }
            i++;
        }

        for (i = 0; i < cut.size(); i++) {
            File out = new File(context.getFilesDir().getAbsolutePath() + "/" + i + ".dtb");
            FileOutputStream fileOutputStream = new FileOutputStream(out);
            int end = (int) dtb.length();
            try {
                end = cut.get(i + 1);
            } catch (Exception ignored) {
            }

            fileOutputStream.write(dtb_bytes, cut.get(i), end - cut.get(i));
            fileInputStream.close();
        }

        if (!dtb.delete())
            throw new IOException();

        return cut.size();
    }

    private static void dts2dtb(Context context, int index) throws IOException {
        String filesDir = context.getFilesDir().getAbsolutePath();
        List<String> output = RootHelper.execShForOutput(
                "cd " + filesDir,
                "./dtc -I dts -O dtb " + index + ".dts -o " + index + ".dtb");
        
        if (!new File(filesDir + "/" + index + ".dtb").exists()) {
            StringBuilder log = new StringBuilder();
            for (String line : output) {
                log.append(line).append("\n");
            }
            throw new IOException(log.toString());
        }
    }


    private static void dtb2bootImage(Context context) throws IOException {
        String filesDir = context.getFilesDir().getAbsolutePath();
        List<String> commands = new ArrayList<>();
        commands.add("cd " + filesDir);
        if (dtb_type == dtb_types.both) {
            commands.add("cp dtb kernel_dtb");
        }
        commands.add("./magiskboot repack boot.img boot_new.img");
        
        List<String> output = RootHelper.execShForOutput(commands.toArray(new String[0]));
        
        if (!new File(filesDir + "/boot_new.img").exists()) {
            StringBuilder log = new StringBuilder();
            for (String line : output) {
                log.append(line).append("\n");
            }
            throw new IOException(log.toString());
        }
    }

    public static void linkDtbs(Context context) throws IOException {
        File out;

        if (dtb_type == dtb_types.dtb)
            out = new File(context.getFilesDir().getAbsolutePath() + "/dtb");
        else if (dtb_type == dtb_types.kernel_dtb)
            out = new File(context.getFilesDir().getAbsolutePath() + "/kernel_dtb");
        else if (dtb_type == dtb_types.both) {
            out = new File(context.getFilesDir().getAbsolutePath() + "/dtb");
        } else {
            throw new IOException();
        }

        FileOutputStream fileOutputStream = new FileOutputStream(out);
        for (int i = 0; i < dtb_num; i++) {
            File input = new File(context.getFilesDir().getAbsolutePath() + "/" + i + ".dtb");
            FileInputStream fileInputStream = new FileInputStream(input);
            byte[] b = new byte[(int) input.length()];
            if (fileInputStream.read(b) != input.length())
                throw new IOException();
            fileOutputStream.write(b);
            fileInputStream.close();
        }
        fileOutputStream.close();
    }

    public static void dts2bootImage(Context context) throws IOException {
        for (int i = 0; i < dtb_num; i++) {
            dts2dtb(context, i);
        }
        linkDtbs(context);
        dtb2bootImage(context);
    }

    public static int getDtbIndex() {
        int ret = -1;
        try {
            ret = Integer.parseInt(SystemProperties.get("ro.boot.dtb_idx", null));
        } catch (Exception ignored) {

        }
        return ret;
    }
}
