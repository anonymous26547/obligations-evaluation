package wu.ac.at.owl.generation;

import java.time.Duration;
import java.util.List;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class ObligationGenerator {

    // Namespaces
    public static final String EX  = "http://example.org/ex#";
    public static final String EMR = "http://wu.ac.at/domain/emr#";
    public static final String GC = "http://www.wu.ac.at/2026/obligation-core-model#";
    public static final String GEM = "http://www.wu.ac.at/2026/obligation-execution-model#";

    private final GeneratorConfig config;
    List<TemporalCase> temporalCases;

    public ObligationGenerator(GeneratorConfig config, List<TemporalCase> temporalCases) {
        this.config = config;
        this.temporalCases = temporalCases;
    }

    public void generate(Model outModel, List<Resource> patients) {

        // Ontology triple
        String ontologyUri = "http://wu.ac.at/ocm/abox-" + java.util.UUID.randomUUID();
        Resource ontology = outModel.createResource(ontologyUri);
        ontology.addProperty(RDF.type, OWL.Ontology);
        ontology.addProperty(OWL.imports, outModel.createResource("http://wu.ac.at/2026/obligation-compliance-model"));

        int obligationIndex = 0;

        // Properties
        Property obligationContent   = outModel.createProperty(GC, "obligationContent");
        Property activationCondition = outModel.createProperty(GC, "activationCondition");
        Property startTime           = outModel.createProperty(GC, "numericStartTime");
        Property deadline            = outModel.createProperty(GC, "numericDeadline");
        Property atTime              = outModel.createProperty(GC, "numericAtTime");
        Property actionContent       = outModel.createProperty(GC, "actionContent");
        Property actionProp          = outModel.createProperty(GC, "action");
        Property entityProp          = outModel.createProperty(GC, "entity");
        Property resourceProp        = outModel.createProperty(GC, "resource");

        // Classes
        Resource Obligation      = outModel.createResource(GC + "Obligation");
        Resource TemporalAction  = outModel.createResource(GC + "TemporalAction");
        Resource RegulatedAction = outModel.createResource(GC + "RegulatedAction");
        Resource Patient        = outModel.createResource(EMR + "Patient");
        Resource AdmissionForm  = outModel.createResource(EMR + "AdmissionForm");
        Resource alwaysTriggered = outModel.createResource(GEM + "alwaysTriggered");
        Resource elapse          = outModel.createResource(GC + "elapse");

        // Schema (written once)
        outModel.add(Patient, RDFS.subClassOf, outModel.createResource(GC + "Entity"));
        outModel.add(AdmissionForm, RDFS.subClassOf, outModel.createResource(GC + "Resource"));
        

        outModel.add(alwaysTriggered, RDF.type, outModel.createResource(GEM + "TriggeredEvent"));

        // final elapse time (epoch millis)
        long finalElapseTime  = RandomUtils.randomEpochMillisSafe(
                config.getMinTimeMillis(),
                config.getMaxTimeMillis()
        );

        outModel.add(elapse, atTime, outModel.createTypedLiteral(finalElapseTime));


        
        
        for (Resource patient : patients) {
        	
        	// add Patient 
            outModel.add(patient, RDF.type, Patient);

            for (int i = 0; i < config.getAdmissionFormsPerPatient(); i++) {
            	
                
                TemporalCase temporalCase = temporalCases.get(obligationIndex);

                // Add Admission form
                Resource form = RandomUtils.randomResource(outModel, EMR, "admissionForm");             
                outModel.add(form, RDF.type, AdmissionForm);
                
                // Obligation resources
                Resource obligation = RandomUtils.randomResource(outModel, EX, "obligation");
                Resource tempAction = RandomUtils.randomResource(outModel, EX, "obligation-content");
                Resource regAction  = RandomUtils.randomResource(outModel, EX, "action-content");

                // Obligation
                outModel.add(obligation, RDF.type, Obligation);
                outModel.add(obligation, obligationContent, tempAction);
                outModel.add(obligation, activationCondition, alwaysTriggered);

                // Temporal action
                outModel.add(tempAction, RDF.type, TemporalAction);
                outModel.add(tempAction, actionContent, regAction);

                // Generate start & deadline using millis arithmetic
                long start = RandomUtils.randomEpochMillisSafe(
                        finalElapseTime - Duration.ofDays(10).toMillis(),
                        finalElapseTime - Duration.ofDays(5).toMillis()
                );

                long end = RandomUtils.randomEpochMillisSafe(
                        finalElapseTime + Duration.ofDays(5).toMillis(),
                        finalElapseTime + Duration.ofDays(10).toMillis()
                );


                outModel.add(tempAction, startTime, outModel.createTypedLiteral(start));
                outModel.add(tempAction, deadline, outModel.createTypedLiteral(end));

                // Apply temporal case
                switch (temporalCase) {

                    case ELAPSE_BETWEEN_START_AND_DEADLINE:
 	    	           //System.out.println("case" +temporalCase);

                    	// do nothing 
                        break;

                    case ACTION_BETWEEN_START_AND_DEADLINE:
 	    	            //System.out.println("case" +temporalCase);
                    	Literal elapseLiteral1 = outModel.listObjectsOfProperty(elapse, atTime)
                        .next().asLiteral();
						//Convert to epoch milliseconds
						long elapse1 = elapseLiteral1.getLong();
                        long actionTime = RandomUtils.randomEpochMillisSafe(
                                start + Duration.ofMinutes(1).toMillis(),
                                elapse1 - Duration.ofMinutes(1).toMillis()
                        );

                        outModel.add(regAction, atTime, outModel.createTypedLiteral(actionTime));
                        break;

                    case DEADLINE_BEFORE_ELAPSE:
 	    	            //System.out.println("case" +temporalCase);
                    	Literal elapseLiteral2 =  outModel.listObjectsOfProperty(elapse, atTime)
                        .next().asLiteral();
						long elapse2 = elapseLiteral2.getLong();
                        long newDeadline = RandomUtils.randomEpochMillisSafe(
                                start + Duration.ofHours(5).toMillis(),
                                elapse2 - Duration.ofHours(8).toMillis()
                        );

                        outModel.removeAll(tempAction, deadline, null);
                        outModel.add(tempAction, deadline, outModel.createTypedLiteral(newDeadline));
                        break;
                }

                // Regulated action
                outModel.add(regAction, RDF.type, RegulatedAction);
                outModel.add(regAction, entityProp, patient);
                outModel.add(regAction, resourceProp, form);
                
                // add radnom action
                Resource action = RandomUtils.randomResource(outModel, GC, "action");         
                outModel.add(action, RDF.type, outModel.createResource(GC + "Action"));
                outModel.add(regAction, actionProp, action);

                obligationIndex++;
            }
        }
    }
}
