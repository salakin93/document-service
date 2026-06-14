package edu.usip.document.api.error;

public class FileSizeExceededException extends RuntimeException {
    public FileSizeExceededException(long fileSize) {
        super("El archivo supera el tamaño máximo permitido de " + fileSize + "bytes");
    }
}
