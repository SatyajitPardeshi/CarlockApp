#include <WiFi.h>

// Define pin numbers for connected components
#define RELAY_PIN   9   // Relay controls the door locking mechanism
#define BUZZER_PIN  3   // Buzzer provides audible feedback on actions

// WiFi Access Point credentials
char ssid[] = "CarLockSystem";
char password[] = "12345678";

// Create an HTTP server on port 80
WiFiServer server(80);

// Track the current lock status
bool isLocked = false;

void setup() {
  Serial.begin(115200);

  // Configure pin modes for output
  pinMode(RELAY_PIN, OUTPUT);
  pinMode(BUZZER_PIN, OUTPUT);

  // Initialize system in unlocked state
  digitalWrite(RELAY_PIN, LOW);
  digitalWrite(BUZZER_PIN, LOW);

  // Start WiFi in Access Point mode
  WiFi.beginNetwork(ssid, password);
  
  while (WiFi.localIP() == INADDR_NONE) {
    delay(100);
    Serial.println("Waiting for AP IP...");
  }

  // Display Access Point information
  Serial.println("WiFi Access Point initialized");
  Serial.print("SSID: ");
  Serial.println(ssid);
  Serial.print("IP Address: ");
  Serial.println(WiFi.localIP());
  Serial.println("Available commands: /lock and /unlock");

  // Start the web server
  server.begin();
}

void loop() {
  // Check if a client is connected to the server
  WiFiClient client = server.available();
  if (client) {
    // Read the client's HTTP request
    String request = client.readStringUntil('\r');
    client.flush();

    // Process HTTP GET requests
    if (request.indexOf("/lock") >= 0) {
      lockDoor();
    } else if (request.indexOf("/unlock") >= 0) {
      unlockDoor();
    }

    // Send a basic HTTP response
    client.println("HTTP/1.1 200 OK");
    client.println("Content-Type: text/html");
    client.println("Connection: close");
    client.println();
    client.println("<html><body><h1>Command Received</h1></body></html>");
    client.stop();
  }

  // Manual control via serial input
  if (Serial.available()) {
    char input = Serial.read();
    if (input == 'L' || input == 'l') {
      lockDoor();
    } else if (input == 'U' || input == 'u') {
      unlockDoor();
    }
  }
}

// Locks the vehicle by activating the relay
void lockDoor() {
  digitalWrite(RELAY_PIN, HIGH);
  Serial.println("Vehicle locked");
  beep(3);
  isLocked = true;
}

// Unlocks the vehicle by deactivating the relay
void unlockDoor() {
  digitalWrite(RELAY_PIN, LOW);
  Serial.println("Vehicle unlocked");
  beep(1);
  isLocked = false;
}

// Generates a beep sound multiple times
void beep(int times) {
  for (int i = 0; i < times; i++) {
    digitalWrite(BUZZER_PIN, HIGH);
    delay(150);
    digitalWrite(BUZZER_PIN, LOW);
    delay(150);
  }
}
