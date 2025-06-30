package mk.ukim.finki.cdn_simulatorproject.exceptions;

public class ReplicaServerException extends RuntimeException{

    public ReplicaServerException() {
        super("Can not find the least loaded Replica Server.");
    }
}
