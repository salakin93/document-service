package edu.usip.document.dto.records;

import org.springframework.core.io.Resource;

public record DocumentDownload(Resource resource, String fileName) {}