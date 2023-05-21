package utils;

import java.util.LinkedList;
import java.util.Queue;

public class CustomExecutorService {
    private final int numThreads;
    private final Thread[] threads;
    private final RunnableQueue taskQueue;
    private volatile boolean isShutdown;

    public CustomExecutorService(int numThreads) {
        this.numThreads = numThreads;
        this.threads = new Thread[numThreads];
        this.taskQueue = new RunnableQueue();
        this.isShutdown = false;
        initializeThreads();
    }

    public void submit(Runnable task) {
        synchronized (taskQueue) {
            if (isShutdown) {
                throw new IllegalStateException("ExecutorService has been shut down");
            }
            taskQueue.offer(task);
            taskQueue.notify();
        }
    }

    public void shutdown() {
        synchronized (taskQueue) {
            isShutdown = true;
            taskQueue.notifyAll();
        }
    }

    private void initializeThreads() {
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                while (true) {
                    Runnable task;
                    synchronized (taskQueue) {
                        while (taskQueue.isEmpty() && !isShutdown) {
                            try {
                                taskQueue.wait();
                            } catch (InterruptedException e) {
                                // Handle interrupted exception if needed
                            }
                        }
                        if (isShutdown) {
                            break;
                        }
                        task = taskQueue.poll();
                    }
                    try {
                        task.run();
                    } catch (Exception e) {
                        // Handle exception if needed
                    }
                }
            });
            threads[i].start();
        }
    }

    private static class RunnableQueue {
        private final Queue<Runnable> queue;

        public RunnableQueue() {
            this.queue = new LinkedList<>();
        }

        public synchronized void offer(Runnable runnable) {
            queue.offer(runnable);
        }

        public synchronized Runnable poll() {
            return queue.poll();
        }

        public synchronized boolean isEmpty() {
            return queue.isEmpty();
        }
    }
}
