# SmartHome AI Agent

## Overview
This is a SmartHome AI Agent system that can control various IoT devices in a smart home environment. The system uses a multi-agent architecture where different agents are responsible for different types of devices:
- Appliance Agent: Controls appliances like lights, TVs, coffee makers, etc.
- Climate Agent: Controls climate devices like thermostats, heaters, etc.
- Security Agent: Controls security devices like cameras, door locks, etc.

## Building the Application
To build the application, run:
```bash
./gradlew build
```

## Running the Application
There are several ways to run the application:

### 1. Using Gradle (Note: Interactive mode may not work properly)
```bash
./gradlew run
```

### 2. Using the fat JAR (Recommended for interactive mode)
First, build the fat JAR:
```bash
./gradlew fatJar
```

Then run it directly with Java:
```bash
java -jar build/libs/SmartHome_AI_Agent-1.0-SNAPSHOT-all.jar
```

### 3. Using the provided batch script (Windows)
```bash
run-app.bat
```

## Using the Application
Once the application is running, you can interact with it by typing commands in natural language. Some example commands:
- "Turn on the lights"
- "Set temperature to 70 degrees"
- "Lock all doors"
- "Brew coffee"
- "Turn on the TV and play some music"

Type 'help' to see a list of available commands.
Type 'quit' or 'exit' to exit the application.

## Device Management
The system is aware of 30 different IoT devices distributed across the smart home:
- 13 Appliance devices (lights, kitchen appliances, laundry appliances, entertainment devices)
- 6 Climate devices (thermostats, heaters, air conditioners)
- 11 Security devices (cameras, door locks, motion sensors, alarm systems)

Each agent is only aware of the devices it is responsible for controlling.