package mk.ukim.finki.cdn_simulatorproject.exceptions;

public class EdgeServerException extends RuntimeException{

    public EdgeServerException() {
        super("Can not find the least loaded Edge Server.");
    }
}
