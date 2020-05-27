package fi.casa.webapp;


import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.function.Consumer;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static fi.casa.webapp.MainActivity.TAG;
import static fi.casa.webapp.ViewMatchers.isRefreshing;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestsWithMockedWebView {
    private WebView ewv = null;

//    @Rule
//    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    private ActivityScenario<MainActivity> scenario = null;

    private Integer reloadCount = -1;

    @Before
    public void setUp() {
        reloadCount = -1;
    }

    protected void doPutMockWebViewIntoPlace(Activity activity) {
        // FrameLayout - SwipeRefresh - WebView
        ViewGroup content = activity.findViewById(R.id.content);

        final MockWebView nwv = (MockWebView) activity.getLayoutInflater().inflate(R.layout.shadowwebview, null);

        ((CasaWebAppApplication) activity.getApplication()).setWebView(nwv);

        final SwipeRefreshLayout esrl = (SwipeRefreshLayout) content.getChildAt(0);
//        ewv = (WebView) esrl.findViewById(R.id.webview);
        final ViewGroup wvc = esrl.findViewById(R.id.webview_container);
        ewv = (WebView) wvc.getChildAt(0);

        esrl.removeView(ewv);
        esrl.addView(nwv);

//        nwv.setId(R.id.webview);
    }

    private void putMockWebViewIntoPlace() {
        ActivityScenario.launch(MainActivity.class).onActivity(activity -> {
            doPutMockWebViewIntoPlace(activity);
        });
    }

    protected void openMainMenu() {
        /* Q: is this enough? */
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }

    protected void assertWebViewIsDisplayed() {
        onView(allOf(withId(R.id.webview_container), withChild(instanceOf(WebView.class)))).check(matches(isDisplayed()));
    }

    protected void isAtMainView() {
        onView(withId(R.id.root_layout)).check(matches(isDisplayed()));
    }

    /**
     * assert web view shows up.
     */
    @Test
//    @Ignore
    public void webViewIsDisplayed() {
        ActivityScenario.launch(MainActivity.class);
        assertWebViewIsDisplayed();
    }

    /**
     * assert the webview reload method is called from the action in toolbar menu.
     */
    @Test
    public void reloadPageFromToolbarMenu() {
        putMockWebViewIntoPlace();

        isAtMainView();

        try {
            openMainMenu();
        } catch (Exception e) {
            // ok
        }

//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        try {
            onView(withText("Refresh")).perform(click());
            Assert.fail();
        } catch (RuntimeException e) {
            Log.i(TAG, "Got RuntimeException as expected: "+ e.getMessage());
        }
    }

    private void doUpdateReloadCount(MainActivity activity) {
        WebView webView = activity.getWebView();
        MockWebView mockWebView = (MockWebView) webView;
        reloadCount = mockWebView.getReloadCount();
    }

    private Activity mainActivity = null;
    private void updateReloadCount() {
        ActivityScenario.launch(MainActivity.class).onActivity(activity -> {
            mainActivity = activity;
            doUpdateReloadCount(activity);
        });
    }

    Consumer<MainActivity> consumePutMockWebViewIntoPlace = (MainActivity activity) -> doPutMockWebViewIntoPlace(activity);
    Consumer<MainActivity> consumeUpdateReloadCount = (MainActivity activity) -> doUpdateReloadCount(activity);

    static void forEach(Consumer[] consumers, MainActivity activity) {
        for (Consumer c : consumers) {
            c.accept(activity);
        }
    }

    protected void runOnActivity(Consumer[] consumers) {
        ActivityScenario.launch(MainActivity.class).onActivity(activity -> {
            mainActivity = activity;
            forEach(consumers, activity);
        });
    }

    /**
     * assert webview reload is called when page is swiped downwards
     */
    @Test
    public void reloadPageBySwipe() {
        runOnActivity(new Consumer[]{consumePutMockWebViewIntoPlace, consumeUpdateReloadCount});
        Integer before = new Integer(reloadCount);

        isAtMainView();
        /* commented because MockWebView is not visible after having been put in place of AdvancedWebView.
         * NOTE DO NOT move this into another file just because of this.
         */
//        assertWebViewIsDisplayed();

        onView(withId(R.id.swiperefresh)).perform(ViewActions.swipeDown());
        onView(withId(R.id.swiperefresh)).check(matches(isRefreshing()));

        /* would it be possible to catch the Exception thrown from a listener callback ? */
//        updateReloadCount();
        doUpdateReloadCount((MainActivity) mainActivity);

        Integer after = new Integer(reloadCount);
        Assert.assertEquals(before.longValue() + 1, after.longValue());
    }
}
