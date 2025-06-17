package beat.osu.client.view.shared.bancho.cards;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import beat.osu.client.helper.LocaleManager;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.util.Duration;

public interface UserCardBehavior {
    
    void setupBehavior(UserCard card);
    void removeBehavior(UserCard card);
    
    UserCardBehavior HOVER_TIME_COUNTRY = new UserCardBehavior() {
        @Override
        public void setupBehavior(UserCard card) {
            card.setOnMouseEntered(e -> {
                if (card.getCurrentTransition() != null) {
                    card.getCurrentTransition().stop();
                }
                card.setHovering(true);
                showTimeAndCountryWithTransition(card);
            });

            card.setOnMouseExited(e -> {
                if (card.getCurrentTransition() != null) {
                    card.getCurrentTransition().stop();
                }
                card.setHovering(false);
                showNormalStatsWithTransition(card);
            });
        }
        
        @Override
        public void removeBehavior(UserCard card) {
            card.setOnMouseEntered(null);
            card.setOnMouseExited(null);
        }
        
        private void showTimeAndCountryWithTransition(UserCard card) {
            if (card.getUserStats() == null || card.getTimeStats() == null) return;

            String timeAndCountry = getTimeAndCountryString(card.getCountryCode());
            card.getTimeLabel().setText(timeAndCountry);

            FadeTransition fadeOutStats = new FadeTransition(Duration.millis(200), card.getUserStats());
            fadeOutStats.setFromValue(1.0);
            fadeOutStats.setToValue(0.0);

            FadeTransition fadeInTime = new FadeTransition(Duration.millis(200), card.getTimeStats());
            fadeInTime.setFromValue(0.0);
            fadeInTime.setToValue(1.0);

            ParallelTransition transition = new ParallelTransition(fadeOutStats, fadeInTime);
            card.setCurrentTransition(transition);
            transition.play();
        }

        private void showNormalStatsWithTransition(UserCard card) {
            if (card.getUserStats() == null || card.getTimeStats() == null) return;

            FadeTransition fadeOutTime = new FadeTransition(Duration.millis(200), card.getTimeStats());
            fadeOutTime.setFromValue(1.0);
            fadeOutTime.setToValue(0.0);

            FadeTransition fadeInStats = new FadeTransition(Duration.millis(200), card.getUserStats());
            fadeInStats.setFromValue(0.0);
            fadeInStats.setToValue(1.0);

            ParallelTransition transition = new ParallelTransition(fadeOutTime, fadeInStats);
            card.setCurrentTransition(transition);
            transition.play();
        }

        private String getTimeAndCountryString(String countryCode) {
            if (countryCode == null || countryCode.trim().isEmpty()) {
                return "Time unavailable";
            }

            LocalDateTime currentTime = LocaleManager.getCurrentTime(countryCode);
            String countryName = LocaleManager.getCountryName(countryCode);

            if (currentTime == null) {
                return "Time unavailable @ " + countryName;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            String formattedTime = currentTime.format(formatter);

            return formattedTime + " @ " + countryName;
        }
    };
    
    UserCardBehavior STATIC = new UserCardBehavior() {
        @Override
        public void setupBehavior(UserCard card) {
        }
        
        @Override
        public void removeBehavior(UserCard card) {
            card.setOnMouseEntered(null);
            card.setOnMouseExited(null);
        }
    };
}
