package ma.emsi.mouhssine.tp1_mouhssine.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.*;
import jakarta.json.stream.JsonGenerator;
import ma.emsi.mouhssine.tp1_mouhssine.client.LlmClientPourGemini;
import ma.emsi.mouhssine.tp1_mouhssine.exception.RequeteException;
import ma.emsi.mouhssine.tp1_mouhssine.model.LlmInteraction;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class JsonUtilPourGemini {

    @Inject
    private LlmClientPourGemini llmClient;

    private JsonArray historiqueConversation = null;

    public LlmInteraction envoyerRequete(String question) throws RequeteException {
        ajouterQuestionDansJsonRequete(question);
        String jsonRequete = creerJsonRequete();
        String jsonReponse = llmClient.envoyerRequete(jsonRequete);
        String reponseExtraite = extraireReponse(jsonReponse);
        ajouterReponseDansHistorique(reponseExtraite);

        String jsonRequetePretty = prettyPrinting(jsonRequete);
        String jsonReponsePretty = prettyPrinting(jsonReponse);

        return new LlmInteraction(jsonRequetePretty, jsonReponsePretty, reponseExtraite);
    }

    public void setRoleSysteme(String roleSysteme) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        JsonObjectBuilder contentBuilder = Json.createObjectBuilder();
        contentBuilder.add("role", "user");
        JsonArrayBuilder partsBuilder = Json.createArrayBuilder();
        partsBuilder.add(Json.createObjectBuilder().add("text", roleSysteme));
        contentBuilder.add("parts", partsBuilder);
        arrayBuilder.add(contentBuilder);
        this.historiqueConversation = arrayBuilder.build();
    }

    public void reinitialiserHistorique() {
        this.historiqueConversation = null;
    }

    private void ajouterQuestionDansJsonRequete(String question) {
        JsonArrayBuilder arrayBuilder;
        if (historiqueConversation == null) {
            arrayBuilder = Json.createArrayBuilder();
        } else {
            arrayBuilder = Json.createArrayBuilder(historiqueConversation);
        }
        JsonObjectBuilder contentBuilder = Json.createObjectBuilder();
        contentBuilder.add("role", "user");
        JsonArrayBuilder partsBuilder = Json.createArrayBuilder();
        partsBuilder.add(Json.createObjectBuilder().add("text", question));
        contentBuilder.add("parts", partsBuilder);
        arrayBuilder.add(contentBuilder);
        this.historiqueConversation = arrayBuilder.build();
    }

    private void ajouterReponseDansHistorique(String reponse) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder(historiqueConversation);
        JsonObjectBuilder contentBuilder = Json.createObjectBuilder();
        contentBuilder.add("role", "model");
        JsonArrayBuilder partsBuilder = Json.createArrayBuilder();
        partsBuilder.add(Json.createObjectBuilder().add("text", reponse));
        contentBuilder.add("parts", partsBuilder);
        arrayBuilder.add(contentBuilder);
        this.historiqueConversation = arrayBuilder.build();
    }

    private String creerJsonRequete() {
        JsonObjectBuilder requestBuilder = Json.createObjectBuilder();
        requestBuilder.add("contents", historiqueConversation);
        return requestBuilder.build().toString();
    }

    private String extraireReponse(String jsonReponse) throws RequeteException {
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonReponse))) {
            JsonObject responseObject = jsonReader.readObject();
            JsonArray candidates = responseObject.getJsonArray("candidates");
            JsonObject candidate = candidates.getJsonObject(0);
            JsonObject content = candidate.getJsonObject("content");
            JsonArray parts = content.getJsonArray("parts");
            JsonObject part = parts.getJsonObject(0);
            return part.getString("text");
        } catch (Exception e) {
            throw new RequeteException("Erreur parsing JSON", e);
        }
    }

    private String prettyPrinting(String jsonString) {
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonString))) {
            JsonStructure jsonStructure = jsonReader.read();
            StringWriter stringWriter = new StringWriter();
            Map<String, Object> properties = new HashMap<>();
            properties.put(JsonGenerator.PRETTY_PRINTING, true);
            JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
            try (JsonWriter jsonWriter = writerFactory.createWriter(stringWriter)) {
                jsonWriter.write(jsonStructure);
            }
            return stringWriter.toString();
        } catch (Exception e) {
            return jsonString;
        }
    }
}
