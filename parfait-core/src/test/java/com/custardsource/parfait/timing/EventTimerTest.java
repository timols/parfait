package com.custardsource.parfait.timing;

import junit.framework.TestCase;

import com.custardsource.parfait.MonitorableRegistry;
import com.custardsource.parfait.timing.EventCounters;

public class EventTimerTest extends TestCase {

    private EventTimer metricFactory;
    private Timeable workflowWizardControl;
    private Timeable logonControl;
    private Timeable attachmentControl;

    @Override
    protected void setUp() throws Exception {
        initEvents();
    }

    private void initEvents() {
        MonitorableRegistry.clearDefaultRegistry();
        metricFactory = new EventTimer("test");
        workflowWizardControl = new DummyTimeable();
        logonControl = new DummyTimeable();
        attachmentControl = new DummyTimeable();

    }

    public void testTotalMonitoredCounterSize() {

        metricFactory.registerTimeable(workflowWizardControl, "/WorkFlowWizard");

        /**
         * This total value includes the invocation count counter, which is stored separately to the
         * other counters in the counter set object. So when comparing the number of counters for
         * each event, this should not be taken into consideration.
         */
        Integer totalEventCounterSize = metricFactory.getNumberOfTotalEventCounters();
        EventCounters wizardCounterSet = metricFactory
                .getCounterSetForEvent(workflowWizardControl);
        Integer numberOfMetricCounters = wizardCounterSet.numberOfTimerCounters();
        assertEquals("Should be the same number of per event and total counters",
                --totalEventCounterSize, numberOfMetricCounters);

        metricFactory.registerTimeable(logonControl, "/Logon");

        assertEquals(
                "Number of total event counters should not change after adding event",
                ++totalEventCounterSize, metricFactory.getNumberOfTotalEventCounters());
        EventCounters logonCounterSet = metricFactory
                .getCounterSetForEvent(logonControl);
        assertEquals("Should be the same number of per event and total counters",
                --totalEventCounterSize, logonCounterSet.numberOfTimerCounters());

        metricFactory.registerTimeable(attachmentControl, "/Attachments");
        assertEquals(
                "Number of total event counters should not change after adding event",
                ++totalEventCounterSize, metricFactory.getNumberOfTotalEventCounters());
        EventCounters attachmentCounterSet = metricFactory
                .getCounterSetForEvent(attachmentControl);
        assertEquals("Should be the same number of per event and total counters",
                --totalEventCounterSize, attachmentCounterSet.numberOfTimerCounters());

    }

    public void testTotalMonitoredCounterSingletons() {

        metricFactory.registerTimeable(logonControl, "/Logon");
        metricFactory.registerTimeable(workflowWizardControl, "/WorkflowWizard");
        metricFactory.registerTimeable(attachmentControl, "/Attachments");

        EventCounters wizardCounterSet = metricFactory.getCounterSetForEvent(workflowWizardControl);
        EventCounters logonCounterSet = metricFactory.getCounterSetForEvent(logonControl);
        EventCounters attachmentsCounterSet = metricFactory
                .getCounterSetForEvent(attachmentControl);

        assertNotNull("Couldnt obtain counter set for workflow wizard event", wizardCounterSet);
        assertNotNull("Couldnt obtain counter set for logon event", logonCounterSet);
        assertNotNull("Couldnt obtain counter set for attachments event", attachmentsCounterSet);

        for (ThreadMetric metric : wizardCounterSet.getMetrics().keySet()) {
            EventMetricCounters wizardCounter = wizardCounterSet.getMetrics().get(metric);
            assertNotNull("Couldnt obtain wizard counter for metric " + metric.getMetricName(),
                    wizardCounter);
            EventMetricCounters logonCounter = logonCounterSet.getMetrics().get(metric);
            assertNotNull("Couldnt obtain logon counter for metric " + metric.getMetricName(),
                    logonCounter);
            EventMetricCounters attachmentsCounter = attachmentsCounterSet.getMetrics()
                    .get(metric);
            assertNotNull(
                    "Couldnt obtain attachments counter for metric " + metric.getMetricName(),
                    attachmentsCounter);

            assertEquals("Total counter for metric " + metric + " is not a singleton",
                    wizardCounter.getTotalCounter(), logonCounter.getTotalCounter());
            assertEquals("Total counter for metric " + metric + " is not a singleton", logonCounter
                    .getTotalCounter(), attachmentsCounter.getTotalCounter());

        }

    }
    
    public static class DummyTimeable implements Timeable {
        public void setEventTimer(EventTimer timer) {
        }
    }
}