package tfsapps.securityalarmplus;;

import android.os.Bundle;
import android.preference.PreferenceFragment;
/**
 * Created by FURUKAWA on 2016/09/30.
 */
public class CrSetFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
