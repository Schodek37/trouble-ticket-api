package pl.netia.troubleticket.exception;

public class TroubleTicketNotFoundException extends RuntimeException {
    public TroubleTicketNotFoundException(String id) {
        super("Trouble ticket not found: " + id);
    }
}