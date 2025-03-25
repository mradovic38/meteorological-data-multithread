package command_processing.command_handlers;

import command_processing.Command;

public interface CommandHandler {
    void handle(Command command);
}
