package beat.osu.client.helper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class LocaleManager {

    private static final Map<String, String> COUNTRY_TIMEZONE_MAP = new HashMap<>();

    static {
        COUNTRY_TIMEZONE_MAP.put("US", "America/New_York");
        COUNTRY_TIMEZONE_MAP.put("CA", "America/Toronto");
        COUNTRY_TIMEZONE_MAP.put("GB", "Europe/London");
        COUNTRY_TIMEZONE_MAP.put("DE", "Europe/Berlin");
        COUNTRY_TIMEZONE_MAP.put("FR", "Europe/Paris");
        COUNTRY_TIMEZONE_MAP.put("IT", "Europe/Rome");
        COUNTRY_TIMEZONE_MAP.put("ES", "Europe/Madrid");
        COUNTRY_TIMEZONE_MAP.put("RU", "Europe/Moscow");
        COUNTRY_TIMEZONE_MAP.put("CN", "Asia/Shanghai");
        COUNTRY_TIMEZONE_MAP.put("JP", "Asia/Tokyo");
        COUNTRY_TIMEZONE_MAP.put("KR", "Asia/Seoul");
        COUNTRY_TIMEZONE_MAP.put("IN", "Asia/Kolkata");
        COUNTRY_TIMEZONE_MAP.put("AU", "Australia/Sydney");
        COUNTRY_TIMEZONE_MAP.put("NZ", "Pacific/Auckland");
        COUNTRY_TIMEZONE_MAP.put("BR", "America/Sao_Paulo");
        COUNTRY_TIMEZONE_MAP.put("AR", "America/Argentina/Buenos_Aires");
        COUNTRY_TIMEZONE_MAP.put("MX", "America/Mexico_City");
        COUNTRY_TIMEZONE_MAP.put("ZA", "Africa/Johannesburg");
        COUNTRY_TIMEZONE_MAP.put("EG", "Africa/Cairo");
        COUNTRY_TIMEZONE_MAP.put("ID", "Asia/Jakarta");
        COUNTRY_TIMEZONE_MAP.put("TH", "Asia/Bangkok");
        COUNTRY_TIMEZONE_MAP.put("SG", "Asia/Singapore");
        COUNTRY_TIMEZONE_MAP.put("MY", "Asia/Kuala_Lumpur");
        COUNTRY_TIMEZONE_MAP.put("PH", "Asia/Manila");
        COUNTRY_TIMEZONE_MAP.put("VN", "Asia/Ho_Chi_Minh");
        COUNTRY_TIMEZONE_MAP.put("TR", "Europe/Istanbul");
        COUNTRY_TIMEZONE_MAP.put("SA", "Asia/Riyadh");
        COUNTRY_TIMEZONE_MAP.put("AE", "Asia/Dubai");
        COUNTRY_TIMEZONE_MAP.put("IL", "Asia/Jerusalem");
        COUNTRY_TIMEZONE_MAP.put("SE", "Europe/Stockholm");
        COUNTRY_TIMEZONE_MAP.put("NO", "Europe/Oslo");
        COUNTRY_TIMEZONE_MAP.put("DK", "Europe/Copenhagen");
        COUNTRY_TIMEZONE_MAP.put("FI", "Europe/Helsinki");
        COUNTRY_TIMEZONE_MAP.put("NL", "Europe/Amsterdam");
        COUNTRY_TIMEZONE_MAP.put("BE", "Europe/Brussels");
        COUNTRY_TIMEZONE_MAP.put("CH", "Europe/Zurich");
        COUNTRY_TIMEZONE_MAP.put("AT", "Europe/Vienna");
        COUNTRY_TIMEZONE_MAP.put("PL", "Europe/Warsaw");
        COUNTRY_TIMEZONE_MAP.put("CZ", "Europe/Prague");
        COUNTRY_TIMEZONE_MAP.put("HU", "Europe/Budapest");
        COUNTRY_TIMEZONE_MAP.put("PT", "Europe/Lisbon");
        COUNTRY_TIMEZONE_MAP.put("GR", "Europe/Athens");
        COUNTRY_TIMEZONE_MAP.put("BG", "Europe/Sofia");
        COUNTRY_TIMEZONE_MAP.put("RO", "Europe/Bucharest");
        COUNTRY_TIMEZONE_MAP.put("HR", "Europe/Zagreb");
        COUNTRY_TIMEZONE_MAP.put("SI", "Europe/Ljubljana");
        COUNTRY_TIMEZONE_MAP.put("SK", "Europe/Bratislava");
        COUNTRY_TIMEZONE_MAP.put("LT", "Europe/Vilnius");
        COUNTRY_TIMEZONE_MAP.put("LV", "Europe/Riga");
        COUNTRY_TIMEZONE_MAP.put("EE", "Europe/Tallinn");
    }

    public static String getCurrentCountry() {
        Locale locale = Locale.getDefault();
        return locale.getCountry();
    }

    public static String getCountryName(String countryCode) {
        if (countryCode == null || countryCode.trim().isEmpty()) {
            return "Unknown";
        }

        try {
            Locale locale = new Locale("", countryCode.toUpperCase());
            String countryName = locale.getDisplayCountry();

            if (countryName.isEmpty() || countryName.equalsIgnoreCase(countryCode)) {
                return "Unknown";
            }

            return countryName;
        } catch (Exception e) {
            return "Unknown";
        }
    }

    public static String getTimezone(String countryCode) {
        if (countryCode == null || countryCode.trim().isEmpty()) {
            return null;
        }
        return COUNTRY_TIMEZONE_MAP.get(countryCode.toUpperCase());
    }

    public static LocalDateTime getCurrentTime(String countryCode) {
        String timezoneId = getTimezone(countryCode);
        if (timezoneId == null) {
            return null;
        }

        try {
            ZoneId zoneId = ZoneId.of(timezoneId);
            return LocalDateTime.now(zoneId);
        } catch (Exception e) {
            return null;
        }
    }
}