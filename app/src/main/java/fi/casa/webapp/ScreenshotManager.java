package fi.casa.webapp;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.webkit.WebView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;

public class ScreenshotManager {
    private static final ScreenshotManager sInstance;
    private final Handler handler;
    private static final ExecutorService executor;
    private static ScreenshotTask screenshotTask;

    protected final static int STATE_INITIAL = 0;
    protected final static int STATE_CAPTURE_STARTED = 1;
    protected final static int STATE_CAPTURE_COMPLETE = 2;
    protected final static int STATE_CROP_STARTED = 3;
    protected final static int STATE_CROP_COMPLETED = 4;
    protected final static int STATE_CROP_FAILED = 5;

    static {
        sInstance = new ScreenshotManager();
        executor = Executors.newSingleThreadExecutor();
    }

    private ScreenshotManager() {
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                ScreenshotTask task = (ScreenshotTask) msg.obj;
                // get WebViewCollector from task and set the cropped image to it.

                switch (msg.what) {
                    case STATE_INITIAL:
                        break;
                    case STATE_CAPTURE_COMPLETE:
                        task.runCaptureDoneCallback();
                        break;
                    case STATE_CROP_COMPLETED:
                        task.runCropDoneCallback();
                        break;
                    default:
                        super.handleMessage(msg);
                        break;
                }
            }
        };
    }

    public static ScreenshotManager getInstance() {
        return sInstance;
    }

    public void handleState(final ScreenshotTask screenshotTask, final int state) {
        final Message completeMessage;

        switch (state) {
            case STATE_CAPTURE_COMPLETE:
//                executor.execute(screenshotTask.getCropImageRunnable());
            case STATE_CROP_COMPLETED:
                completeMessage = handler.obtainMessage(state, screenshotTask);
                completeMessage.sendToTarget();
            default:
                break;
        }
    }

    public static ScreenshotTask startCapture(final ScreenshotTask task) {
        screenshotTask = task;

        if (task.hasImage()) {
            sInstance.handleState(task, STATE_CAPTURE_COMPLETE);
        } else {
            executor.execute(task.getCaptureScreenshotRunnable());
        }

        return task;
    }

    public static ScreenshotTask startCapture(final WebView webView) {
        final ScreenshotTask task;

//        try {
//            task = sInstance.screenshot task queue.poll();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//        if (task == null) {
            task = new ScreenshotTask();
            task.setWebView(webView);
//        }
        screenshotTask = task;

        if (task.hasImage()) {
            sInstance.handleState(task, STATE_CAPTURE_COMPLETE);
        } else {
//            sInstance.capture thread pool.execute(task.getWebView());
            executor.execute(task.getCaptureScreenshotRunnable());
        }

        return task;
    }

    public static ScreenshotTask crop(final WebView webView, final int width, final int height) {
        final ScreenshotTask task;
        task = screenshotTask;

        executor.execute(task.getCropImageRunnable(width, height));

        return task;
    }
}
