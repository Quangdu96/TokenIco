package tokenico.environment;

public enum ProjectContract {
    
    MY_TOKEN("MyToken"),
    ICO_MANAGER("IcoManager");

    private final String canonicalName;

    private ProjectContract(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    @Override
    public String toString() {
        return canonicalName;
    }
}
