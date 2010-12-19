package com.custardsource.parfait;

import java.util.Timer;
import java.util.TimerTask;

import com.google.common.base.Supplier;

/**
 * Designed to run code after the MonitorableRegistry has become quiet, in terms of addition of new metrics
 */
public class QuiescentRegistryListener implements MonitorableRegistryListener {
    private final Scheduler quiescentScheduler;
    private volatile long lastTimeMonitorableAdded = 0;
	private final Supplier<Long> clock;

    public QuiescentRegistryListener(final Runnable runnable, final long quietPeriodInMillis) {
    	this (runnable, new SystemTimePoller(), quietPeriodInMillis, new TimerScheduler(new Timer(QuiescentRegistryListener.class.getName())));
    }
    
    QuiescentRegistryListener(final Runnable runnable, final Supplier<Long> clock, final long quietPeriodInMillis, Scheduler scheduler) {
        this.quiescentScheduler = scheduler;
        this.clock = clock;
        quiescentScheduler.schedule(new TimerTask() {
            @Override
            public void run() {
                if (lastTimeMonitorableAdded > 0 && clock.get().longValue() >= (lastTimeMonitorableAdded + quietPeriodInMillis)) {
                    runnable.run();
                    lastTimeMonitorableAdded = 0;
                }
            }
        }, 1000, quietPeriodInMillis
        );
    }

    @Override
    public void monitorableAdded(Monitorable<?> monitorable) {
        this.lastTimeMonitorableAdded = clock.get();
    }
}
