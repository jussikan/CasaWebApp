package fi.casa.webapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.webkit.WebView;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.web.webdriver.Locator;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.uiautomator.UiDevice;

import static android.content.Context.CLIPBOARD_SERVICE;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.actionWithAssertions;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.web.assertion.WebViewAssertions.webContent;
import static androidx.test.espresso.web.assertion.WebViewAssertions.webMatches;
import static androidx.test.espresso.web.matcher.DomMatchers.hasElementWithId;
import static androidx.test.espresso.web.sugar.Web.onWebView;
import static androidx.test.espresso.web.webdriver.DriverAtoms.findElement;
import static androidx.test.espresso.web.webdriver.DriverAtoms.getText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static fi.casa.webapp.MainActivity.TAG;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;


/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ApplicationEspressoTests {
    private String url;

    private ActivityScenario<MainActivity> scenario = null;

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

    private CasaWebAppApplication getApplication() {
        final CasaWebAppApplication app = (CasaWebAppApplication) getInstrumentation().getTargetContext().getApplicationContext();
        return app;
    }

    @Before
    public void setUp() {
        final CasaWebAppApplication app = getApplication();

        try {
            loadParameters();
        } catch (IOException e) {
            e.printStackTrace();
        }

        url = getUrl();

        app.setUrl(url);
    }

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

    protected void assertWebViewIsDisplayed() {
//        onView(withId(R.id.webview)).check(matches(isDisplayed()));
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
     * main menu a.k.a. toolbar menu
     */
    @Test
    public void mainMenuIsOpened() {
        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();
        openMainMenu();
    }

    /**
     * assert web page looks certain way
     * open toolbar menu
     * press the switch action
     * assert web page looks different
     */
    @Test
    public void switchBetweenDesktopAndMobilePage() {
        scenario = ActivityScenario.launch(MainActivity.class);
        assertIsAtMainView();

        onWebView().withElement(findElement(Locator.ID, "deviceType"))
                .check(webMatches(getText(), containsString("mobile")));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        onView(withText("Desktop/Mobile")).perform(click());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onWebView().check(webContent(hasElementWithId("deviceType")));

        onWebView().withElement(findElement(Locator.ID, "deviceType"))
                .check(webMatches(getText(), containsString("desktop")));
    }

    protected ClipboardManager getClipboardManager() {
        return (ClipboardManager) getInstrumentation()
            .getTargetContext().getSystemService(CLIPBOARD_SERVICE);
    }

    protected String getPrimaryStringFromClipboard(@Nullable ClipboardManager clipboardManager) {
        if (clipboardManager == null) {
            clipboardManager = getClipboardManager();
        }

        ClipData cp = clipboardManager.getPrimaryClip();
        String copiedText = cp.getItemAt(0).getText().toString();
        return copiedText;
    }

    protected void clearClipboard(@Nullable ClipboardManager clipboardManager) {
        if (clipboardManager == null) {
            clipboardManager = getClipboardManager();
        }

        clipboardManager.setPrimaryClip(ClipData.newPlainText("clear", ""));
    }

    private ViewInteraction onMyWebView() {
        return onView(allOf(withId(R.id.webview_container), withChild(instanceOf(WebView.class))));
    }

    @Test
    public void backButtonWorks() throws MalformedURLException {
        scenario = ActivityScenario.launch(MainActivity.class);

        final CasaWebAppApplication app = getApplication();
        final URL paramUrl = new URL(url);
        final URL testUrl = new URL(paramUrl.toString() +"dynamic?alink[]=1,0&alink[]=1,20&flink[]=1,40");
        app.setUrl(testUrl.toString());
        ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();
        assertWebViewIsDisplayed();

        onView(withId(R.id.main_toolbar_subtitle)).check(matches(withText(testUrl.toString())));

        /* click on a link that takes user to an allowed page */
        onMyWebView()
            .perform(actionWithAssertions(
                new GeneralClickAction(
                    Tap.SINGLE,
                    MockPageLinkPosition.ALINK1,
                    Press.FINGER,
                    InputDevice.SOURCE_UNKNOWN,
                    MotionEvent.BUTTON_PRIMARY)
            ));

        /* assert current url has changed */
        onView(withId(R.id.main_toolbar_subtitle)).check(matches(not(withText(testUrl.toString()))));

        onView(withId(R.id.buttonBack)).perform(click());

        /* assert current url equals to initial */
        onView(withId(R.id.main_toolbar_subtitle)).check(matches(withText(testUrl.toString())));
    }

    @Test
    public void forwardButtonWorks() throws MalformedURLException {
        scenario = ActivityScenario.launch(MainActivity.class);

        final CasaWebAppApplication app = getApplication();
        final URL paramUrl = new URL(url);
        final URL testUrl = new URL(paramUrl.toString() +"dynamic?alink[]=1,0&alink[]=1,20&flink[]=1,40");
        app.setUrl(testUrl.toString());
        ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();
        assertWebViewIsDisplayed();

        onView(withId(R.id.main_toolbar_subtitle)).check(matches(withText(testUrl.toString())));

        /* click on a link that takes user to an allowed page */
        onMyWebView()
            .perform(actionWithAssertions(
                new GeneralClickAction(
                    Tap.SINGLE,
                    MockPageLinkPosition.ALINK1,
                    Press.FINGER,
                    InputDevice.SOURCE_UNKNOWN,
                    MotionEvent.BUTTON_PRIMARY)
            ));

        /* assert current url has changed */
        onView(withId(R.id.main_toolbar_subtitle)).check(matches(not(withText(testUrl.toString()))));

        onView(withId(R.id.buttonBack)).perform(click());
        /* assert current url equals to initial */
        onView(withId(R.id.main_toolbar_subtitle)).check(matches(withText(testUrl.toString())));

        onView(withId(R.id.buttonForward)).perform(click());
        onView(withId(R.id.main_toolbar_subtitle)).check(matches(not(withText(testUrl.toString()))));
    }

    @Test
    public void deviceBackButtonTakesBackInHistory() throws MalformedURLException {
        scenario = ActivityScenario.launch(MainActivity.class);

        final CasaWebAppApplication app = getApplication();
        final URL paramUrl = new URL(url);
        final URL testUrl = new URL(paramUrl.toString() +"dynamic?alink[]=1,0&alink[]=1,20&flink[]=1,40");
        app.setUrl(testUrl.toString());
        ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();
        assertWebViewIsDisplayed();

        onView(withId(R.id.main_toolbar_subtitle)).check(matches(withText(testUrl.toString())));

        /* click on a link that takes user to an allowed page */
        onMyWebView()
            .perform(actionWithAssertions(
                new GeneralClickAction(
                    Tap.SINGLE,
                    MockPageLinkPosition.ALINK1,
                    Press.FINGER,
                    InputDevice.SOURCE_UNKNOWN,
                    MotionEvent.BUTTON_PRIMARY)
            ));

        /* assert current url has changed */
        onView(withId(R.id.main_toolbar_subtitle)).check(matches(not(withText(testUrl.toString()))));

        Espresso.pressBack();

        /* assert current url equals to initial */
        onView(withId(R.id.main_toolbar_subtitle)).check(matches(withText(testUrl.toString())));
    }

    /**
     * long click on a link on web page
     * choose the copy action
     * assert the location of a link is copied to clipboard from context menu
     */
    @Test
    public void copyUrlToClipboardFromContextMenu() {
        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();
        assertWebViewIsDisplayed();

        clearClipboard(null);

        onView(allOf(withId(R.id.webview_container), withChild(instanceOf(WebView.class))))
            .perform(actionWithAssertions(
            new GeneralClickAction(
                Tap.LONG,
                MockPageLinkPosition.TARGET,
                Press.FINGER,
                InputDevice.SOURCE_UNKNOWN,
                MotionEvent.BUTTON_PRIMARY)
        ));

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withText("Copy URL to clipboard")).perform(click());

        try {
            /* NOTE maybe better would be to wait until onPageFinished() in WebView has ran .. twice. */
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String copiedText = getPrimaryStringFromClipboard(null);

        Assert.assertTrue(copiedText.contains(url));
    }

    /**
     * long click on a link on web page
     * choose the share action
     * assert the OS share function is called, and the page url is given to it
     * -- fails because a link needed on the mock server page
     */
    @Test
    public void shareUrlFromContextMenu() {
        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();
        assertWebViewIsDisplayed();

        clearClipboard(null);

        onView(allOf(withId(R.id.webview_container), withChild(instanceOf(WebView.class))))
            .perform(actionWithAssertions(
                new GeneralClickAction(
                    Tap.LONG,
                    MockPageLinkPosition.TARGET,
                    Press.FINGER,
                    InputDevice.SOURCE_UNKNOWN,
                    MotionEvent.BUTTON_PRIMARY)
            )
        );

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withText("Share URL to another app")).check(matches(isDisplayed()));
        Log.i(TAG, "derp");

        /* TODO how to use the system share dialog ?? */
    }

    /**
     * open toolbar menu
     * click on the share action
     * assert the OS share function is called, and the page url is given to it
     */
    @Test
    public void sharePageFromToolbarMenu() {
        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        onView(withText("Share")).perform(click());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /* this causes the share dialog to disappear. */
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        device.click(100, 100);
    }

    /**
     * assert page title is set to toolbar
     * -- fails because mock server was not running
     */
    @Test
    public void pageTitleIsSetToToolbar() {
        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();
        assertWebViewIsDisplayed();
        String title = "General mock web page title";

        try {
            /* NOTE maybe better would be to wait until onPageFinished() in WebView has ran .. twice. */
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.main_toolbar_title)).check(matches(withText(title)));
    }

    /**
     * assert there is no url in toolbar
     * tap the toolbar
     * assert there is url in toolbar
     * -- fails because url is null
     */
    @Test
    public void pageUrlIsSetToToolbar() {
        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();
        assertWebViewIsDisplayed();

        try {
            /* NOTE maybe better would be to wait until onPageFinished() in WebView has ran .. twice. */
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        onView(withId(R.id.my_toolbar_subtitle)).check(matches(not(withText(url))));
        onView(withId(R.id.main_toolbar_subtitle)).check(matches(withText(url)));
    }

    protected void goToSettings() {
        try {
            openMainMenu();
        } catch (Exception e) {
            // ok
        }

        onView(withText("Settings")).perform(click());
    }

    /**
     * go to settings
     * return to home view
     */
    @Test
    public void goToSettingsAndReturnHome() {
        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();
        goToSettings();

        onView(withId(R.id.settings_view)).check(matches(isDisplayed()));

        /* click back button on Toolbar */
        onView(isAssignableFrom(AppCompatImageButton.class)).perform(click());

        assertIsAtMainView();
        assertWebViewIsDisplayed();
    }

    /**
     * go to settings
     * change user agent string
     * return to home view
     * go to settings
     * assert the new string is in place
     */
    @Test
    public void setUserAgentString() {
        scenario = ActivityScenario.launch(MainActivity.class);

        assertIsAtMainView();
        goToSettings();
        onView(withId(R.id.settings_view)).check(matches(isDisplayed()));

        final String newUserAgent = "My Best Browser";
        onView(withId(R.id.setting_field_userAgent)).perform(ViewActions.clearText());
        onView(withId(R.id.setting_field_userAgent)).perform(typeText(newUserAgent));
        onView(isAssignableFrom(AppCompatImageButton.class)).perform(click());

        assertIsAtMainView();
        goToSettings();
        onView(withId(R.id.settings_view)).check(matches(isDisplayed()));
        onView(withId(R.id.setting_field_userAgent)).check(matches(withText(newUserAgent)));
    }
}
