package fi.casa.webapp;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by jussi on 09/12/2017.
 */

public class SettingsFragment extends Fragment {
    private View field_userAgent = null;
    private Button button_restore_userAgent_default = null;

    private Map defaultSettings = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Settings");

        field_userAgent = getView().findViewById(R.id.setting_field_userAgent);

        button_restore_userAgent_default = (Button) getView().findViewById(R.id.restore_userAgent_default);
        button_restore_userAgent_default.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((EditText) field_userAgent).setText((String) defaultSettings.get("userAgent"));
            }
        });
    }

    private void printSettings(final Bundle settings) {
        final Iterator<String> it = settings.keySet().iterator();
        String key = null;
        while (it.hasNext()) {
            key = it.next();
            Log.i("JEES", "from settings, "+ key +": "+ settings.getString(key));
        }
    }

    private void placeSettingsValues(final Bundle settings) {
        ((EditText) field_userAgent).setText(settings.getString("userAgent"));
    }

    @Override
    public void onResume() {
        super.onResume();

        final Bundle settings =
//            getArguments()
            ((MainActivity) getActivity()).prepareSettings()
        ;
        defaultSettings = ((MainActivity) getActivity()).getDefaultSettings();
        placeSettingsValues(settings);
    }

    protected Map collectSettings() {
        final Map data = new HashMap<String, Object>();

        data.put("userAgent", ((EditText) field_userAgent).getText());

        return data;
    }

    @Override
    public void onPause() {
        super.onPause();

        /* send settings to calling Activity */
        final Map settings = collectSettings();

        ((MainActivity) getActivity()).saveSettings(settings);
    }
}
