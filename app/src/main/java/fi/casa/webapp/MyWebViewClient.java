package fi.casa.webapp;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MyWebViewClient extends WebViewClient {
    private MainActivity activity = null;
    private Firewall firewall = null;
    private boolean wasCommitCalled = false;
//    private Consumer onForbiddenHostnameRequestCallback;

    public MyWebViewClient(final MainActivity activity, final Firewall firewall) {
        this.activity = activity;
        this.firewall = firewall;
    }

//    public void setOnForbiddenHostnameRequestInformer(Consumer callback) {
//        this.onForbiddenHostnameRequestCallback = callback;
//    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView webView, String url) {
        /* if the hostname may not be accessed */
        if (! firewall.isHostnameAllowed(url)) {
            /* cancel the original request */
            Log.i(getClass().getName(), "tried to navigate to a forbidden hostname");
            activity.onForbiddenHostnameRequest(webView, url);
            return true;
        }

//            /* if there is a user-specified handler available */
//            if (mCustomWebViewClient != null) {
//                // if the user-specified handler asks to override the request
//                if (mCustomWebViewClient.shouldOverrideUrlLoading(webView, url)) {
//                    // cancel the original request
//                    return true;
//                }
//            }

        Log.i(this.getClass().getName(), "navigating to "+ url);

        // cancel the original request with true
        return false;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        wasCommitCalled = false;
    }

    @Override
    public void onPageCommitVisible(WebView view, String url) {
        wasCommitCalled = true;
    }

    @Override
    public void onPageFinished(WebView webView, String url) {
        super.onPageFinished(webView, url);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            new Handler().postDelayed(new Runnable() {
//
//                @Override
//                public void run() {
//                    if (!wasCommitCalled) {
//                        webView.loadUrl(url);
//                    }
//                }
//            }, 2500);
//        }

        activity.onPageFinished(webView, url);
    }
}
