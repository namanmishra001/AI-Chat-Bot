package com.ai.bot.dataloader;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class GymDataLoader {

    private static final Logger log = LoggerFactory.getLogger(GymDataLoader.class);

    private final VectorStore vectorStore;

    @Value("classpath:docs/sample_pdf_with_gym_schedule.pdf")
    private Resource pdfResource;

    public GymDataLoader(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void init() {
        log.info("loading GYM Data....");
        PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder().withPagesPerDocument(1).build();
        PagePdfDocumentReader reader = new PagePdfDocumentReader(pdfResource, config);
        var textSplitter = new TokenTextSplitter();
        vectorStore.accept(textSplitter.apply(reader.get()));
        log.info("GYM Data loaded succesfuly");
    }
}
