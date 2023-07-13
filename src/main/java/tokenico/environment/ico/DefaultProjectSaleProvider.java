package tokenico.environment.ico;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONObject;

import tokenico.environment.ProjectEnvironment;
import tokenico.environment.ProjectProviderFailureException;
import tokenico.models.Sale;

public final class DefaultProjectSaleProvider implements ProjectSaleProvider {
    
    private static DefaultProjectSaleProvider singletonInstance;

    public static DefaultProjectSaleProvider getInstance() throws ProjectProviderFailureException {
        if (singletonInstance == null) {
            singletonInstance = new DefaultProjectSaleProvider();   
        }
        return singletonInstance;
    }

    private static final String SALES_FILEPATH = "../json/" + ProjectEnvironment.current.toString() + "/sales.json";

    private final JSONObject sales;

    private DefaultProjectSaleProvider() throws ProjectProviderFailureException {
        String salesFileContent;
        try {
            salesFileContent = new String(
                Files.readAllBytes(Paths.get(
                    getClass().getResource(SALES_FILEPATH).toURI() )),
                StandardCharsets.UTF_8);

        } catch (IOException | URISyntaxException e) {
            throw new ProjectProviderFailureException("Failed to read " + SALES_FILEPATH + " file", e);
        }

        sales = new JSONObject(salesFileContent);
    }

    @Override
    public Sale getSale(String saleId) {
        JSONObject saleJson = sales.getJSONObject(saleId);
        return Sale.builder()
                   .startTime(saleJson.getBigInteger("startTime"))
                   .endTime(saleJson.getBigInteger("endTime"))
                   .price(saleJson.getBigInteger("price"))
                   .openStatus(saleJson.getBoolean("openStatus"))
                   .build();
    }
}
