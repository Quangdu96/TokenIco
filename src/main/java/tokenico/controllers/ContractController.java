package tokenico.controllers;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;

import lombok.Getter;
import lombok.ToString;
import tokenico.environment.ProjectContract;
import tokenico.environment.ProjectContractStorageProvider;
import tokenico.environment.ProjectCredentialProvider;
import tokenico.environment.ProjectGasProvider;
import tokenico.environment.ProjectNetwork;
import tokenico.environment.ProjectProviderFailureException;

@Getter
@ToString
public abstract class ContractController {
    
    protected final ProjectNetwork network;
    @ToString.Exclude
    protected final Web3j web3j;

    protected final ProjectContract contractName;
    protected       String address;
    protected       boolean deployed;

    protected final ProjectContractStorageProvider projectContractStorageProvider;
    protected final ProjectCredentialProvider projectCredentialProvider;
    protected final ProjectGasProvider projectGasProvider;

    protected ContractController(
            ProjectNetwork network,
            ProjectContract contractName,
            ProjectContractStorageProvider projectContractStorageProvider,
            ProjectCredentialProvider projectCredentialProvider,
            ProjectGasProvider projectGasProvider) throws ProjectProviderFailureException {
        this.network = network;
        this.web3j = Web3j.build(new HttpService(network.RPC_URL));

        this.contractName = contractName;
        try {
            this.address = projectContractStorageProvider.getAddress(network, contractName);
            this.deployed = true;
        } catch (RuntimeException e) {
            this.deployed = false;
        }

        this.projectContractStorageProvider = projectContractStorageProvider;
        this.projectCredentialProvider = projectCredentialProvider;
        this.projectGasProvider = projectGasProvider;
    }

    public final void safeDeploy() throws Exception {
        if (deployed) {
            throw new UnsupportedOperationException("Contract " + contractName + " has already been deployed on " + network);
        }
        deploy();
    }

    public abstract void deploy() throws Exception;

    protected final void saveDeployedContract(Contract contract) throws ProjectProviderFailureException {
        projectContractStorageProvider.putAddress(
            network,
            contractName,
            contract.getContractAddress());
        projectContractStorageProvider.putBlockNumber(
            network,
            contractName,
            contract.getTransactionReceipt().get().getBlockNumber());
    }

    protected final void throwIfUndeployed() {
        if (!deployed) {
            throw new UndeployedContractException("Contract " + contractName + " has not been deployed on " + network);
        }
    }

    public static class UndeployedContractException extends RuntimeException {

        public UndeployedContractException(String errorMessage) {
            super(errorMessage);
        }

        public UndeployedContractException(Throwable err) {
            super(err);
        }

        public UndeployedContractException(String errorMessage, Throwable err) {
            super(errorMessage, err);
        }
    }
}
