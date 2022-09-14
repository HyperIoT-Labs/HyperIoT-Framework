pragma solidity ^0.8.6;


/**
*  @dev Smart Contract responsible to notarize documents on the Ethereum Blockchain
*/
contract DataRegistry {

    struct Data {
        address signer; // Notary
        uint date; // Date of notarization
        bytes32 hash; // _dataHash
    }

    /**
     *  @dev Storage space used to record all documents notarized with metadata
   */
    mapping(bytes32 => Data) registry;

    /**
     *  @dev Notarize a document identified by its 32 bytes hash by recording the hash, the sender and date in the registry
   *  @dev Emit an event Notarized in case of success
   *  @param _dataHash Document hash
   */
    function notarizeDocument(bytes32 _dataHash) external returns (bool) {
        registry[_dataHash].signer = msg.sender;
        registry[_dataHash].date = block.timestamp;
        registry[_dataHash].hash = _dataHash;

        emit Notarized(msg.sender, _dataHash);

        return true;
    }

    /**
     *  @dev Verify a document identified by its hash was noterized in the registry.
   *  @param _dataHash Document hash
   *  @return bool if document was noterized previsouly in the registry
   */
    function isNotarized(bytes32 _dataHash) external view returns (bool) {
        return registry[_dataHash].hash ==  _dataHash;
    }

    /**
     *  @dev Definition of the event triggered when a document is successfully notarized in the registry
   */
    event Notarized(address indexed _signer, bytes32 _dataHash);
}