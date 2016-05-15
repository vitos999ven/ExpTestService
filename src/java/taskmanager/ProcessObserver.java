package taskmanager;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import taskmanager.tasks.ProcessTask;

public class ProcessObserver<T> {

    private static final int timeToWait = 100;
    private volatile int currentIteration = 0;
    private final int iterationsCount;
    private Future<T> future = null;
    private final ProcessTask task;
    private volatile boolean finished = false;

    public ProcessObserver(ProcessTask task, int iterationsCount) {
        this.task = task;
        this.iterationsCount = iterationsCount;
    }
    
    public void setFuture(Future<T> future) {
        this.future = future;
    }

    public void increment() {
        ++currentIteration;
    }
    
    public void increment(int value) {
        currentIteration += value;
    }
    
    public void finish() {
        currentIteration = iterationsCount;
        finished = true;
    }
    
    public ProcessTask getTask() {
        return task;
    }

    public Double status() {
        return ((double) currentIteration) / iterationsCount;
    }
    
    public T get() throws InterruptedException, ExecutionException, TimeoutException {
        if (future == null) {
            return null;
        }
        return future.get(timeToWait, TimeUnit.MILLISECONDS);
    }

    public boolean isFinished() {
        return finished;
    }
    
    public static void incrementIfExists(ProcessObserver observer) {
        if (observer != null) {
            observer.increment();
        }
    }
    
    public static void incrementIfExists(ProcessObserver observer, int value) {
        if (observer != null) {
            observer.increment(value);
        }
    }
}
