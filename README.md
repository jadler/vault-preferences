# Vault Preferences API
Implementation of the Java `Preferences` API backed by **HashiCorp Vault**.

This project allows you to persist Java preferences securely in Vault using the official Java driver and supports both system and user preferences.

## Features

- Java Preferences API backed by Vault (kv-v2, cubbyhole, etc.)
- Works with any Vault-compatible storage engine
- Simple integration via system properties
- Secure, token-based authentication
- Container-based integration tests

# Technologies Used

- Java (JDK 17+)
- Vault (via vault-java-driver)
- Java Preferences API
- Testcontainers (for integration tests)

## How to Use
Add the following dependency:

```xml
<dependency>
    <groupId>br.dev.jadl.preferences</groupId>
    <artifactId>vault-preferences</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Configuration via System Properties

You can configure the Vault backend via system properties or environment variables.

- `br.dev.jadl.prefs.VaultPreferences.address`	(VAULT_ADDR) Vault address
- `br.dev.jadl.prefs.VaultPreferences.token` (VAULT_TOKEN) Vault token for access

## Testing
This project uses Testcontainers to spin up a real Vault container during testing. **Ensure Docker is running.**

Run tests with:

```bash
mvn test
```

## Known Issues

- No support yet for user identity-based separation.
- sync() and flush() are not implemented due to Vault's stateless nature.
- Uses KV engines directly — no encryption keys, policies or leases managed internally.


## **Contributing**

If you encounter any issues or would like to contribute to improving this project, feel free to open an issue or submit
a pull request. We welcome your contributions!

## **License**

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
