package fi.casa.webapp;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;

import androidx.appcompat.widget.AppCompatButton;

public class TabButton extends AppCompatButton {
    public final GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(new FrameLayout.LayoutParams(
            GridLayout.LayoutParams.WRAP_CONTENT,
            GridLayout.LayoutParams.WRAP_CONTENT
    ));

    public TabButton(Context context) {
        super(context);

//            tabButton.set column weight ??
        setGravity(Gravity.FILL);
    }

    public TabButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setGravity(Gravity.FILL);
    }

    public TabButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setGravity(Gravity.FILL);
    }
}
