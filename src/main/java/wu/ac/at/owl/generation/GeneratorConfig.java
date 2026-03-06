package wu.ac.at.owl.generation;

import java.time.LocalDateTime;
import java.time.ZoneId;


public class GeneratorConfig {

    private final int admissionFormsPerPatient;

    // Epoch milliseconds instead of LocalDateTime
    private final long minTimeMillis;
    private final long maxTimeMillis;

    private final String inputFile;
    private String outputFile;

    public GeneratorConfig(
            String inputFile,
            String outputFile,
            int admissionFormsPerPatient,
            long minTimeMillis,
            long maxTimeMillis
    ) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.admissionFormsPerPatient = admissionFormsPerPatient;
        this.minTimeMillis = minTimeMillis;
        this.maxTimeMillis = maxTimeMillis;
    }

    public int getAdmissionFormsPerPatient() {
        return admissionFormsPerPatient;
    }

    public long getMinTimeMillis() {
        return minTimeMillis;
    }

    public long getMaxTimeMillis() {
        return maxTimeMillis;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public String getInputFile() {
        return inputFile;
    }

    public String getOutputFile() {
        return outputFile;
    }

  
    public static GeneratorConfig fromDateTimes(
            String inputFile,
            String outputFile,
            int admissionFormsPerPatient,
            LocalDateTime minTime,
            LocalDateTime maxTime
    ) {
        return new GeneratorConfig(
                inputFile,
                outputFile,
                admissionFormsPerPatient,
                toMillis(minTime),
                toMillis(maxTime)
        );
    }

    private static long toMillis(LocalDateTime time) {
        return time.atZone(ZoneId.systemDefault())
                   .toInstant()
                   .toEpochMilli();
    }
}
