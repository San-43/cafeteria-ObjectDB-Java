package org.cafeteria.cafeteria.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public final class DbBootstrap {
    private DbBootstrap() {
    }

    public static Path init() throws IOException {
        Path dataDir = Path.of("data");
        Files.createDirectories(dataDir);

        Path databaseFile = dataDir.resolve("cafeteria.odb");
        if (Files.notExists(databaseFile)) {
            String header = "Cafetería inicializada en " + LocalDateTime.now() + System.lineSeparator();
            Files.writeString(databaseFile, header, StandardCharsets.UTF_8);
        }

        return databaseFile;
    }
}
