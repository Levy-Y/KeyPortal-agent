package io.levysworks.beans;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class KeyManager {
    private final Logger logger = Logger.getLogger(KeyManager.class.getName());
    private final List<String> keys;

    public KeyManager() {
        this.keys = new ArrayList<>();
    }

    @PostConstruct
    void init() throws IOException {
        this.readKeys();
    }

    public List<String> getKeys() {
        return keys;
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

    public void readKeys() throws IOException {
        this.keys.clear();

        this.ensureFileExists();

        String userHome = System.getProperty("user.home");
        File dotSSHFolder = new File(userHome, ".ssh");
        File authorizedKeysFile = new File(dotSSHFolder, "authorized_keys");

        try (BufferedReader reader = new BufferedReader(new FileReader(authorizedKeysFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                this.keys.add(line);
            }
        } catch (IOException ex) {
            this.logger.log(Level.WARNING, "Error reading authorized keys file", ex);
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
}
