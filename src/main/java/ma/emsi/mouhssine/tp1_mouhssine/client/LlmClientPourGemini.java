package ma.emsi.mouhssine.tp1_mouhssine.client;

import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.Serializable;

@Dependent
public class LlmClientPourGemini implements Serializable {
    private final String key;
    private Client clientRest;
    private final WebTarget target;

    public LlmClientPourGemini() {
        // Récupérer la clé depuis la variable d'environnement
        this.key = System.getenv("GEMINI_API_KEY");

        // ✅ AJOUT : Vérification et log
        if (this.key == null || this.key.isEmpty()) {
            System.err.println("❌ ERREUR : La variable d'environnement GEMINI_API_KEY n'est pas définie !");
            throw new IllegalStateException("GEMINI_API_KEY non définie");
        }

        // ✅ AJOUT : Log pour vérifier (masquer une partie de la clé pour sécurité)
        System.out.println("✅ Clé API chargée : " + this.key.substring(0, Math.min(10, this.key.length())) + "...");

        this.clientRest = ClientBuilder.newClient();

        // Construire l'URL complète avec la clé
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=" + this.key;

        // ✅ AJOUT : Log de l'URL (masquer la clé)
        System.out.println("✅ URL cible : " + url.substring(0, url.indexOf("?key=") + 10) + "...");

        this.target = clientRest.target(url);
    }

    public Response envoyerRequete(Entity requestEntity) {
        Invocation.Builder request = target.request(MediaType.APPLICATION_JSON_TYPE);
        return request.post(requestEntity);
    }

    public void closeClient() {
        this.clientRest.close();
    }
}
