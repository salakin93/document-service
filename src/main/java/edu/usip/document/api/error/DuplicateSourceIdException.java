package edu.usip.document.api.error;

public class DuplicateSourceIdException extends RuntimeException {
    public DuplicateSourceIdException(String sourceId) {
        super("Ya existe un documento con sourceId= " + sourceId);
    }
}
