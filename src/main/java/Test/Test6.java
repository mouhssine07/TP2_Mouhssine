package Test;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;

public class Test6 {
    public static void main(String[] args) {

        // 1️⃣ — Créer le modèle LLM (Gemini ici)
        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(System.getenv("GEMINI_API_KEY"))
                .modelName("gemini-2.0-flash-exp")
                .temperature(0.5)
                .logRequestsAndResponses(true)  // active le logging
                .build();

        // 2️⃣ — Créer un assistant capable d’utiliser l’outil MeteoTool
        AssistantMeteo assistant = AiServices.builder(AssistantMeteo.class)
                .chatModel(model)
                .tools(new MeteoTool())  // on ajoute l’outil météo ici
                .build();

        try {
            // 3️⃣ — Poser quelques questions au LLM
            String rep1 = assistant.chat("Quel temps fait-il à Paris ?");
            System.out.println("➡️ Réponse 1 : " + rep1);

            String rep2 = assistant.chat("J’ai prévu d’aller à Marrakech, dois-je prendre un parapluie ?");
            System.out.println("➡️ Réponse 2 : " + rep2);

            String rep3 = assistant.chat("Quel est le résultat de 45 * 33 ?");
            System.out.println("➡️ Réponse 3 : " + rep3);

            String rep4 = assistant.chat("Peux-tu me donner la météo de la ville de Blablaville ?");
            System.out.println("➡️ Réponse 4 : " + rep4);
        }catch (Exception e){
            System.out.println("Erreur"+e.getMessage());
        }
    }
}
