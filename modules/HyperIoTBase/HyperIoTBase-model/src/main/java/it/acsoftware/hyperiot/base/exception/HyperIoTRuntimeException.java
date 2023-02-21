/*
 * Copyright 2019-2023 HyperIoT
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package it.acsoftware.hyperiot.base.exception;

/**
 * @author Aristide Cittadino Model class for HyperIoTRuntimeException. It is
 * used to map all error messages produced by runtime exceptions.
 */
public class HyperIoTRuntimeException extends RuntimeException {

    /**
     * A unique serial version identifier
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor for HyperIoTRuntimeException
     */
    public HyperIoTRuntimeException() {
        super();
    }

    /**
     * Constructor for HyperIoTRuntimeException with the specified detail message,
     * cause, suppression enabled or disabled, and writable stack trace enabled or
     * disabled.
     *
     * @param message            parameter that indicates the detail message
     * @param cause              parameter that indicates the cause of runtime
     *                           exception
     * @param enableSuppression  parameter that determines if the suppression is
     *                           enable or not
     * @param writableStackTrace parameter that determines if the stack trace should
     *                           be writable
     */
    public HyperIoTRuntimeException(String message, Throwable cause, boolean enableSuppression,
                                    boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Constructor for HyperIoTRuntimeException with the specified detail message,
     * cause.
     *
     * @param message parameter that indicates the detail message
     * @param cause   parameter that indicates the cause of runtime exception
     */
    public HyperIoTRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor for HyperIoTRuntimeException with the specified detail message.
     *
     * @param message parameter that indicates the detail message
     */
    public HyperIoTRuntimeException(String message) {
        super(message);
    }

    /**
     * Constructor for HyperIoTRuntimeException with the specified cause of runtime
     * exception.
     *
     * @param cause parameter that indicates the cause of runtime exception
     */
    public HyperIoTRuntimeException(Throwable cause) {
        super(cause);
    }

}
