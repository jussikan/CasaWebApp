package fi.casa.webapp;

import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;
import androidx.test.espresso.util.HumanReadables;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.actionWithAssertions;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static fi.casa.webapp.MainActivity.TAG;
import static fi.casa.webapp.ViewMatchers.nthChildOf;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;

interface ItemViewAssertion<A> {
    void check(A item, View view, NoMatchingViewException e);
}

class RecyclerItemViewAssertion<A> implements ViewAssertion {

    private int position;
    private A item;
    private ItemViewAssertion<A> itemViewAssertion;

    public RecyclerItemViewAssertion(int position, A item, ItemViewAssertion<A> itemViewAssertion) {
        this.position = position;
        this.item = item;
        this.itemViewAssertion = itemViewAssertion;
    }

    @Override
    public final void check(View view, NoMatchingViewException e) {
        RecyclerView recyclerView = (RecyclerView) view;
        RecyclerView.ViewHolder viewHolderForPosition = recyclerView.findViewHolderForLayoutPosition(position);
        if (viewHolderForPosition == null) {
            throw (new PerformException.Builder())
                    .withActionDescription(toString())
                    .withViewDescription(HumanReadables.describe(view))
                    .withCause(new IllegalStateException("No view holder at position: " + position))
                    .build();
        } else {
            View viewAtPosition = viewHolderForPosition.itemView;
            itemViewAssertion.check(item, viewAtPosition, e);
        }
    }
}

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

        onView(withId(R.id.action_show_tabs)).perform(click());
        assertIsAtTabsView();

//        onView(allOf(withId(R.id.tab_grid), withId(R.id.tab))).check(ViewAssertions.matches(isDisplayed()));
//        onView(withId(R.id.tabderp)).check(ViewAssertions.matches(isDisplayed()));
//        onView(withId(R.id.tab_grid)).check(ViewAssertions.matches(isDisplayed()));
//        onView(withId(R.id.tab_grid)).check(ViewAssertions.matches(hasChildCount(1)));

//        onData(withId(R.id.tabderp)).inAdapterView(withId(R.id.tab_grid)).check(matches(isDisplayed()));
//        onData(allOf(withId(R.id.tabderp), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).check(matches(isDisplayed()));
//        onData(allOf(withId(R.id.tab_grid), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).check(matches(isCompletelyDisplayed()));
//        onView(withId(R.id.tab_grid)).check(matches());

//        onView(nthChildOf(withId(R.id.tab_grid), 0)).check(matches(isDisplayed()));
//        onView(allOf(nthChildOf(withId(R.id.tab_grid), 0), withId(R.id.tabderp))).check(matches(isDisplayed()));
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

        onView(withId(R.id.action_show_tabs)).perform(click());
        assertIsAtTabsView();
        assertTabIsVisibleAtPosition(0);
        try {
            assertTabIsVisibleAtPosition(1);
        } catch (NullPointerException npe) {
            //
        }

        onView(withId(R.id.addTab)).perform(click());
        onView(withId(R.id.action_show_tabs)).perform(click());
        assertTabIsVisibleAtPosition(1);
    }

    @Test
    public void tabCanBeClosed() {
        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();

        onView(withId(R.id.action_show_tabs)).perform(click());
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

    @Ignore
    @Test
    public void pressingLinkOnSecondTabDoesNotAffectFirstTab() {}

    @Ignore
    @Test
    public void switchingBetweenTabsChangesToolbarTitles() {
        final String pageTitle = "General mock web page title";

        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();

//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        /* check the page title of first tab */
        onView(withId(R.id.main_toolbar_title)).check(matches(withText(pageTitle)));

        onView(withId(R.id.action_show_tabs)).perform(click());
        assertIsAtTabsView();

        /* assert title matches to the tab title */
//        assertTabIsVisibleAtPosition(0);
//        onView(withId(R.id.main_toolbar_title)).check(matches(withText("General mock web page title")));
//        onTab(0).check(matches(withText(pageTitle)));
//        onView(allOf(getTab(0), withId(R.id.tab_title))).check(matches(withText(pageTitle)));

        /* assert title of 2nd tab is different than that of 1st tab */
        /* click on the 2nd tab */
        /* assert title of page in 2nd page is different */
//        onView(allOf(nthChildOf(withId(R.id.tab_grid), 0), withId(R.id.tabderp))).check(matches(withText(pageTitle)));
    }

    @Test
    public void newTabStartsWithConfiguredURL() throws MalformedURLException {
        final CasaWebAppApplication app = getApplication();
        final URL paramUrl = new URL(url);

        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();

        onView(withId(R.id.action_show_tabs)).perform(click());
        assertIsAtTabsView();

        onView(withId(R.id.addTab)).perform(click());

        onView(withId(R.id.main_toolbar_subtitle)).check(matches(withText(paramUrl.toString())));
    }

    @Test
    public void linkCanBeOpenedInNewTab() throws MalformedURLException {
        final CasaWebAppApplication app = getApplication();
        final URL paramUrl = new URL(url);/*
        final URL testUrl = new URL(paramUrl.toString() +"dynamic?alink[]=1,0&alink[]=1,20&flink[]=1,40");
        app.setUrl(testUrl.toString());*/

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

        onView(withId(R.id.action_show_tabs)).perform(click());
        assertIsAtTabsView();

        onView(withId(R.id.addTab)).perform(click());
        onView(withId(R.id.action_show_tabs)).perform(click());
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

        onView(withId(R.id.action_show_tabs)).perform(click());
        assertIsAtTabsView();

        onView(withId(R.id.addTab)).perform(click());
        onView(withId(R.id.action_show_tabs)).perform(click());
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

    @Ignore
    @Test
    public void tabContentIsPageScreenshot() {}

    @Test
    public void pageTitleIsSetAtopOfTab() {
        final String pageTitle = "General mock web page title";

        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /* get copy of page title */
        onView(withId(R.id.main_toolbar_title)).check(matches(withText(pageTitle)));

        onView(withId(R.id.action_show_tabs)).perform(click());
        assertIsAtTabsView();

        /* assert title matches to the tab title */
        assertTabIsVisibleAtPosition(0);
//        onView(withId(R.id.main_toolbar_title)).check(matches(withText("General mock web page title")));
//        onTab(0).check(matches(withText(pageTitle)));
//        onView(allOf(getTab(0), withId(R.id.tab_title))).check(matches(withText(pageTitle)));
//        onView(withId(R.id.tab_title)).check(matches(withText(pageTitle)));

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

//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        /* get copy of page title */
//        onView(withId(R.id.main_toolbar_title)).check(matches(withText(pageTitle)));

        onView(withId(R.id.action_show_tabs)).perform(click());
        assertIsAtTabsView();

        onView(withId(R.id.addTab)).perform(click());
        onView(withId(R.id.action_show_tabs)).perform(click());
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
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.main_toolbar_subtitle)).check(matches(not(withText(testUrl.toString()))));

        Espresso.pressBack();
        onView(withId(R.id.main_toolbar_subtitle)).check(matches(withText(testUrl.toString())));
    }

    @Ignore
    @Test
    public void switchingBetweenMobileAndDesktopModeOn2ndTabUpdatesThumbnail() {}

    // TODO ja sit viel vanhat testit tabien kohdalla..

    /**
     * assert web page looks certain way
     * open toolbar menu
     * press the switch action
     * assert web page looks different
     */
//    @Test
//    public void switchBetweenDesktopAndMobilePage() {
//        scenario = ActivityScenario.launch(MainActivity.class);
//        assertIsAtMainView();
//
//        onWebView().withElement(findElement(Locator.ID, "deviceType"))
//                .check(webMatches(getText(), containsString("mobile")));
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
//
//        onView(withText("Desktop/Mobile")).perform(click());
//
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        onWebView().check(webContent(hasElementWithId("deviceType")));
//
//        onWebView().withElement(findElement(Locator.ID, "deviceType"))
//                .check(webMatches(getText(), containsString("desktop")));
//    }
//
//    public enum MockPageLinkPosition implements CoordinatesProvider {
//        TARGET {
//            @Override
//            public float[] calculateCoordinates(View view) {
//                final int[] location = new int[2];
//                view.getLocationOnScreen(location);
//
//                float[] coordinates = {
//                        location[0] + 1,
//                        location[1] + 1
//                };
//                return coordinates;
//            }
//        }
//    }
//
//    protected ClipboardManager getClipboardManager() {
//        return (ClipboardManager) getInstrumentation()
//                .getTargetContext().getSystemService(CLIPBOARD_SERVICE);
//    }
//
//    protected String getPrimaryStringFromClipboard(@Nullable ClipboardManager clipboardManager) {
//        if (clipboardManager == null) {
//            clipboardManager = getClipboardManager();
//        }
//
//        ClipData cp = clipboardManager.getPrimaryClip();
//        String copiedText = cp.getItemAt(0).getText().toString();
//        return copiedText;
//    }
//
//    protected void clearClipboard(@Nullable ClipboardManager clipboardManager) {
//        if (clipboardManager == null) {
//            clipboardManager = getClipboardManager();
//        }
//
//        clipboardManager.setPrimaryClip(ClipData.newPlainText("clear", ""));
//    }
//
//    /**
//     * long click on a link on web page
//     * choose the copy action
//     * assert the location of a link is copied to clipboard from context menu
//     *
//     * TODO find coordinates of a link
//     */
//    @Test
//    public void copyUrlToClipboardFromContextMenu() {
//        scenario = ActivityScenario.launch(MainActivity.class);
//
//        assertIsAtMainView();
//        assertWebViewIsDisplayed();
//
//        clearClipboard(null);
//
//        onView(withId(R.id.webview)).perform(actionWithAssertions(
//                new GeneralClickAction(
//                        Tap.LONG,
//                        MockPageLinkPosition.TARGET,
//                        Press.FINGER,
//                        InputDevice.SOURCE_UNKNOWN,
//                        MotionEvent.BUTTON_PRIMARY)
//        ));
//
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        onView(withText("Copy URL to clipboard")).perform(click());
//
//        try {
//            /* NOTE maybe better would be to wait until onPageFinished() in WebView has ran .. twice. */
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        String copiedText = getPrimaryStringFromClipboard(null);
//
//        /* TODO get configured domain here
//         * TODO assert vs the domain
//         */
//        Assert.assertTrue(copiedText.contains(url));
//    }
//
//    /**
//     * long click on a link on web page
//     * choose the share action
//     * assert the OS share function is called, and the page url is given to it
//     * -- fails because a link needed on the mock server page
//     */
//    @Test
//    public void shareUrlFromContextMenu() {
//        scenario = ActivityScenario.launch(MainActivity.class);
//
//        assertIsAtMainView();
//        assertWebViewIsDisplayed();
//
//        clearClipboard(null);
//
//        onView(withId(R.id.webview)).perform(actionWithAssertions(
//                new GeneralClickAction(
//                        Tap.LONG,
//                        MockPageLinkPosition.TARGET,
//                        Press.FINGER,
//                        InputDevice.SOURCE_UNKNOWN,
//                        MotionEvent.BUTTON_PRIMARY)
//        ));
//
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        onView(withText("Share URL to another app")).check(matches(isDisplayed()));
//
//        /* TODO how to use the system share dialog ?? */
//    }
//
//    /**
//     * open toolbar menu
//     * click on the share action
//     * assert the OS share function is called, and the page url is given to it
//     */
//    @Test
//    public void sharePageFromToolbarMenu() {
//        scenario = ActivityScenario.launch(MainActivity.class);
//
//        assertIsAtMainView();
//        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
//
//        onView(withText("Share")).perform(click());
//
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        /* this causes the share dialog to disappear. */
//        UiDevice device = UiDevice.getInstance(getInstrumentation());
//        device.click(100, 100);
//    }
//
//    /**
//     * assert page title is set to toolbar
//     * -- fails because mock server was not running
//     */
//    @Test
//    public void pageTitleIsSetToToolbar() {
//        scenario = ActivityScenario.launch(MainActivity.class);
//
//        assertIsAtMainView();
//        assertWebViewIsDisplayed();
//        String title = "General mock web page title";
//
//        try {
//            /* NOTE maybe better would be to wait until onPageFinished() in WebView has ran .. twice. */
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        onView(withId(R.id.main_toolbar_title)).check(matches(withText(title)));
//    }
//
//    /**
//     * assert there is no url in toolbar
//     * tap the toolbar
//     * assert there is url in toolbar
//     * -- fails because url is null
//     */
//    @Test
//    public void pageUrlIsSetToToolbar() {
//        scenario = ActivityScenario.launch(MainActivity.class);
//
//        assertIsAtMainView();
//        assertWebViewIsDisplayed();
//
//        try {
//            /* NOTE maybe better would be to wait until onPageFinished() in WebView has ran .. twice. */
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
////        onView(withId(R.id.my_toolbar_subtitle)).check(matches(not(withText(url))));
//        onView(withId(R.id.main_toolbar_subtitle)).check(matches(withText(url)));
//    }
//
//    protected void goToSettings() {
//        try {
//            openMainMenu();
//        } catch (Exception e) {
//            // ok
//        }
//
//        onView(withText("Settings")).perform(click());
//    }
//
//    /**
//     * go to settings
//     * return to home view
//     */
//    @Test
//    public void goToSettingsAndReturnHome() {
//        scenario = ActivityScenario.launch(MainActivity.class);
//
//        assertIsAtMainView();
//        goToSettings();
//
//        onView(withId(R.id.settings_view)).check(matches(isDisplayed()));
//
//        /* click back button on Toolbar */
//        onView(isAssignableFrom(AppCompatImageButton.class)).perform(click());
//
//        assertIsAtMainView();
//        assertWebViewIsDisplayed();
//    }
//
////    final EditText titleInput = (EditText) activity.findViewById(R.id.titleInput);
////    getInstrumentation().runOnMainSync(new Runnable() {
////        public void run() {
////            titleInput.setText("Engineer");
////        }
////    });
//
//    /**
//     * go to settings
//     * change user agent string
//     * return to home view
//     * go to settings
//     * assert the new string is in place
//     */
//    @Test
//    public void setUserAgentString() {
//        scenario = ActivityScenario.launch(MainActivity.class);
//
//        assertIsAtMainView();
//        goToSettings();
//        onView(withId(R.id.settings_view)).check(matches(isDisplayed()));
//
//        final String newUserAgent = "My Best Browser";
//        onView(withId(R.id.setting_field_userAgent)).perform(ViewActions.clearText());
//        onView(withId(R.id.setting_field_userAgent)).perform(typeText(newUserAgent));
//        onView(isAssignableFrom(AppCompatImageButton.class)).perform(click());
//
//        assertIsAtMainView();
//        goToSettings();
//        onView(withId(R.id.settings_view)).check(matches(isDisplayed()));
//        onView(withId(R.id.setting_field_userAgent)).check(matches(withText(newUserAgent)));
//    }
}

