package Test;


import dev.langchain4j.model.chat.ChatModel;

import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;



public class Test1 {

    public static void main(String[] args) {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null) {
            System.err.println("GEMINIKEY non définie !");
            return;
        }

        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.5-flash")
                .temperature(0.7)
                .build();

        // Pose une question simple
        String question = "Quelle est la capitale du canada ?";
        String reponse = model.chat(question);

        // Affiche la réponse
        System.out.println("Question : " + question);
        System.out.println("Réponse Gemini : " + reponse);
    }
}