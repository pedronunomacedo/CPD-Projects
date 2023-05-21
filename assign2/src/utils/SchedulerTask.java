package utils;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class SchedulerTask {
    private Timer timer;

    public SchedulerTask() {
        timer = new Timer();
    }

    public void scheduleTask(long delay) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // Code to be executed on the scheduled time
                System.out.println("Task executed at: " + new Date());
            }
        };

        timer.schedule(task, delay);
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

    public void cancelTask() {
        timer.cancel();
    }
}
