package wu.ac.at.owl.generation;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

public class RandomUtils {

    private RandomUtils() {}

    public static Literal randomDateTime(
            Model model,
            ZonedDateTime min,
            ZonedDateTime max
    ) {
        long minMillis = min.toInstant().toEpochMilli();
        long maxMillis = max.toInstant().toEpochMilli();

        long randomMillis =
                ThreadLocalRandom.current().nextLong(minMillis, maxMillis);

        return model.createTypedLiteral(
                Instant.ofEpochMilli(randomMillis).toString(),
                XSDDatatype.XSDdateTime
        );
    }
    public static Literal randomDateTimeBetween(
            Model model,
            Literal start,
            Literal end
    ) {
        ZonedDateTime s = ZonedDateTime.parse(start.getString());
        ZonedDateTime e = ZonedDateTime.parse(end.getString());
        return randomDateTime(model, s, e);
    }
    
    public static long safeRandomEpochMillis(long a, long b) {
        long origin = Math.min(a, b);
        long bound  = Math.max(a, b) + 1; // ensure strictly greater
        return RandomUtils.randomEpochMillisSafe(origin, bound);
    }

    public static Literal randomDateTimeSafe(Model model, LocalDateTime localDateTime, LocalDateTime localDateTime2) {
        // Ensure start < end
        if (!localDateTime.isBefore(localDateTime2)) {
            localDateTime2 = localDateTime.plusSeconds(1);
        }

        // Convert start/end to epoch seconds (UTC)
        long startEpoch = localDateTime.toEpochSecond(ZoneOffset.UTC);
        long endEpoch   = localDateTime2.toEpochSecond(ZoneOffset.UTC);

        // Pick random second in range
        long randomEpoch = startEpoch + (long)((endEpoch - startEpoch) * Math.random());

        // Convert back to LocalDateTime (UTC)
        LocalDateTime randomLdt = LocalDateTime.ofEpochSecond(randomEpoch, 0, ZoneOffset.UTC);

        // Format as xsd:dateTime without timezone
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return model.createTypedLiteral(randomLdt.format(formatter),XSDDatatype.XSDdateTime);
    }
    public static long randomEpochMillisSafe(long min, long max) {
        return java.util.concurrent.ThreadLocalRandom.current().nextLong(min, max);
    }


    public static Resource randomResource(Model model, String baseUri, String prefix) {
        return model.createResource(baseUri + prefix + "-" + UUID.randomUUID());
    }
}
