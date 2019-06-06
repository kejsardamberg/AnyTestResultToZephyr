package se.kejsardamberg.any2zephyr.main.server;

/**
 * Prepares a delayed system exit
 */
public class ShutdownRegistration {

    public ShutdownRegistration(int seconds){
        Thread shutdown = new Thread(new ShutdownTrigger(seconds));
        shutdown.start();
    }

    class ShutdownTrigger implements Runnable{

        private int delayInSeconds = 0;

        public ShutdownTrigger(int delayInSeconds){
            this.delayInSeconds = delayInSeconds;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(1000* delayInSeconds);
            } catch (InterruptedException ignored) { }
            System.exit(0);
        }
    }
}
