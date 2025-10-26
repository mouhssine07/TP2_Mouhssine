package Test;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import java.util.Map;

public class Test2 {
    public static void main(String[] args) {
        // Récupération de la clé API Gemini depuis les variables d'environnement
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

        PromptTemplate template = PromptTemplate.from("""
                Traduis le texte suivant en anglais : {{texte}}""");


        String texteATraduire = "Au revoir, madame! Bonne journée!" + "\n";


        Prompt prompt = template.apply(Map.of("texte", texteATraduire));

        String answer = model.chat(prompt.text());





        // Affichage
        System.out.println("Texte à traduire : " + texteATraduire);
        System.out.println(answer);
    }
}
