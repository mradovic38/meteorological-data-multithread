package report_generation;

public class ReportGenerator implements Runnable{


    private final MapWriter mapWriter;


    public ReportGenerator(MapWriter mapWriterl){
           this.mapWriter = mapWriterl;
    }
    @Override
    public void run() {
        mapWriter.write("[REPORT_GEN] ");
    }
}
