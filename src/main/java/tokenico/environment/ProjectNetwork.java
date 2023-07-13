package tokenico.environment;

public enum ProjectNetwork {
    
    LOCALHOST_8545,
    LOCALHOST_8546,
    ETH_MAIN,
    BSC_MAIN,
    BSC_TEST;

    public final String NAME;
    public final int CHAIN_ID;
    public final String RPC_URL;

    private ProjectNetwork() {
        switch (name()) {
            case "LOCALHOST_8545":
                NAME = "localhost:8545";
                CHAIN_ID = 31337;
                RPC_URL = "http://localhost:8545";
                break;
            case "LOCALHOST_8546":
                NAME = "localhost:8546";
                CHAIN_ID = 31338;
                RPC_URL = "http://localhost:8546";
                break;
            case "ETH_MAIN":
                NAME = "Ethereum Mainnet";
                CHAIN_ID = 1;
                RPC_URL = "https://mainnet.infura.io/v3";
                break;
            case "BSC_MAIN":
                NAME = "Binance Smart Chain Mainnet";
                CHAIN_ID = 56;
                RPC_URL = "https://bsc-dataseed.binance.org";
                break;
            case "BSC_TEST":
                NAME = "Binance Smart Chain Testnet";
                CHAIN_ID = 97;
                RPC_URL = "https://data-seed-prebsc-1-s1.binance.org:8545";
                break;
            default:
                throw new UnsupportedOperationException("Specified network is not supported");
        }
    }

    @Override
    public String toString() {
        return NAME;
    }
}
