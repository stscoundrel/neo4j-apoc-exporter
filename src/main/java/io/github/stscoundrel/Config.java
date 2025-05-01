package io.github.stscoundrel;

public record Config(
        String neo4jUri,
        String neo4jUser,
        String neo4jPassword,
        int exporterPort
) {
    public static Config fromEnv() {
        return new Config(
                getEnvOrDefault("NEO4J_URI", "bolt://localhost:7687"),
                getEnvOrDefault("NEO4J_USER", "neo4j"),
                getEnvOrDefault("NEO4J_PASSWORD", "password"),
                getEnvOrDefaultInt("EXPORTER_PORT", 17687)
        );
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }

    private static int getEnvOrDefaultInt(String key, int defaultValue) {
        String value = System.getenv(key);
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            System.err.println("Invalid number for " + key + ": " + value + ", falling back to " + defaultValue);
            return defaultValue;
        }
    }
}