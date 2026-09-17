package mk.ukim.finki.cdn_simulatorproject.exceptions;

public class ReplicaServerException extends RuntimeException {

    public ReplicaServerException() {
        super("Could not find the least loaded replica server.");
    }
}