package xzr.konabess.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import xzr.konabess.GpuTableEditor;
import xzr.konabess.MainActivity;

public class GpuFrequencyFragment extends Fragment {
    private LinearLayout contentContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentContainer = new LinearLayout(requireContext());
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        contentContainer.setPadding(16, 16, 16, 16);
        
        loadContent();
        return contentContainer;
    }

    private void loadContent() {
        if (getActivity() instanceof MainActivity) {
            new GpuTableEditor.gpuTableLogic((MainActivity) getActivity(), contentContainer).start();
        }
    }
}

