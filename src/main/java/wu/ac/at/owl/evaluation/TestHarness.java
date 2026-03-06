package wu.ac.at.owl.evaluation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.bridge.SLF4JBridgeHandler;

import wu.ac.at.gucont.owl.GUCONContextManager;
import wu.ac.at.gucont.owl.GUCONInputManager;
import wu.ac.at.gucont.owl.GUCONQueryService;
import wu.ac.at.gucont.owl.GUCONReasoner;

public class TestHarness {
	public static void main(String[] args) throws Exception {

		// Remove default JUL handlers
        SLF4JBridgeHandler.removeHandlersForRootLogger();
       // Route JUL → SLF4J
        SLF4JBridgeHandler.install();
        
		  if (args.length < 3) {
	            System.err.println(
	                "Usage:\n" +
	                "  java -jar testHarness.jar <abox.ttl> [elapseTime] <QUERY_KEYWORD> <ITERATION_NUMBER>\n\n" +
	                "Keywords:\n" +
	                "  OBLIGATION_STATE\n" +
	                "  REGULATED_ACTION_STATE\n" +
	                "  TEMPORAL_ACTION_STATE\n" +
	                "  EVENT_STATE"
	            );
	            System.exit(1);
	        }

		  String aboxPath;
		  String elapseTime = null;
		  String queryKeyword;
		  int iterationNb;

		  if (args.length == 4) {
		      // (elapseTime provided)
		      aboxPath     = args[0];
		      elapseTime   = args[1];
		      queryKeyword = args[2];
		      iterationNb  = Integer.parseInt(args[3]);

		  } else if (args.length == 3) {
		      // elapseTime omitted
		      aboxPath     = args[0];
		      queryKeyword = args[1];
		      iterationNb  = Integer.parseInt(args[2]);

		  } else {
		      System.err.println(
		          "Usage:\n" +
		          "  java -jar testHarness.jar <abox.ttl> [elapseTime] <QUERY_KEYWORD> <ITERATION_NUMBER>"
		      );
		      System.exit(1);
		      return;
		  }
		    
		    String aboxFileName = aboxPath.replace(".ttl", "");
		    //String aboxSizeStr = aboxFileName.replaceAll("[^0-9]", "");
		    String aboxSizeOblig = aboxFileName.replaceAll("[^0-9]", "");


		    
	        /* =========================
	           1. Load input ontologies
	           ========================= */
	        GUCONInputManager inputManager =
	                new GUCONInputManager(elapseTime);

	        inputManager.loadTboxFromClasspath("ontology-ocm-with-swrl.ttl");
	        inputManager.loadAbox(aboxPath);
	        //System.out.println("Abox loaded");

	        inputManager.importTboxIntoAbox();
	        
	        // Load rules once
	        
		    Runtime runtime = Runtime.getRuntime(); 
		    
		    long startMemory = runtime.totalMemory() - runtime.freeMemory();
		    long startTime = System.nanoTime();
			    
			 // GC to clean up before timing and measuring memory
			   // awaitFullGc(); 
			 //   System.gc();          // 
			  //  Thread.sleep(50);    // Allow GC to finish
			    
		      

	        /* =========================
	           2. Create context
	           ========================= */
	        GUCONContextManager ctx =
	                new GUCONContextManager(inputManager);
	        

	        /* =========================
	           3. Run GUCON reasoning
	           ========================= */

	        
	        GUCONReasoner reasoner =
	                new GUCONReasoner(ctx);
	        
		    
	        reasoner.runInferencePipeline();


	        /* =========================
	           4. Query
	           ========================= */
	        GUCONQueryService queryService =
	                new GUCONQueryService(reasoner);

	       queryService.queryByKeyword(queryKeyword);

		   // awaitFullGc();  // <- Ensure memory is cleaned up consistently
		    

	        /* =========================
	           5. Print results
	           ========================= */
	       /* results.forEach((cls, inds) -> {
	            System.out.println("\n== " + cls + " ==");
	            if (inds.isEmpty()) {
	                System.out.println("  (none)");
	            } else {
	                inds.forEach(i ->
	                        System.out.println("  " + i.getIRI()));
	            }
	        });*/
	       
		    ctx.getOrCreate().dispose();

		    long endTime = System.nanoTime();
		    long endMemory = runtime.totalMemory() - runtime.freeMemory();

		    long elapsedTimeInMillis = (endTime - startTime) / 1_000_000;
		    long memoryUsedInKB = Math.abs(endMemory - startMemory) / 1024;
		    
		    
		    //System.out.printf("Iteration %d - Time: %d ms, Memory: %d KB%n", iterationNb, elapsedTimeInMillis, memoryUsedInKB);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            // Return output as a single CSV-formatted line (no header)
            System.out.printf("%d,%s,%s,%s,%d,%d%n", iterationNb, timestamp, aboxSizeOblig, queryKeyword, elapsedTimeInMillis, memoryUsedInKB);
		
	}
}
