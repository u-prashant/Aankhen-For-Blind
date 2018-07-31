#define trigPin 12
#define echoPin 11
#define buzzer 6
#define ledPin 13

int prev = -1;
int curr;

void setup() {
  // put your setup code here, to run once:
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);
  pinMode(ledPin, OUTPUT);
  pinMode(buzzer, OUTPUT);
  Serial.begin(9600);
}

void loop() {
  // put your main code here, to run repeatedly:
  long duration, distance;
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);
  duration = pulseIn(echoPin, HIGH);
  distance = (duration/2) / 29.1;
  if (distance < 70)
  {
    curr = 0;
    digitalWrite(ledPin, HIGH);
    // digitalWrite(motor, HIGH);
    digitalWrite(buzzer, HIGH);
    if (prev!=curr) {
      Serial.print("WAIT");
    }   
  }
  else
  {
    curr = 1;
    digitalWrite(13, LOW);
    // digitalWrite(motor, LOW);
    digitalWrite(buzzer, LOW);
    if (prev!=curr) {
      Serial.print("MOVE");
    }   
  }
  prev = curr;
  delay(500);
}
