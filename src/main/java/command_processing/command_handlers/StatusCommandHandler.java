package command_processing.command_handlers;

import command_processing.Command;

public class StatusCommandHandler implements CommandHandler {

    public StatusCommandHandler() {

    }
    public void handle(Command command) {
        String job = command.getArgByKey("job");
        if(job == null){
            System.err.println("[STATUS] Error: Job not found");
            return;
        }
        System.out.println();
    }
}
