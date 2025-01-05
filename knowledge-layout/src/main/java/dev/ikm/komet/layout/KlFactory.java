package dev.ikm.komet.layout;

public interface KlFactory {

    /**
     * TODO: move this to a text utility in tinkar-core ?
     * @param camelCaseString
     * @return
     */
    static String camelCaseToWords(String camelCaseString) {
        if (camelCaseString.startsWith("kl")) {
            camelCaseString = camelCaseString.replaceFirst("^kl", "knowledgeLayout");
        } else if (camelCaseString.startsWith("Kl")) {
            camelCaseString = camelCaseString.replaceFirst( "^Kl", "KnowledgeLayout");
        }
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < camelCaseString.length(); i++) {
            char ch = camelCaseString.charAt(i);

            if (Character.isUpperCase(ch)) {
                if (i > 0) {
                    result.append(" ");
                }
                result.append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }

        return result.toString().replace("kl ", "Knowledge Layout ");
    }

    /**
     * Retrieves the name of this factory.
     *
     * @return A string representing the name of the factory.
     */
    default String name() {
        return camelCaseToWords(this.getClass().getSimpleName());
    }
}
