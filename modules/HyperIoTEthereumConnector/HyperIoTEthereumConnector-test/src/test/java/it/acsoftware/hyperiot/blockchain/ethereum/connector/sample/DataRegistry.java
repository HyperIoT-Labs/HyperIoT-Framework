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

package it.acsoftware.hyperiot.blockchain.ethereum.connector.sample;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.9.0.
 */
@SuppressWarnings("rawtypes")
public class DataRegistry extends Contract {
    public static final String BINARY = "608060405234801561001057600080fd5b5061016e806100206000396000f3fe608060405234801561001057600080fd5b5060043610610052577c010000000000000000000000000000000000000000000000000000000060003504634c565d5b8114610057578063fe6ad6c61461007e575b600080fd5b61006a61006536600461011f565b6100a2565b604051901515815260200160405180910390f35b61006a61008c36600461011f565b6000818152602081905260409020600201541490565b600081815260208190526040808220805473ffffffffffffffffffffffffffffffffffffffff1916339081178255426001830155600290910184905590517fa820f77cc16cde8316ee3e311571ae6db0b10f00e3e689f92beedc5329110fb49061010f9085815260200190565b60405180910390a2506001919050565b60006020828403121561013157600080fd5b503591905056fea26469706673582212205606ae4f5ebf79ff2b17b1c461b42c0b639291f98e0274cb68ce3ea11eea243764736f6c634300080c0033";

    public static final String FUNC_ISNOTARIZED = "isNotarized";

    public static final String FUNC_NOTARIZEDOCUMENT = "notarizeDocument";

    public static final Event NOTARIZED_EVENT = new Event("Notarized", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Bytes32>() {}));
    ;

    @Deprecated
    protected DataRegistry(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected DataRegistry(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected DataRegistry(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected DataRegistry(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public List<NotarizedEventResponse> getNotarizedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(NOTARIZED_EVENT, transactionReceipt);
        ArrayList<NotarizedEventResponse> responses = new ArrayList<NotarizedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            NotarizedEventResponse typedResponse = new NotarizedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._signer = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse._dataHash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<NotarizedEventResponse> notarizedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, NotarizedEventResponse>() {
            @Override
            public NotarizedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(NOTARIZED_EVENT, log);
                NotarizedEventResponse typedResponse = new NotarizedEventResponse();
                typedResponse.log = log;
                typedResponse._signer = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse._dataHash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<NotarizedEventResponse> notarizedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(NOTARIZED_EVENT));
        return notarizedEventFlowable(filter);
    }

    public RemoteFunctionCall<Boolean> isNotarized(byte[] _dataHash) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_ISNOTARIZED, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(_dataHash)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<TransactionReceipt> notarizeDocument(byte[] _dataHash) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_NOTARIZEDOCUMENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(_dataHash)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static DataRegistry load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new DataRegistry(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static DataRegistry load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new DataRegistry(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static DataRegistry load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new DataRegistry(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static DataRegistry load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new DataRegistry(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<DataRegistry> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(DataRegistry.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<DataRegistry> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(DataRegistry.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<DataRegistry> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(DataRegistry.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<DataRegistry> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(DataRegistry.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public static class NotarizedEventResponse extends BaseEventResponse {
        public String _signer;

        public byte[] _dataHash;
    }
}
