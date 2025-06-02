package br.dev.jadl.prefs;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.TRACE;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

import io.github.jopenlibs.vault.VaultException;
import io.github.jopenlibs.vault.api.Logical;
import io.github.jopenlibs.vault.response.LogicalResponse;

public class VaultPreferences extends AbstractPreferences {

    private static final System.Logger logger = System.getLogger(VaultPreferences.class.getCanonicalName());

    private final Set<String> children = new HashSet<>();

    private final String engine;
    private final Logical logical;

    VaultPreferences(final String engine, final Logical logical) {
        this(null, "", engine, logical);
    }

    private VaultPreferences(final AbstractPreferences parent, final String name, final String engine, final Logical logical) {
        super(parent, name);
        this.engine = engine;
        this.logical = logical;
    }

    @Override
    protected void putSpi(final String key, final String value) {
        try {
            final Map<String, String> data = this.logical.read(this.path()).getData();
            data.put(key, value);
            this.logical.write(this.path(), new HashMap<>(data));
        } catch (final VaultException e) {
            logger.log(ERROR, e.getMessage());
            if (logger.isLoggable(TRACE)) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected String getSpi(final String key) {
        try {
            final LogicalResponse response = this.logical.read(this.path());
            return response.getData().get(key);
        } catch (final VaultException e) {
            logger.log(ERROR, e.getMessage());
            if (logger.isLoggable(TRACE)) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected void removeSpi(final String key) {
        try {
            final Map<String, String> data = this.logical.read(this.path()).getData();
            data.remove(key);
            this.logical.write(this.path(), new HashMap<>(data));
        } catch (final VaultException e) {
            logger.log(ERROR, e.getMessage());
            if (logger.isLoggable(TRACE)) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void removeNodeSpi() throws BackingStoreException {
        try {
            this.logical.delete(this.path());
        } catch (final VaultException e) {
            logger.log(ERROR, e.getMessage());
            if (logger.isLoggable(TRACE)) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected String[] keysSpi() {
        try {
            return this.logical.read(this.path())
                .getData()
                .keySet()
                .toArray(String[]::new);
        } catch (VaultException e) {
            logger.log(ERROR, e.getMessage());
            if (logger.isLoggable(TRACE)) {
                e.printStackTrace();
            }
        }

        return new String[0];
    }

    @Override
    protected String[] childrenNamesSpi() {
        return this.children.toArray(String[]::new);
    }

    @Override
    protected AbstractPreferences childSpi(final String node) {
        final VaultPreferences child = new VaultPreferences(this, node, this.engine, this.logical);
        this.children.add(child.name());
        return child;
    }

    @Override
    protected synchronized void syncSpi() throws BackingStoreException {
        // Currently a no-op: Vault does not require explicit sync.
    }

    @Override
    protected synchronized void flushSpi() throws BackingStoreException {
        // No-op: All changes are written immediately to Vault in putSpi/removeSpi.
        // If local caching is implemented in the future, flush should persist them here.
    }

    private String path() {
        return Path.of(this.engine, this.absolutePath())
            .normalize()
            .toString();
    }
}
