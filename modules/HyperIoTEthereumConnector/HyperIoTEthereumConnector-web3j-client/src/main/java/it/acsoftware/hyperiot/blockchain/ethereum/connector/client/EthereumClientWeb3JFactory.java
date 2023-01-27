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

package it.acsoftware.hyperiot.blockchain.ethereum.connector.client;

import it.acsoftware.hyperiot.blockchain.ethereum.connector.api.EthereumClient;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.api.EthereumClientFactory;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.model.EthereumBlockChain;
import it.acsoftware.hyperiot.blockchain.ethereum.connector.util.EthereumConnectorUtil;
import org.osgi.service.component.annotations.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import static org.apache.commons.lang3.Validate.notNull;

@Component(service = EthereumClientFactory.class, immediate = true, property = {
        EthereumConnectorUtil.ETH_CONNECTOR_CLIENT_FACTORY + "=" + EthereumConnectorUtil.ETH_CONNECTOR_CLIENT_FACTORY_WEB3J
})
public class EthereumClientWeb3JFactory implements EthereumClientFactory {

    private EthereumBlockChain ethereumBlockChain;

    @Override
    public EthereumClientFactory withEthereumBlockChain(EthereumBlockChain ethereumBlockChain) {
        notNull(ethereumBlockChain);
        this.ethereumBlockChain = ethereumBlockChain;
        return this;
    }

    @Override
    public EthereumClient build() {
        Web3j web3j = Web3j.build(new HttpService(ethereumBlockChain.getProtocol() + "://" + ethereumBlockChain.getHost() + ":" + ethereumBlockChain.getPort()));
        EthereumClient web3jClient = new EthereumWeb3JClient(web3j);
        this.reset();
        return web3jClient;
    }

    public void reset() {
        this.ethereumBlockChain = null;
    }

}
