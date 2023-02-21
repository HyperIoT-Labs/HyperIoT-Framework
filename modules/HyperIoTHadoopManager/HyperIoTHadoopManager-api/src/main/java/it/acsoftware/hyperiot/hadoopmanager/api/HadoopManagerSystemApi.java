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

package it.acsoftware.hyperiot.hadoopmanager.api;

import it.acsoftware.hyperiot.base.api.HyperIoTBaseSystemApi;

import java.io.File;
import java.io.IOException;

/**
 * 
 * @author Aristide Cittadino Interface component for %- projectSuffixUC SystemApi. This
 *         interface defines methods for additional operations.
 *
 */
public interface HadoopManagerSystemApi extends HyperIoTBaseSystemApi {

    /**
     * It copies file to HDFS
     * @param file file to copy
     * @param path HDFS path
     * @param deleteSource Whether delete file if it exists or not
     * @throws IOException IOException
     */
    void copyFile(File file, String path, boolean deleteSource) throws IOException;

    /**
     * It deletes file
     * @param path Path to file
     * @throws IOException IOException
     */
    void deleteFile(String path) throws IOException;

    /**
     * It deletes folder
     * @param path Path to folder
     */
    void deleteFolder(String path) throws IOException;

}