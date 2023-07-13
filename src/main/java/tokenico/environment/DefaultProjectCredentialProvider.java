package tokenico.environment;

import java.util.EnumMap;

import org.web3j.crypto.Credentials;

public final class DefaultProjectCredentialProvider implements ProjectCredentialProvider {
    
    private static DefaultProjectCredentialProvider singletonInstance;

    public static DefaultProjectCredentialProvider getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new DefaultProjectCredentialProvider();
        }
        return singletonInstance;
    }
    
    /**
     * Accounts used for test.
     * ADMIN: Hardhat account #0
     * INCOME_RECEIVER: Hardhat account #1
     * BUYER_0: Hardhat account #10
     * BUYER_1: Hardhat account #11
     */
    private static final String TEST_ADMIN_ADDRESS = "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266";
    private static final String TEST_ADMIN_PRIVATE_KEY = "0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80";
    private static final String TEST_INCOME_RECEIVER_ADDRESS = "0x70997970C51812dc3A010C7d01b50e0d17dc79C8";
    private static final String TEST_INCOME_RECEIVER_PRIVATE_KEY = "0x59c6995e998f97a5a0044966f0945389dc9e86dae88c7a8412f4603b6b78690d";
    private static final String TEST_BUYER_0_ADDRESS = "0xBcd4042DE499D14e55001CcbB24a551F3b954096";
    private static final String TEST_BUYER_0_PRIVATE_KEY = "0xf214f2b2cd398c806f84e317254e0f0b801d0643303237d97a22a48e01628897";
    private static final String TEST_BUYER_1_ADDRESS = "0x71bE63f3384f5fb98995898A86B02Fb2426c5788";
    private static final String TEST_BUYER_1_PRIVATE_KEY = "0x701b615bbdfb9de65240bc28bd21bbc0d996645a3dd57e7b12bc2bdf6f192c82";

    private final EnumMap<ProjectCredential, String> addressStore;
    private final EnumMap<ProjectCredential, Credentials> credentialStore;

    private DefaultProjectCredentialProvider() {
        addressStore = new EnumMap<>(ProjectCredential.class);
        credentialStore = new EnumMap<>(ProjectCredential.class);

        switch (ProjectEnvironment.current) {
            case DEVELOP:
                addressStore.put(ProjectCredential.ADMIN, TEST_ADMIN_ADDRESS);
                credentialStore.put(ProjectCredential.ADMIN, Credentials.create(TEST_ADMIN_PRIVATE_KEY));

                addressStore.put(ProjectCredential.INCOME_RECEIVER, TEST_INCOME_RECEIVER_ADDRESS);
                credentialStore.put(ProjectCredential.INCOME_RECEIVER, Credentials.create(TEST_INCOME_RECEIVER_PRIVATE_KEY));

                addressStore.put(ProjectCredential.BUYER_0, TEST_BUYER_0_ADDRESS);
                credentialStore.put(ProjectCredential.BUYER_0, Credentials.create(TEST_BUYER_0_PRIVATE_KEY));

                addressStore.put(ProjectCredential.BUYER_1, TEST_BUYER_1_ADDRESS);
                credentialStore.put(ProjectCredential.BUYER_1, Credentials.create(TEST_BUYER_1_PRIVATE_KEY));

                break;
            case PRODUCTION:
                throw new UnsupportedOperationException("Not implemented");
        }
    }

    @Override
    public String getAddress(ProjectCredential of) {
        String result = addressStore.get(of);
        if (result == null) {
            throw new UnsupportedOperationException("Address does not exist for " + of.toString());
        }
        return result;
    }

    @Override
    public Credentials getCredential(ProjectCredential of) {
        Credentials result = credentialStore.get(of);
        if (result == null) {
            throw new UnsupportedOperationException("Credential does not exist for " + of.toString());
        }
        return result;
    }
}
