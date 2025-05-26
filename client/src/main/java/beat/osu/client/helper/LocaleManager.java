package beat.osu.client.helper;

import java.util.Locale;

public class LocaleManager {

    public static String getCurrentCountry() {
        Locale locale = Locale.getDefault();
        return locale.getCountry();
    }
}
