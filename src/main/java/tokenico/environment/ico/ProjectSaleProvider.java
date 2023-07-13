package tokenico.environment.ico;

import tokenico.models.Sale;

public interface ProjectSaleProvider {
    Sale getSale(String saleId);
}
