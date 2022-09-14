package it.acsoftware.hyperiot.base.exception;

import it.acsoftware.hyperiot.base.api.HyperIoTResource;

import javax.validation.ConstraintViolation;
import java.util.Set;

/**
 * @author Aristide Cittadino Model class for HyperIoTValidationException. It is
 * used to describe any constraint violation that occurs during runtime
 * exceptions.
 */
public class HyperIoTValidationException extends HyperIoTRuntimeException {
    /**
     * A unique serial version identifier
     */
    private static final long serialVersionUID = 1L;

    /**
     * Collection that contains constraint violations
     */
    private Set<ConstraintViolation<HyperIoTResource>> violations;

    /**
     * Constructor for HyperIoTValidationException
     *
     * @param violations parameter that indicates constraint violations produced
     */
    public HyperIoTValidationException(Set<ConstraintViolation<HyperIoTResource>> violations) {
        this.violations = violations;
    }

    /**
     * Gets the constraint violations
     *
     * @return Collection of constraint violations
     */
    public Set<ConstraintViolation<HyperIoTResource>> getViolations() {
        return violations;
    }

}
