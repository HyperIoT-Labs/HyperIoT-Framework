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

package it.acsoftware.hyperiot.contentrepository.model;

public enum DocumentResourceType {


    FOLDER_TYPE(Names.FOLDER_TYPE),
    FILE_TYPE(Names.FILE_TYPE);

    private String name;

    /**
     * Role Action with the specified name.
     *
     * @param name parameter that represent the ContentRepository  action
     */
    private DocumentResourceType(String name) {
        this.name = name;
    }

    /**
     * Gets the name of ContentRepository action
     */
    public String getName() {
        return name;
    }

    public static class Names {

        private static final String FOLDER_TYPE = "FOLDER_TYPE";

        private static final String FILE_TYPE = "FILE_TYPE";

        private Names() {
            throw new IllegalStateException("Utility class");
        }
    }
}
