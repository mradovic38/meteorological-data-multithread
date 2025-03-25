package utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.concurrent.atomic.AtomicReference;

public class StationStats {
    private AtomicReference<BigInteger> count;
    private AtomicReference<BigDecimal> sum;

    public StationStats() {
        this.count = new AtomicReference<>(BigInteger.ZERO);
        this.sum = new AtomicReference<>(BigDecimal.ZERO);
    }

    public void addMeasurement(double temperature) {
        this.count.getAndUpdate(c -> c.add(BigInteger.ONE));
        this.sum.getAndUpdate(c -> c.add(new BigDecimal(temperature, MathContext.DECIMAL64)));
    }


    public StationStatsSnapshot getSnapshot() {
        return new StationStatsSnapshot(count.get(), sum.get());
    }

    // pomocna klasa - immutable, nisu atomic
    public static class StationStatsSnapshot {
        private final BigInteger count;
        private final BigDecimal sum;

        public StationStatsSnapshot(BigInteger count, BigDecimal sum) {
            this.count = count;
            this.sum = sum;
        }

        public BigInteger getCount() { return count; }
        public BigDecimal getSum() { return sum; }

        public String toString() {
            return  count + " - " + sum;
        }
    }

}