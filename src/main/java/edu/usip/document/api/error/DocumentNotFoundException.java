package edu.usip.document.api.error;

public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException(Long id) {
        super("Document no encontrado con id= " + id);
    }
}
