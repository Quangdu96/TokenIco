package tokenico.controllers.builders;

import lombok.AccessLevel;
import lombok.Getter;

import tokenico.controllers.ContractController;
import tokenico.environment.DefaultProjectContractStorageProvider;
import tokenico.environment.DefaultProjectCredentialProvider;
import tokenico.environment.DefaultProjectGasProvider;
import tokenico.environment.ProjectContractStorageProvider;
import tokenico.environment.ProjectCredentialProvider;
import tokenico.environment.ProjectGasProvider;
import tokenico.environment.ProjectNetwork;
import tokenico.environment.ProjectProviderFailureException;

@Getter(value = AccessLevel.PROTECTED)
abstract class ContractControllerInitData<B extends ContractControllerInitData<B>> extends Fluent<B> {

    private ProjectNetwork network;
    private ProjectContractStorageProvider projectContractStorageProvider;
    private ProjectCredentialProvider projectCredentialProvider;
    private ProjectGasProvider projectGasProvider;

    protected ContractControllerInitData() throws ProjectProviderFailureException {
        this.network = ProjectNetwork.LOCALHOST_8545;
        this.projectContractStorageProvider = DefaultProjectContractStorageProvider.getInstance();
        this.projectCredentialProvider = DefaultProjectCredentialProvider.getInstance();
        this.projectGasProvider = DefaultProjectGasProvider.getInstance();
    }

    public final B network(ProjectNetwork network) {
        this.network = network;
        return thisObject();
    }
    
    public final B projectContractStorageProvider(ProjectContractStorageProvider projectContractStorageProvider) {
        this.projectContractStorageProvider = projectContractStorageProvider;
        return thisObject();
    }
    
    public final B projectCredentialProvider(ProjectCredentialProvider projectCredentialProvider) {
        this.projectCredentialProvider = projectCredentialProvider;
        return thisObject();
    }
    
    public final B projectGasProvider(ProjectGasProvider projectGasProvider) {
        this.projectGasProvider = projectGasProvider;
        return thisObject();
    }
}

public final class ContractControllerBuilder extends ContractControllerInitData<ContractControllerBuilder> {

    protected ContractControllerBuilder() throws ProjectProviderFailureException {}

    public static ContractControllerBuilder newInstance() throws ProjectProviderFailureException {
        return new ContractControllerBuilder();
    }

    @Override
    protected ContractControllerBuilder thisObject() {
        return this;
    }

    public static interface Instantiator<R extends ContractController> {
        R newInstance(
            ProjectNetwork network,
            ProjectContractStorageProvider projectContractStorageProvider,
            ProjectCredentialProvider projectCredentialProvider,
            ProjectGasProvider projectGasProvider) throws ProjectProviderFailureException;
    }

    public <R extends ContractController> R build(Instantiator<R> instantiator) throws ProjectProviderFailureException {
        return instantiator.newInstance(
            getNetwork(),
            getProjectContractStorageProvider(),
            getProjectCredentialProvider(),
            getProjectGasProvider());
    }
}
