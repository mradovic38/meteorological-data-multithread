package utils;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;

public class StationStats {
    private final AtomicInteger count;
    private final DoubleAdder sum; // hteo sam AtomicReference<BigInt> i <BigDecimal> al je presporo

    public StationStats() {
        this.count = new AtomicInteger(0);
        this.sum = new DoubleAdder();
    }

    public void addMeasurement(double temperature) {
        count.incrementAndGet();
        sum.add(temperature);
    }

    public StationStatsSnapshot getSnapshot() {
        return new StationStatsSnapshot(count.get(), sum.sum());
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
