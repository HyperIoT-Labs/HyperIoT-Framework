package it.acsoftware.hyperiot.base.model;

/**
 * Author Aristide Cittadino
 */
public class HyperIoTValidationError {
    /**
     * Error message as placeholder
     */
    private String message;
    /**
     * Field Path
     */
    private String field;
    /**
     * Invalid value
     */
    private String invalidValue;

    public HyperIoTValidationError(String message, String field, String invalidValue) {
        this.message = message;
        this.field = field;
        this.invalidValue = invalidValue;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getInvalidValue() {
        return invalidValue;
    }

    public void setInvalidValue(String invalidValue) {
        this.invalidValue = invalidValue;
    }
}
