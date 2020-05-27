package fi.casa.webapp;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import androidx.test.espresso.matcher.BoundedMatcher;

public class ViewMatchers {
    public static Matcher<View> isRefreshing() {
        return new BoundedMatcher<View, SwipeRefreshLayout>(SwipeRefreshLayout.class) {

            @Override
            public void describeTo(Description description) {
                description.appendText("is refreshing");
            }

            @Override
            protected boolean matchesSafely(SwipeRefreshLayout item) {
                return item.isRefreshing();
            }
        };
    }

    public static Matcher<View> nthChildOf(final Matcher<View> parentMatcher, final int childPosition) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("with "+childPosition+" child view of type parentMatcher");
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!(view.getParent() instanceof ViewGroup)) {
                    return parentMatcher.matches(view.getParent());
                }

                ViewGroup group = (ViewGroup) view.getParent();
                return parentMatcher.matches(view.getParent()) && group.getChildAt(childPosition).equals(view);
            }
        };
    }
}
