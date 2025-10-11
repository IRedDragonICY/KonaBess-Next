# Chipset Selector Feature

## Overview
Fitur Chipset Selector memungkinkan user untuk melihat dan mengganti target chipset langsung dari GPU Frequency section tanpa perlu kembali ke menu awal.

## Features

### 1. **Chipset Selector Card**
- Menampilkan chipset yang sedang aktif/dipilih
- Card professional dengan Material Design 3
- Icon 🎯 untuk visual indicator
- Click untuk membuka dialog selector

### 2. **Visual Indicators**
- ✓ (Checkmark) - Chipset yang sedang dipilih
- 📱 (Phone icon) - Possible DTB berdasarkan `ro.boot.dtb_idx`
- Highlight pada chipset aktif

### 3. **Chipset Switching**
- Dialog konfirmasi sebelum switch chipset
- Loading indicator saat reload GPU table
- Auto-refresh bins list setelah switch
- Toast notification untuk feedback

## UI/UX Design

### Card Layout
```
┌─────────────────────────────────────┐
│  Target Chipset                     │
│  ┌───────────────────────────────┐  │
│  │ 📱  Snapdragon 8 Elite     ⚙️ │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
```

**Material Design Icons Used:**
- `ic_developer_board.xml` - Developer board icon for chipset (24dp)
- `ic_tune.xml` - Settings/tune icon for configuration (24dp)
- `ic_check.xml` - Checkmark for currently selected chipset
- `ic_phone_android.xml` - Phone icon for possible DTB indicator

### Dialog Layout
```
┌──────────────────────────────────────┐
│  Select Target Chipset               │
│  Choose the chipset configuration... │
│                                       │
│  ┌─────────────────────────────────┐ │
│  │ 0 Snapdragon 8 Elite            │ │
│  │ Currently Selected              │ │
│  ├─────────────────────────────────┤ │
│  │ 1 Snapdragon 8+Gen1             │ │
│  │ Possible DTB                    │ │
│  └─────────────────────────────────┘ │
│                                       │
│                        [Cancel]       │
└──────────────────────────────────────┘
```

## Technical Implementation

### Key Components

#### 1. KonaBessCore.java
```java
// Store current selected DTB
private static dtb currentDtb;

// Get current DTB
public static dtb getCurrentDtb() {
    return currentDtb;
}

// Set current DTB when choosing target
public static void chooseTarget(dtb dtb, Activity activity) {
    dts_path = activity.getFilesDir().getAbsolutePath() + "/" + dtb.id + ".dts";
    ChipInfo.which = dtb.type;
    currentDtb = dtb;  // Store current selection
    prepared = true;
}
```

#### 2. GpuTableEditor.java
```java
// Create chipset selector card
private static View createChipsetSelectorCard(Activity activity, LinearLayout page)

// Show chipset selector dialog
private static void showChipsetSelectorDialog(Activity activity, LinearLayout page, TextView chipsetNameView)

// Switch to different chipset
private static void switchChipset(Activity activity, LinearLayout page, KonaBessCore.dtb newDtb, TextView chipsetNameView)
```

### Workflow

1. **Display Current Chipset**
   - Card ditampilkan di bagian atas GPU Frequency
   - Hanya muncul jika ada multiple chipsets (dtbs.size() > 1)
   - Menampilkan nama dan deskripsi chipset aktif

2. **User Clicks Chipset Card**
   - Dialog muncul dengan list semua available chipsets
   - Current selection ditandai dengan ✓
   - Possible DTB ditandai dengan 📱

3. **User Selects Different Chipset**
   - Confirmation dialog muncul
   - Jika confirm, loading indicator tampil
   - Background thread:
     - Switch ke chipset baru
     - Reload DTS file
     - Decode GPU table
     - Patch throttle level
   - UI refresh dengan bins dari chipset baru
   - Toast notification "Switched to chipset X"

4. **Error Handling**
   - Loading error: Show error dialog
   - No chipsets available: Toast notification
   - Same chipset selected: Dialog dismiss langsung

## Benefits

### For Users
- **Convenience**: Tidak perlu restart app untuk ganti chipset
- **Visual Feedback**: Jelas chipset mana yang sedang aktif
- **Safety**: Confirmation dialog mencegah accidental switch
- **Efficiency**: Langsung bisa edit chipset lain tanpa navigasi kompleks

### For Developers
- **Clean Code**: Chipset state management terpusat di KonaBessCore
- **Reusability**: Dialog dan card bisa digunakan di section lain
- **Maintainability**: Single source of truth untuk current chipset

## Future Enhancements

1. **Multi-language Support**
   - Add translations for chipset selector strings
   - Support RTL layouts

2. **Enhanced Visuals**
   - Add chipset icons/logos
   - Color coding untuk different chipset families
   - Animation saat switch

3. **Advanced Features**
   - Compare mode: View multiple chipsets side-by-side
   - Favorite chipsets
   - Quick switch shortcut

## Testing Checklist

- [x] Build successful
- [x] App installs without errors
- [ ] Card displays correctly with single chipset (should hide)
- [ ] Card displays correctly with multiple chipsets
- [ ] Dialog shows all available chipsets
- [ ] Current chipset highlighted with ✓
- [ ] Possible DTB marked with 📱
- [ ] Confirmation dialog works
- [ ] Chipset switching reloads GPU table
- [ ] Toast notification appears
- [ ] Error handling works
- [ ] UI doesn't freeze during switch

## Code Quality

- Material Design 3 guidelines followed
- Proper error handling with try-catch
- Background thread untuk heavy operations
- UI updates on main thread (runOnUiThread)
- Resource cleanup (dialog dismiss)
- Consistent naming conventions
- Professional typography and spacing

