package it.acsoftware.hyperiot.base.model;

import it.acsoftware.hyperiot.base.api.HyperIoTResource;
import it.acsoftware.hyperiot.base.exception.HyperIoTDuplicateEntityException;
import it.acsoftware.hyperiot.base.exception.HyperIoTScreenNameAlreadyExistsException;
import it.acsoftware.hyperiot.base.exception.HyperIoTValidationException;
import it.acsoftware.hyperiot.base.messages.HyperIoTValidationMessages;
import it.acsoftware.hyperiot.base.util.HyperIoTErrorConstants;
import it.acsoftware.hyperiot.base.validation.PasswordMustMatch;
import it.acsoftware.hyperiot.base.validation.ValidPassword;

import javax.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Aristide Cittadino Model class for HyperIoTBaseError. It is used to
 * map, in json format, all error messages produced by exceptions during
 * interaction with the HyperIoT platform.
 */
public class HyperIoTBaseError {

    /**
     * int status code of error
     */
    private int statusCode;
    /**
     * String type of error
     */
    private String type;
    /**
     * List of error messages
     */
    private List<String> errorMessages;

    /**
     * Validation Errors
     */
    private List<HyperIoTValidationError> validationErrors;

    /**
     * Empty constructor for HyperIoTBaseError
     */
    public HyperIoTBaseError() {
        this.errorMessages = new ArrayList<>();
        this.validationErrors = new ArrayList<>();
    }

    /**
     * Constructor with parameters for HyperIoTBaseError
     *
     * @param statusCode    parameter that indicates status code of error
     * @param type          parameter that indicates type of error
     * @param errorMessages parameter that indicates list of error messages
     */
    public HyperIoTBaseError(int statusCode, String type, List<String> errorMessages) {
        this.statusCode = statusCode;
        this.type = type;
        this.errorMessages = errorMessages;
        this.validationErrors = new ArrayList<>();
    }

    /**
     * Constructor with parameters for HyperIoTBaseError
     *
     * @param statusCode parameter that indicates status code of error
     * @param type       parameter that indicates type of error
     */
    public HyperIoTBaseError(int statusCode, String type) {
        this.statusCode = statusCode;
        this.type = type;
        this.errorMessages = new ArrayList<>();
        this.validationErrors = new ArrayList<>();
    }

    /**
     * Gets status code of error
     *
     * @return Status code of error
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Sets status code of error
     *
     * @param statusCode parameter that set up status code of error
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Gets type of error
     *
     * @return Type of error
     */
    public String getType() {
        return type;
    }

    /**
     * Sets type of error
     *
     * @param type parameter that set up type of error
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets list of error messages
     *
     * @return List of error messages
     */
    public List<String> getErrorMessages() {
        return errorMessages;
    }

    /**
     * Sets list of error messages
     *
     * @param errorMessages parameter that indicates list of error messages
     */
    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }

    /**
     * @return
     */
    public List<HyperIoTValidationError> getValidationErrors() {
        return validationErrors;
    }

    /**
     * @param validationErrors
     */
    public void setValidationErrors(List<HyperIoTValidationError> validationErrors) {
        this.validationErrors = validationErrors;
    }

    protected static Logger log = LoggerFactory.getLogger(HyperIoTBaseError.class.getName());

    /**
     * Return errors produced during interaction with HyperIoT platform
     *
     * @param t          parameter that indicates all exceptions in HyperIoT
     *                   platform
     * @param messages   parameter that indicates list of error messages
     * @param statusCode parameter that indicates status code of error
     * @return errors and exceptions produced during interaction with HyperIoT
     * platform
     */
    public static HyperIoTBaseError generateHyperIoTError(Throwable t, List<String> messages, int statusCode) {
        log.debug( "Invoking generateHyperIoTError with throwable : " + t);
        HyperIoTBaseError errorResponse = new HyperIoTBaseError();
        errorResponse.setType(t.getClass().getName());
        if (messages != null) {
            errorResponse.setErrorMessages(messages);
        }
        if (statusCode > 0) {
            errorResponse.setStatusCode(statusCode);
        }
        return errorResponse;
    }

    /**
     * Return errors produced during interaction with HyperIoT platform
     *
     * @param t          parameter that indicates all exceptions in HyperIoT
     *                   platform
     * @param statusCode parameter that indicates status code of error
     * @return errors and exceptions produced during interaction with HyperIoT
     * platform
     */
    public static HyperIoTBaseError generateHyperIoTError(Throwable t, int statusCode) {
        log.debug( "Invoking generateHyperIoTError with throwable : " + t);
        return generateHyperIoTError(t, Collections.emptyList(), statusCode);
    }


    /**
     * Return exceptions produced during interaction with HyperIoT platform
     *
     * @param e parameter that indicates the exceptions produced during the
     *          interaction with the platform
     * @return List of exceptions produced
     */
    public static HyperIoTBaseError generateValidationError(HyperIoTValidationException e) {
        log.debug( "Invoking generateValidationError with HyperIoTValidationException " + e);
        List<HyperIoTValidationError> vErrors = new ArrayList<>();
        Iterator<ConstraintViolation<HyperIoTResource>> it = e.getViolations().iterator();
        while (it.hasNext()) {
            ConstraintViolation<HyperIoTResource> violation = it.next();
            String annotation = violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
            HyperIoTValidationError vError = null;
            String message = "";
            String path = "";
            String wrongValue = "";
            if (annotation.equalsIgnoreCase(PasswordMustMatch.class.getSimpleName())) {
                message = HyperIoTValidationMessages.PASSWORD_MUST_MATCH_VALIDATION;
                path = violation.getLeafBean().getClass().getSimpleName().toLowerCase() + "-" + "password";
                String confirmPassowordPath = violation.getLeafBean().getClass().getSimpleName().toLowerCase() + "-" + "passwordConfirm";
                wrongValue = "";
                vError = new HyperIoTValidationError(message, path, wrongValue);
                vErrors.add(vError);
                vError = new HyperIoTValidationError(message, confirmPassowordPath, wrongValue);
                vErrors.add(vError);
            } else if (annotation.equalsIgnoreCase(ValidPassword.class.getSimpleName())) {
                message = violation.getMessage();
                path = violation.getLeafBean().getClass().getSimpleName().toLowerCase() + "-" + "password";
                String confirmPassowordPath = violation.getLeafBean().getClass().getSimpleName().toLowerCase() + "-" + "passwordConfirm";
                wrongValue = "";
                vError = new HyperIoTValidationError(message, path, wrongValue);
                vErrors.add(vError);
                vError = new HyperIoTValidationError(message, confirmPassowordPath, wrongValue);
                vErrors.add(vError);
            } else {
                message = violation.getMessage();
                path = violation.getLeafBean().getClass().getSimpleName().toLowerCase() + "-" + violation.getPropertyPath().toString().toLowerCase();
                wrongValue = (violation.getInvalidValue() != null) ? violation.getInvalidValue().toString() : "";
                vError = new HyperIoTValidationError(message, path, wrongValue);
                vErrors.add(vError);
            }


            log.debug( "Add validation message: " + violation.getMessage());
        }
        HyperIoTBaseError error = generateHyperIoTError(e, HyperIoTErrorConstants.VALIDATION_ERROR);
        error.setValidationErrors(vErrors);
        return error;
    }

    public static HyperIoTBaseError generateValidationError(HyperIoTScreenNameAlreadyExistsException e) {
        HyperIoTBaseError error = generateHyperIoTError(e, HyperIoTErrorConstants.VALIDATION_ERROR);
        List<HyperIoTValidationError> vErrors = new ArrayList<>();
        HyperIoTValidationError vError = new HyperIoTValidationError(e.getMessage(), e.getFieldName(), e.getScreeNameValue());
        vErrors.add(vError);
        error.setValidationErrors(vErrors);
        return error;
    }

    /**
     * Return exceptions when trying to persist a new entity that already exists in
     * database.
     *
     * @param e parameter that indicates the exceptions produced during the
     *          interaction with the platform
     * @return List of exceptions produced
     */
    public static HyperIoTBaseError generateEntityDuplicatedError(HyperIoTDuplicateEntityException e) {
        log.debug( "Invoking generateValidationError with HyperIoTValidationException " + e);
        String[] fields = e.getUniqueFields();
        List<String> messages = new ArrayList<>();
        for (int i = 0; i < fields.length; i++) {
            messages.add(fields[i]);
            log.debug( "Add unique fields constraint: " + fields[i]);
        }
        return generateHyperIoTError(e, messages, HyperIoTErrorConstants.ENTITY_DUPLICATED_ERROR);
    }

}
