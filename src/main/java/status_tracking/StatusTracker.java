package status_tracking;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StatusTracker {
    private static final Map<String, JobStatus> jobs = new ConcurrentHashMap<>();

    public static void updateStatus(String jobId, JobStatus status) {
        jobs.put(jobId, status);
        if(status == JobStatus.COMPLETED || status == JobStatus.FAILED) {
            System.out.println(getStatus(jobId));
            jobs.remove(jobId);
        }
    }

    public static String getStatus(String jobId) {
        JobStatus status = jobs.getOrDefault(jobId, JobStatus.UNKNOWN);
        return "[STATUS] " + jobId + " is " + status.toString().toLowerCase();
    }

    public enum JobStatus {
        PENDING, RUNNING, COMPLETED, FAILED, UNKNOWN
    }
}