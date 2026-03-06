package wu.ac.at.owl.generation;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDF;

public class GeneratorMain {

    public static void main(String[] args) throws Exception {

        //Load properties
        Properties props = new Properties();
        props.load(new FileInputStream("benchmark.properties"));

        String[] datasets = props.getProperty("datasets").split(",");

        String inputDir = props.getProperty("inputDir");
        String outputDir = props.getProperty("outputDir");

        int admissionsPerPatient =
                Integer.parseInt(props.getProperty("admissionsPerPatient"));

        LocalDateTime start =
                LocalDateTime.parse(props.getProperty("start"));

        LocalDateTime end =
                LocalDateTime.parse(props.getProperty("end"));

        long seed =
                Long.parseLong(props.getProperty("seed"));

        // Loop over datasets
        for (String dataset : datasets) {

            dataset = dataset.trim();

            String inputFile = inputDir + "patients-" + dataset + ".ttl";
            String outputFile = null ;

            System.out.println("\n=== DATASET " + dataset + " ===");
            System.out.println("Input: " + inputFile);

            // Load patients
            Model patientModel = RDFDataMgr.loadModel(inputFile);

            Resource Patient = patientModel.createResource(
                    ObligationGenerator.EMR + "Patient");

            List<Resource> patients =
                    patientModel.listResourcesWithProperty(RDF.type, Patient).toList();

            System.out.println("Patients: " + patients.size());

            // Output model
            Model out = ModelFactory.createDefaultModel();
            out.setNsPrefix("ex", ObligationGenerator.EX);
            out.setNsPrefix("emr", ObligationGenerator.EMR);
            out.setNsPrefix("gc", ObligationGenerator.GC);
            out.setNsPrefix("gem", ObligationGenerator.GEM);

            // ✅ Config
            GeneratorConfig config = GeneratorConfig.fromDateTimes(
                    inputFile,
                    outputFile,
                    admissionsPerPatient,
                    start,
                    end
            );

            int totalObligations =
                    patients.size() * config.getAdmissionFormsPerPatient();
            
          
            
            System.out.println("Admissions per patient: " + admissionsPerPatient);
            System.out.println("Total obligations: " + totalObligations);
            

            // Temporal cases
            List<TemporalCase> temporalCases =
                    TemporalCaseAssigner.buildTemporalCases(totalObligations, seed);

            // Generate
            ObligationGenerator generator =
                    new ObligationGenerator(config, temporalCases);

            generator.generate(out, patients);
            
            // get number of generated triples and update output file name
            
            long nbOfGeneratedTriples = out.size();
            outputFile = outputDir + "generated-obligations-" + totalObligations + ".ttl";
            config.setOutputFile(outputFile);
            
           // System.out.println("Total triples: " + nbOfGeneratedTriples);


            // Write output
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                RDFDataMgr.write(fos, out, RDFFormat.TURTLE_PRETTY);
            }

            System.out.println("Generated triples: " + nbOfGeneratedTriples);
        }
    }
}
