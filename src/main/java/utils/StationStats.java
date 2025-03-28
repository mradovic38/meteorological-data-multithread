package utils;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class StationStats {
    private final AtomicInteger count;
    private final AtomicReference<Double> sum; // hteo sam AtomicReference<BigInt> i <BigDecimal> al je presporo

    public StationStats() {
        this.count = new AtomicInteger(0);
        this.sum = new AtomicReference<>((double) 0);
    }

    public void addMeasurement(double temperature) {
        count.incrementAndGet();
        sum.updateAndGet(oldValue -> oldValue + temperature);
    }

    public StationStatsSnapshot getSnapshot() {
        return new StationStatsSnapshot(count.get(), sum.get());
    }

    public static class StationStatsSnapshot {
        private final int count;
        private final double sum;

        public StationStatsSnapshot(int count, double sum) {
            this.count = count;
            this.sum = sum;
        }

        public long getCount() { return count; }
        public double getSum() { return sum; }

        public String toString() {
            return count + " - " + sum;
        }
    }
}
