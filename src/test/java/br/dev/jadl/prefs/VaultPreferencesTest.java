package br.dev.jadl.prefs;

import static java.util.logging.Level.OFF;
import static java.util.logging.Logger.GLOBAL_LOGGER_NAME;

import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.vault.VaultContainer;

import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.VaultException;

public class VaultPreferencesTest extends PreferencesTest {

    private static final String VAULT_TOKEN = "root-token";

    @SuppressWarnings("resource")
    public static VaultContainer<?> container = new VaultContainer<>(DockerImageName.parse("hashicorp/vault:1.13"))
        .withVaultToken(VAULT_TOKEN)
        .withInitCommand("secrets enable kv-v2")
        .withReuse(false);

    private static Vault vault;

    @BeforeAll
    public static void init() {
        // Required to suppress log messages as vault-java-driver uses JUL;
        LogManager.getLogManager().reset();
        Logger.getLogger(GLOBAL_LOGGER_NAME).setLevel(OFF);
    }

    @BeforeEach
    public void setup() {

        container.start();
        
        final String host = container.getHttpHostAddress();

        try {
            VaultConfig config = new VaultConfig()
                .address(host)
                .token(VAULT_TOKEN)
                .build();

            vault = Vault.create(config);

        } catch (VaultException ignored) {
            Assertions.fail();
        }

        final String cname = VaultPreferences.class.getCanonicalName();
        System.setProperty(PreferencesFactory.class.getCanonicalName(), VaultPreferencesFactory.class.getCanonicalName());
        System.setProperty(String.format("%s.address", cname), host);
    }

    @AfterEach
    public void cleanup() {
        container.stop();
    }

    private static Stream<Arguments> provider() {
        return Stream.of(
            Arguments.of((Supplier<Preferences>)() -> Preferences.userRoot(), "cubbyhole/data"),
            Arguments.of((Supplier<Preferences>)() -> Preferences.systemRoot(), "secret/data"));
    }

    @ParameterizedTest
    @MethodSource("provider")
    public void testPersistedData(final Supplier<Preferences> supplier, final String path) throws Exception {
        final String key = "test";
        final String value = "test-value";

        final Preferences prefs = supplier.get();
        prefs.put(key, value);
        Map<String, String> data = Assertions.assertDoesNotThrow(() -> vault.logical().read(path).getData());
        Assertions.assertEquals(value, data.get(key));
    }
}
