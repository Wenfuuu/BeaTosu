package beat.osu.beatosu.helper;

import java.util.Locale;

public class LocaleManager {

    public static String getCurrentCountry() {
        Locale locale = Locale.getDefault();
        String country = locale.getCountry();
        return country;
    }
}
