package fi.casa.webapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import im.delight.android.webview.AdvancedWebView;

public class MainActivity extends AppCompatActivity
//    implements AdvancedWebView.Listener
{
    final MainActivity self = this;
    public static final String TAG = "MainActivity";
    private MyRecyclerViewAdapter recyclerViewAdapter;
    private int tabGridColumnCount = 2;
    String settingPreferencesKey = null;

    protected URL url;

    String domain;

//    protected final List<String> mPermittedHostnames = new LinkedList<String>();
    protected Firewall firewall = null;
    private MyWebViewClient myWebViewClient;

    private Fragment fragment = null;

    protected WebView mWebView;

    private String focusedUrl = null;
    final String desktopUserAgent = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0";
    String mobileUserAgent;
    boolean desktopMode = false;
    private SwipeRefreshLayout swipeRefresh = null;
    private ShareActionProvider mShareActionProvider = null;

    final private MainActivity mainActivity = this;
    private FrameLayout webviewContainer;
    private Button addWebView;
    private WebView focusedWebView;
    AppCompatButton buttonBack;
    AppCompatButton buttonForward = null;

    private List<WebViewCollector> webViewCollectors = new ArrayList<>();
    private List<WebView> webViews = new LinkedList<>();
    private HashMap<Object, Map<String, String>> webViewDataMap = new HashMap<Object, Map<String, String>>();

    private FrameLayout.LayoutParams webViewLayoutParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
    );


    public MainActivity() {
    }

    private void fillPermittedHostnames(String[] phn) {
//        for (String s : phn) {
//            mPermittedHostnames.add(s);
//        }
//        ((AdvancedWebView) webView).addPermittedHostname(h);
    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    public String getFocusedUrl() {
        return focusedUrl;
    }

    public void setFocusedUrl(String focusedUrl) {
        this.focusedUrl = focusedUrl;
    }

    private void setFragment(Fragment frg) {
        fragment = frg;
    }

    private static Pattern ptrnExtractExternalUrl = null;

    protected void setExternalUrlExtractPattern(URL url) {
        setExternalUrlExtractPattern(url.getProtocol());
    }

    protected void setExternalUrlExtractPattern(String protocol) {
        ptrnExtractExternalUrl = Pattern.compile(".*("+ protocol +":\\/\\/.*)[^\\/]*");
    }

    protected static String getUrlWithoutProtocol(final String url) {
        URL helper;
        try {
            helper = new URL(url);
        } catch (Exception e) {
            return null;
        }

        final String restOfUrl = url.replaceFirst(helper.getProtocol() +"s?:\\/\\/", "");
        return restOfUrl;
    }

    protected String pickHostFromUrl(final String url) {
        final Matcher domainMatcher = ptrnExtractExternalUrl.matcher(url);

        final boolean matches = domainMatcher.matches();
        final String host = matches
            ? domainMatcher.group(1)
            : null
        ;
        return host;
    }

    protected static String pickURLAfterBaseURL(final String url) {
        Log.i(TAG, "plop url: "+ url);
        final String restOfUrl = getUrlWithoutProtocol(url);

        Log.i(TAG, "restOfUrl: "+ restOfUrl);
        final Matcher domainMatcher = ptrnExtractExternalUrl.matcher(restOfUrl);

        final boolean matches = domainMatcher.matches();
        final String referredUrl = matches
            ? domainMatcher.group(1)
            : null
        ;

        return referredUrl;
    }

    protected String pickDomainFromURLAfterBaseURL(final String url, final String baseUrl) {
        Log.i(TAG, "plop url: "+ url);
        final String restOfUrl = getUrlWithoutProtocol(url);

        Log.i(TAG, "restOfUrl: "+ restOfUrl);
        final Matcher domainMatcher = ptrnExtractExternalUrl.matcher(restOfUrl);

        final boolean matches = domainMatcher.matches();
        final String referredUrl = matches
                ? domainMatcher.group(1)
                : null
                ;

        String referredUrlWithoutProtocol = getUrlWithoutProtocol(referredUrl);

        final String domainInRestOfUrl = referredUrl.startsWith("http")
                ? referredUrlWithoutProtocol
                : null;

        return domainInRestOfUrl;
    }

    /* TODO some unit tests would be good for this */
//    protected boolean isHostnameAllowed(final String url) {
//        /* if the permitted hostnames have not been restricted to a specific set */
//        if (mPermittedHostnames.size() == 0) {
//            /* all hostnames are allowed */
//            return true;
//        }
//
//        /* get the actual hostname of the URL that is to be checked */
//        final String actualHost = Uri.parse(url).getHost();
//        Log.i(TAG, "actualHost: "+ actualHost);
//
//        String decodedUrl = null;
//        try {
//            decodedUrl = URLDecoder.decode(url, "UTF-8");
//            Log.i(TAG, "decodedUrl: "+ decodedUrl);
//        }
//        catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
////            return false;
//        }
//
//        for (String expectedHost : mPermittedHostnames) {
//            /* if the two hostnames match or if the actual host is a subdomain of the expected host */
//            if (actualHost.equals(expectedHost) || actualHost.endsWith("."+ expectedHost)) {
//                /* the actual hostname of the URL to be checked is allowed */
//                return true;
//            }
//        }
//
//        return false;
//    }

    protected void setDesktopMode(final WebView webView, final boolean enabled) {
        desktopMode = enabled;

        if (enabled) {
            webView.getSettings().setUserAgentString(desktopUserAgent);
        }
        else {
            webView.getSettings().setUserAgentString(mobileUserAgent);
        }
        webView.getSettings().setLoadWithOverviewMode(enabled);
        webView.getSettings().setUseWideViewPort(enabled);

        webView.getSettings().setSupportZoom(enabled);
        webView.getSettings().setBuiltInZoomControls(enabled);
        webView.getSettings().setDisplayZoomControls(! enabled);

        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(! enabled);
    }

    private void initializeSettings() {
        Log.i(TAG, "at initializeSettings");
        SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(settingPreferencesKey, Context.MODE_PRIVATE).edit();
        editor.putString("userAgent", mobileUserAgent).apply();
    }

    protected WebView getWebView() {
        WebView wv = ((CasaWebAppApplication) getApplicationContext()).getWebView();

        if (wv == null) {
            wv = (WebView) ((ViewGroup) findViewById(R.id.webview_container)).getChildAt(0);
        }

        return wv;
    }

//    protected String readDomain() {
//        String domain = getResources().getString(R.string.domain);
//        this.domain = domain;
//        return domain;
//    }

    private WebView createWebView() {
        /* 2. ja 3. argumentti ?? */
        final AdvancedWebView webView = new AdvancedWebView(this);
        webView.setLayoutParams(webViewLayoutParams);

        webView.setLongClickable(true);
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            WebViewLongClickHandler handler = new WebViewLongClickHandler(webView);

            @Override
            public boolean onLongClick(View v) {
                Message msg = handler.obtainMessage();
                Log.i(TAG, "WebView longClick, msg:"+ msg.toString());
                webView.requestFocusNodeHref(msg);
                return true;
            }
        });
        registerForContextMenu(webView);

        return webView;
    }

    private void switchToWebView(WebView webView) {
        focusedWebView.setVisibility(View.INVISIBLE);
        webView.setVisibility(View.VISIBLE);
        setFocusedWebView(webView);
        setFocusedUrl(webView.getUrl());
    }

    protected void switchToLatestWebViewTab() {
        switchToWebView(webViewCollectors.get(webViewCollectors.size() - 1).getWebView());
    }

    private void setFocusedWebView(WebView webView) {
        focusedWebView = webView;
        ((CasaWebAppApplication) getApplication()).setWebView(webView);
    }

    protected static class WebViewLongClickHandler extends Handler {
        private WebView webView;

        public WebViewLongClickHandler(WebView webView) {
            this.webView = webView;
        }

        @Override
        public void handleMessage(Message msg) {
            final String hrefUrl;
            final String externalUrl;
            final String url;
            final String urlDecoded;

            hrefUrl = (String) msg.getData().get("url");
            try {
                urlDecoded = URLDecoder.decode(hrefUrl, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                /* ignore */
                return;
            } catch (NullPointerException npe) {
                /* ignore */
                return;
            }

            externalUrl = urlDecoded != null ? pickURLAfterBaseURL(urlDecoded) : null;
            url = externalUrl != null ? externalUrl : hrefUrl;

            Log.i(TAG, "mWebView handleMessage, externalUrl:" + externalUrl);

            ((MainActivity) this.webView.getContext()).setFocusedUrl(url);
            ((MainActivity) this.webView.getContext()).openContextMenu(this.webView);
        }
    }

    private OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            Log.i(TAG, "at handleOnBackPressed");
        }
    };

    public int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Toolbar myToolbar = findViewById(R.id.main_toolbar);

        buttonBack = findViewById(R.id.buttonBack);
        buttonForward = findViewById(R.id.buttonForward);

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(/* Button */ View v) {
                final WebView webView = getWebView();
                Log.i(TAG, "going back");
                webView.goBack();
            }
        });
        buttonForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(/* Button */ View v) {
                final WebView webView = getWebView();
                Log.i(TAG, "going forward");
                webView.goForward();
            }
        });

        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        /* TODO pick initial, configured page title from strings.xml ?? */
        /* https://stackoverflow.com/a/40587169 ! */
        final RecyclerView recyclerView = findViewById(R.id.tab_grid);
        recyclerViewAdapter = new MyRecyclerViewAdapter(this, webViewCollectors);
        recyclerViewAdapter.setColumnCount(tabGridColumnCount);
        recyclerView.setLayoutManager(new GridLayoutManager(this, tabGridColumnCount));
        recyclerView.setAdapter(recyclerViewAdapter);


        webviewContainer = findViewById(R.id.webview_container);

        addWebView = findViewById(R.id.addTab);
        addWebView.setOnClickListener(v -> {
            createNewWebViewTab(getWebView().getUrl());
            switchToLatestWebViewTab();
            toggleTabs(false);
        });

        try {
            url = new URL( ((CasaWebAppApplication) getApplication()).getUrl() );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        setExternalUrlExtractPattern(url);

//        fillPermittedHostnames(getResources().getStringArray(R.array.permitted_hostnames));
//        introducePermittedHostnames();
        firewall = new Firewall(this);

        setSupportActionBar(myToolbar);

        final WebView webView = createWebView();
        final WebViewCollector webViewCollector = new WebViewCollector(webView);
        webViewCollector.setContext(self);
        ((AdvancedWebView) webView).setListener(this, webViewCollector);
        webViewCollectors.add(webViewCollector);
        webViewCollector.setOnShouldUpdateViewAdapterCallback(() -> {
            recyclerViewAdapter.notifyItemChanged(0);
        });
        webViewCollector.setViewController(recyclerViewAdapter);
        recyclerViewAdapter.setOnItemClickCallback((Integer position) -> {
            switchToWebView(webViews.get(position));
            setToolbarTitle(webViewCollectors.get(position).getTitle());
            /* NPE can occur from somewhere here */
            setToolbarSubtitle(webViewCollectors.get(position).getUrl().toString());
            toggleTabs(false);
        });
        webViews.add(webView);
        webviewContainer.addView(webView);
        setFocusedWebView(webView);

        ((AdvancedWebView) webView).setListener(this, webViewCollector);

        mobileUserAgent = webView.getSettings().getUserAgentString();
        Log.i(TAG, "mobileUserAgent: "+ mobileUserAgent);
        settingPreferencesKey = getApplicationName(getApplicationContext()) + "_settings";

        Log.i(TAG, "collectSettings().size(): "+ collectSettings().size());
        if (collectSettings().size() == 0) {
            initializeSettings();
        }


        swipeRefresh = findViewById(R.id.swiperefresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefresh.setRefreshing(true);
                try {
                    final WebView webView = (WebView) getWebView();
                    webView.reload();
                } catch (Exception e) {
                    Log.i(TAG, "vituixmän"); // TODO change this text..
                }
                swipeRefresh.setRefreshing(false);
            }
        });

        /* TODO yritä tehdä tästä oma luokka, jonka annat jokaiselle luodulle WebView'lle .. pitäs korjata bugin että URL ei päivity subtitleen. */
//        webView.setWebViewClient(new WebViewClient() {
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                /* if the hostname may not be accessed */
//                if (!firewall.isHostnameAllowed(url)) {
//                    /* if a listener is available */
//                    if (self != null) {
//                        /* inform the listener about the request */
//                        ((AdvancedWebView.Listener) self).onExternalPageRequest(url);
//                    }
//
//                    /* cancel the original request */
//                    return true;
//                }
//
////            /* if there is a user-specified handler available */
////            if (mCustomWebViewClient != null) {
////                // if the user-specified handler asks to override the request
////                if (mCustomWebViewClient.shouldOverrideUrlLoading(view, url)) {
////                    // cancel the original request
////                    return true;
////                }
////            }
//
//                Log.i(TAG, "got url: "+ url);
//
//                // cancel the original request with true
//                return false;
//            }
//
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                super.onPageFinished(view, url);
//                ((MainActivity) self).setToolbarTitle(view.getTitle());
//                ((MainActivity) self).setToolbarSubtitle(url);
//
//                final boolean canGoBack = view.canGoBack();
//                final boolean canGoForward = view.canGoForward();
//                buttonBack.setEnabled(canGoBack);
//                buttonForward.setEnabled(canGoForward);
//            }
//        });
        myWebViewClient = new MyWebViewClient(this, this.firewall);
        webView.setWebViewClient(myWebViewClient);

//        try {
//            for (String h : mPermittedHostnames) {
//                ((AdvancedWebView) webView).addPermittedHostname(h);
//            }
//        } catch (Exception e) {
//            //
//        }
//        webView.getSettings().setJavaScriptEnabled(true);
//        webView.getSettings().setBuiltInZoomControls(false);
//        webView.getSettings().setSupportZoom(false);
//        webView.getSettings().setLoadWithOverviewMode(true);
//        webView.getSettings().setUseWideViewPort(false);
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        webView.loadUrl(url.toString());

//        if (mWebView instanceof View) {
            registerForContextMenu(webView);
//        }

        webView.setLongClickable(true);
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            WebViewLongClickHandler handler = new WebViewLongClickHandler(webView);

            @Override
            public boolean onLongClick(View v) {
                Message msg = handler.obtainMessage();
                Log.i(TAG, "WebView longClick, msg:"+ msg.toString());
                webView.requestFocusNodeHref(msg);
                return true;
            }
        });

        Log.i(TAG, "getLoadWithOverviewMode "+ webView.getSettings().getLoadWithOverviewMode());
        Log.i(TAG, "getUseWideViewPort "+ webView.getSettings().getUseWideViewPort());
        Log.i(TAG, "getBuiltInZoomControls "+ webView.getSettings().getBuiltInZoomControls());
        Log.i(TAG, "getDisplayZoomControls "+ webView.getSettings().getDisplayZoomControls());

//        "getLoadWithOverviewMode " false
//        "getUseWideViewPort " false
//        "getBuiltInZoomControls " false
//        "getDisplayZoomControls " true

//        webView.getSettings().setLoadWithOverviewMode(enabled);   => true
//        webView.getSettings().setUseWideViewPort(enabled);        => true
//        webView.getSettings().setBuiltInZoomControls(enabled);    => true
//        webView.getSettings().setDisplayZoomControls(! enabled);  => false

        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);
    }

    public void saveSettings(Map data) {
        final SharedPreferences pref = getApplicationContext().getSharedPreferences(settingPreferencesKey, MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        Object objectValue = null;
        String value = null;

        Iterator<Object> it = data.keySet().iterator();
        while (it.hasNext()) {
            try {
                String key = (String) it.next();
                Log.i(TAG, "key at saveSettings: "+ key);
                objectValue = data.get(key);

                if (! (objectValue instanceof String)) {
                    value = objectValue.toString();
                }

                editor.putString(key, value);
            }
            catch (ClassCastException e) {
                Log.d(TAG, "at saveSettings(): "+ e.getMessage());
            }
        }

        editor.apply();
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.i(TAG, "at onSupportNavigateUp()");

        if (null == fragment) {
            onBackPressed();
        }
        else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);

            getSupportFragmentManager().beginTransaction()
                .remove(fragment)
                .commit()
            ;
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        return true;
    }

    private Map collectSettings() {
        return getApplicationContext().getSharedPreferences(settingPreferencesKey, Context.MODE_PRIVATE).getAll();
    }

    private Bundle prepareSettings(Map settings) {
        final Bundle settingsBundle = new Bundle();

        final Iterator<String> it = settings.keySet().iterator();
        String key = null;

        while (it.hasNext()) {
            key = it.next();
            try {
                settingsBundle.putString(key, (String) settings.get(key));
            }
            catch (ClassCastException e) {
                /* ignore */
            }
            catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
        }

        return settingsBundle;
    }

    public Bundle prepareSettings() {
        final Map settings = collectSettings();
        final Bundle settingsBundle = new Bundle();

        final Iterator<String> it = settings.keySet().iterator();
        String key = null;

        while (it.hasNext()) {
            key = it.next();
            try {
                settingsBundle.putString(key, (String) settings.get(key));
            }
            catch (ClassCastException e) {
                /* ignore */
            }
            catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
        }

        return settingsBundle;
    }

    public Map getDefaultSettings() {
        final Map settings = new HashMap<String, String>();
        settings.put("userAgent", mobileUserAgent);
        return settings;
    }

    private boolean showingTabs = false;

    protected void toggleTabs(boolean show) {
        showingTabs = show;
        if (showingTabs) {
            findViewById(R.id.mainframe).setVisibility(View.INVISIBLE);
            findViewById(R.id.tabframe).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.tabframe).setVisibility(View.INVISIBLE);
            findViewById(R.id.mainframe).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_tabs:
                toggleTabs(!showingTabs);
                return true;
            case R.id.action_refresh:
                getWebView().reload();
                return true;
            case R.id.action_share_url:
                /* TODO pick string from strings.xml. */
                startActivity(Intent.createChooser(createShareIntent(),"Share using"));
                return true;
            case R.id.action_mode_change:
                /* NOTE these two must use same WebView !
                 * NOTE focusedWebView is not good for tests !
                 */
                setDesktopMode(getWebView(), ! desktopMode);
                getWebView().reload();
                /* TODO check (and test!) that screenshot is taken after page is finished ! */
                return true;
            case R.id.action_settings:
                SettingsFragment frg = new SettingsFragment();

                // Add the fragment to the 'fragment_container' FrameLayout
                getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, frg)
                    .commit()
                ;

                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);

                setFragment(frg);

                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        Log.i(TAG, "at onCreateContextMenu.");

        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);

        final MenuItem item = menu.findItem(R.id.action_share_link);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        setShareIntent(createShareIntent());

        menu.findItem(R.id.action_open_link_in_new_tab).setEnabled(firewall.isHostnameAllowed( focusedUrl ));
    }

    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    private Intent createShareIntent() {
//        final String url = getFocusedUrl() != null ? getFocusedUrl() : focusedWebView.getUrl();
        final String url = focusedWebView.getUrl();
        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);
        return shareIntent;
    }

    protected void createNewWebViewTab(final String url) {
        final WebView webView;
        final int position;
        final WebViewCollector webViewCollector;

        webView = createWebView();
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        webView.setWebViewClient(myWebViewClient);
//        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(url);

        webViews.add(webView);
        position = webViews.size() - 1;

        webViewCollector = new WebViewCollector(webView);
        webViewCollector.setContext(self);
        webViewCollector.setViewController(recyclerViewAdapter);
        ((AdvancedWebView) webView).setListener(this, webViewCollector);
        webViewCollector.setOnShouldUpdateViewAdapterCallback(() -> recyclerViewAdapter.notifyItemChanged(position));

        recyclerViewAdapter.setOnItemClickCallback((Integer itemPosition) -> {
            switchToWebView(webViews.get(itemPosition));
            Log.i(TAG, "setting title");
            setToolbarTitle(webViewCollectors.get(itemPosition).getTitle());
            /* TODO NPE from somewhere here? */
            Log.i(TAG, "setting subtitle");
            setToolbarSubtitle(webViewCollectors.get(itemPosition).getUrl().toString());
            toggleTabs(false);
        });

        webViewCollectors.add(webViewCollector);

        webviewContainer.addView(webView);
        recyclerViewAdapter.notifyItemInserted( position );
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Log.i(TAG, "at onContextItemSelected.");

        switch (item.getItemId()) {
            case R.id.action_copy_url_to_clipboard:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("focusedUrl", getWebView().getUrl());
                clipboard.setPrimaryClip(clip);
                return true;

            case R.id.action_share_link:
                startActivity(Intent.createChooser(createShareIntent(),"Share using"));
                return true;

            case R.id.action_open_link_in_new_tab:
                createNewWebViewTab(focusedUrl);
                switchToLatestWebViewTab();
                toggleTabs(false);
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        /* call onPause on all WebViews ? */
        focusedWebView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        /* should call onDestroy on all WebViews */
        try {
            ((AdvancedWebView) focusedWebView).onDestroy();
        } catch (ClassCastException cce) {
        }
        super.onDestroy();
    }

    protected void setToolbarTitle(String title) {
        ((TextView) findViewById(R.id.main_toolbar_title)).setText(title);
    }

    protected void setToolbarSubtitle(String subtitle) {
        ((TextView) findViewById(R.id.main_toolbar_subtitle)).setText(subtitle);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        ((AdvancedWebView) focusedWebView).onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onBackPressed() {
        if (!((AdvancedWebView) focusedWebView).onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

//    @Override
//    public void onPageStarted(String url, Bitmap favicon) {
//        // pass url to WebViewCollector
//    }

//    @Override
//    public void onPageError(int errorCode, String description, String failingUrl) {
//        Log.i(TAG, "MainActivity.onPageError(): "+ String.valueOf(errorCode));
//        Log.i(TAG, "MainActivity.onPageError(): "+ description);
//    }
//
//    @Override
//    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) { }

    public void onPageFinished(final WebView webView, final String url) {
        self.setToolbarTitle(webView.getTitle());
        self.setToolbarSubtitle(url);

        final boolean canGoBack = webView.canGoBack();
        final boolean canGoForward = webView.canGoForward();
        buttonBack.setEnabled(canGoBack);
        buttonForward.setEnabled(canGoForward);
    }

    public void onForbiddenHostnameRequest(final WebView webView, final String url) {
        Toast.makeText(self, R.string.link_contains_an_unallowed_url, Toast.LENGTH_SHORT).show();
    }

//    @Override
    public void onExternalPageRequest(String url) {
        Log.i(TAG, "at onExternalPageRequest, url: "+ url);
        if (! firewall.isHostnameAllowed(url)) {
            Toast.makeText(self, R.string.link_contains_an_unallowed_url, Toast.LENGTH_SHORT).show();
        }
    }

//    public boolean shouldOverrideUrlLoading(WebView view, String url) {
//        /* if the hostname may not be accessed */
//        if (! firewall.isHostnameAllowed(url)) {
//            /* cancel the original request */
//            return true;
//        }
//
////            /* if there is a user-specified handler available */
////            if (mCustomWebViewClient != null) {
////                // if the user-specified handler asks to override the request
////                if (mCustomWebViewClient.shouldOverrideUrlLoading(view, url)) {
////                    // cancel the original request
////                    return true;
////                }
////            }
//
//        Log.i(TAG, "got url: "+ url);
//
//        // cancel the original request with true
//        return false;
//    }
}
