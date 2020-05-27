package fi.casa.webapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.widget.ShareActionProvider;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import im.delight.android.webview.AdvancedWebView;

public class CasaWebView extends RelativeLayout implements AdvancedWebView.Listener, WebViewComponent {
    MainActivity activity = null;
    final View self = this;
    public static final String TAG = "CasaWebView";

    String domain = null;
//    final boolean secure = true;
//    final String protocol = "http";
    String baseUrl = null;

    private AdvancedWebView advancedWebView;

    private String focusedUrl = null;
    final String desktopUserAgent = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0";
    String mobileUserAgent;
    boolean desktopMode = false;
    private SwipeRefreshLayout swipeRefresh = null;
    private ShareActionProvider mShareActionProvider = null;

    public CasaWebView(Context context) {
        super(context);
        init();
    }

    public CasaWebView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CasaWebView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public CasaWebView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    public void reload() {
        advancedWebView.reload();
    }

    @Override
    public void setListener(Activity a, AdvancedWebView.Listener l) {
        advancedWebView.setListener(a, l);
    }

    @Override
    public WebSettings getSettings() {
        return advancedWebView.getSettings();
    }

    @Override
    public void setWebViewClient(WebViewClient c) {
        advancedWebView.setWebViewClient(c);
    }

    @Override
    public void addPermittedHostname(String s) {
        advancedWebView.addPermittedHostname(s);
    }

    @Override
    public void loadUrl(String u) {
        advancedWebView.loadUrl(u);
    }

    @Override
    public void requestFocusNodeHref(Message m) {
        advancedWebView.requestFocusNodeHref(m);
    }

    @Override
    public String getUrl() {
        return advancedWebView.getUrl();
    }

    @Override
    public void onPause() {
        advancedWebView.onPause();
    }

    @Override
    public void onDestroy() {
        advancedWebView.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        advancedWebView.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public boolean onBackPressed() {
        return advancedWebView.onBackPressed();
    }

    @Override
    public String getTitle() {
        return advancedWebView.getTitle();
    }

//    public String getFocusedUrl() {
//        return focusedUrl;
//    }

//    public void setFocusedUrl(String focusedUrl) {
//        this.focusedUrl = focusedUrl;
//    }

//    final private Pattern ptrnExtractExternalUrl = Pattern.compile(".*("+ protocol +"s?:\\/\\/.*)[^\\/]*");

//    private String pickDomainFromURLAfterBaseURL(final String url, final String baseUrl) {
//        Log.i(TAG, "plop url: "+ url);
//        final String restOfUrl = url.replaceFirst(protocol +"s?:\\/\\/([^\\/]*)\\/", "");
//
//        Log.i(TAG, "restOfUrl: "+ restOfUrl);
//        final Matcher domainMatcher = ptrnExtractExternalUrl.matcher(restOfUrl);
//
//        final boolean matches = domainMatcher.matches();
//        final String domainInRestOfUrl = matches
//            ? domainMatcher.group(1)
//            : null
//        ;
//        return domainInRestOfUrl;
//    }

//    private void initializeSettings() {
//        Log.i(TAG, "at initializeSettings");
//        SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(settingPreferencesKey, Context.MODE_PRIVATE).edit();
//        editor.putString("userAgent", mobileUserAgent).apply();
//    }

    private void init() {
        activity = (MainActivity) getContext();

        inflate();
        onViewCreated();
    }

    private View root_layout = null;

    private void inflate() {
        root_layout = inflate(getContext(), R.layout.casawebview, this);
    }

    private void onViewCreated() {
        advancedWebView = (AdvancedWebView) findViewById(R.id.advancedwebview);
        advancedWebView.setListener(activity, this);

        /* antaako tää aina mobiili-user agentin? */
        mobileUserAgent = advancedWebView.getSettings().getUserAgentString();
        Log.i(TAG, "mobileUserAgent: "+ mobileUserAgent);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefresh.setRefreshing(true);
                advancedWebView.reload();
                swipeRefresh.setRefreshing(false);
            }
        });

        advancedWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                /* if the hostname may not be accessed */
//                if (! activity.isHostnameAllowed(url)) {
//                    /* inform the listener about the request */
//                    ((AdvancedWebView.Listener) self).onExternalPageRequest(url);
//
//                    /* cancel the original request */
//                    return true;
//                }

//            /* if there is a user-specified handler available */
//            if (mCustoadvancedWebViewClient != null) {
//                // if the user-specified handler asks to override the request
//                if (mCustoadvancedWebViewClient.shouldOverrideUrlLoading(view, url)) {
//                    // cancel the original request
//                    return true;
//                }
//            }

                Log.i(TAG, "got url: "+ url);

                // cancel the original request with true
                return false;
            }
        });

//        advancedWebView.addPermittedHostname(domain);
//        advancedWebView.loadUrl(baseUrl);

        activity.registerForContextMenu(advancedWebView);

        advancedWebView.setLongClickable(true);
        advancedWebView.setOnLongClickListener(new View.OnLongClickListener() {
            private Handler mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    // Get link-URL.
                    final String hrefUrl = (String) msg.getData().get("url");
                    final String externalUrl;
                    final String url;

                    String urlDecoded = null;
                    try {
                        urlDecoded = URLDecoder.decode(hrefUrl, "UTF-8");
                    }
                    catch (UnsupportedEncodingException e) {
                        /* ignore */
                    }

                    externalUrl = urlDecoded != null ? activity.pickDomainFromURLAfterBaseURL(urlDecoded, baseUrl) : null;
                    url = externalUrl != null ? externalUrl : hrefUrl;

                    Log.i(TAG, "advancedWebView handleMessage, externalUrl:"+ externalUrl);

                    activity.setFocusedUrl(url);
                    activity.openContextMenu(advancedWebView);
                }
            };

            @Override
            public boolean onLongClick(View v) {
//                WebView.HitTestResult result = advancedWebView.getHitTestResult();
                Message msg = mHandler.obtainMessage();
                Log.i(TAG, "advancedWebView longClick, msg:"+ msg.toString());
                advancedWebView.requestFocusNodeHref(msg);
                return true;
            }
        });

        Log.i(TAG, "getLoadWithOverviewMode "+ advancedWebView.getSettings().getLoadWithOverviewMode());
        Log.i(TAG, "getUseWideViewPort "+ advancedWebView.getSettings().getUseWideViewPort());
        Log.i(TAG, "getBuiltInZoomControls "+ advancedWebView.getSettings().getBuiltInZoomControls());
        Log.i(TAG, "getDisplayZoomControls "+ advancedWebView.getSettings().getDisplayZoomControls());

//        "getLoadWithOverviewMode " false
//        "getUseWideViewPort " false
//        "getBuiltInZoomControls " false
//        "getDisplayZoomControls " true

//        webView.getSettings().setLoadWithOverviewMode(enabled);   => true
//        webView.getSettings().setUseWideViewPort(enabled);        => true
//        webView.getSettings().setBuiltInZoomControls(enabled);    => true
//        webView.getSettings().setDisplayZoomControls(! enabled);  => false

        advancedWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        advancedWebView.setScrollbarFadingEnabled(false);
    }

    public void setDomain(String domain) {
        this.domain = domain;
        advancedWebView.addPermittedHostname(this.domain);
    }

    public void setBaseURL(String baseUrl) {
        this.baseUrl = baseUrl;
        advancedWebView.loadUrl(this.baseUrl);
    }

    public AdvancedWebView getAdvancedWebView() {
        return advancedWebView;
    }

    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

//    private Intent createShareIntent() {
//        final String url = getFocusedUrl() != null ? getFocusedUrl() : advancedWebView.getUrl();
//        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
//        shareIntent.setType("text/plain");
//        shareIntent.putExtra(Intent.EXTRA_TEXT, url);
//        return shareIntent;
//    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) { }

    @Override
    public void onPageFinished(String url) {
//        advancedWebView.getSettings().setLoadWithOverviewMode(enabled);
//        advancedWebView.getSettings().setUseWideViewPort(enabled);
//
//        advancedWebView.getSettings().setBuiltInZoomControls(enabled);
//        advancedWebView.getSettings().setDisplayZoomControls(! enabled);
        final String title = advancedWebView.getTitle();

        // Update the action bar
        activity.getSupportActionBar().setTitle(title);
        activity.getSupportActionBar().setSubtitle(url);
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) { }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) { }

    @Override
    public void onExternalPageRequest(String url) {
        Log.i(TAG, "at onExternalPageRequest, url: "+ url);
//        if (! activity.isHostnameAllowed(url)) {
//            activity.openContextMenu(advancedWebView);
//        }
    }
}
