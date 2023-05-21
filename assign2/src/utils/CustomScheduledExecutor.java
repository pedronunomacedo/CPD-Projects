package utils;

import java.util.Timer;
import java.util.TimerTask;

public class CustomScheduledExecutor {
    private Timer timer;

    public CustomScheduledExecutor() {
        this.timer = new Timer();
    }

    public void scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnits unit) {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        };

        long delayInMillis = unit.toMillis(initialDelay);
        long periodInMillis = unit.toMillis(period);

        timer.scheduleAtFixedRate(timerTask, delayInMillis, periodInMillis);
    }
}