package command_processing.command_handlers;

import command_processing.Command;
import report_generation.MapWriter;

public class ExportmapCommandHandler implements CommandHandler {

    private final MapWriter mapWriter;


    public ExportmapCommandHandler(MapWriter mapWriter) {

        this.mapWriter = mapWriter;
    }

    public void handle(Command command) {
        mapWriter.write("[EXPORTMAP] ");
    }
}
