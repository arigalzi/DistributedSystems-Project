package lib.message;

public class ErrorMessage implements Message {
    public char getType() { return 'E'; }

    public String getTransmissionString() {
        return "E";
    }
}
