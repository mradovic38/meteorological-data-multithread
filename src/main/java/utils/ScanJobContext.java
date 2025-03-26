package utils;

import command_processing.Command;

import java.util.concurrent.atomic.AtomicBoolean;

public class ScanJobContext {
    private String jobId;
    private Command command;
    private final AtomicBoolean cancelledFlag;

    public ScanJobContext(String jobId, Command command, AtomicBoolean cancelledFlag) {
        this.jobId = jobId;
        this.command = command;
        this.cancelledFlag = cancelledFlag;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public boolean getCancelledFlag() {
        return cancelledFlag.get();
    }

    public void setCancelledFlag(boolean cancelledFlag) {
        this.cancelledFlag.set(cancelledFlag);
    }
}