package tokenico.environment;

import org.web3j.crypto.Credentials;

public interface ProjectCredentialProvider {
    String getAddress(ProjectCredential of) throws ProjectProviderFailureException;
    Credentials getCredential(ProjectCredential of) throws ProjectProviderFailureException;
}
