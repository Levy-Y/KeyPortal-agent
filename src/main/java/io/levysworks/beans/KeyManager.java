package io.levysworks.beans;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class KeyManager {
    private final Logger logger = Logger.getLogger(KeyManager.class.getName());

    public KeyManager() {}

    @PostConstruct
    void init() throws IOException {
        this.logger.log(Level.INFO, "Initializing KeyManager");
        this.ensureFileExists();
    }

    private void ensureFileExists() throws IOException {
        String userHome = System.getProperty("user.home");
        File dotSSHFolder = new File(userHome, ".ssh");
        File authorizedKeysFile = new File(dotSSHFolder, "authorized_keys");

        if (!authorizedKeysFile.exists()) {
            authorizedKeysFile.getParentFile().mkdirs();
            authorizedKeysFile.createNewFile();
        }
    }

    public void addKey(String key) throws IOException {
        this.ensureFileExists();

        String userHome = System.getProperty("user.home");
        File dotSSHFolder = new File(userHome, ".ssh");
        File authorizedKeysFile = new File(dotSSHFolder, "authorized_keys");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(authorizedKeysFile, true))) {
            writer.write(key);
        }
    }

    public void removeKey(String keyUidToRemove) throws IOException {
        this.ensureFileExists();

        String userHome = System.getProperty("user.home");
        File dotSSHFolder = new File(userHome, ".ssh");
        File authorizedKeysFile = new File(dotSSHFolder, "authorized_keys");

        List<String> currentKeys = new ArrayList<>();

        try (Scanner reader = new Scanner(authorizedKeysFile)) {
            while (reader.hasNextLine()) {
                String line = reader.nextLine().trim();
                if (!line.isEmpty()) {
                    currentKeys.add(line);
                }
            }
        }

        List<String> finalKeys = currentKeys.stream().filter(key -> {
            String[] keyParts = key.split(" ");
            return !Objects.equals(keyParts[2], keyUidToRemove);
        }).toList();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(authorizedKeysFile))) {
            for (String key : finalKeys) {
                writer.write(key);
                writer.newLine();
            }
        }
    }
}
