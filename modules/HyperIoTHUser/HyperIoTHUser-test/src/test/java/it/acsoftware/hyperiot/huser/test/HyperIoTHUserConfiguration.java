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

package it.acsoftware.hyperiot.huser.test;

import it.acsoftware.hyperiot.base.test.HyperIoTTestConfigurationBuilder;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.role.test.HyperIoTRoleConfiguration;
import org.ops4j.pax.exam.ConfigurationFactory;
import org.ops4j.pax.exam.Option;

public class HyperIoTHUserConfiguration implements ConfigurationFactory {
    public static final String CODE_COVERAGE_PACKAGE_FILTER = "it.acsoftware.hyperiot.huser.*";

    static final String hyperIoTException = "it.acsoftware.hyperiot.base.exception.";
    static final String huserResourceName = HUser.class.getName();

    static final int defaultDelta = 10;
    static final int defaultPage = 1;

    @Override
    public Option[] createConfiguration() {
        return HyperIoTTestConfigurationBuilder.createStandardConfiguration()
                .withCodeCoverage(CODE_COVERAGE_PACKAGE_FILTER, HyperIoTRoleConfiguration.CODE_COVERAGE_PACKAGE_FILTER)
                .build();
    }
}
