package Test;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.CosineSimilarity;

import java.time.Duration;

public class Test3 {
    public static void main(String[] args) {

        String apiKey = System.getenv("GEMINI_API_KEY");



        EmbeddingModel modele = GoogleAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-embedding-001")
                .taskType(GoogleAiEmbeddingModel.TaskType.SEMANTIC_SIMILARITY)
                .outputDimensionality(300)
                .timeout(Duration.ofSeconds(2))
                .build();

        String phrase1 = "Quel est ton plat préféré ?";
        String phrase2 = "As-tu déjà visité le Maroc ?";
        String phrase3 = "tu aime apprendre la programmation ? ";


        Response<Embedding> reponse1 = modele.embed(phrase1);
        Response<Embedding> reponse2 = modele.embed(phrase2);
        Response<Embedding> reponse3 = modele.embed(phrase3);

        Embedding emb1 = reponse1.content();
        Embedding emb2 = reponse2.content();
        Embedding emb3 = reponse3.content();

        double similarite12 = CosineSimilarity.between(emb1, emb2);
        double similarite13 = CosineSimilarity.between(emb1, emb3);
        double similarite23 = CosineSimilarity.between(emb2, emb3);

        System.out.println("Similarité phrase1-phrase2 : " + similarite12);
        System.out.println("Similarité phrase1-phrase3 : " + similarite13);
        System.out.println("Similarité phrase2-phrase3 : " + similarite23);
    }
}
