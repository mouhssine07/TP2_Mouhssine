package ma.emsi.mouhssine.tp1_mouhssine.client;

import jakarta.enterprise.context.ApplicationScoped;
import ma.emsi.mouhssine.tp1_mouhssine.exception.RequeteException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@ApplicationScoped
public class LlmClientPourGemini {

    private final String apiKey;
    private final HttpClient httpClient;

    public LlmClientPourGemini() {
        // À COMPLÉTER : Récupérer la clé depuis variable d'environnement GEMINI_API_KEY
        this.apiKey = System.getenv("GEMINI_API_KEY");
        this.httpClient = HttpClient.newHttpClient();
    }

    public String envoyerRequete(String jsonRequete) throws RequeteException {
        try {
            // À COMPLÉTER : Construire l'URL avec la clé API
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=" + apiKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequete))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RequeteException("Erreur HTTP " + response.statusCode());
            }

            return response.body();

        } catch (IOException | InterruptedException e) {
            throw new RequeteException("Erreur connexion", e);
        }
    }
}