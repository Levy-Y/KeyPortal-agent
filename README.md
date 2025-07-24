# Keyportal (Agent)

Keyportal Agent is a Quarkus-based service that consumes RabbitMQ queues (`.add` and `.remove`) to manage SSH keys on a
server by adding or removing keys from the authorized_keys file based on the UID embedded as the comment in the OpenSSH
key.

## Overview

The agent listens to RabbitMQ queues for key management instructions. When a new key is added to the `.add` queue, it
appends the key to the user's `authorized_keys`. When a removal message is received on the `.remove` queue, it removes
the
key matching the UID from the `authorized_keys` file. The UID corresponds to the comment part (3rd field) of the OpenSSH
key.
This agent runs as a systemd service on servers where SSH key management is required.

## Features

- Consumes `.add` and `.remove` RabbitMQ queues to sync SSH keys
- Adds new SSH public keys to `authorized_keys` for users
- Removes SSH keys by UID (key comment) from `authorized_keys`
- Installed and managed as a systemd service
- Built with Quarkus for fast startup and low resource use

## Prerequisites

- OpenSSH Server installed and running (includes ssh-keygen command)
- RabbitMQ server accessible with configured credentials
- User account with permission to modify .ssh/authorized_keys
- Systemd init system

## Installation

Install via the included `install.sh` script, which prompts for configuration details and installs/uninstalls the agent
as
a systemd service.

```sh
./install.sh
```

The script will ask for:

- Service installation or uninstallation option
- Unique agent name
- RabbitMQ connection details

## Usage

Once installed and running, the agent works autonomously. It continuously listens to the queues for new actions.
No further action is required.

Logs are available via `journalctl -u keyportal-agent.service`.

## Configuration

Configuration is handled during installation and stored in the service environment. It includes:

- RabbitMQ host, port, username, password
- Agent identifier (the name must match the one configured on the central manager server)

## Troubleshooting

- Verify OpenSSH server and ssh-keygen command are installed.
- Check systemd service status:
    ```sh
    systemctl status keyportal-agent.service
    ```
- Inspect logs for errors:
    ```sh
    journalctl -u keyportal-agent.service -f
    ```
- Confirm RabbitMQ credentials and connectivity.

## Development

This is a Quarkus application. To build from source:

```sh
./mvnw clean package
```
or
```sh
make build
```

Run locally with:

```sh
java -jar target/keyportal-agent-*.jar
```

## License

This project is licensed under the AGPL-3.0 License.

## Author

**Levente Hagymási**
<br>
GitHub: [@Levy-Y](https://github.com/Levy-Y) <br>
LinkedIn: [in/leventehagymasi](https://www.linkedin.com/in/leventehagymasi)
