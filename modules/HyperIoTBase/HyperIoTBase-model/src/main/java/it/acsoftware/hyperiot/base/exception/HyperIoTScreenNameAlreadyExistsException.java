package it.acsoftware.hyperiot.base.exception;

/**
 * @author Aristide Cittadino Model class for HyperIoTValidationException. It is
 * used to describe any constraint violation that occurs during runtime
 * exceptions.
 */
public class HyperIoTScreenNameAlreadyExistsException extends HyperIoTRuntimeException {
    /**
     * A unique serial version identifier
     */
    private static final long serialVersionUID = 1L;

    private String screeNameValue;

    private String fieldName;

    public HyperIoTScreenNameAlreadyExistsException(String fieldName,String screeNameValue) {
        super("Screen name already exists");
        this.screeNameValue = screeNameValue;
        this.fieldName = fieldName;
    }

    public String getScreeNameValue() {
        return screeNameValue;
    }

    public String getFieldName() {
        return fieldName;
    }
}
