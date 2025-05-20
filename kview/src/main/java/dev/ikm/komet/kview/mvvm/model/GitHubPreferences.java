package dev.ikm.komet.kview.mvvm.model;

/**
 * Immutable data class to hold GitHub preferences information for a given repository.
 */
public record GitHubPreferences(String gitUrl, String gitEmail, String gitUsername, char[] gitPassword) {}
