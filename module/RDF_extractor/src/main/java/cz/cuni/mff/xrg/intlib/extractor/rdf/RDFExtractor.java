package cz.cuni.mff.xrg.intlib.extractor.rdf;

import cz.cuni.xrg.intlib.commons.configuration.ConfigException;
import cz.cuni.xrg.intlib.commons.configuration.Configurable;
import cz.cuni.xrg.intlib.commons.data.DataUnitCreateException;
import cz.cuni.xrg.intlib.commons.data.DataUnitType;
import cz.cuni.xrg.intlib.commons.extractor.Extract;
import cz.cuni.xrg.intlib.commons.extractor.ExtractContext;
import cz.cuni.xrg.intlib.commons.extractor.ExtractException;
import cz.cuni.xrg.intlib.commons.message.MessageType;
import cz.cuni.xrg.intlib.commons.web.AbstractConfigDialog;
import cz.cuni.xrg.intlib.commons.web.ConfigDialogProvider;
import cz.cuni.xrg.intlib.rdf.exceptions.RDFException;
import cz.cuni.xrg.intlib.rdf.interfaces.RDFDataRepository;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jiri Tomes
 * @author Petyr
 */
public class RDFExtractor implements Extract,
		Configurable<RDFExtractorConfig>, ConfigDialogProvider<RDFExtractorConfig> {

	/**
	 * DPU configuration.
	 */
	private RDFExtractorConfig config = null;

	/**
	 * Logger class.
	 */
	private static final Logger LOG = LoggerFactory
			.getLogger(RDFExtractor.class);

	@Override
	public void extract(ExtractContext context) throws ExtractException, DataUnitCreateException {

		RDFDataRepository repository =
				(RDFDataRepository) context.addOutputDataUnit(DataUnitType.RDF,
				"output");

		try {
			final URL endpointURL = new URL(config.SPARQL_endpoint);
			final String hostName = config.Host_name;
			final String password = config.Password;
			final List<String> defaultGraphsUri = config.GraphsUri;
			final String query = config.SPARQL_query;

			final boolean useStatisticHandler = config.UseStatisticalHandler;

			repository.extractfromSPARQLEndpoint(endpointURL, defaultGraphsUri,
					query, hostName, password, useStatisticHandler);
		} catch (MalformedURLException ex) {
			context.sendMessage(MessageType.ERROR,
					"MalformedURLException: " + ex.getMessage());
			throw new ExtractException(ex);
		} catch (RDFException ex) {
			throw new ExtractException(ex.getMessage(), ex);
		}
	}

	@Override
	public AbstractConfigDialog<RDFExtractorConfig> getConfigurationDialog() {
		return new RDFExtractorDialog();
	}

	@Override
	public void configure(RDFExtractorConfig c) throws ConfigException {
		config = c;
	}

	@Override
	public RDFExtractorConfig getConfiguration() {
		return config;
	}
}
