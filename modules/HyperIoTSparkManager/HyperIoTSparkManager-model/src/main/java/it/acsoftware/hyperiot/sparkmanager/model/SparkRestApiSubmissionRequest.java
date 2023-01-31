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

package it.acsoftware.hyperiot.sparkmanager.model;

import java.util.Map;

public class SparkRestApiSubmissionRequest {

    private String action;
    private String[] appArgs;
    private String appResource;
    private String clientSparkVersion;
    private Map<String, String> environmentVariables;
    private String mainClass;
    private Map<String, String> sparkProperties;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String[] getAppArgs() {
        return appArgs;
    }

    public void setAppArgs(String[] appArgs) {
        this.appArgs = appArgs;
    }

    public String getAppResource() {
        return appResource;
    }

    public void setAppResource(String appResource) {
        this.appResource = appResource;
    }

    public String getClientSparkVersion() {
        return clientSparkVersion;
    }

    public void setClientSparkVersion(String clientSparkVersion) {
        this.clientSparkVersion = clientSparkVersion;
    }

    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    public void setEnvironmentVariables(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public Map<String, String> getSparkProperties() {
        return sparkProperties;
    }

    public void setSparkProperties(Map<String, String> sparkProperties) {
        this.sparkProperties = sparkProperties;
    }

}
