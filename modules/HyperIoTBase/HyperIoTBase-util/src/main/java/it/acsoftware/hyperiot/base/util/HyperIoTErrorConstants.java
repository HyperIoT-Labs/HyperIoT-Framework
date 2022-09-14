package it.acsoftware.hyperiot.base.util;

/**
 * @author Aristide Cittadino Model class for HyperIoTErrorConstants. It is used
 * to define all constants that contain the error codes. Moreover these
 * constants are used when exceptions occur during interaction with the
 * HyperIoT platform.
 */
public class HyperIoTErrorConstants {
    public static final int NOT_AUTHORIZED_ERROR = 403;
    public static final int ENTITY_NOT_FOUND_ERROR = 404;
    public static final int ENTITY_DUPLICATED_ERROR = 409;
    public static final int VALIDATION_ERROR = 422;
    public static final int INTERNAL_ERROR = 500;
}
