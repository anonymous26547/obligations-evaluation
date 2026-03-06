package wu.ac.at.owl.generation;
import java.util.ArrayList;
import java.util.List;

public class TemporalCaseAssigner {
	    public static List<TemporalCase> buildTemporalCases(
	            int totalObligations,
	            long seed
	    ) {
	        int third = totalObligations / 3;
	        int casesActive=0; int casesFulfilled =0; int casesExpired =0;
	        List<TemporalCase> cases = new ArrayList<>(totalObligations);

	        for (int i = 0; i < third; i++)
	            {cases.add(TemporalCase.ELAPSE_BETWEEN_START_AND_DEADLINE);
	           casesActive++;}
	        for (int i = 0; i < third; i++)
	            {cases.add(TemporalCase.ACTION_BETWEEN_START_AND_DEADLINE);
	           casesFulfilled++; }
	        /*for (int i = 0; i < third; i++)
	            cases.add(TemporalCase.DEADLINE_BEFORE_ELAPSE);
                  */
	        // remaining obligations (if not divisible by 4)
	        while (cases.size() < totalObligations)
	            {cases.add(TemporalCase.DEADLINE_BEFORE_ELAPSE);
	            casesExpired++;}
	        //Collections.shuffle(cases, new Random(seed));
	            
	            System.out.println("number of active cases " +casesActive); 
	            System.out.println("number of fulfilled cases " +casesFulfilled); 
	            System.out.println("number of expired cases " +casesExpired);
	        return cases;
	    }
	
}
