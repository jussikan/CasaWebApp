package fi.casa.webapp;

import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by jussi on 21/11/2017.
 */

public class MyWebClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return true;
    }
    @Override
    public void onLoadResource(WebView view, String url) {
        if( url.equals("http://yoururl.com") ){
            // do something
        }
    }
}
