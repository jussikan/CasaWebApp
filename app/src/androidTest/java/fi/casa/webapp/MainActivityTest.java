package fi.casa.webapp;

import org.junit.runner.RunWith;

import javax.inject.Singleton;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import dagger.Component;
import fi.casa.webapp.mocks.MockWebViewModule;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Singleton
    @Component(modules = MockWebViewModule.class)
    public interface MockWebViewComponent {
        public void reload();
    }
}
