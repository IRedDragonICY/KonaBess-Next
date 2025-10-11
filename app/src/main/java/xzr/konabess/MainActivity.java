package xzr.konabess;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import xzr.konabess.adapters.ParamAdapter;
import xzr.konabess.adapters.ViewPagerAdapter;
import xzr.konabess.fragments.GpuFrequencyFragment;
import xzr.konabess.fragments.ImportExportFragment;
import xzr.konabess.fragments.SettingsFragment;
import xzr.konabess.utils.DialogUtil;
import xzr.konabess.utils.LocaleUtil;

public class MainActivity extends AppCompatActivity {
    private static final String KEY_LAST_GPU_TITLE = "key_last_gpu_toolbar_title";
    private static final String KEY_CURRENT_TITLE = "key_current_toolbar_title";

    AlertDialog waiting;
    boolean cross_device_debug = false;
    onBackPressedListener onBackPressedListener = null;
    
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNav;
    private MaterialToolbar toolbar;
    private boolean isPageChangeFromUser = true;
    private String currentTitle;
    private String lastGpuToolbarTitle;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleUtil.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply color palette theme BEFORE super.onCreate()
        applyColorPalette();
        super.onCreate(savedInstanceState);

        try {
            setTitle(getString(R.string.app_name) + " " +
                    getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        if (savedInstanceState != null) {
            lastGpuToolbarTitle = savedInstanceState.getString(KEY_LAST_GPU_TITLE);
            currentTitle = savedInstanceState.getString(KEY_CURRENT_TITLE);
        }

        if (KonaBessCore.isPrepared()) {
            showMainView();
            return;
        }

        ChipInfo.which = ChipInfo.type.unknown;

        try {
            if (!cross_device_debug)
                KonaBessCore.cleanEnv(this);
            KonaBessCore.setupEnv(this);
        } catch (Exception e) {
            DialogUtil.showError(this, R.string.environ_setup_failed);
            return;
        }

        new unpackLogic().start();
    }

    @Override
    public void onBackPressed() {
        if (onBackPressedListener != null)
            onBackPressedListener.onBackPressed();
        else
            super.onBackPressed();
    }

    private static Thread permission_worker;

    public static void runWithStoragePermission(Activity activity, Thread what) {
        MainActivity.permission_worker = what;
        if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0);
        } else {
            what.start();
            permission_worker = null;
        }
    }

    static class fileWorker extends Thread {
        public Uri uri;
    }

    private static fileWorker file_worker;

    public static void runWithFilePath(Activity activity, fileWorker what) {
        MainActivity.file_worker = what;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        activity.startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            file_worker.uri = data.getData();
            if (file_worker != null) {
                file_worker.start();
                file_worker = null;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (permission_worker != null) {
                permission_worker.start();
                permission_worker = null;
            }
        } else {
            Toast.makeText(this, R.string.storage_permission_failed, Toast.LENGTH_SHORT).show();
        }
    }

    LinearLayout mainView;
    LinearLayout showdView;

    void showMainView() {
        onBackPressedListener = null;
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        if (lastGpuToolbarTitle == null) {
            lastGpuToolbarTitle = getDefaultGpuToolbarTitle();
        }
        if (currentTitle == null) {
            currentTitle = lastGpuToolbarTitle;
        }
        updateToolbarTitle(currentTitle);
        viewPager = findViewById(R.id.view_pager);
        bottomNav = findViewById(R.id.bottom_navigation);

        setupViewPager();
        setupBottomNavigation();
    }

    private void setupViewPager() {
        ArrayList<Fragment> fragments = new ArrayList<>(Arrays.asList(
            new GpuFrequencyFragment(),
            new ImportExportFragment(),
            new SettingsFragment()
        ));

        ViewPagerAdapter adapter = new ViewPagerAdapter(this, fragments);
        viewPager.setAdapter(adapter);

        // Sync ViewPager with BottomNavigation
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                onBackPressedListener = null;
                if (isPageChangeFromUser) {
                    switch (position) {
                        case 0:
                            restoreGpuToolbarTitle();
                            bottomNav.setSelectedItemId(R.id.nav_edit_freq);
                            break;
                        case 1:
                            updateToolbarTitle(getString(R.string.import_export));
                            bottomNav.setSelectedItemId(R.id.nav_import_export);
                            break;
                        case 2:
                            updateToolbarTitle(getString(R.string.settings));
                            bottomNav.setSelectedItemId(R.id.nav_settings);
                            break;
                    }
                }
            }
        });

        // Start with GPU Frequency section
        restoreGpuToolbarTitle();
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            isPageChangeFromUser = false;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_edit_freq) {
                viewPager.setCurrentItem(0, true);
                restoreGpuToolbarTitle();
            } else if (itemId == R.id.nav_import_export) {
                viewPager.setCurrentItem(1, true);
                updateToolbarTitle(getString(R.string.import_export));
            } else if (itemId == R.id.nav_settings) {
                viewPager.setCurrentItem(2, true);
                updateToolbarTitle(getString(R.string.settings));
            }
            isPageChangeFromUser = true;
            return true;
        });
    }

    private void hideMainView() {
        // Handled by back navigation now
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_LAST_GPU_TITLE, lastGpuToolbarTitle);
        outState.putString(KEY_CURRENT_TITLE, currentTitle);
    }

    public void updateToolbarTitle(String title) {
        currentTitle = title;
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }

    private String getDefaultGpuToolbarTitle() {
        return getString(R.string.edit_freq_table);
    }

    public void updateGpuToolbarTitle(String title) {
        lastGpuToolbarTitle = title;
        updateToolbarTitle(title);
    }

    public void restoreGpuToolbarTitle() {
        if (lastGpuToolbarTitle == null) {
            lastGpuToolbarTitle = getDefaultGpuToolbarTitle();
        }
        updateToolbarTitle(lastGpuToolbarTitle);
    }

    public String getCurrentToolbarTitle() {
        return currentTitle;
    }

    public class backupBoot extends Thread {
        Activity activity;
        AlertDialog waiting;
        boolean is_err;

        public backupBoot(Activity activity) {
            this.activity = activity;
        }

        public void run() {
            is_err = false;
            runOnUiThread(() -> {
                waiting = DialogUtil.getWaitDialog(activity, R.string.backuping_img);
                waiting.show();
            });
            try {
                KonaBessCore.backupBootImage(activity);
            } catch (Exception e) {
                is_err = true;
            }
            runOnUiThread(() -> {
                waiting.dismiss();
                if (is_err)
                    DialogUtil.showError(activity, R.string.failed_backup);
                else
                    Toast.makeText(activity, R.string.backup_success, Toast.LENGTH_SHORT).show();
            });

        }
    }


    public class repackLogic extends Thread {
        boolean is_err;
        String error = "";

        public void run() {
            is_err = false;
            {
                runOnUiThread(() -> {
                    waiting = DialogUtil.getWaitDialog(MainActivity.this, R.string.repacking);
                    waiting.show();
                });

                try {
                    KonaBessCore.dts2bootImage(MainActivity.this);
                } catch (Exception e) {
                    is_err = true;
                    error = e.getMessage();
                }
                runOnUiThread(() -> {
                    waiting.dismiss();
                    if (is_err)
                        DialogUtil.showDetailedError(MainActivity.this, R.string.repack_failed,
                                error);
                });
                if (is_err)
                    return;
            }

            if (!cross_device_debug) {
                runOnUiThread(() -> {
                    waiting = DialogUtil.getWaitDialog(MainActivity.this, R.string.flashing_boot);
                    waiting.show();
                });

                try {
                    KonaBessCore.writeBootImage(MainActivity.this);
                } catch (Exception e) {
                    is_err = true;
                }
                runOnUiThread(() -> {
                    waiting.dismiss();
                    if (is_err)
                        DialogUtil.showError(MainActivity.this, R.string.flashing_failed);
                    else {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.reboot_complete_title)
                                .setMessage(R.string.reboot_complete_msg)
                                .setPositiveButton(R.string.yes, (dialog, which) -> {
                                    try {
                                        KonaBessCore.reboot();
                                    } catch (IOException e) {
                                        DialogUtil.showError(MainActivity.this,
                                                R.string.failed_reboot);
                                    }
                                })
                                .setNegativeButton(R.string.no, null)
                                .create().show();
                    }
                });
            }
        }
    }

    class unpackLogic extends Thread {
        String error = "";
        boolean is_err;
        int dtb_index;

        public void run() {
            is_err = false;
            {
                runOnUiThread(() -> {
                    waiting = DialogUtil.getWaitDialog(MainActivity.this, R.string.getting_image);
                    waiting.show();
                });
                try {
                    if (!cross_device_debug)
                        KonaBessCore.getBootImage(MainActivity.this);
                } catch (Exception e) {
                    is_err = true;
                }
                runOnUiThread(() -> {
                    waiting.dismiss();
                    if (is_err)
                        DialogUtil.showError(MainActivity.this, R.string.failed_get_boot);
                });
                if (is_err)
                    return;
            }

            {
                runOnUiThread(() -> {
                    waiting = DialogUtil.getWaitDialog(MainActivity.this, R.string.unpacking);
                    waiting.show();
                });
                try {
                    KonaBessCore.bootImage2dts(MainActivity.this);
                } catch (Exception e) {
                    is_err = true;
                    error = e.getMessage();
                }
                runOnUiThread(() -> {
                    waiting.dismiss();
                    if (is_err)
                        DialogUtil.showDetailedError(MainActivity.this, R.string.unpack_failed,
                                error);
                });
                if (is_err)
                    return;
            }

            {
                runOnUiThread(() -> {
                    waiting = DialogUtil.getWaitDialog(MainActivity.this, R.string.checking_device);
                    waiting.show();
                });
                try {
                    KonaBessCore.checkDevice(MainActivity.this);
                    dtb_index = KonaBessCore.getDtbIndex();
                } catch (Exception e) {
                    is_err = true;
                    error = e.getMessage();
                }
                runOnUiThread(() -> {
                    waiting.dismiss();
                    if (is_err)
                        DialogUtil.showDetailedError(MainActivity.this,
                                R.string.failed_checking_platform, error);
                });
                if (is_err)
                    return;
            }

            runOnUiThread(() -> {
                if (KonaBessCore.dtbs.size() == 0) {
                    DialogUtil.showError(MainActivity.this, R.string.incompatible_device);
                    return;
                }
                if (KonaBessCore.dtbs.size() == 1) {
                    KonaBessCore.chooseTarget(KonaBessCore.dtbs.get(0), MainActivity.this);
                    showMainView();
                    return;
                }
                ListView listView = new ListView(MainActivity.this);
                ArrayList<ParamAdapter.item> items = new ArrayList<>();
                for (KonaBessCore.dtb dtb : KonaBessCore.dtbs) {
                    items.add(new ParamAdapter.item() {{
                        title = dtb.id + " " + ChipInfo.name2chipdesc(dtb.type, MainActivity.this);
                        subtitle = dtb.id == dtb_index ?
                                MainActivity.this.getString(R.string.possible_dtb) : "";
                    }});
                }
                listView.setAdapter(new ParamAdapter(items, MainActivity.this));

                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.select_dtb_title)
                        .setMessage(R.string.select_dtb_msg)
                        .setView(listView)
                        .setCancelable(false)
                        .create();
                dialog.show();

                listView.setOnItemClickListener((parent, view, position, id) -> {
                    KonaBessCore.chooseTarget(KonaBessCore.dtbs.get(position), MainActivity.this);
                    dialog.dismiss();
                    showMainView();
                });
            });
        }
    }

    private void applyColorPalette() {
        android.content.SharedPreferences prefs = getSharedPreferences("KonaBessSettings", Context.MODE_PRIVATE);
        int palette = prefs.getInt("color_palette", 0);
        
        switch (palette) {
            case 1: // Purple & Teal
                setTheme(R.style.Theme_KonaBess_Purple);
                break;
            case 2: // Blue & Orange
                setTheme(R.style.Theme_KonaBess_Blue);
                break;
            case 3: // Green & Red
                setTheme(R.style.Theme_KonaBess_Green);
                break;
            case 4: // Pink & Cyan
                setTheme(R.style.Theme_KonaBess_Pink);
                break;
            case 5: // Pure AMOLED
                setTheme(R.style.Theme_KonaBess_AMOLED);
                break;
            default: // Dynamic (Material You)
                setTheme(R.style.Theme_KonaBess);
                break;
        }
    }

    public static abstract class onBackPressedListener {
        public abstract void onBackPressed();
    }

}