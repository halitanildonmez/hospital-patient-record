package nl.gerimedica.assignment.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class HospitalUtils {

    private static final AtomicInteger usageCounter = new AtomicInteger(0);

    public static void recordUsage(String context) {
        int currentCount = usageCounter.incrementAndGet();
        log.info("HospitalUtils used. Counter: {} | Context: {}", currentCount, context);
    }

    public static boolean isPayloadInvalid(List<String> reasons, List<String> dates) {
        return (reasons == null || dates == null || reasons.isEmpty() || dates.isEmpty());
    }
}
