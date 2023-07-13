package tokenico.environment;

public class ProjectProviderFailureException extends Exception {
    
    public ProjectProviderFailureException(String errorMessage) {
        super(errorMessage);
    }

    public ProjectProviderFailureException(Throwable err) {
        super(err);
    }

    public ProjectProviderFailureException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
