package it.acsoftware.hyperiot.osgi.util.filter;

/**
 * Author Aristide Cittadino
 * OSGi Filter OR Condition
 */
public class OSGiFilterOrCondition implements OSGiFilter {
    private OSGiFilter first;
    private OSGiFilter second;
    private boolean not;

    public OSGiFilterOrCondition(OSGiFilter first, OSGiFilter second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public String getFilter() {
        String filterCondition = "(|" + first.getFilter() + second.getFilter() + ")";
        if (!not)
            return filterCondition;
        else
            return "(!" + filterCondition + ")";
    }

    @Override
    public OSGiFilter and(OSGiFilter filter) {
        return new OSGiFilterOrCondition(this, filter);
    }

    @Override
    public OSGiFilter and(String propertyName, String propertyValue) {
        return new OSGiFilterAndCondition(this, new OSGiPropertyFilter(propertyName, propertyValue));
    }

    @Override
    public OSGiFilter or(OSGiFilter filter) {
        return new OSGiFilterOrCondition(this, filter);
    }

    @Override
    public OSGiFilter or(String propertyName, String propertyValue) {
        return new OSGiFilterOrCondition(this, new OSGiPropertyFilter(propertyName, propertyValue));
    }

    @Override
    public OSGiFilter not() {
        not = true;
        return this;
    }

}
