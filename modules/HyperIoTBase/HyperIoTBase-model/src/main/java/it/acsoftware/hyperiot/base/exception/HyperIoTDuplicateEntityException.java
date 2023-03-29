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
