package Test;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.util.Scanner;

public class Test5 {

    interface Assistant {
        String chat(String userMessage);
    }

    public static void main(String[] args) {
        String llmKey = System.getenv("GEMINI_API_KEY");
        if (llmKey == null) {
            System.err.println("GEMINI_API_KEY non définie !");
            return;
        }

        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(llmKey)
                .modelName("gemini-2.0-flash-exp")
                .temperature(0.3)
                .maxOutputTokens(512)
                .build();

        String nomDocument = "ml.pdf";
        Document document;
        try {
            document = FileSystemDocumentLoader.loadDocument(nomDocument);
            System.out.println("Document chargé avec succès");
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du document: " + e.getMessage());
            return;
        }

        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        try {
            EmbeddingStoreIngestor.ingest(document, embeddingStore);
            System.out.println("Document ingéré dans le embedding store");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ingestion du document: " + e.getMessage());
            return;
        }

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(EmbeddingStoreContentRetriever.from(embeddingStore))
                .build();

        convWith(assistant);
    }

    private static void convWith(Assistant assistant) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("==================================================");
                System.out.println("Posez votre question (ou tapez 'fin' pour quitter) : ");
                String question = scanner.nextLine();
                if (question.isBlank()) {
                    continue;
                }
                System.out.println("==================================================");
                if ("fin".equalsIgnoreCase(question)) {
                    System.out.println("Conversation terminée.");
                    break;
                }
                try {
                    String reponse = assistant.chat(question);
                    System.out.println("Assistant : " + reponse);
                } catch (Exception e) {
                    System.err.println("Erreur lors de la génération de la réponse: " + e.getMessage());
                }
                System.out.println("==================================================");
            }
        }
    }
}
