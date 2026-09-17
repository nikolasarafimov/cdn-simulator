package mk.ukim.finki.cdn_simulatorproject.exceptions;

public class EdgeServerException extends RuntimeException {

    public EdgeServerException() {
        super("Could not find the least loaded edge server.");
    }
}