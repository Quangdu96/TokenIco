package tokenico.environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.WalletUtils;

public final class DefaultProjectContractStorageProvider implements ProjectContractStorageProvider {
    
    private static DefaultProjectContractStorageProvider singletonInstance;

    public static DefaultProjectContractStorageProvider getInstance() throws ProjectProviderFailureException {
        if (singletonInstance == null) {
            singletonInstance = new DefaultProjectContractStorageProvider();
        }
        return singletonInstance;
    }

    private static final String CONTRACT_ADDRESS_FILEPATH = "json/" + ProjectEnvironment.current.toString() + "/deployed.json";

    private final JSONObject addressStore;

    private DefaultProjectContractStorageProvider() throws ProjectProviderFailureException {
        String contractAddressFileContent;
        try {
            contractAddressFileContent = new String(
                Files.readAllBytes(Paths.get(
                    getClass().getResource(CONTRACT_ADDRESS_FILEPATH).toURI() )),
                StandardCharsets.UTF_8);
                
        } catch (IOException | URISyntaxException e) {
            throw new ProjectProviderFailureException("Failed to read " + CONTRACT_ADDRESS_FILEPATH + " file", e);
        }

        addressStore = new JSONObject(contractAddressFileContent);
    }

    private String get(ProjectNetwork network, ProjectContract contract, String infoKey) {
        String result;

        try {
            result = addressStore.getJSONObject(network.name())
                                 .getJSONObject(contract.toString())
                                 .getString(infoKey);
        } catch (JSONException e) {
            throw new RuntimeException(infoKey + " not found", e);
        }

        return result;
    }

    private void put(ProjectNetwork network, ProjectContract contract, String infoKey, Object value) throws ProjectProviderFailureException {
        if (value == null) {
            throw new NullPointerException("Value is null");
        }

        JSONObject level1Json = addressStore.getJSONObject(network.name());
        JSONObject level2Json = level1Json.getJSONObject(contract.toString());
        level2Json.put(infoKey, value);
        level1Json.put(contract.toString(), level2Json);
        addressStore.put(network.name(), level1Json);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(
                new File( getClass().getResource(CONTRACT_ADDRESS_FILEPATH).toURI() )))) {
                    
            addressStore.write(writer, 4, 0);
        } catch (IOException | URISyntaxException e) {
            throw new ProjectProviderFailureException("Failed to write " + CONTRACT_ADDRESS_FILEPATH + " file", e);
        }
    }

    @Override
    public String getAddress(ProjectNetwork network, ProjectContract contract) {
        String result = get(network, contract, "ADDRESS");
        if (!WalletUtils.isValidAddress(result)) {
            throw new RuntimeException("Address is malformed");
        }
        return result;
    }

    @Override
    public void putAddress(ProjectNetwork network, ProjectContract contract, String address) throws ProjectProviderFailureException {
        if (!WalletUtils.isValidAddress(address)) {
            throw new IllegalArgumentException("Address is malformed");
        }
        put(network, contract, "ADDRESS", address);
    }

    @Override
    public BigInteger getBlockNumber(ProjectNetwork network, ProjectContract contract) {
        return new BigInteger(get(network, contract, "BLOCK_NUMBER"));
    }    

    @Override
    public void putBlockNumber(ProjectNetwork network, ProjectContract contract, BigInteger blockNumber) throws ProjectProviderFailureException {
        put(network, contract, "BLOCK_NUMBER", blockNumber);
    }
}
