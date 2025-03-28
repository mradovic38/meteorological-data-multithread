package status_tracking;

import command_processing.Command;
import utils.JobInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.Comparator;

public class StatusTracker {
    public static final Map<String, JobInfo> jobs = new ConcurrentHashMap<>();



    public static synchronized void updateStatus(String jobId, JobStatus status, Command cmd) {
        JobInfo jobInfo = jobs.get(jobId);
        if (jobInfo != null) {
            jobInfo.setStatus(status);
            jobInfo.setCommand(cmd);
        } else {
            // zbog timestampa
            jobs.put(jobId, new JobInfo(status, cmd));
        }
        System.out.println(getStatus(jobId));
    }


    public static String getStatus(String jobId) {
        JobInfo jobInfo = jobs.get(jobId);
        if (jobInfo == null) {
            return "[STATUS] " + jobId + " is unknown.";
        }
        return "[STATUS] " + jobId + " is " + jobInfo.getStatus().toString().toLowerCase() + ".";
    }

    public static List<String> getCommandsForRunningOrPendingJobs() {
        return jobs.values().stream()
                .filter(jobInfo -> jobInfo.getStatus() == JobStatus.PENDING || jobInfo.getStatus() == JobStatus.RUNNING)
                .filter(jobInfo -> jobInfo.getCommand() != null)  // Skip jobs with null command
                .sorted(Comparator.comparing(JobInfo::getTimestamp))  // Sort by timestamp (oldest first)
                .map(jobInfo -> jobInfo.getCommand().toString())  // Get the toString() of the command
                .collect(Collectors.toList());
    }

    public enum JobStatus {
        PENDING, RUNNING, COMPLETED, FAILED, CANCELLED, UNKNOWN
    }
}