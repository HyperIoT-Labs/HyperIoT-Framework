package it.acsoftware.hyperiot.blockchain.ethereum.connector.api;

import it.acsoftware.hyperiot.blockchain.ethereum.connector.model.EthereumBlockChain;

public interface EthereumClientFactory {
    EthereumClientFactory withEthereumBlockChain(EthereumBlockChain ethereumBlockChain);
    EthereumClient build();
}
