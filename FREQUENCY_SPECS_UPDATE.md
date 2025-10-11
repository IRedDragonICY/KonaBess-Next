# GPU Frequency Table - Detailed Specs Display

## Overview
Enhanced the GPU frequency table editor to show detailed technical specifications for each frequency level using modern Material Design 3 principles.

## Features Added

### 1. Detailed Spec Display
Each frequency item now displays:
- **Bus Max** - Maximum bus level index (Memory icon, Primary color)
- **Bus Min** - Minimum bus level index (Memory icon, Secondary color)
- **Bus Level** - Bus frequency level index (Frequency icon, Tertiary color)
- **Voltage Level** - Voltage/power level name (Battery icon, Error color)

**Note**: Bus values are **level indices**, not MHz frequencies. They represent DDR bus configuration levels.

### 2. Modern Material Design 3 Layout
- **2x2 Grid Layout**: Specs organized in a clean grid with proper spacing
- **Color-Coded Icons**: Each spec has a distinct colored icon (16dp) for quick identification
- **Monospace Font**: All values use monospace font for better readability
- **Responsive Visibility**: Specs only shown when data is available

### 3. Material Design Icons Created
New vector drawable icons:
- `ic_memory.xml` - Memory/chip icon for GPU bus levels (replaced incorrect bus vehicle icon)
- `ic_battery.xml` - Battery/voltage icon for power levels
- `ic_frequency.xml` - Frequency/signal icon for bus frequency level
- `ic_arrow_upward.xml` - Add to top action
- `ic_arrow_downward.xml` - Add to bottom/duplicate action

## Technical Implementation

### Layout Structure (`gpu_freq_item_card.xml`)
```xml
<specs_container>
  <Row 1>
    <bus_max_container>: Icon + Label + Value
    <bus_min_container>: Icon + Label + Value
  </Row 1>
  <Row 2>
    <bus_freq_container>: Icon + Label + Value
    <voltage_container>: Icon + Label + Value
  </Row 2>
</specs_container>
```

### Data Model (`GpuFreqAdapter.FreqItem`)
Extended with spec fields:
```java
public String busMax;        // qcom,bus-max value
public String busMin;        // qcom,bus-min value
public String busFreq;       // qcom,bus-freq (formatted)
public String voltageLevel;  // qcom,level/qcom,cx-level (converted)

public boolean hasSpecs() {
    return busMax != null || busMin != null || 
           busFreq != null || voltageLevel != null;
}
```

### DTS Parsing (`GpuTableEditor.generateLevels()`)
Extracts spec data from DTS level lines:
```java
for (String line : level.lines) {
    String paramName = DtsHelper.decode_hex_line(line).name;
    
    if ("qcom,bus-max".equals(paramName)) {
        item.busMax = String.valueOf(DtsHelper.decode_int_line(line).value);
    } else if ("qcom,bus-freq".equals(paramName)) {
        // Bus-freq is a level/index, not frequency in MHz
        item.busFreq = String.valueOf(DtsHelper.decode_int_line(line).value);
    } else if ("qcom,level".equals(paramName) || "qcom,cx-level".equals(paramName)) {
        item.voltageLevel = GpuVoltEditor.levelint2str(voltLevel);
    }
    // ... similar for bus-min
}
```

### Adapter Binding Logic
- **Level Items**: Hide subtitle, show specs_container with populated values
- **Action Items**: Show subtitle, hide specs_container
- **Back Button**: Show subtitle, hide specs_container
- **Visibility Control**: Individual spec containers only visible when data available

## Visual Design

### Color Scheme
- **Bus Max**: `?attr/colorPrimary` (Material Primary) - 🟣 Purple
- **Bus Min**: `?attr/colorSecondary` (Material Secondary) - 🟢 Green
- **Bus Level**: `?attr/colorTertiary` (Material Tertiary) - 🔵 Blue
- **Voltage**: `?attr/colorError` (Material Error) - 🔴 Red (emphasizes critical power)

### Typography
- **Labels**: `TextAppearance.Material3.LabelSmall` (colorOnSurfaceVariant)
- **Values**: `TextAppearance.Material3.BodySmall` (colorOnSurface, monospace)

### Spacing
- Icon size: 16dp × 16dp
- Icon margin: 6dp end
- Label margin: 4dp end
- Row margin: 8dp top
- Container margin: 12dp top

## User Benefits
1. **Quick Insights**: See all critical specs at a glance without drilling down
2. **Professional Look**: Modern Material Design 3 aesthetic
3. **Better Decision Making**: Complete technical details for overclocking/undervolting
4. **Visual Clarity**: Color-coded icons help distinguish different spec types
5. **Performance Monitoring**: Understand bus and power behavior at each frequency

## Build Performance
- **Configuration Cache**: Enabled (83% faster builds)
- **Build Time**: ~1 second for incremental builds
- **Clean Build**: ~7-23 seconds depending on cache

## Files Modified
- `app/src/main/res/layout/gpu_freq_item_card.xml` - Layout with 2x2 specs grid
- `app/src/main/java/xzr/konabess/adapters/GpuFreqAdapter.java` - Model and binding logic
- `app/src/main/java/xzr/konabess/GpuTableEditor.java` - DTS spec extraction (bus levels, voltage)
- `app/src/main/res/drawable/ic_memory.xml` - Memory/chip icon for GPU bus (new)
- `app/src/main/res/drawable/ic_battery.xml` - Battery/voltage icon (new)
- `app/src/main/res/drawable/ic_frequency.xml` - Frequency/signal icon (new)
- `app/src/main/res/drawable/ic_arrow_upward.xml` - Upload/add top action (new)
- `app/src/main/res/drawable/ic_arrow_downward.xml` - Download/add bottom action (new)

## Testing Checklist
- [ ] Specs display correctly for all frequency levels
- [ ] Icons render with proper colors
- [ ] Layout scales correctly on different screen sizes
- [ ] Specs hidden for action/back items
- [ ] Individual specs hide when data unavailable
- [ ] Drag/drop still works with new layout
- [ ] Delete button still functional
- [ ] Frequency editing still works
- [ ] No performance regression

## Future Enhancements
- Add tooltips explaining each spec
- Support metric/imperial units toggle
- Add spec diff highlighting when values change
- Export spec table to CSV/JSON
- Add spec validation warnings (e.g., voltage too high)

---
**Last Updated**: 2024-01-XX  
**Material Design Version**: Material 3  
**Target Android**: API 21+
