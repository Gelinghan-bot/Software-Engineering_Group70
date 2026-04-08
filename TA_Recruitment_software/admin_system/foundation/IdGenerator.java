package TA_Recruitment_software.admin_system.foundation;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public final class IdGenerator {
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private IdGenerator() {
    }

    public static String nextId(String prefix) {
        int value = COUNTER.updateAndGet(old -> (old + 1) % 100000);
        return prefix + "_" + Instant.now().toEpochMilli() + "_" + value;
    }
}
