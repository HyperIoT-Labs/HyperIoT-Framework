package it.acsoftware.hyperiot.osgi.util.filter;

/**
 * Author Aristide Cittadino
 * OSGi filter builder
 */
public class OSGiFilterBuilder {

	public static OSGiFilter createFilter(String name, String value) {
		return new OSGiPropertyFilter(name, value);
	}

}
