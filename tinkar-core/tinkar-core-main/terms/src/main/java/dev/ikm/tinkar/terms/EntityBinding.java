package dev.ikm.tinkar.terms;
import java.util.UUID;


/**
 * The {@code EntityBinding} interface serves as a structure defining various entity types and their specific patterns
 * and field indices. It organizes key types such as {@code Component}, {@code Concept}, {@code Pattern},
 * {@code Semantic}, and {@code Stamp} into logical groupings, each with their own nested {@code Version} interfaces
 * and associated patterns and indices. These definitions provide utility for entity management in systems relying on field-based patterns.
 */
public interface EntityBinding {
    interface Component {
        static EntityProxy.Pattern pattern() {
            return EntityProxy.Pattern.make("Component field pattern",
                    UUID.fromString("e5d91cfb-ce2c-49e2-b522-0a3f285f1c53"));
        }

        static int publicIdFieldDefinitionIndex() {
            return 0;
        }

        static int versionsFieldDefinitionIndex() {
            return 1;
        }

        static int versionItemDefinitionIndex() {
            return 2;
        }

        interface Version {
            // Component version field pattern: [95ebfa49-3ca7-4a86-ab91-bcc2081ab265]
            static EntityProxy.Pattern pattern() {
                return EntityProxy.Pattern.make("Component version field pattern",
                        UUID.fromString("95ebfa49-3ca7-4a86-ab91-bcc2081ab265"));
            }

            static int stampFieldDefinitionIndex() {
                return 0;
            }
        }
    }

    interface Concept {
        static EntityProxy.Pattern pattern() {
            return EntityProxy.Pattern.make("Concept field pattern",
                    UUID.fromString("8e9a8888-9d06-45c8-af47-aacc78ed66ee"));
        }

        static int publicIdFieldDefinitionIndex() {
            return Component.publicIdFieldDefinitionIndex();
        }

        static int versionsFieldDefinitionIndex() {
            return Component.versionsFieldDefinitionIndex();
        }

        static int versionItemPatternIndex() {
            return Component.versionItemDefinitionIndex();
        }

        interface Version {
            static EntityProxy.Pattern pattern() {
                // Concept version field pattern: [c20dbe3e-3b5e-40cf-a96d-68e9b95e7a90]
                return EntityProxy.Pattern.make("Concept version field pattern",
                        UUID.fromString("c20dbe3e-3b5e-40cf-a96d-68e9b95e7a90"));
            }

            static int stampFieldDefinitionIndex() {
                return Component.Version.stampFieldDefinitionIndex();
            }
        }
    }

    interface Pattern {
        static EntityProxy.Pattern pattern() {
            //Pattern field pattern: [8521a438-f84f-4f84-9f99-35aab5b10bd9]
            return EntityProxy.Pattern.make("Pattern field pattern",
                    UUID.fromString("8521a438-f84f-4f84-9f99-35aab5b10bd9"));
        }

        static int publicIdFieldDefinitionIndex() {
            return Component.publicIdFieldDefinitionIndex();
        }

        static int versionsFieldDefinitionIndex() {
            return Component.versionsFieldDefinitionIndex();
        }

        static int versionItemDefinitionIndex() {
            return Component.versionItemDefinitionIndex();
        }

        interface Version {
            static EntityProxy.Pattern pattern() {
                // Pattern version field pattern: [fd84d158-8877-47d7-b7dc-17d97f2d8207]
                return EntityProxy.Pattern.make("Pattern version field pattern",
                        UUID.fromString("fd84d158-8877-47d7-b7dc-17d97f2d8207"));
            }

            static int stampFieldDefinitionIndex() {
                return Component.Version.stampFieldDefinitionIndex();
            }

            static int patternMeaningFieldDefinitionIndex() {
                return 1;
            }

            static int patternPurposeFieldDefinitionIndex() {
                return 2;
            }

            static int fieldDefinitionListFieldDefinitionIndex() {
                return 3;
            }
            static int fieldDefinitionListItemIndex() {
                return 4;
            }
        }
    }

    interface Semantic {
        static EntityProxy.Pattern pattern() {
            // Semantic field pattern: [2e97cc13-2994-4f0e-bcb3-0739e9109bf6]
            return EntityProxy.Pattern.make("Semantic field pattern",
                    UUID.fromString("2e97cc13-2994-4f0e-bcb3-0739e9109bf6"));
        }

        static int publicIdFieldDefinitionIndex() {
            return Component.publicIdFieldDefinitionIndex();
        }

        /**
         * WARNING: In the current pattern, the versions field is the 4th (index 3) rather than the 2nd (index 1) field.
         * TODO: Decide how to handle version ordering in semantic fields.
         * @return field index
         */
        static int versionsFieldDefinitionIndex() {
            return Component.versionsFieldDefinitionIndex();
        }

        static int versionItemDefinitionIndex() {
            return Component.versionItemDefinitionIndex();
        }
        /**
         * WARNING: In the current pattern, the pattern field is the 2nd (index 1) rather than the 3rd (index 2) field.
         * TODO: Decide how to handle version ordering in semantic fields.
         * @return field index
         */
        static int patternFieldDefinitionIndex() {
            return Component.versionItemDefinitionIndex() + 1;
        }

        /**
         * WARNING: In the current pattern, the referenced component field is the 3rd (index 2) rather than the 4th (index 3) field.
         * TODO: Decide how to handle version ordering in semantic fields.
         * @return field index
         */
        static int referencedComponentFieldDefinitionIndex() {
            return patternFieldDefinitionIndex() + 1;
        }

        interface Version {
            static EntityProxy.Pattern pattern() {
                // Semantic version field pattern: [368d1ff5-017e-46ca-90ba-e82f25c2c5fa]
                return EntityProxy.Pattern.make("Semantic version field pattern",
                        UUID.fromString("368d1ff5-017e-46ca-90ba-e82f25c2c5fa"));
            }
            static int stampFieldDefinitionIndex() {
                return Component.Version.stampFieldDefinitionIndex();
            }
            static int semanticFieldsDefinitionIndex() { return 1;}
        }
    }

    interface Stamp {
        static EntityProxy.Pattern pattern() {
            // NOTE: We have two STAMP patterns in the database.
            // STAMP pattern: [9fd67fee-abf9-551d-9d0e-76a4b1e8b4ee]
            // STAMP field pattern: [15687f5d-6028-4491-b005-7bb6f9f6ebad]

            // STAMP field pattern: [15687f5d-6028-4491-b005-7bb6f9f6ebad]
            return EntityProxy.Pattern.make("STAMP field pattern",
                    UUID.fromString("15687f5d-6028-4491-b005-7bb6f9f6ebad"));
        }

        static int publicIdFieldDefinitionIndex() {
            return Component.publicIdFieldDefinitionIndex();
        }

        static int versionsFieldDefinitionIndex() {
            return Component.versionsFieldDefinitionIndex();
        }

        static int versionItemDefinitionIndex() {
            return Component.versionItemDefinitionIndex();
        }

        interface Version {
            static EntityProxy.Pattern pattern() {
                // STAMP version field pattern: [fcf637ce-63fe-4f52-a4f3-401f46f71a60]
                return EntityProxy.Pattern.make("STAMP version field pattern",
                        UUID.fromString("fcf637ce-63fe-4f52-a4f3-401f46f71a60"));
            }

            /**
             * Yes, redundant that the stamp version has a stampNid field that is its own nid,
             * but it is inherited from the version superclass.
             * WARNING: Not in the pattern.
             * @return field index
             */
            static int stampFieldDefinitionIndex() {
                return Component.Version.stampFieldDefinitionIndex();
            }

            /**
             * WARNING: off by one because of missing stampField in the pattern.
             * @return field index
             */
            static int statusFieldDefinitionIndex() {
                return 0; //TODO VALIDATE STARTER SET: change back to 1 when starter set if fixed
            }
            /**
             * WARNING: off by one because of missing stampField in the pattern.
             * @return field index
             */
            static int timeFieldDefinitionIndex() {
                return 1; //TODO VALIDATE STARTER SET: change back to 2 when starter set if fixed
            }
            /**
             * WARNING: off by one because of missing stampField in the pattern.
             * @return field index
             */
            static int authorFieldDefinitionIndex() {
                return 2; //TODO VALIDATE STARTER SET: change back to 3 when starter set if fixed
            }
            /**
             * WARNING: off by one because of missing stampField in the pattern.
             * @return field index
             */
            static int moduleFieldDefinitionIndex() {
                return 3; //TODO VALIDATE STARTER SET: change back to 4 when starter set if fixed
            }
            /**
             * WARNING: off by one because of missing stampField in the pattern.
             * @return field index
             */
            static int pathFieldDefinitionIndex() {
                return 4; //TODO VALIDATE STARTER SET: change back to 5 when starter set if fixed
            }
        }
    }

}