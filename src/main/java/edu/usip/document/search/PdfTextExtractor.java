package edu.usip.document.search;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.metadata.Metadata;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Slf4j
@Component
public class PdfTextExtractor {

    public String extract(InputStream in) {
        try {
            var handler = new BodyContentHandler(-1); // sin límite
            var metadata = new Metadata();
            var parser = new AutoDetectParser();
            parser.parse(in, handler, metadata);
            return handler.toString();
        } catch (Exception e) {
            log.error("Error extrayendo texto", e);
            return "";
        }
    }
}