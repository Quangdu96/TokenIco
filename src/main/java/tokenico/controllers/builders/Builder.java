package tokenico.controllers.builders;

import tokenico.controllers.ContractController;
import tokenico.environment.ProjectProviderFailureException;

public interface Builder<T extends ContractController> {
    T build() throws ProjectProviderFailureException;
}
