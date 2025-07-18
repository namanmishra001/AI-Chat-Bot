package com.ai.bot.dataloader;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class ApexDataLoader {

    private static final Logger log = LoggerFactory.getLogger(ApexDataLoader.class);

    private final VectorStore vectorStore;

    @Value("classpath:docs/Portal API and OAuth Documentation.pdf")
    private Resource pdfResource;

    public ApexDataLoader(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

   // @PostConstruct
    public void init() {
        log.info("loading Apex Data....");
        PdfDocumentReaderConfig config = PdfDocumentReaderConfig
                .builder()
                .withPageTopMargin(0)
                .withPageExtractedTextFormatter(ExtractedTextFormatter
                        .builder()
                        .withNumberOfTopTextLinesToDelete(0)
                        .build())
                .withPagesPerDocument(1)
                .build();
        PagePdfDocumentReader reader = new PagePdfDocumentReader(pdfResource, config);
        var textSplitter = new TokenTextSplitter();
        vectorStore.accept(textSplitter.apply(reader.get()));
        log.info("Apex Data loaded succesfuly");
    }
}
