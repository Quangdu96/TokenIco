package tokenico.controllers;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.web3j.crypto.Credentials;
import org.web3j.tuples.generated.Tuple6;

import lombok.Getter;
import lombok.ToString;
import tokenico.environment.ProjectContract;
import tokenico.environment.ProjectContractStorageProvider;
import tokenico.environment.ProjectCredential;
import tokenico.environment.ProjectCredentialProvider;
import tokenico.environment.ProjectGasProvider;
import tokenico.environment.ProjectNetwork;
import tokenico.environment.ProjectProviderFailureException;
import tokenico.environment.ico.ProjectSaleProvider;
import tokenico.generated.contracts.IcoManager;
import tokenico.generated.contracts.MyToken;
import tokenico.models.Sale;

@Getter
@ToString(callSuper = true)
public class IcoManagerController extends ContractController {

    private final ProjectSaleProvider projectSaleProvider;
    
    private IcoManagerController(
            ProjectNetwork network,
            ProjectContractStorageProvider projectContractStorageProvider,
            ProjectCredentialProvider projectCredentialProvider,
            ProjectGasProvider projectGasProvider,
            ProjectSaleProvider projectSaleProvider) throws ProjectProviderFailureException {
        super(network, ProjectContract.ICO_MANAGER,
            projectContractStorageProvider, projectCredentialProvider,
            projectGasProvider);
        this.projectSaleProvider = projectSaleProvider;
    }

    public static IcoManagerController newInstance(
            ProjectNetwork network,
            ProjectContractStorageProvider projectContractStorageProvider,
            ProjectCredentialProvider projectCredentialProvider,
            ProjectGasProvider projectGasProvider,
            ProjectSaleProvider projectSaleProvider) throws ProjectProviderFailureException {
        return new IcoManagerController(network, projectContractStorageProvider,
            projectCredentialProvider, projectGasProvider,
            projectSaleProvider);
    }

    protected final IcoManager loadContract(ProjectCredential caller) throws ProjectProviderFailureException {
        throwIfUndeployed();

        return IcoManager.load(
            projectContractStorageProvider.getAddress(network, contractName),
            web3j,
            projectCredentialProvider.getCredential(caller),
            projectGasProvider.getGasProvider(network, contractName));
    }

    protected final IcoManager loadContract(Credentials caller) throws ProjectProviderFailureException {
        throwIfUndeployed();
        
        return IcoManager.load(
            projectContractStorageProvider.getAddress(network, contractName),
            web3j,
            caller,
            projectGasProvider.getGasProvider(network, contractName));
    }

    @Override
    public void deploy() throws Exception {
        IcoManager icoManager = IcoManager.deploy(
            web3j,
            projectCredentialProvider.getCredential(ProjectCredential.ADMIN),
            projectGasProvider.getGasProvider(network, contractName),
            projectCredentialProvider.getAddress(ProjectCredential.INCOME_RECEIVER)
        ).send();

        saveDeployedContract(icoManager);
        this.address = icoManager.getContractAddress();
        this.deployed = true;
    }

    /* ********************************************************************************************** */

    public void increaseFund(BigInteger amount) throws Exception {
        throwIfUndeployed();

        MyToken myToken = MyToken.load(
            projectContractStorageProvider.getAddress(network, ProjectContract.MY_TOKEN),
            web3j,
            projectCredentialProvider.getCredential(ProjectCredential.ADMIN),
            projectGasProvider.getGasProvider(network, ProjectContract.MY_TOKEN));

        myToken.transfer(address, amount).send();
    }

    /* ********************************************************************************************** */

    public void addSale(String saleId) throws Exception {
        IcoManager icoManager = loadContract(ProjectCredential.ADMIN);

        Sale sale = projectSaleProvider.getSale(saleId);

        icoManager.addSale(
            saleId, sale.startTime(),
            sale.endTime(), sale.price(),
            sale.openStatus()
        ).send();
    }

    protected final Sale getOnChainSale(String saleId) throws Exception {
        IcoManager icoManager = loadContract(ProjectCredential.ADMIN);

        Tuple6<Boolean, BigInteger, BigInteger, BigInteger, Boolean, BigInteger> sale = icoManager.sales(saleId).send();
        
        return Sale.builder().created(sale.component1())
                             .startTime(sale.component2())
                             .endTime(sale.component3())
                             .price(sale.component4())
                             .openStatus(sale.component5())
                             .totalSold(sale.component6()).build();
    }

    public void updateSale(String saleId) throws Exception {
        Sale beforeUpdateSale = getOnChainSale(saleId);
        if (!beforeUpdateSale.created()) {
            throw new RuntimeException("Sale does not exist");
        }
        
        IcoManager icoManager = loadContract(ProjectCredential.ADMIN);
        Sale afterUpdateSale = projectSaleProvider.getSale(saleId);

        if (!beforeUpdateSale.startTime().equals(afterUpdateSale.startTime())) {
            icoManager.editSaleStartTime(saleId, afterUpdateSale.startTime()).send();
        }

        if (!beforeUpdateSale.endTime().equals(afterUpdateSale.endTime())) {
            icoManager.editSaleEndTime(saleId, afterUpdateSale.endTime()).send();
        }

        if (!beforeUpdateSale.price().equals(afterUpdateSale.price())) {
            icoManager.editSalePrice(saleId, afterUpdateSale.price()).send();
        }

        if (beforeUpdateSale.openStatus() != afterUpdateSale.openStatus()) {
            icoManager.editSaleOpenStatus(saleId, afterUpdateSale.openStatus()).send();
        }
    }

    public void batchSetAllowance(String saleId, Map<String, BigInteger> allowanceMap) throws Exception {
        Sale sale = getOnChainSale(saleId);
        if (!sale.created()) {
            throw new RuntimeException("Sale does not exist");
        }
        
        IcoManager icoManager = loadContract(ProjectCredential.ADMIN);

        List<IcoManager.BuyerAndAllowance> buyerAndAllowanceList = allowanceMap.entrySet()
            .parallelStream()
            .map( entry -> new IcoManager.BuyerAndAllowance(entry.getKey(), entry.getValue()) )
            .collect(Collectors.toList());

        icoManager.batchSetAllowance(saleId, buyerAndAllowanceList).send();
    }

    public void batchIncreaseAllowance(String saleId, Map<String, BigInteger> allowanceMap) throws Exception {
        Sale sale = getOnChainSale(saleId);
        if (!sale.created()) {
            throw new RuntimeException("Sale does not exist");
        }
        
        IcoManager icoManager = loadContract(ProjectCredential.ADMIN);

        List<IcoManager.BuyerAndAllowance> buyerAndAllowanceList = allowanceMap.entrySet()
            .parallelStream()
            .map( entry -> new IcoManager.BuyerAndAllowance(entry.getKey(), entry.getValue()) )
            .collect(Collectors.toList());

        icoManager.batchIncreaseAllowance(saleId, buyerAndAllowanceList).send();
    }

    public void stop() throws Exception {
        IcoManager icoManager = loadContract(ProjectCredential.ADMIN);
        icoManager.stop().send();
    }

    public void withdraw(BigInteger amount) throws Exception {
        IcoManager icoManager = loadContract(ProjectCredential.ADMIN);
        icoManager.withdraw(amount).send();
    }

    /* ********************************************************************************************** */

    public void buy(String saleId, Credentials buyer, BigInteger amount) throws Exception {
        Sale sale = getOnChainSale(saleId);
        if (!sale.created()) {
            throw new RuntimeException("Sale does not exist");
        }
        
        IcoManager icoManager = loadContract(buyer);

        icoManager.buy(saleId, amount, amount.multiply(sale.price())).send();
    }
}
