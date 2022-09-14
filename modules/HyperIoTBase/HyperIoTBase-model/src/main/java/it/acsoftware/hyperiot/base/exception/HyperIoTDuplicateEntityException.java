package it.acsoftware.hyperiot.base.exception;

/**
 * @author Aristide Cittadino Model class for HyperIoTDuplicateEntityException.
 * It is used to map, in json format, all error messages when tries to
 * persist a new entity that already exists in database.
 */
public class HyperIoTDuplicateEntityException extends HyperIoTRuntimeException {
    /**
     * A unique serial version identifier
     */
    private static final long serialVersionUID = 1L;

    /**
     * array of {@code String}s unique fields, used to return exception if the value
     * is not unique
     */
    private String[] uniqueFields;

    /**
     * Constructor for HyperIoTDuplicateEntityException
     *
     * @param uniqueFields parameter that indicates unique field of entity
     */
    public HyperIoTDuplicateEntityException(String[] uniqueFields) {
        super();
        this.uniqueFields = uniqueFields;
    }

    /**
     * Gets {@code String}s unique fields of entity
     *
     * @return array of {@code String}s unique fields
     */
    public String[] getUniqueFields() {
        return uniqueFields;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < uniqueFields.length; i++) {
            if (i > 0)
                sb.append(",");
            sb.append(this.uniqueFields[i]);
        }
        return sb.toString();
    }

}
