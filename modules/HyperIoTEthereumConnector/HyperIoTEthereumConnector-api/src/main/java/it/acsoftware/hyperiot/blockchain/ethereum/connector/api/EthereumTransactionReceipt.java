package it.acsoftware.hyperiot.blockchain.ethereum.connector.api;

import java.math.BigInteger;

public interface EthereumTransactionReceipt {

    String getTransactionHash();

    BigInteger getTransactionIndex();

    String getBlockHash();

    BigInteger getBlockNumber();

    BigInteger getCumulativeGasUsed();

    BigInteger getGasUsed();

    String getContractAddress();

    String getRoot();

    String getStatus();

    String getFrom();

    String getTo();

    String getRevertReason();

    String getType();

    String getEffectiveGasPrice();
}
