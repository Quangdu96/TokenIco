package tokenico.controllers;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import tokenico.environment.ProjectContract;
import tokenico.environment.ProjectContractStorageProvider;
import tokenico.environment.ProjectCredentialProvider;
import tokenico.environment.ProjectGasProvider;
import tokenico.environment.ProjectNetwork;
import tokenico.environment.ProjectProviderFailureException;

@Getter @Accessors(fluent = true)
@ToString
public abstract class ContractController {
    
    private final ProjectNetwork network;
    @ToString.Exclude
    private final Web3j web3j;

    private final ProjectContract contractName;
    private       String address;
    private       boolean deployed;

    private final ProjectContractStorageProvider projectContractStorageProvider;
    private final ProjectCredentialProvider projectCredentialProvider;
    private final ProjectGasProvider projectGasProvider;

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
            throw new AlreadyDeployedContractException(contractName, network);
        }
        deploy();
    }

    public final void deploy() throws Exception {
        Contract contract = deployImpl();
        afterDeploy(contract);
    }

    protected abstract Contract deployImpl() throws Exception;

    private final void afterDeploy(Contract contract) throws ProjectProviderFailureException {
        saveDeployedContract(contract);
        this.address = contract.getContractAddress();
        this.deployed = true;
    }

    private final void saveDeployedContract(Contract contract) throws ProjectProviderFailureException {
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
            throw new UndeployedContractException(contractName, network);
        }
    }

    public static class UndeployedContractException extends RuntimeException {

        public UndeployedContractException(ProjectContract contractName, ProjectNetwork network) {
            super("Contract " + contractName + " has not been deployed on " + network);
        }

        public UndeployedContractException(Throwable err) {
            super(err);
        }

        public UndeployedContractException(ProjectContract contractName, ProjectNetwork network, Throwable err) {
            super("Contract " + contractName + " has not been deployed on " + network, err);
        }
    }

    public static class AlreadyDeployedContractException extends RuntimeException {

        public AlreadyDeployedContractException(ProjectContract contractName, ProjectNetwork network) {
            super("Contract " + contractName + " has already been deployed on " + network);
        }

        public AlreadyDeployedContractException(Throwable err) {
            super(err);
        }

        public AlreadyDeployedContractException(ProjectContract contractName, ProjectNetwork network, Throwable err) {
            super("Contract " + contractName + " has already been deployed on " + network, err);
        }
    }
}
