package fi.casa.webapp.mocks;

import org.mockito.Mockito;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import fi.casa.webapp.CasaWebView;
import fi.casa.webapp.WebViewComponent;
//import im.delight.android.webview.AdvancedWebView;


@Module
public class MockWebViewModule {
    @Provides
    @Singleton
    static WebViewComponent provideWebViewComponent() {
        return Mockito.mock(CasaWebView.class);
    }

//    @Provides
//    AdvancedWebView provideAdvancedWebView() {
////        return AdvancedWebView.;
//    }
}
