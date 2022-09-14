package it.acsoftware.hyperiot.osgi.util.filter;

/**
 * Interface for creating OSGi query dinamically
 */
public interface OSGiFilter {
    public String getFilter();

    public OSGiFilter and(OSGiFilter filter);

    public OSGiFilter and(String propertyName, String propertyValue);

    public OSGiFilter or(OSGiFilter filter);

    public OSGiFilter or(String propertyName, String propertyValue);

    public OSGiFilter not();
}
