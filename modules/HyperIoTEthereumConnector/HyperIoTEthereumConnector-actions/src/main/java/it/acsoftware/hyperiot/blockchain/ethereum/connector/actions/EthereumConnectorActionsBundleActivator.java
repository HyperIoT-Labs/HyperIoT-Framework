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

import it.acsoftware.hyperiot.base.action.HyperIoTActionList;
import it.acsoftware.hyperiot.base.action.HyperIoTPermissionActivator;
import it.acsoftware.hyperiot.base.action.util.HyperIoTActionFactory;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.model.EthereumBlockChain;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.model.EthereumSmartContract;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Aristide Cittadino Model class that define a bundle activator and
 * register actions for EthereumConnector
 */
public class EthereumConnectorActionsBundleActivator extends HyperIoTPermissionActivator {

    /**
     * Return a list actions that have to be registerd as OSGi components
     */
    @Override
    public List<HyperIoTActionList> getActions() {
        // creates base Actions save,update,remove,find,findAll for the specified entity
        log.info("Registering base CRUD actions...");
        ArrayList<HyperIoTActionList> actionsLists = new ArrayList<>();
        HyperIoTActionList actionList = HyperIoTActionFactory.createBaseCrudActionList(EthereumBlockChain.class.getName(),
                EthereumBlockChain.class.getName());
        //Permission which allows a user to deploy a contract on a specific blockchain
        actionList.addAction(HyperIoTActionFactory.createAction("Blockchain", EthereumBlockChain.class.getName(), EthereumConnectorAction.ADD_CONTRACT));
        actionList.addAction(HyperIoTActionFactory.createAction("Blockchain", EthereumBlockChain.class.getName(), EthereumConnectorAction.LOAD_CONTRACT));
        HyperIoTActionList smartContractsActionsList = HyperIoTActionFactory.createBaseCrudActionList(EthereumSmartContract.class.getName(),
                EthereumSmartContract.class.getName());
        actionsLists.add(actionList);
        actionsLists.add(smartContractsActionsList);
        //TO DO: add more actions to actionList here...
        return actionsLists;
    }

}
