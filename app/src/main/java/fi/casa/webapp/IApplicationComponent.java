package fi.casa.webapp;

import dagger.Component;

@Component
public interface IApplicationComponent {
    void inject(MainActivity mainActivity);
}
