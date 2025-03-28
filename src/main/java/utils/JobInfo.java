package utils;

import command_processing.Command;
import status_tracking.StatusTracker;

public class JobInfo {
    private StatusTracker.JobStatus status;
    private Command command;
    private final long timestamp;


    public JobInfo(StatusTracker.JobStatus status, Command command) {
        this.status = status;
        this.command = command;
        this.timestamp = System.currentTimeMillis();
    }

    public StatusTracker.JobStatus getStatus() {
        return status;
    }

    public Command getCommand() {
        return command;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setStatus(StatusTracker.JobStatus status) {
        this.status = status;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return "JobInfo{" +
                "status=" + status +
                ", command=" + command +
                '}';
    }
}