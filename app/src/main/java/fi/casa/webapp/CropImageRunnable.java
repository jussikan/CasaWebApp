package fi.casa.webapp;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

public class CropImageRunnable implements Runnable {
    private ScreenshotTask screenshotTask;
    private int width;
    private int height;

    CropImageRunnable(ScreenshotTask screenshotTask) {
        this.screenshotTask = screenshotTask;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public void run() {
        final BitmapDrawable originalDrawable;
        final Bitmap originalBitmap;
        final Bitmap croppedBitmap;
        final BitmapDrawable croppedDrawable;
        final Resources resources;

        final int width, height;
        final int newWidth, newHeight;
        final int pixelArraySize;
        final int[] pixels;
        final float desiredRatio;

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        screenshotTask.setThread(Thread.currentThread());

        screenshotTask.handleCropState(ScreenshotManager.STATE_CROP_STARTED);

        desiredRatio = ((float) this.width) / ((float) this.height);
        resources = screenshotTask.getWebView().getResources();
        originalDrawable = screenshotTask.getOriginalImage();

        try {
            originalBitmap = originalDrawable.getBitmap();
        } catch (NullPointerException npe) {
            Log.i(getClass().getName(), "crop failed: couldn't get original bitmap");
            screenshotTask.handleCropState(ScreenshotManager.STATE_CROP_FAILED);
            return;
        }

        width = originalBitmap.getWidth();
        height = originalBitmap.getHeight();

        newHeight = (int) (((float) width) / desiredRatio);
        newWidth = width;

        pixelArraySize = width * height;
        pixels = new int[pixelArraySize];

        originalBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        croppedBitmap = Bitmap.createBitmap(newWidth, newHeight, originalBitmap.getConfig());
        croppedBitmap.setPixels(pixels, 0, width, 0, 0, newWidth, newHeight);

        croppedDrawable = new BitmapDrawable(resources, croppedBitmap);
        screenshotTask.setModifiedImage(croppedDrawable);

        screenshotTask.handleCropState(ScreenshotManager.STATE_CROP_COMPLETED);
    }
}
