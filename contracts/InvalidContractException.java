package contracts;

public class InvalidContractException extends RuntimeException {
    //Konštruktor:
    public InvalidContractException(String message) {
        super(message);
    }
}
