package Test;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.CosineSimilarity;

public class Test3 {
    public static void main(String[] args) {
        // IMPORTANT : Utilise System.getenv() pour lire la variable d'environnement
        String apiKey = System.getenv("GEMINI_API_KEY");



        // Utilise le modèle recommandé text-embedding-004 ou gemini-embedding-001
        EmbeddingModel modele = GoogleAiEmbeddingModel.builder()
                .apiKey(apiKey)  // Utilise la variable apiKey, pas "GEMINIKEY"
                .modelName("text-embedding-004")  // Modèle supporté
                .build();

        String phrase1 = "Bonjour, comment allez-vous ?";
        String phrase2 = "Salut, quoi de neuf ?";
        String phrase3 = "Le ciel est bleu.";

        Response<Embedding> reponse1 = modele.embed(phrase1);
        Response<Embedding> reponse2 = modele.embed(phrase2);
        Response<Embedding> reponse3 = modele.embed(phrase3);

        Embedding emb1 = reponse1.content();
        Embedding emb2 = reponse2.content();
        Embedding emb3 = reponse3.content();

        // Calcul de similarité cosinus entre les embeddings
        double similarite12 = CosineSimilarity.between(emb1, emb2);
        double similarite13 = CosineSimilarity.between(emb1, emb3);

        System.out.println("Similarité phrase1-phrase2 : " + similarite12);
        System.out.println("Similarité phrase1-phrase3 : " + similarite13);
    }
}
