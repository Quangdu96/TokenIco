package tokenico.controllers.builders;

import lombok.AccessLevel;
import lombok.Getter;

import tokenico.controllers.IcoManagerController;
import tokenico.environment.ProjectProviderFailureException;
import tokenico.environment.ico.DefaultProjectSaleProvider;
import tokenico.environment.ico.ProjectSaleProvider;

@Getter(value = AccessLevel.PROTECTED)
abstract class IcoManagerControllerInitData<B extends IcoManagerControllerInitData<B>> extends ContractControllerInitData<B> {

    private ProjectSaleProvider projectSaleProvider;

    protected IcoManagerControllerInitData() throws ProjectProviderFailureException {
        projectSaleProvider = DefaultProjectSaleProvider.getInstance();
    }

    public final B projectSaleProvider(ProjectSaleProvider projectSaleProvider) {
        this.projectSaleProvider = projectSaleProvider;
        return thisObject();
    }
}

public final class IcoManagerControllerBuilder
        extends IcoManagerControllerInitData<IcoManagerControllerBuilder>
        implements Builder<IcoManagerController> {

    protected IcoManagerControllerBuilder() throws ProjectProviderFailureException {}

    public static IcoManagerControllerBuilder newInstance() throws ProjectProviderFailureException {
        return new IcoManagerControllerBuilder();
    }

    @Override
    protected IcoManagerControllerBuilder thisObject() {
        return this;
    }

    @Override
    public IcoManagerController build() throws ProjectProviderFailureException {
        return IcoManagerController.newInstance(
            getNetwork(), getProjectContractStorageProvider(),
            getProjectCredentialProvider(), getProjectGasProvider(),
            getProjectSaleProvider());
    }
}
