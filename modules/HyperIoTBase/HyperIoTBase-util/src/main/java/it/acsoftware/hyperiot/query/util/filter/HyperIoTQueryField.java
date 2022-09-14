package it.acsoftware.hyperiot.query.util.filter;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

/**
 * This class maps the concept of Query entity field
 */
public class HyperIoTQueryField<T> {

    private String name;

    public HyperIoTQueryField(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     *
     * @return
     */
    public Path<?> getPath(Root<T> entityDef) {
        String[] dottedRelationships = name.split("\\.");
        Path<?> p = entityDef.get(dottedRelationships[0]);
        for (int i = 1; i < dottedRelationships.length; i++) {
            p = p.get(dottedRelationships[i]);
        }
        return p;
    }

}
