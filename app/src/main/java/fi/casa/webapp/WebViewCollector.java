package fi.casa.webapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.Log;
import android.webkit.WebView;

import java.net.MalformedURLException;
import java.net.URL;

import androidx.annotation.RequiresApi;
import im.delight.android.webview.AdvancedWebView;

import static fi.casa.webapp.MainActivity.TAG;

public class WebViewCollector
    implements AdvancedWebView.Listener
{
    private Context context;
    private MyRecyclerViewAdapter viewController;
    private WebView webView;
    private Runnable onShouldUpdateViewAdapterCallback;
//    private Consumer onShouldUpdateViewAdapterCallback;
    private String title;
    private URL url;
    private BitmapDrawable screenshot;

    public WebViewCollector(WebView webView) {
        this.webView = webView;
    }

    public void setContext(final Context context) {
        this.context = context;
    }

    public WebView getWebView() {
        return this.webView;
    }

    public URL getUrl() {
        return this.url;
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public BitmapDrawable getScreenshot() {
        return screenshot;
    }

    public void setViewController(final MyRecyclerViewAdapter vc) {
        viewController = vc;
    }

    /* TODO alternatively, take screenshot when user goes to tab view */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onPageFinished(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        setTitle(webView.getTitle());

        final ScreenshotTask screenshotTask = new ScreenshotTask();
        screenshotTask.setWebView(webView);

        screenshotTask.setOnCaptureDoneCallback(new Runnable() {
            @Override
            public void run() {
                final int width;
                final int height;
                width = viewController.getDesiredThumbnailWidth();
                height = viewController.getDesiredThumbnailHeight();
                ScreenshotManager.crop(webView, width, height);
            }
        });
        screenshotTask.setOnCropDoneCallback(new Runnable() {
            @Override
            public void run() {
                screenshot = screenshotTask.getModifiedImage();
                if (onShouldUpdateViewAdapterCallback != null) {
                    onShouldUpdateViewAdapterCallback.run();
                }
            }
        });

        ScreenshotManager.startCapture(screenshotTask);

        if (onShouldUpdateViewAdapterCallback != null) {
//            new Thread(onShouldUpdateViewAdapterCallback).start();
            onShouldUpdateViewAdapterCallback.run();
        }
        /* TODO kokeile myöhemmin uudelleen */
//        ((Consumer) onShouldUpdateViewAdapterCallback).accept(null);
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {
        // TODO check MainActivity
    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {
    }

    @Override
    public void onExternalPageRequest(String url) {
        // TODO check MainActivity
        Log.i(TAG, "link clicked");
        if (context != null) {
            ((MainActivity) context).onExternalPageRequest(url);
        }
    }

//    public void setOnShouldUpdateViewAdapterCallback(Consumer<Integer> c) {
////        Consumer<Integer> c = _position -> recyclerViewAdapter.notifyItemChanged(_position);
//        this.onShouldUpdateViewAdapterCallback = c;
//    }

    public void setOnShouldUpdateViewAdapterCallback(Runnable r) {
        this.onShouldUpdateViewAdapterCallback = r;
    }
}
