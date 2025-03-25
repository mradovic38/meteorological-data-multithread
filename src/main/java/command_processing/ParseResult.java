package command_processing;

public class ParseResult {
    private final Command command;
    private final String errorMessage;

    public ParseResult(Command command) {
        this.command = command;
        this.errorMessage = null;
    }

    public ParseResult(String errorMessage) {
        this.command = null;
        this.errorMessage = errorMessage;
    }

    public boolean hasErrors() {
        return errorMessage != null;
    }

    public Command getCommand() {
        return command;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}