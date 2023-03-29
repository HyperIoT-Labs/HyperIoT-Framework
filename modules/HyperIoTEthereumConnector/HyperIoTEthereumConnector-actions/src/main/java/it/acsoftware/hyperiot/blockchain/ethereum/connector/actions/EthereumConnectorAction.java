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

package it.acsoftware.hyperiot.blockchain.ethereum.connector.actions;

import it.acsoftware.hyperiot.base.action.HyperIoTActionName;

/**
 * @author Aristide Cittadino Model class that enumerate EthereumConnector Actions
 */
public enum EthereumConnectorAction implements HyperIoTActionName {

    //TO DO: add enumerations here
    ADD_CONTRACT(Names.ADD_CONTRACT),
    REMOVE_CONTRACT(Names.REMOVE_CONTRACT),
    LOAD_CONTRACT(Names.LOAD_CONTRACT);

    private String name;

    /**
     * Role Action with the specified name.
     *
     * @param name parameter that represent the EthereumConnector  action
     */
    private EthereumConnectorAction(String name) {
        this.name = name;
    }

    /**
     * Gets the name of EthereumConnector action
     */
    public String getName() {
        return name;
    }

    public static class Names {

        public static final String ADD_CONTRACT = "add_contract";
        public static final String LOAD_CONTRACT = "load_contract";
        public static final String REMOVE_CONTRACT = "remove_contract";

        private Names() {
            throw new IllegalStateException("Utility class");
        }

    }

}
