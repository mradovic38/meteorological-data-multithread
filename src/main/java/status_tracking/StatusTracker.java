package status_tracking;

import command_processing.Command;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StatusTracker {
    private static final Map<String, JobStatus> jobs = new ConcurrentHashMap<>();
    private static final Map<String, Command> jobLoads = new ConcurrentHashMap<>();

    // prosledjivati null kao cmd ako nije Pending
    public static void updateStatus(String jobId, JobStatus status, Command cmd) {
        jobs.put(jobId, status);

        if(status == JobStatus.PENDING) {
            if(cmd == null)
                System.out.println("FORGOT TO ADD CMD TO PENDING STATUS");
            else
                jobLoads.put(jobId, cmd);
        }
        else{
            jobLoads.remove(jobId);
        }

        if(status == JobStatus.COMPLETED || status == JobStatus.FAILED) {
            System.out.println(getStatus(jobId));
            jobs.remove(jobId);
        }
    }

    public static Map<String, Command> getJobLoads() {
        return jobLoads;
    }


    public static String getStatus(String jobId) {
        JobStatus status = jobs.getOrDefault(jobId, JobStatus.UNKNOWN);
        return "[STATUS] " + jobId + " is " + status.toString().toLowerCase();
    }

    public enum JobStatus {
        PENDING, RUNNING, COMPLETED, FAILED, UNKNOWN
    }
}