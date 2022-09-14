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
