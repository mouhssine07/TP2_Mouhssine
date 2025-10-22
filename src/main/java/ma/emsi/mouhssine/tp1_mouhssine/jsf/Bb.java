package ma.emsi.mouhssine.tp1_mouhssine.jsf;


import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ma.emsi.mouhssine.tp1_mouhssine.model.LlmInteraction;
import ma.emsi.mouhssine.tp1_mouhssine.util.JsonUtilPourGemini;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Backing bean pour la page JSF index.xhtml.
 * Portée view pour conserver l'état de la conversation qui dure pendant plusieurs requêtes HTTP.
 * La portée view nécessite l'implémentation de Serializable (le backing bean peut être mis en mémoire secondaire).
 */
@Named
@ViewScoped
public class Bb implements Serializable {

    /**
     * Rôle "système" que l'on attribuera plus tard à un LLM.
     * Valeur par défaut que l'utilisateur peut modifier.
     * Possible d'écrire un nouveau rôle dans la liste déroulante.
     */
    private String roleSysteme;

    // --- Mode debug ---
    private boolean debug = false;
    private String texteRequeteJson;
    private String texteReponseJson;


    /**
     * Quand le rôle est choisi par l'utilisateur dans la liste déroulante,
     * il n'est plus possible de le modifier (voir code de la page JSF), sauf si on veut un nouveau chat.
     */
    private boolean roleSystemeChangeable = true;

    /**
     * Liste de tous les rôles de l'API prédéfinis.
     */
    private List<SelectItem> listeRolesSysteme;

    /**
     * Dernière question posée par l'utilisateur.
     */
    private String question;
    /**
     * Dernière réponse de l'API OpenAI.
     */
    private String reponse;
    /**
     * La conversation depuis le début.
     */
    private StringBuilder conversation = new StringBuilder();

    /**
     * Contexte JSF. Utilisé pour qu'un message d'erreur s'affiche dans le formulaire.
     */
    @Inject
    private FacesContext facesContext;

    @Inject
    private JsonUtilPourGemini jsonUtil;
    /**
     * Obligatoire pour un bean CDI (classe gérée par CDI), s'il y a un autre constructeur.
     */
    public Bb() {
    }

    public String getRoleSysteme() {
        return roleSysteme;
    }

    public void setRoleSysteme(String roleSysteme) {
        this.roleSysteme = roleSysteme;
    }

    public boolean isRoleSystemeChangeable() {
        return roleSystemeChangeable;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReponse() {
        return reponse;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getTexteRequeteJson() {
        return texteRequeteJson;
    }

    public void setTexteRequeteJson(String texteRequeteJson) {
        this.texteRequeteJson = texteRequeteJson;
    }

    public String getTexteReponseJson() {
        return texteReponseJson;
    }

    public void setTexteReponseJson(String texteReponseJson) {
        this.texteReponseJson = texteReponseJson;
    }

    public void toggleDebug() {
        this.setDebug(!isDebug());
    }

    /**
     * setter indispensable pour le textarea.
     *
     * @param reponse la réponse à la question.
     */
    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getConversation() {
        return conversation.toString();
    }

    public void setConversation(String conversation) {
        this.conversation = new StringBuilder(conversation);
    }

    /**
     * Envoie la question au serveur.
     * En attendant de l'envoyer à un LLM, le serveur fait un traitement quelconque, juste pour tester :
     * Le traitement consiste à copier la question en minuscules et à l'entourer avec "||". Le rôle système
     * est ajouté au début de la première réponse.
     *
     * @return null pour rester sur la même page.
     */
    public String envoyer() {
        if (question == null || question.isBlank()) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Texte question vide", "Il manque le texte de la question");
            facesContext.addMessage(null, message);
            return null;
        }

        // Si début conversation, envoyer rôle système
        if (this.conversation.isEmpty()) {
            jsonUtil.setSystemRole(roleSysteme);  // ✅ CORRECTION : setSystemRole au lieu de setRoleSysteme
            this.roleSystemeChangeable = false;
        }

        try {
            LlmInteraction interaction = jsonUtil.envoyerRequete(question);
            this.reponse = interaction.reponseExtraite();
            this.texteRequeteJson = interaction.questionJson();
            this.texteReponseJson = interaction.reponseJson();

            // ✅ CORRECTION : Afficher conversation APRÈS avoir reçu la réponse
            afficherConversation();

        } catch (Exception e) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Problème de connexion avec l'API du LLM",
                    "Problème de connexion avec l'API du LLM : " + e.getMessage());
            facesContext.addMessage(null, message);
        }

        return null;
    }



    /**
     * Pour un nouveau chat.
     * Termine la portée view en retournant "index" (la page index.xhtml sera affichée après le traitement
     * effectué pour construire la réponse) et pas null. null aurait indiqué de rester dans la même page (index.xhtml)
     * sans changer de vue.
     * Le fait de changer de vue va faire supprimer l'instance en cours du backing bean par CDI et donc on reprend
     * tout comme au début puisqu'une nouvelle instance du backing va être utilisée par la page index.xhtml.
     * @return "index"
     */
    public String nouveauChat() {
        jsonUtil.reinitialiserHistorique();
        return "index";
    }

    /**
     * Pour afficher la conversation dans le textArea de la page JSF.
     */
    private void afficherConversation() {
        this.conversation.append("== User:\n").append(question).append("\n== Serveur:\n").append(reponse).append("\n");
    }

    public List<SelectItem> getRolesSysteme() {
        if (this.listeRolesSysteme == null) {
            this.listeRolesSysteme = new ArrayList<>();

            // Rôle 1 : Assistant
            String role = """
                You are a helpful assistant. You help the user to find the information they need.
                If the user type a question, you answer it.
                """;
            this.listeRolesSysteme.add(new SelectItem(role, "Assistant"));

            // Rôle 2 : Traducteur Anglais-Français
            role = """
                You are an interpreter. You translate from English to French and from French to English.
                If the user type a French text, you translate it into English.
                If the user type an English text, you translate it into French.
                If the text contains only one to three words, give some examples of usage of these words in English.
                """;
            this.listeRolesSysteme.add(new SelectItem(role, "Traducteur Anglais-Français"));

            // Rôle 3 : Guide touristique
            role = """
                Your are a travel guide. If the user type the name of a country or of a town,
                you tell them what are the main places to visit in the country or the town
                are you tell them the average price of a meal.
                """;
            this.listeRolesSysteme.add(new SelectItem(role, "Guide touristique"));

            // ✅ NOUVEAU RÔLE 4 : [Choisissez parmi les exemples ci-dessous]

            // Exemple 1 : Coach en programmation
            role = """
                You are a programming tutor specialized in Java and Jakarta EE.
                When the user asks a coding question, provide clear explanations with code examples.
                Always explain the concepts step by step and encourage best practices.
                End your answers with a helpful tip related to the topic.
                """;
            this.listeRolesSysteme.add(new SelectItem(role, "Coach Programmation Java"));

            // OU Exemple 2 : Expert en développement web
            role = """
                You are a web development expert specializing in modern web technologies.
                Help users with HTML, CSS, JavaScript, and web frameworks.
                Provide practical examples and explain web development concepts clearly.
                Focus on responsive design and user experience best practices.
                """;
            this.listeRolesSysteme.add(new SelectItem(role, "Expert Développement Web"));

            // OU Exemple 3 : Professeur de mathématiques
            role = """
                You are a mathematics teacher. When the user asks a math question,
                explain the solution step by step with clear reasoning.
                Use simple language and provide examples to illustrate concepts.
                Always encourage the user to understand the logic behind the solution.
                """;
            this.listeRolesSysteme.add(new SelectItem(role, "Professeur de Mathématiques"));

            // OU Exemple 4 : Conseiller en carrière IT
            role = """
                You are a career advisor specialized in IT and software development.
                Provide guidance on career paths, skill development, and job search strategies.
                Give practical advice based on current industry trends.
                Be encouraging and motivational in your responses.
                """;
            this.listeRolesSysteme.add(new SelectItem(role, "Conseiller Carrière IT"));

            // OU Exemple 5 : Expert en cybersécurité
            role = """
                You are a cybersecurity expert. When asked about security topics,
                explain vulnerabilities, best practices, and protection methods clearly.
                Focus on practical security measures and real-world examples.
                Always emphasize the importance of security awareness.
                """;
            this.listeRolesSysteme.add(new SelectItem(role, "Expert Cybersécurité"));
        }

        return this.listeRolesSysteme;
    }


}

