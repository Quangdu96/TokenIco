package tokenico.environment;

import org.web3j.tx.gas.ContractGasProvider;

public interface ProjectGasProvider {
    ContractGasProvider getGasProvider(ProjectNetwork network, ProjectContract contract) throws ProjectProviderFailureException;
}
