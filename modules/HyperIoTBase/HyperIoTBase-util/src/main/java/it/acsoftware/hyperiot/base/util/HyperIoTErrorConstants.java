/*
 * Copyright 2019-2023 ACSoftware
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
