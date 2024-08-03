package tfsapps.securityalarmplus;;

import android.os.Bundle;
import android.preference.PreferenceActivity;
/**
 * Created by FURUKAWA on 2016/09/30.
 */
public class CrSetActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new CrSetFragment()).commit();
    }
}
