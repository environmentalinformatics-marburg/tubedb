package tsdb.remote;

import java.security.Security;


import org.tinylog.Logger;

/**
 * This class helps to configure deserialization.
 */
public class DeserializationConfig {
	

    /**
     * This filter specifies classes that are allowed for
     deserialization in RMI communications.
     *
     * @see <a href="http://openjdk.java.net/jeps/290">JEP 290: Filter Incoming Serialization Data</a>
     */
    private static final String DEFAULT_DESERIALIZATION_FILTER =
            String.join(";",
                    "tsdb.**",
                    "java.lang.Boolean",  "java.lang.Byte",
                    "java.lang.Character", "java.lang.Double", "java.lang.Enum",
                    "java.lang.Float", "java.lang.Integer", "java.lang.Long",
                    "java.lang.Number", "java.lang.Object",
                    "java.lang.Short",
                    "java.util.*",
                    "!*"
            );

    /**
     * Returns a process-wide deserialization filter.
     */
    private static String getSerialFilter() {
        String filter = System.getProperty("jdk.serialFilter");
        if (filter != null) {
            return filter;
        }
        return Security.getProperty("jdk.serialFilter");
    }

    /**
     * Sets a process-wide deserialization filter if it is not already set.
     */
    public static void setDeserializationFilterIfNecessary() {
        String filter = getSerialFilter();
        if (filter == null) {
        	//System.out.printf("Use the following filter for deserialization:%n%s%n", DEFAULT_DESERIALIZATION_FILTER);
            Logger.info("inserting deserialization filter, used on Java 9 or newer only");
            System.setProperty("jdk.serialFilter", DEFAULT_DESERIALIZATION_FILTER);
        }
    }
}
