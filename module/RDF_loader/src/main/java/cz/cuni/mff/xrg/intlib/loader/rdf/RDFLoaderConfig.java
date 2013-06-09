package cz.cuni.mff.xrg.intlib.loader.rdf;

import java.util.LinkedList;
import java.util.List;

import cz.cuni.xrg.intlib.commons.configuration.Config;
import cz.cuni.xrg.intlib.commons.data.rdf.WriteGraphType;

/**
 *
 * @author Petyr
 * @author Jiri Tomes
 *
 */
public class RDFLoaderConfig implements Config {
    public String SPARQL_endpoint;
    public String Host_name;
    public String Password;
    public List<String> GraphsUri = new LinkedList<>();
    public WriteGraphType Options; 
}