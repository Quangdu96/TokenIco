package tokenico.environment;

public final class ProjectEnvironment {

    public enum EnvironmentOption {
        DEVELOP,
        PRODUCTION
    }
    
    public static final EnvironmentOption current = EnvironmentOption.DEVELOP;

    private ProjectEnvironment() {}
}
