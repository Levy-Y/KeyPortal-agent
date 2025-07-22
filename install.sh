#!/bin/bash

function actionPrompt() {
  while true; do
      read -r -p "Do you want to install the agent, or uninstall an existing one? (i/U): " actionPrompt
      case "$actionPrompt" in
        [iI])
          echo "Proceeding to install the agent..."
          userInput
          break
          ;;
        [uU])
          uninstall
          break
          ;;
        *)
          echo "Please enter i or u."
          ;;
      esac
    done
}

function uninstall() {
    sudo systemctl disable keyportal-agent.service
    sudo systemctl stop keyportal-agent.service

    sudo rm /usr/local/bin/keyportal-agent
    sudo rm /etc/systemd/system/keyportal-agent.service

    sudo systemctl daemon-reload
}

function userInput() {
  read -r -p "Specify this agent's name: " agentName
  read -r -p "Specify RabbitMQ host address (ex.: 192.168.1.200): " rabbitMQHost
  read -r -p "Specify RabbitMQ port (ex.: 5672): " rabbitMQPort
  read -r -p "Specify RabbitMQ username: " rabbitMQUser
  read -r -p "Specify RabbitMQ password: " rabbitMQPass

  clear
  echo "You specified: "
  echo "Agent name: $agentName"
  echo "RabbitMQ: "
  echo "  Host: $rabbitMQHost"
  echo "  Port: $rabbitMQPort"
  echo "  Username: $rabbitMQUser"
  echo "  Password: $rabbitMQPass"

  confirm
}

function confirm() {
  while true; do
    read -r -p "Confirm? (y/N) " confirm
    case "$confirm" in
      [yY])
        echo "Proceeding to install the agent..."
        install
        break
        ;;
      [nN])
        userInput
        break
        ;;
      *)
        echo "Please enter y or n."
        ;;
    esac
  done
}

function install() {
  ./mvnw clean package -Pnative

  file=$(find ./target -maxdepth 1 -type f -name '*-runner' | head -n 1)
  if [ -n "$file" ]; then

    sudo cp "$file" /usr/local/bin/keyportal-agent

    user=$(whoami)

    sudo tee /etc/systemd/system/keyportal-agent.service > /dev/null <<EOF
[Unit]
Description=Keyportal Agent service

[Service]
Type=simple
User=$user
ExecStart=/usr/local/bin/keyportal-agent
Restart=on-failure
Environment=POLL_AGENT_NAME=$agentName
Environment=RABBITMQCLIENT_USERNAME=$rabbitMQUser
Environment=RABBITMQCLIENT_PASSWORD=$rabbitMQPass
Environment=RABBITMQCLIENT_HOSTNAME=$rabbitMQHost
Environment=RABBITMQCLIENT_PORT=$rabbitMQPort

[Install]
WantedBy=multi-user.target
EOF

    sudo systemctl daemon-reload
    sudo systemctl enable keyportal-agent.service
    sudo systemctl start keyportal-agent.service
  else
      echo "Build might have failed, as no executable found in /target"
  fi

}

actionPrompt