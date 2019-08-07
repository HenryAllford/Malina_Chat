package chat.infobox.hasnat.ume.ume.Utils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

@SuppressLint("Registered")
public class UserLastSeenTime extends Application {
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public static String getTimeAgo(long time, Context applicationContext) {
        if (time < 1000000000000L) {
            time *= 1000;
        }
        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return "Online";
        }
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "Был в сети несколько секунд назад";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "Был в сети минуту назад";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return "Был в сети " + diff / MINUTE_MILLIS + " минут(-ы) назад";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "Был в сети час назад";
        } else if (diff < 24 * HOUR_MILLIS) {
            return "Был в сети " + diff / HOUR_MILLIS + " часа(-ов) назад";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "Был в сети вчера";
        } else {
            return "Был в сети " + diff / DAY_MILLIS + " дня(-ей) назад";
        }
    }
}
