package com.ireddragonicy.konabessnext.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ireddragonicy.konabessnext.MainActivity;
import com.ireddragonicy.konabessnext.TableIO;

public class ImportExportFragment extends Fragment {
    private LinearLayout contentContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    contentContainer = new LinearLayout(requireContext());
    contentContainer.setOrientation(LinearLayout.VERTICAL);
    contentContainer.setPadding(0, 24, 0, 24);

        loadContent();
        return contentContainer;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh the import/export actions so they reflect the latest preparation state
        if (contentContainer != null) {
            loadContent();
        }
    }

    private void loadContent() {
        if (!isAdded()) {
            return;
        }
        if (getActivity() instanceof MainActivity) {
            new TableIO.TableIOLogic((MainActivity) getActivity(), contentContainer).start();
        }
    }
}

