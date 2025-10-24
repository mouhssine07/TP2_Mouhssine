package Test;


import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

import java.time.Duration;


public class Test1 {

    public static void main(String[] args) {
        // Clé API depuis variable d'environnement
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null) {
            System.err.println("Variable d'environnement GEMINI_API_KEY non définie !");
            return;
        }

        // Création du modèle Gemini 2.5 Flash (température 0.7)
        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.5-flash")
                .temperature(0.7)
                .build();

        // Pose une question simple
        String question = "Quelle est la capitale du Maroc ?";
        String reponse = model.chat(question);

        // Affiche la réponse
        System.out.println("Question : " + question);
        System.out.println("Réponse Gemini : " + reponse);
    }
}