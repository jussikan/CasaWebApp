package fi.casa.webapp;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import fi.casa.webapp.tools.DrawableMatcher;
import fi.casa.webapp.tools.RecyclerViewMatcher;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.actionWithAssertions;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static fi.casa.webapp.MainActivity.TAG;
import static fi.casa.webapp.tools.EspressoTestsMatchers.withDrawable;
import static fi.casa.webapp.tools.ViewMatchers.nthChildOf;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TabTests {
    String url;

    private Map<String, String> parameters = new HashMap<String, String>();

    private void parseParameterLineAndStore(String line) {
        String[] parts = line.split("=", 2);
        parameters.put(parts[0], parts[1]);
    }

    private void loadParameters() throws IOException {
        String filename = "parameters.txt";
        BufferedReader br = new BufferedReader(new InputStreamReader(
                getInstrumentation().getContext().getAssets().open(filename)
        ));

        String line = null;
        do {
            try {
                line = br.readLine();
                if (line != null) {
                    parseParameterLineAndStore(line);
                }
            } catch (IOException e) {
                break;
            }
        } while (line != null);
        Log.i(TAG, "parametrit loppu");
    }

//    private static class CustomFailureHandler implements FailureHandler {
//        private final FailureHandler delegate;
//
//        public CustomFailureHandler(Context targetContext) {
//            delegate = new DefaultFailureHandler(targetContext);
//        }
//
//        @Override
//        public void handle(Throwable error, Matcher<View> viewMatcher) {
//            try {
//                delegate.handle(error, viewMatcher);
//            } catch (NoMatchingViewException e) {
//                throw new MySpecialException(e);
//            }
//        }
//    }

    protected String getUrl() {
        return parameters.get("url");
    }

    @Before
    public void setUp() {
        CasaWebAppApplication app = (CasaWebAppApplication) getInstrumentation().getTargetContext().getApplicationContext();

        try {
            loadParameters();
        } catch (IOException e) {
            e.printStackTrace();
        }

        url = getUrl();

        app.setUrl(url);
    }

    private ActivityScenario<MainActivity> scenario = null;

    @After
    public void after() {
        scenario.close();
    }

    protected void openMainMenu() {
        /* Q: is this enough? */
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
    }

    protected void assertIsAtMainView() {
        onView(withId(R.id.root_layout)).check(matches(isDisplayed()));
    }

    protected void assertIsAtTabsView() {
        onView(withId(R.id.tabframe)).check(matches(isDisplayed()));
    }

    protected void assertWebViewIsDisplayed() {
//        onView(withId(R.id.webview_container)).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.webview_container), withChild(instanceOf(WebView.class)))).check(matches(isDisplayed()));
    }

    /**
     * assert web view shows up.
     */
    @Test
    public void webViewIsVisible() {
        scenario = ActivityScenario.launch(MainActivity.class);
        assertIsAtMainView();
        assertWebViewIsDisplayed();
    }

    /**
     * a.k.a. toolbar menu
     */
    @Test
    public void mainMenuIsOpened() {
        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();
        openMainMenu();
    }

    @Test
    public void tabExistsEvenForSinglePage() {
        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();

        onView(withId(R.id.buttonTabs)).perform(click());
        assertIsAtTabsView();

        assertTabIsVisibleAtPosition(0);
    }

    protected Matcher<View> getTab(final int index) {
        return allOf(nthChildOf(withId(R.id.tab_grid), index), withId(R.id.tab));
    }

    protected ViewInteraction onTab(final int index) {
        return onView(allOf(nthChildOf(withId(R.id.tab_grid), index), withId(R.id.tab)));
    }

    protected void assertTabIsVisibleAtPosition(final int index) {
        onView(nthChildOf(withId(R.id.tab_grid), index)).check(matches(isDisplayed()));
        onView(allOf(nthChildOf(withId(R.id.tab_grid), index), withId(R.id.tab))).check(matches(isDisplayed()));
    }

    @Test
    public void tabCanBeAdded() {
        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();

        onView(withId(R.id.buttonTabs)).perform(click());
        assertIsAtTabsView();
        assertTabIsVisibleAtPosition(0);
        try {
            assertTabIsVisibleAtPosition(1);
        } catch (NullPointerException npe) {
            //
        }

        onView(withId(R.id.addTab)).perform(click());
        onView(withId(R.id.buttonTabs)).perform(click());
        assertTabIsVisibleAtPosition(1);
    }

    @Test
    public void tabCanBeClosed() {
        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();

        onView(withId(R.id.buttonTabs)).perform(click());
        assertIsAtTabsView();
        assertTabIsVisibleAtPosition(0);

        onView(withId(R.id.close_tab)).perform(click());

        try {
            onView(allOf(nthChildOf(withId(R.id.tab_grid), 0), withId(R.id.tab))).check(matches(not(isDisplayed())));;
            Assert.assertTrue(false);
        } catch (Exception e) {
            Log.i(getClass().getName(), "OK");
            /* should come here ! */
        }
    }

    @Test
    public void pressingLinkOnSecondTabDoesNotAffectFirstTab() throws MalformedURLException {
        final CasaWebAppApplication app = getApplication();
        final URL paramUrl = new URL(url);
        final URL testUrl = new URL(paramUrl.toString() +"dynamic?alink[]=1,0&alink[]=1,20&flink[]=1,40");
        app.setUrl(testUrl.toString());

        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.main_toolbar_subtitle)).check(matches(withText(testUrl.toString())));

        onView(withId(R.id.buttonTabs)).perform(click());
        assertIsAtTabsView();

        onView(withId(R.id.addTab)).perform(click());

        onView(withId(R.id.main_toolbar_subtitle)).check(matches(withText(testUrl.toString())));

        onMyWebView()
            .perform(actionWithAssertions(
                new GeneralClickAction(
                    Tap.SINGLE,
                    MockPageLinkPosition.ALINK1,
                    Press.FINGER,
                    InputDevice.SOURCE_UNKNOWN,
                    MotionEvent.BUTTON_PRIMARY)
            ));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.main_toolbar_subtitle)).check(matches(not(withText(testUrl.toString()))));

        onView(withId(R.id.buttonTabs)).perform(click());
        assertIsAtTabsView();

        onView(allOf(nthChildOf(withId(R.id.tab_grid), 0), withId(R.id.tab))).perform(click());
        onView(withId(R.id.main_toolbar_subtitle)).check(matches(withText(testUrl.toString())));
    }

    /**
     * assert web page looks certain way
     * open toolbar menu
     * press the switch action
     * assert web page looks different
     */
    @Test
    public void switchingBetweenTabsChangesToolbarTitles() throws MalformedURLException {
        final CasaWebAppApplication app = getApplication();
        final URL paramUrl = new URL(url);
        final URL testUrl = new URL(paramUrl.toString() +"dynamic?alink[]=1,0&alink[]=1,20&flink[]=1,40");
        app.setUrl(testUrl.toString());

        final String pageTitle = "Dynamic links";

        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /* check the page title of first tab */
        onView(withId(R.id.main_toolbar_title)).check(matches(withText(pageTitle)));

        onView(withId(R.id.buttonTabs)).perform(click());
        assertIsAtTabsView();

        /* assert title matches to the tab title */
        assertTabIsVisibleAtPosition(0);
        onView(withId(R.id.main_toolbar_title)).check(matches(withText(pageTitle)));

        onView(allOf(
            withId(R.id.tab_title), withParent(allOf(nthChildOf(withId(R.id.tab_grid), 0), withId(R.id.tab)))
        ))
            .check(matches(withText(pageTitle)));

        onView(withId(R.id.addTab)).perform(click());
        onMyWebView()
            .perform(actionWithAssertions(
                new GeneralClickAction(
                    Tap.SINGLE,
                    MockPageLinkPosition.ALINK1,
                    Press.FINGER,
                    InputDevice.SOURCE_UNKNOWN,
                    MotionEvent.BUTTON_PRIMARY)
            ));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.main_toolbar_title)).check(matches(not(withText(pageTitle))));

        /* assert title of 2nd tab is different than that of 1st tab */
        onView(withId(R.id.buttonTabs)).perform(click());

        /* assert title of page in 2nd page is different */
        onView(allOf(
            withId(R.id.tab_title), withParent(allOf(nthChildOf(withId(R.id.tab_grid), 1), withId(R.id.tab)))
        ))
            .check(matches(not(withText(pageTitle))));
    }

    @Test
    public void newTabStartsWithConfiguredURL() throws MalformedURLException {
        final CasaWebAppApplication app = getApplication();
        final URL paramUrl = new URL(url);

        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();

        onView(withId(R.id.buttonTabs)).perform(click());
        assertIsAtTabsView();

        onView(withId(R.id.addTab)).perform(click());

        onView(withId(R.id.main_toolbar_subtitle)).check(matches(withText(paramUrl.toString())));
    }

    @Test
    public void linkCanBeOpenedInNewTab() throws MalformedURLException {
        final CasaWebAppApplication app = getApplication();
        final URL paramUrl = new URL(url);

        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();

        onMyWebView()
            .perform(actionWithAssertions(
                new GeneralClickAction(
                    Tap.LONG,
                    MockPageLinkPosition.ALINK1,
                    Press.FINGER,
                    InputDevice.SOURCE_UNKNOWN,
                    MotionEvent.BUTTON_PRIMARY)
            ));

//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        onView(withText("Open link in new tab")).check(matches(isDisplayed()));
        onView(withText("Open link in new tab")).perform(click());

//        onView(withId(R.id.main_toolbar_subtitle)).check(matches(not(withText(testUrl.toString()))));
//        onView(withId(R.id.main_toolbar_subtitle)).check(matches(not(withText(paramUrl.toString()))));
    }

    @Test
    public void backButtonWorksOn2ndTab() throws MalformedURLException {
        final CasaWebAppApplication app = getApplication();
        final URL paramUrl = new URL(url);
        final URL testUrl = new URL(paramUrl.toString() +"dynamic?alink[]=1,0&alink[]=1,20&flink[]=1,40");
        app.setUrl(testUrl.toString());

        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();

        onView(withId(R.id.buttonTabs)).perform(click());
        assertIsAtTabsView();

        onView(withId(R.id.addTab)).perform(click());
        onView(withId(R.id.buttonTabs)).perform(click());
        assertTabIsVisibleAtPosition(1);

        onView(allOf(nthChildOf(withId(R.id.tab_grid), 0), withId(R.id.tab))).perform(click());
        onView(withId(R.id.main_toolbar_subtitle)).check(matches(withText(testUrl.toString())));

        onMyWebView()
            .perform(actionWithAssertions(
                new GeneralClickAction(
                    Tap.SINGLE,
                    MockPageLinkPosition.ALINK1,
                    Press.FINGER,
                    InputDevice.SOURCE_UNKNOWN,
                    MotionEvent.BUTTON_PRIMARY)
            ));

//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        onView(withId(R.id.main_toolbar_subtitle)).check(matches(not(withText(testUrl.toString()))));
        onView(withId(R.id.buttonBack)).perform(click());

        onView(withId(R.id.main_toolbar_subtitle)).check(matches(withText(testUrl.toString())));
    }

    @Test
    public void forwardButtonWorksOn2ndTab() throws MalformedURLException {
        final CasaWebAppApplication app = getApplication();
        final URL paramUrl = new URL(url);
        final URL testUrl = new URL(paramUrl.toString() +"dynamic?alink[]=1,0&alink[]=1,20&flink[]=1,40");
        app.setUrl(testUrl.toString());

        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();

        onView(withId(R.id.buttonTabs)).perform(click());
        assertIsAtTabsView();

        onView(withId(R.id.addTab)).perform(click());
        onView(withId(R.id.buttonTabs)).perform(click());
        assertTabIsVisibleAtPosition(1);

        onView(allOf(nthChildOf(withId(R.id.tab_grid), 0), withId(R.id.tab))).perform(click());
        onView(withId(R.id.main_toolbar_subtitle)).check(matches(withText(testUrl.toString())));

        onMyWebView()
            .perform(actionWithAssertions(
                new GeneralClickAction(
                    Tap.SINGLE,
                    MockPageLinkPosition.ALINK1,
                    Press.FINGER,
                    InputDevice.SOURCE_UNKNOWN,
                    MotionEvent.BUTTON_PRIMARY)
            ));

        onView(withId(R.id.main_toolbar_subtitle)).check(matches(not(withText(testUrl.toString()))));

        onView(withId(R.id.buttonBack)).perform(click());
        onView(withId(R.id.main_toolbar_subtitle)).check(matches(withText(testUrl.toString())));
    }

    @Test
    public void tabContentIsPageScreenshot() {
        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();

        onView(withId(R.id.buttonTabs)).perform(click());
        assertIsAtTabsView();

        onView(withId(R.id.tab_thumbnail)).check(matches(withDrawable(DrawableMatcher.ANY_IMAGE)));
    }

    @Test
    public void pageTitleIsSetAtopOfTab() {
        final String pageTitle = "General mock web page title";

        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /* get copy of page title */
        onView(withId(R.id.main_toolbar_title)).check(matches(withText(pageTitle)));

        onView(withId(R.id.buttonTabs)).perform(click());
        assertIsAtTabsView();

        /* assert title matches to the tab title */
        assertTabIsVisibleAtPosition(0);

        onView(RecyclerViewMatcher.withRecyclerView(R.id.tab_grid)
            .atPositionOnView(0, R.id.tab_title))
            .check(matches(withText(pageTitle)));
    }

    private CasaWebAppApplication getApplication() {
        final CasaWebAppApplication app = (CasaWebAppApplication) getInstrumentation().getTargetContext().getApplicationContext();
        return app;
    }

    private ViewInteraction onMyWebView() {
        return onView(allOf(withId(R.id.webview_container), withChild(instanceOf(WebView.class))));
    }

    @Test
    public void deviceBackButtonOn2ndTabTakesBackInHistory() throws MalformedURLException {
        final CasaWebAppApplication app = getApplication();
        final URL paramUrl = new URL(url);
        final URL testUrl = new URL(paramUrl.toString() +"dynamic?alink[]=1,0&alink[]=1,20&flink[]=1,40");
        app.setUrl(testUrl.toString());

        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();

        /* get copy of page title */
        onView(withId(R.id.buttonTabs)).perform(click());
        assertIsAtTabsView();

        onView(withId(R.id.addTab)).perform(click());
        onView(withId(R.id.buttonTabs)).perform(click());
        assertTabIsVisibleAtPosition(1);

        onView(allOf(nthChildOf(withId(R.id.tab_grid), 1), withId(R.id.tab))).perform(click());

        onMyWebView()
            .perform(actionWithAssertions(
                new GeneralClickAction(
                    Tap.SINGLE,
                    MockPageLinkPosition.ALINK1,
                    Press.FINGER,
                    InputDevice.SOURCE_UNKNOWN,
                    MotionEvent.BUTTON_PRIMARY)
            ));

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.main_toolbar_subtitle)).check(matches(not(withText(testUrl.toString()))));

        Espresso.pressBack();
        onView(withId(R.id.main_toolbar_subtitle)).check(matches(withText(testUrl.toString())));
    }

    @Test
    public void newTabIncreasesNumberOnTabsButton() {
        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();

        onView(withId(R.id.buttonTabs)).check(matches(withText("1")));

        onView(withId(R.id.buttonTabs)).perform(click());
        assertIsAtTabsView();

        onView(withId(R.id.addTab)).perform(click());

        assertIsAtMainView();
        onView(withId(R.id.buttonTabs)).check(matches(withText("2")));
    }

    @Test
    public void closingTabDecreasesNumberOnTabsButton() {
        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();

        onView(withId(R.id.buttonTabs)).perform(click());
        assertIsAtTabsView();

        onView(withId(R.id.addTab)).perform(click());
        onView(withId(R.id.buttonTabs)).check(matches(withText("2")));
        onView(withId(R.id.buttonTabs)).perform(click());

        onView(allOf(
            withId(R.id.close_tab), withParent(allOf(nthChildOf(withId(R.id.tab_grid), 0), withId(R.id.tab)))
        ))
            .perform(click());
        onView(allOf(nthChildOf(withId(R.id.tab_grid), 0), withId(R.id.tab))).perform(click());

        onView(withId(R.id.buttonTabs)).check(matches(withText("1")));
    }

    @Test
    public void switchingBetweenMobileAndDesktopModeOn2ndTabUpdatesThumbnail() {
        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();

        onView(withId(R.id.buttonTabs)).perform(click());
        assertIsAtTabsView();

        onView(withId(R.id.addTab)).perform(click());
        onView(withId(R.id.buttonTabs)).perform(click());

        /* get screenshot of mobile mode */
        AtomicReference<Drawable> screenshot1 = new AtomicReference<>();
        AtomicReference<Drawable> screenshot2 = new AtomicReference<>();
        scenario.onActivity(activity -> {
            View item = ((RecyclerView) activity.findViewById(R.id.tab_grid)).findViewHolderForAdapterPosition(1).itemView;
            AppCompatButton button = item.findViewById(R.id.tab_thumbnail_container).findViewById(R.id.tab_thumbnail);
            screenshot1.set(button.getBackground());
        });

        onView(allOf(nthChildOf(withId(R.id.tab_grid), 1), withId(R.id.tab))).perform(click());

        /* switch to desktop mode */
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText("Desktop/Mobile")).perform(click());

        onView(withId(R.id.buttonTabs)).perform(click());

        /* get screenshot of desktop mode */
        scenario.onActivity(activity -> {
            View item = ((RecyclerView) activity.findViewById(R.id.tab_grid)).findViewHolderForAdapterPosition(1).itemView;
            AppCompatButton button = item.findViewById(R.id.tab_thumbnail_container).findViewById(R.id.tab_thumbnail);
            screenshot2.set(button.getBackground());
        });

        /* compare the images on byte level */
        Assert.assertFalse(areBitmapsEqual(
            ((BitmapDrawable) screenshot1.get()).getBitmap(),
            ((BitmapDrawable) screenshot2.get()).getBitmap()
        ));
    }

    private static boolean areBitmapsEqual(Bitmap bitmap1, Bitmap bitmap2) {
        // compare two bitmaps by their bytes
        byte[] array1 = BitmapToByteArray(bitmap1);
        byte[] array2 = BitmapToByteArray(bitmap2);
        if (java.util.Arrays.equals(array1, array2)) {
            return true;
        }
        return false;
    }

    private static byte[] BitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] result = bos.toByteArray();
        return result;
    }
}

