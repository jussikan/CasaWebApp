package fi.casa.webapp;

import android.app.Application;
import android.webkit.WebView;

public class CasaWebAppApplication extends Application {
//    IApplicationComponent appComponent = DaggerIApplicationComponent.create();
    private WebViewComponent webViewComponent;
    private WebView webView;
    private String url;
//    private SwipeRefreshComponent swipeRefreshComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        if (webViewComponent == null) {
//            webViewComponent = appComponent
//            webViewComponent = n
//            DaggerIApplicationComponent.builder().a
        }

        setUrl(getResources().getString(R.string.url));
    }

    // This allows providing mock component from test
    public void setComponent(WebViewComponent component) {
        webViewComponent = component;
    }

    public WebViewComponent component() {
        return webViewComponent;
    }

    public void setWebView(WebView wv) {
        webView = wv;
    }

    public WebView getWebView() {
        return webView;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }
}
