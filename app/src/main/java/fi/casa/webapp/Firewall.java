package fi.casa.webapp;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

public class Firewall {
    private Context context = null;
    protected final List<String> permittedHostnames = new LinkedList<String>();

    public Firewall(final Context context) {
        this.context = context;
        init();
    }

    private void init() {
        fillPermittedHostnames(context.getResources().getStringArray(R.array.permitted_hostnames));
    }

    private void fillPermittedHostnames(final String[] phn) {
        for (String s : phn) {
            permittedHostnames.add(s);
        }
    }

    /* TODO some unit tests would be good for this */
    public boolean isHostnameAllowed(final String url) {
        /* if the permitted hostnames have not been restricted to a specific set */
        if (permittedHostnames.size() == 0) {
            /* all hostnames are allowed */
            return true;
        }

        /* get the actual hostname of the URL that is to be checked */
        final String actualHost = Uri.parse(url).getHost();
        Log.i(this.getClass().getName(), "actualHost: "+ actualHost);

        for (String expectedHost : permittedHostnames) {
            /* if the two hostnames match or if the actual host is a subdomain of the expected host */
            if (actualHost.equals(expectedHost) || actualHost.endsWith("."+ expectedHost)) {
                /* the actual hostname of the URL to be checked is allowed */
                return true;
            }
        }

        Log.i(getClass().getName(), "forbidden hostname");

        return false;
    }
}
