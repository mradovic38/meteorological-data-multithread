package tasks;

import command_processing.Command;

import java.util.concurrent.Callable;

public abstract class Task implements Callable<String> {
    protected final Command command;

    public Task(Command command) {
        this.command = command;
    }

    public Command getCommand() {
        return command;
    }
}
