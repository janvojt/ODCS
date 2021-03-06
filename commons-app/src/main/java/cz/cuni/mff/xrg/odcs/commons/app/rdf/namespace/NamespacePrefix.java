package cz.cuni.mff.xrg.odcs.commons.app.rdf.namespace;

import cz.cuni.mff.xrg.odcs.commons.app.dao.DataObject;
import javax.persistence.*;

/**
 * Entity representing RDF namespace prefix.
 *
 * @author Jan Vojt
 */
@Entity
@Table(name = "rdf_ns_prefix")
public class NamespacePrefix implements DataObject {
	
	/**
	 * Primary key of entity.
	 */
	@Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_rdf_ns_prefix")
	@SequenceGenerator(name = "seq_rdf_ns_prefix", allocationSize = 1)
	private Long id;
	
	/**
	 * Prefix for namespace.
	 */
	@Column(length = 25)
	private String name;
	
	/**
	 * URI represented by prefix.
	 */
	@Column(name="uri", length = 255)
	private String prefixURI;

	/**
	 * Default constructor is required by JPA.
	 */
	public NamespacePrefix() {
	}
	
	/**
	 * Constructs new prefix with given name for given URI.
	 * 
	 * @param name prefix
	 * @param prefixURI URI
	 */
	public NamespacePrefix(String name, String prefixURI) {
		this.name = name;
		this.prefixURI = prefixURI;
	}
	
	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPrefixURI() {
		return prefixURI;
	}

	public void setPrefixURI(String prefixURI) {
		this.prefixURI = prefixURI;
	}
	
}
