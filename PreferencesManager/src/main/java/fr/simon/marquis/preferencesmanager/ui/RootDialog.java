package fr.simon.marquis.preferencesmanager.ui;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import fr.simon.marquis.preferencesmanager.R;

public class RootDialog extends DialogFragment {

    public static RootDialog newInstance() {
        return new RootDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Builder(getActivity()).setIcon(R.drawable.ic_action_emo_evil).setTitle(R.string.no_root_title)
                .setMessage(R.string.no_root_message).setPositiveButton(R.string.no_root_button, (dialog, which) -> dismiss()).create();
    }
}
