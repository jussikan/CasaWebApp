package fi.casa.webapp;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.webkit.WebView;

public class CaptureScreenshotRunnable implements Runnable {
    private ScreenshotTask screenshotTask;

    CaptureScreenshotRunnable(ScreenshotTask screenshotTask) {
        this.screenshotTask = screenshotTask;
    }

    public void run() {
        final Bitmap cachedBitmap;
        final Canvas cachedCanvas;
        final BitmapDrawable screenshot;
        final WebView webView;
        final boolean wasDrawingCacheEnabled;

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        screenshotTask.setThread(Thread.currentThread());

        screenshotTask.handleCaptureState(ScreenshotManager.STATE_CAPTURE_STARTED);

        webView = screenshotTask.getWebView();
        /* TODO kokeilepa laittaa WebView'tä alustaessa pois päältä, vaikuttaako suoritukseen tai tehoon */
        /* TODO setDrawingCacheEnabled is deprecated in API 11 */
//        wasDrawingCacheEnabled = webView.isDrawingCacheEnabled();
//        webView.setDrawingCacheEnabled(true);
//        cachedBitmap = Bitmap.createBitmap(webView.getDrawingCache());
//        webView.setDrawingCacheEnabled(wasDrawingCacheEnabled);

        cachedBitmap = Bitmap.createBitmap(webView.getWidth(), webView.getHeight(), Bitmap.Config.ARGB_8888);

        cachedCanvas = new Canvas(cachedBitmap);

        ((MainActivity) webView.getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.draw(cachedCanvas);

                final BitmapDrawable screenshot = new BitmapDrawable(webView.getResources(), cachedBitmap);
//        cachedBitmap.recycle(); // ei vissii kannata

                screenshotTask.setOriginalImage(screenshot);

                screenshotTask.handleCaptureState(ScreenshotManager.STATE_CAPTURE_COMPLETE);
            }
        });
//        webView.draw(cachedCanvas);

//        screenshot = new BitmapDrawable(webView.getResources(), cachedBitmap);
////        cachedBitmap.recycle(); // ei vissii kannata
//
//        screenshotTask.setOriginalImage(screenshot);
//
//        screenshotTask.handleCaptureState(ScreenshotManager.STATE_CAPTURE_COMPLETE);
    }
}
