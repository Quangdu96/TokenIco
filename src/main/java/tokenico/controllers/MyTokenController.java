package tokenico.controllers;

import org.web3j.crypto.Credentials;

import lombok.ToString;
import tokenico.environment.ProjectContract;
import tokenico.environment.ProjectContractStorageProvider;
import tokenico.environment.ProjectCredential;
import tokenico.environment.ProjectCredentialProvider;
import tokenico.environment.ProjectGasProvider;
import tokenico.environment.ProjectNetwork;
import tokenico.environment.ProjectProviderFailureException;
import tokenico.generated.contracts.MyToken;

@ToString(callSuper = true)
public class MyTokenController extends ContractController {
    
    private MyTokenController(
            ProjectNetwork network,
            ProjectContractStorageProvider projectContractStorageProvider,
            ProjectCredentialProvider projectCredentialProvider,
            ProjectGasProvider projectGasProvider) throws ProjectProviderFailureException {
        super(network, ProjectContract.MY_TOKEN,
            projectContractStorageProvider, projectCredentialProvider,
            projectGasProvider);
    }

    public static MyTokenController newInstance(
            ProjectNetwork network,
            ProjectContractStorageProvider projectContractStorageProvider,
            ProjectCredentialProvider projectCredentialProvider,
            ProjectGasProvider projectGasProvider) throws ProjectProviderFailureException {
        return new MyTokenController(network, projectContractStorageProvider,
            projectCredentialProvider, projectGasProvider);
    }

    protected final MyToken loadContract(ProjectCredential caller) throws ProjectProviderFailureException {
        throwIfUndeployed();

        return MyToken.load(
            projectContractStorageProvider.getAddress(network, contractName),
            web3j,
            projectCredentialProvider.getCredential(caller),
            projectGasProvider.getGasProvider(network, contractName));
    }

    protected final MyToken loadContract(Credentials caller) throws ProjectProviderFailureException {
        throwIfUndeployed();
        
        return MyToken.load(
            projectContractStorageProvider.getAddress(network, contractName),
            web3j,
            caller,
            projectGasProvider.getGasProvider(network, contractName));
    }

    @Override
    public void deploy() throws Exception {
        MyToken myToken = MyToken.deploy(
            web3j,
            projectCredentialProvider.getCredential(ProjectCredential.ADMIN),
            projectGasProvider.getGasProvider(network, contractName),
            projectCredentialProvider.getAddress(ProjectCredential.ADMIN)
        ).send();

        saveDeployedContract(myToken);
        this.address = myToken.getContractAddress();
        this.deployed = true;
    }
}
