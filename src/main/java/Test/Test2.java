package Test;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;

import java.util.HashMap;
import java.util.Map;

public class Test2 {
    public static void main(String[] args) {
        // Récupération de la clé API Gemini depuis les variables d'environnement
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null) {
            System.err.println("Variable d'environnement GEMINIKEY non définie !");
            return;
        }

        // Création du modèle Gemini
        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.5-flash")
                .temperature(0.7)
                .build();

        // Définition du template pour la traduction (variable {{texte}})
        PromptTemplate template = PromptTemplate.from("Traduis le texte suivant en anglais : {{texte}}");

        // Texte à traduire
        String texteATraduire = "Au revoir, madame! Bonne journée!" + "\n";

        // Map des variables pour le Prompt
        Map<String, Object> variables = new HashMap<>();
        variables.put("texte", texteATraduire);

        // Création du Prompt LangChain4j
        Prompt prompt = template.apply(variables);

        // Conversion du Prompt en String pour l'appel à Gemini
        String promptStr = prompt.text();

        // Appel au LLM Gemini via LangChain4j
        String reponse = model.chat(promptStr);

        // Affichage
        System.out.println("Texte à traduire : " + texteATraduire);
        System.out.println("Traduction Gemini : " + reponse);
    }
}
