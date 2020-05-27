package fi.casa.webapp;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MyRecyclerViewAdapter
    extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>
    implements MyRecyclerViewItemClickListener
{
    private List<WebViewCollector> dataSource;
    private LayoutInflater mInflater;
    private MyRecyclerViewItemClickListener mClickListener;
    private Consumer<Integer> onItemClickCallback;
    private int columnCount;
    private float widthToHeightRatio = 4.0f / 3.0f;
    private float borderWidthInDp = 2.0f;
    private float borderWidthInPx;
    private float paddingInDp = 5.0f;
    private float paddingInPx;
    private GridLayoutManager.LayoutParams tabLayoutParams;
    private ViewGroup.MarginLayoutParams thumbnailLayoutParams;

    MyRecyclerViewAdapter(Context context, List<WebViewCollector> data) {
        this.mInflater = LayoutInflater.from(context);
        this.dataSource = data;
        setClickListener(this);
        paddingInPx = convertDpToPixel(paddingInDp, context);
        borderWidthInPx = convertDpToPixel(borderWidthInDp, context);
    }

    static void removeItemFromCollection(Collection col, Integer position) {
        final Iterator it = col.iterator();
        Integer p = -1;

        do {
            it.next();
            ++ p;
        } while (p < position);

        it.remove();
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    /* https://stackoverflow.com/questions/4605527/converting-pixels-to-dp */
    public static float convertDpToPixel(float dp, Context context) {
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    protected float calculateWidthForAnItem(final ViewGroup layout) {
        return calculateWidthForAnItem((View) layout);
    }

    protected float calculateWidthForAnItem(final View view) {
        return calculateWidthForAnItem(view.getWidth(), null);
    }

    protected float calculateWidthForAnItem(final int fullWidth, final ViewGroup.MarginLayoutParams layoutParams) {
        final float paddingTotal = (columnCount * 2) * paddingInPx;
        final float widthForItems = fullWidth - paddingTotal;
        final float widthForAnItem = widthForItems / columnCount - layoutParams.leftMargin - layoutParams.rightMargin;
        return widthForAnItem;
    }

    // inflates the cell layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View tab;
        final View thumbnail;
        final float fHeight;

        tab = mInflater.inflate(R.layout.tab_thumbnail, parent, false);
        thumbnail = tab.findViewById(R.id.tab_thumbnail);
        thumbnailLayoutParams = (FrameLayout.LayoutParams) thumbnail.getLayoutParams();

        tabLayoutParams = (GridLayoutManager.LayoutParams) tab.getLayoutParams();

        tabLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        tabLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;

        thumbnailLayoutParams.width = (int) calculateWidthForAnItem(
            ((MainActivity) tab.getContext()).getScreenWidth(),
            thumbnailLayoutParams
        );
        fHeight = ( (float) thumbnailLayoutParams.width) / widthToHeightRatio;
        thumbnailLayoutParams.height = (int) fHeight;

        tabLayoutParams.topMargin = (int) paddingInPx;
        tabLayoutParams.rightMargin = (int) paddingInPx;
        tabLayoutParams.leftMargin = (int) paddingInPx;
        tabLayoutParams.bottomMargin = (int) paddingInPx;
        tab.setLayoutParams(tabLayoutParams);

        ViewHolder vh = new ViewHolder(tab);
        return vh;
    }

    public int getDesiredThumbnailWidth() {
        return thumbnailLayoutParams.width;
    }

    public int getDesiredThumbnailHeight() {
        return thumbnailLayoutParams.height;
    }

//    protected BitmapDrawable getScaledScreenshot(final BitmapDrawable originalDrawable, final Resources resources) {
//        final Bitmap originalBitmap;
//        final Bitmap scaledBitmap;
//        final BitmapDrawable scaledDrawable;
//
//        final int width, height, newWidth, newHeight;
//
//        originalBitmap = originalDrawable.getBitmap();
//        width = originalBitmap.getWidth();
//        height = originalBitmap.getHeight();
//
//        if (width > height) {
//            newWidth = height;
//            newHeight = height;
//        } else {
//            newHeight = width;
//            newWidth = width;
//        }
//
//        scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
//        scaledDrawable = new BitmapDrawable(resources, scaledBitmap);
//
//        return scaledDrawable;
//    }

    // binds the data to the Button in each cell
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final WebViewCollector wvc = dataSource.get(position);
        ((AppCompatButton) holder.tabTitle).setText(wvc.getTitle());

        final BitmapDrawable screenshot = wvc.getScreenshot();
        if (screenshot != null) {
            ((AppCompatButton) holder.thumbnail).setBackgroundDrawable(screenshot);
        }
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ViewHolder self = this;
        private final View tabTitle;
        private final Button buttonCloseTab;
        private final View thumbnail;

        ViewHolder(View itemView) {
            super(itemView);

            tabTitle = itemView.findViewById(R.id.tab_title);
            tabTitle.setOnClickListener(this);

            buttonCloseTab = itemView.findViewById(R.id.close_tab);
            buttonCloseTab.setOnClickListener(v -> {
                final Integer position = getAdapterPosition();

                removeItemFromCollection(dataSource, position);

                notifyItemRemoved(position);
            });

            thumbnail = itemView.findViewById(R.id.tab_thumbnail);
            thumbnail.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return dataSource.get(id).getTitle();
    }

    // allows clicks events to be caught
    void setClickListener(MyRecyclerViewItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public void setOnItemClickCallback(Consumer<Integer> callback) {
        onItemClickCallback = callback;
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.i("TAG", "You clicked number " + getItem(position) + ", which is at cell position " + position);
        if (onItemClickCallback != null) {
//            onItemClickCallback.run();
            onItemClickCallback.accept(position);
        }
    }
}
