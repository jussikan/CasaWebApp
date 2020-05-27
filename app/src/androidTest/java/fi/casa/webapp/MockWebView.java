package fi.casa.webapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

/** Advanced WebView component for Android that works as intended out of the box */
@SuppressWarnings("deprecation")
public class MockWebView extends WebView {

    private Integer reloadCount = 0;

    public MockWebView(Context context) {
        super(context);
        init(context);
    }

    public MockWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MockWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @SuppressLint({ "SetJavaScriptEnabled" })
    protected void init(Context context) {
        setFocusable(true);

        final WebSettings webSettings = getSettings();
        webSettings.setJavaScriptEnabled(true);
    }

    public void reload() {
        ++ reloadCount;
        throw new RuntimeException("mockwebview.reload");
    }

    public Integer getReloadCount() {
        return reloadCount;
    }
}
