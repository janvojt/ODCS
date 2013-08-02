package cz.cuni.xrg.intlib.commons.configuration;

/**
 * Exception used in relation do DPU invalid configuration.
 *
 * @author Petyr
 *
 */
public class ConfigException extends Exception {
	
    public ConfigException() {
    	super("Invalid configuration.");
    }

    public ConfigException(Throwable cause) {
        super(cause);
    }

    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }    


}
