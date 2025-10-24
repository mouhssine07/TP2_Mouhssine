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

import java.util.logging.Level;
import java.util.logging.Logger;


public class Test4 {

    // Interface de l’assistant : une méthode pour dialoguer avec le LLM
    interface Assistant {
        String chat(String userMessage);
    }

    public static void main(String[] args) {

        // 🔑 1. Récupération de la clé API Gemini depuis les variables d’environnement
        String llmKey = System.getenv("GEMINI_API_KEY");
        if (llmKey == null) {
            System.err.println("⚠️ Erreur : variable d'environnement GEMINI_API_KEY non définie !");
            return;
        }

        // 🤖 2. Création du modèle LLM (Gemini)
        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(llmKey)
                .modelName("gemini-2.5-flash") // modèle rapide et récent
                .temperature(0.3)              // température faible = réponses plus précises
                .maxOutputTokens(512)          // limite du nombre de tokens générés
                .build();

        // 📄 3. Chargement du document texte (infos.txt)
        String nomDocument = "infos.txt"; // doit être à la racine du projet
        Document document = FileSystemDocumentLoader.loadDocument(nomDocument);

        // 🧠 4. Création d’une base vectorielle en mémoire
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        // 🧩 5. Calcul des embeddings et ajout au store
        EmbeddingStoreIngestor.ingest(document, embeddingStore);

        // 💬 6. Création de l’assistant conversationnel (avec mémoire + RAG)
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(EmbeddingStoreContentRetriever.from(embeddingStore))
                .build();

        // ❓ 7. Question à poser au LLM
        String question = "Comment s'appelle le chat de Pierre ?";
        String question1 = "Pierre appelle son chat. Qu'est-ce qu'il pourrait dire ?";
        String question2 = "Quelle est la capitale de la France ?";

        // 💡 8. Récupération et affichage de la réponse
        String reponse = assistant.chat(question);
        System.out.println("Question : " + question);
        System.out.println("Réponse  : " + reponse);

        System.out.println(assistant.chat(question1));
        System.out.println("Question : " + question1);
        System.out.println("Reponse : " + reponse);

        System.out.println(assistant.chat(question2));
        System.out.println("Question : " + question2);
        System.out.println("Reponse : " + reponse);

    }
}
