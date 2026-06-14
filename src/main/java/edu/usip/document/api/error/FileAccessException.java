package edu.usip.document.api.error;

public class FileAccessException extends  RuntimeException {
    public FileAccessException(String path, Throwable cause) {
        super("No se pudo acceder el archivo en: " + path, cause);
    }
}
