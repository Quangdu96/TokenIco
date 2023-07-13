package tokenico.environment;

import java.math.BigInteger;

public interface ProjectContractStorageProvider {
    
    String getAddress(ProjectNetwork network, ProjectContract contract) throws ProjectProviderFailureException;
    void putAddress(ProjectNetwork network, ProjectContract contract, String address) throws ProjectProviderFailureException;
    
    BigInteger getBlockNumber(ProjectNetwork network, ProjectContract contract) throws ProjectProviderFailureException;
    void putBlockNumber(ProjectNetwork network, ProjectContract contract, BigInteger blockNumber) throws ProjectProviderFailureException;
}
