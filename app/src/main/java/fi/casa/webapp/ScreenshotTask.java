package fi.casa.webapp;

import android.graphics.drawable.BitmapDrawable;
import android.webkit.WebView;

public class ScreenshotTask {
    private ScreenshotManager screenshotManager;
    private CaptureScreenshotRunnable captureRunnable;
    private CropImageRunnable cropRunnable;
    private WebView webView;
    private Thread thread;
    private BitmapDrawable originalImage;
    private BitmapDrawable modifiedImage;
    private Runnable onCaptureDoneCallback;
    private Runnable onCropDoneCallback;

    public ScreenshotTask() {
        screenshotManager = ScreenshotManager.getInstance();
        captureRunnable = new CaptureScreenshotRunnable(this);
        cropRunnable = new CropImageRunnable(this);
    }

    public void setThread(Thread t) {
        this.thread = t;
    }

    public void setOriginalImage(BitmapDrawable originalImage) {
        this.originalImage = originalImage;
    }

    public boolean hasImage() {
        return this.originalImage != null;
    }

    public BitmapDrawable getOriginalImage() {
        return originalImage;
    }

    public BitmapDrawable getModifiedImage() {
        return modifiedImage;
    }

    public void setModifiedImage(BitmapDrawable modifiedImage) {
        this.modifiedImage = modifiedImage;
    }

    public void setWebView(WebView webView) {
        this.webView = webView;
    }

    public WebView getWebView() {
        return this.webView;
    }

    public CaptureScreenshotRunnable getCaptureScreenshotRunnable() {
        return captureRunnable;
    }

    public CropImageRunnable getCropImageRunnable(final int width, final int height) {
        cropRunnable.setWidth(width);
        cropRunnable.setHeight(height);
        return cropRunnable;
    }

    public void setOnCaptureDoneCallback(Runnable r) {
        onCaptureDoneCallback = r;
    }

    public void setOnCropDoneCallback(Runnable r) {
        onCropDoneCallback = r;
    }

    public void runCaptureDoneCallback() {
        onCaptureDoneCallback.run();
    }

    public void runCropDoneCallback() {
        onCropDoneCallback.run();
    }

    public void handleCaptureState(final int state) {
        final int outState;

        // Converts the decode state to the overall state.
        switch(state) {
            case ScreenshotManager.STATE_CAPTURE_STARTED:
                outState = ScreenshotManager.STATE_CAPTURE_STARTED;
                break;
            case ScreenshotManager.STATE_CAPTURE_COMPLETE:
                outState = ScreenshotManager.STATE_CAPTURE_COMPLETE;
                break;
            default:
                outState = ScreenshotManager.STATE_INITIAL;
                break;
        }

        // Calls the generalized state method
        handleState(outState);
    }

    public void handleCropState(final int state) {
        final int outState;

        // Converts the decode state to the overall state.
        switch(state) {
            case ScreenshotManager.STATE_CAPTURE_STARTED:
                outState = ScreenshotManager.STATE_CAPTURE_STARTED;
                break;
            case ScreenshotManager.STATE_CAPTURE_COMPLETE:
            case ScreenshotManager.STATE_CROP_FAILED:
                outState = ScreenshotManager.STATE_CAPTURE_COMPLETE;
                break;
            case ScreenshotManager.STATE_CROP_STARTED:
                outState = ScreenshotManager.STATE_CROP_STARTED;
                break;
            case ScreenshotManager.STATE_CROP_COMPLETED:
                outState = ScreenshotManager.STATE_CROP_COMPLETED;
                break;
            default:
                outState = ScreenshotManager.STATE_INITIAL;
                break;
        }

        // Calls the generalized state method
        handleState(outState);
    }

    // Passes the state to PhotoManager
    public void handleState(final int state) {
        /*
         * Passes a handle to this task and the
         * current state to the class that created
         * the thread pools
         */
        screenshotManager.handleState(this, state);
    }
}
