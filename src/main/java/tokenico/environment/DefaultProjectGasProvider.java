package tokenico.environment;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONObject;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

public final class DefaultProjectGasProvider implements ProjectGasProvider {
    
    private static DefaultProjectGasProvider singletonInstance;

    public static DefaultProjectGasProvider getInstance() throws ProjectProviderFailureException {
        if (singletonInstance == null) {
            singletonInstance = new DefaultProjectGasProvider();   
        }
        return singletonInstance;
    }

    private static final String GAS_CONFIG_FILEPATH = "json/" + ProjectEnvironment.current.toString() + "/gas_config.json";

    private final JSONObject gasConfig;

    private DefaultProjectGasProvider() throws ProjectProviderFailureException {
        String gasConfigFileContent;
        try {
            gasConfigFileContent = new String(
                Files.readAllBytes(Paths.get(
                    getClass().getResource(GAS_CONFIG_FILEPATH).toURI() )),
                StandardCharsets.UTF_8);

        } catch (IOException | URISyntaxException e) {
            throw new ProjectProviderFailureException("Failed to read " + GAS_CONFIG_FILEPATH + " file", e);
        }

        gasConfig = new JSONObject(gasConfigFileContent);
    }

    public ContractGasProvider getGasProvider(ProjectNetwork network, ProjectContract contract) {
        return new DefaultGasProvider() {
            @Override
            public BigInteger getGasPrice(String contractFunc) {
                return gasConfig.getJSONObject(network.name())
                                .getJSONObject(contract.toString())
                                .getJSONObject(contractFunc)
                                .getBigInteger("GAS_PRICE");
            }
        
            @Override
            public BigInteger getGasLimit(String contractFunc) {
                return gasConfig.getJSONObject(network.name())
                                .getJSONObject(contract.toString())
                                .getJSONObject(contractFunc)
                                .getBigInteger("GAS_LIMIT");
            }
        };
    }
}
