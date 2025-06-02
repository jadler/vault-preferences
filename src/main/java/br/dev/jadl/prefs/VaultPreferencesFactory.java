package br.dev.jadl.prefs;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.VaultException;

public class VaultPreferencesFactory implements PreferencesFactory {

    private static final Map<String, Preferences> preferences = new HashMap<>();

    @Override
    public Preferences systemRoot() {
        // TODO - Config which path (engine) to use
        try {
            return preferences("secret/data", "system");
        } catch (final VaultException e) {
            e.printStackTrace();
            return Preferences.systemRoot(); // Recursive?
        }
    }

    @Override
    public Preferences userRoot() {
        try {
            return preferences("cubbyhole/data", "user");
        } catch (final VaultException e) {
            e.printStackTrace();
            return Preferences.userRoot(); // Recursive?
        }
    }

    private static Preferences preferences(final String engine, final String scope) throws VaultException {
        
        final String prefix = VaultPreferences.class.getCanonicalName();
        final String address = System.getProperty(String.format("%s.address", prefix), System.getenv("VAULT_ADDR"));
        final String token = System.getProperty(String.format("%s.token", prefix), System.getenv("VAULT_TOKEN"));

        final VaultConfig config = new VaultConfig()
            .address(address)
            .token(token)
            .build();

        final Vault vault = Vault.create(config);

        final String key = String.format("%s:%s:%s", address, token, scope);
        
        return preferences.computeIfAbsent(key, k -> new VaultPreferences(engine, vault.logical()));
    }
}

