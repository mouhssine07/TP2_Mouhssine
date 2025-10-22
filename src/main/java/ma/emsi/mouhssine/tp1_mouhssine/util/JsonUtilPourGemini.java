package ma.emsi.mouhssine.tp1_mouhssine.util;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.json.*;
import jakarta.json.stream.JsonGenerator;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ma.emsi.mouhssine.tp1_mouhssine.client.LlmClientPourGemini;
import ma.emsi.mouhssine.tp1_mouhssine.exception.RequeteException;
import ma.emsi.mouhssine.tp1_mouhssine.model.LlmInteraction;

import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Dependent
public class JsonUtilPourGemini implements Serializable {

    private String systemRole;
    private final JsonPointer pointer = Json.createPointer("/contents/-");
    private JsonObject requeteJson;
    private String texteRequeteJson;

    @Inject
    private LlmClientPourGemini geminiClient;

    public void setSystemRole(String systemRole) {
        this.systemRole = systemRole;
    }

    public LlmInteraction envoyerRequete(String question) throws RequeteException {
        String requestBody;
        if (this.requeteJson == null) {
            requestBody = creerRequeteJson(this.systemRole, question);
        } else {
            requestBody = ajouteQuestionDansJsonRequete(question);
        }

        Entity<String> entity = Entity.entity(requestBody, MediaType.APPLICATION_JSON_TYPE);
        this.texteRequeteJson = prettyPrinting(requeteJson);

        try (Response response = geminiClient.envoyerRequete(entity)) {
            String texteReponseJson = response.readEntity(String.class);

            if (response.getStatus() == 200) {
                String reponseExtraite = extractReponse(texteReponseJson);
                // ✅ CORRECTION : Formater aussi la réponse JSON
                String texteReponsePretty = prettyPrinting(texteReponseJson);
                return new LlmInteraction(this.texteRequeteJson, texteReponsePretty, reponseExtraite);
            } else {
                throw new RequeteException("Erreur HTTP " + response.getStatus() + " : " + texteReponseJson);
            }
        }
    }

    private String creerRequeteJson(String systemRole, String question) {
        JsonArray systemInstructionParts = Json.createArrayBuilder()
                .add(Json.createObjectBuilder().add("text", systemRole))
                .build();
        JsonObject systemInstruction = Json.createObjectBuilder()
                .add("parts", systemInstructionParts)
                .build();

        JsonArray userContentParts = Json.createArrayBuilder()
                .add(Json.createObjectBuilder().add("text", question))
                .build();
        JsonObject userContent = Json.createObjectBuilder()
                .add("role", "user")
                .add("parts", userContentParts)
                .build();
        JsonArray contents = Json.createArrayBuilder()
                .add(userContent)
                .build();

        JsonObject rootJson = Json.createObjectBuilder()
                .add("system_instruction", systemInstruction)
                .add("contents", contents)
                .build();
        this.requeteJson = rootJson;

        return rootJson.toString();
    }

    private String ajouteQuestionDansJsonRequete(String nouvelleQuestion) {
        JsonObject nouveauMessageJson = Json.createObjectBuilder()
                .add("text", nouvelleQuestion)
                .build();

        JsonObjectBuilder newPartBuilder = Json.createObjectBuilder()
                .add("role", "user")
                .add("parts", Json.createArrayBuilder().add(nouveauMessageJson).build());

        this.requeteJson = this.pointer.add(this.requeteJson, newPartBuilder.build());
        this.texteRequeteJson = prettyPrinting(requeteJson);
        return this.requeteJson.toString();
    }

    private String prettyPrinting(JsonObject jsonObject) {
        Map<String, Boolean> config = new HashMap<>();
        config.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonWriterFactory writerFactory = Json.createWriterFactory(config);
        StringWriter stringWriter = new StringWriter();
        try (JsonWriter jsonWriter = writerFactory.createWriter(stringWriter)) {
            jsonWriter.write(jsonObject);
        }
        return stringWriter.toString();
    }

    // ✅ AJOUT : Surcharge pour formatter une String JSON
    private String prettyPrinting(String jsonString) {
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonString))) {
            JsonObject jsonObject = jsonReader.readObject();
            return prettyPrinting(jsonObject);
        } catch (Exception e) {
            return jsonString;
        }
    }

    private String extractReponse(String json) {
        try (JsonReader jsonReader = Json.createReader(new StringReader(json))) {
            JsonObject jsonObject = jsonReader.readObject();
            JsonObject messageReponse = jsonObject
                    .getJsonArray("candidates")
                    .getJsonObject(0)
                    .getJsonObject("content");

            this.requeteJson = this.pointer.add(this.requeteJson, messageReponse);
            return messageReponse.getJsonArray("parts").getJsonObject(0).getString("text");
        }
    }

    // ✅ AJOUT : Méthode pour réinitialiser l'historique
    public void reinitialiserHistorique() {
        this.requeteJson = null;
        this.texteRequeteJson = null;
        this.systemRole = null;
    }
}
