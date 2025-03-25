package command_processing.command_handlers;

import command_processing.Command;

public class StatusCommandHandler {

    public static void handleStatusCommand(Command command) {
        String job = command.getArgByKey("job");
        if(job == null){
            System.err.println("Error: Missing job argument");
            return;
        }
        System.out.println();
    }
}
