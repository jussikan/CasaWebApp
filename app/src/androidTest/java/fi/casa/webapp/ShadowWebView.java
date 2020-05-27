package fi.casa.webapp;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import static fi.casa.webapp.MainActivity.TAG;

public class ShadowWebView extends WebView {
    Integer reloadCount = 0;

    public ShadowWebView(Context context) {
        super(context);

//        this.loadUrl("https://www.facebook.com");

        super.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                ++ reloadCount;
                Log.i(TAG, "Entered onPageFinished");
//                            throw new RuntimeException("Entered onPageFinished()");
            }
        });
    }

    public ShadowWebView(Context context, AttributeSet a) {
        super(context, a);

//        this.loadUrl("https://www.facebook.com");

        super.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                ++ reloadCount;
                Log.i(TAG, "Entered onPageFinished");
//                            throw new RuntimeException("Entered onPageFinished()");
            }
        });
    }

    public Integer getReloadCount() {
        return reloadCount;
    }

//        @Override
//        protected void onLayout(boolean changed, int l, int t, int r, int b) {
//            super.onLayout(changed, l, t, r, b);
//            Integer h = this.getHeight(); //height is ready
//            Log.i(TAG, h.toString());
//        }
//
//        @Override
//        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        }

    @Override
    public void reload() {
        Log.i(TAG, "Entered reload()");
//        throw new RuntimeException("Entered reload()");
    }

//        @Override
//        public void onLayout(boolean changed,
//                             int left,
//                             int top,
//                             int right,
//                             int bottom)
//        {
//            Log.i(TAG, "ShadowWebView onLayout");
//        }

//                @Override
//                protected void onCreate(Bundle savedInstanceState) {
//                    super.onCreate(savedInstanceState);
//                    setContentView(R.layout.activity_main);
//
//
////                    mWebView = ((CasaWebAppApplication) getApplicationContext()).getWebView();
////
////                    if (mWebView == null) {
////                        mWebView = (AdvancedWebView) findViewById(R.id.webview);
////                    }
//
////                    try {
////                        ((AdvancedWebView) mWebView).setListener(this, this);
////                    } catch (Exception e) {
////                        //
////                    }
//
//
//                    swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
//                    swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//                        @Override
//                        public void onRefresh() {
//                            swipeRefresh.setRefreshing(true);
//                            mWebView.reload();
//                            swipeRefresh.setRefreshing(false);
//                        }
//                    });
//                }
}