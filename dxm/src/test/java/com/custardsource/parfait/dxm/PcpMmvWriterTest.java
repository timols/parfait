package com.custardsource.parfait.dxm;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.measure.unit.Unit;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.custardsource.parfait.dxm.semantics.Semantics;
import com.custardsource.parfait.dxm.types.MmvMetricType;
import com.custardsource.parfait.dxm.types.TypeHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class PcpMmvWriterTest {

    private PcpMmvWriter pcpMmvWriter;

    @Before
    public void setUp(){
        pcpMmvWriter = new PcpMmvWriter(new InMemoryByteBufferFactory(), IdentifierSourceSet.DEFAULT_SET);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureThatClusterIdentifierIsRestrictedTo12BitsOnly() throws Exception {
        pcpMmvWriter.setClusterIdentifier(1<<13);
    }

    @Test
    public void ensureValid12BitIdentifierIsAllowed(){
        pcpMmvWriter.setClusterIdentifier(1);
    }

    @Test
    public void ensureBoundaryCaseOf12thBitIsOk(){
        pcpMmvWriter.setClusterIdentifier(1<<11);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureNegativeNumbersAreTreatedWithAppropriateContempt() {
        pcpMmvWriter.setClusterIdentifier(-1);

    }

    @Test
    @SuppressWarnings("unchecked")
    public void ensureUpdatesAreIgnoredAfterResetIsCalled() throws IOException {
        MetricName metricName = MetricName.parse("some.metric");
        TypeHandler<CustomType> customTypeTypeHandler = mock(TypeHandler.class);
        when(customTypeTypeHandler.getMetricType()).thenReturn(MmvMetricType.DOUBLE);
        pcpMmvWriter.registerType(CustomType.class, customTypeTypeHandler);
        pcpMmvWriter.start();
        pcpMmvWriter.reset();
        Mockito.reset(customTypeTypeHandler);
        pcpMmvWriter.addMetric(metricName, Semantics.COUNTER, Unit.ONE, new CustomType());
        pcpMmvWriter.updateMetric(metricName, new CustomType());
        verify(customTypeTypeHandler, never()).putBytes(any(ByteBuffer.class), any(CustomType.class));
    }

    @Test(expected = IllegalStateException.class)
    public void ensureSetPerMetricLockIsNotAllowedWhenWriterIsStarted() throws IOException {
        pcpMmvWriter.start();
        pcpMmvWriter.setPerMetricLock(true);
    }

    @Test
    public void ensureSetPerMetricLockIsAllowedAfterResetIsCalled() throws IOException {
        pcpMmvWriter.start();
        pcpMmvWriter.reset();
        pcpMmvWriter.setPerMetricLock(true);
    }

    class CustomType {}

}

