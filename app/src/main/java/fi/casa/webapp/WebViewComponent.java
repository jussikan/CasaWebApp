package fi.casa.webapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Message;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

import im.delight.android.webview.AdvancedWebView;

public interface WebViewComponent {
    void reload();
    void setListener(Activity a, AdvancedWebView.Listener l);
    WebSettings getSettings();
    void setWebViewClient(WebViewClient c);
    void addPermittedHostname(String s);
    void loadUrl(String u);
    void setLongClickable(boolean t);
    void setOnLongClickListener(View.OnLongClickListener l);
    void requestFocusNodeHref(Message m);
    void setScrollBarStyle(int i);
    void setScrollbarFadingEnabled(boolean t);
    String getUrl();
    void onPause();
    void onDestroy();
    void onActivityResult(int requestCode, int resultCode, Intent intent);
    boolean onBackPressed();
    String getTitle();
}
