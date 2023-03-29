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

package it.acsoftware.hyperiot.contentrepository.util;

public final class ContentRepositoryConstants {

    public static final String CONTENT_REPOSITORY_CONFIG_FILE_NAME = "it.acsoftware.hyperiot.content.repository";

    public static final String CONTENT_REPOSITORY_DEFAULT_ADMIN_USER_ID =  "it.acsoftware.hyperiot.content.repository.default.admin.id";

    public static final String CONTENT_REPOSITORY_DEFAULT_ADMIN_PASSWORD =  "it.acsoftware.hyperiot.content.repository.default.admin.password";

    public static final String CONTENT_REPOSITORY_DEFAULT_WORKSPACE_NAME =  "it.acsoftware.hyperiot.content.repository.default.workspace.name";

    private ContentRepositoryConstants() {
        throw new IllegalStateException("Utility class");
    }

}
