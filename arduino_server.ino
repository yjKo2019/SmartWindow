#include "Ethernet.h"
#include "SPI.h"

byte mac[] = { 0xDE, 0xAB, 0xBE, 0xEF, 0xFE, 0xED };
byte ip[] = { 192, 168, 0, 100 };//아두이노 서버 IP 설정
byte gateway[] = { 255, 255, 255, 0 };
byte subnet[] = { 255, 255, 255, 0 };
EthernetServer server(80);//아두이노 서버 포트 설정
EthernetClient client;

String read_data = "";
String send_data = "";
unsigned long lastMillis;



#include <Stepper.h>//스텝모터 라이브러리 사용
const int stepsPerRevolution = 160;//200 스텝 = 1바퀴
Stepper myStepper(stepsPerRevolution, 30, 31, 32, 33);//창문에 쓰일 스텝모터 아두이노 30~33번핀 사용
const int stepsPerlittle = 80;//200 스텝 = 1바퀴
Stepper myStepperlittle(stepsPerlittle, 30, 31, 32, 33);//창문에 쓰일 스텝모터 아두이노 30~33번핀 사용
int set_step = 2;//창문 열고 닫는 회전수
int window_state = 0;//창문 현재 상태를 저장할 변수   0 = off, 1 = on

int manual_automatic = 0;//창문 제어 수동 자동 설정값을 저장할 변수   0 = 수동, 1 = 자동



//현재시간을 저장할 변수
int real_time_h = 0;
int real_time_m = 0;
int real_time_s = 0;

//예약열림시간을 저장할 변수
int set_time_h1 = 0;
int set_time_m1 = 0;
int set_time_s1 = 0;

//예약닫힘시간을 저장할 변수
int set_time_h2 = 0;
int set_time_m2 = 0;
int set_time_s2 = 0;

#include <TimeLib.h>//시간 라이브러리 사용
int time_state = 0;


// 미세먼지 관련 변수
int measurePin = A0;
int ledPower = 2;

int samplingTime = 280;
int deltaTime = 40;
int sleepTime = 9680;

float voMeasured = 0;
float calcVoltage = 0;
float dustDensity = 0;
int int_dustDensity = 0;


#include "DHT.h"//온도 센서 라이브러리 사용
#define DHTPIN 34//온도 센서 아두이노 34번핀 사용
#define DHTTYPE DHT11
DHT dht(DHTPIN, DHTTYPE);
int temp = 0; //현재 온도값을 저장할 변수
int humi = 0; //현재 습도값을 저장할 변수


int water_sensor = A1;//빗물 센서 아두이노 A1번핀 사용
int water_value = 0;
int gas_sensor = A2;//가스 센서 아두이노 A2번핀 사용
int gas_value = 0;



void setup() {
  Serial.begin(9600);
  Ethernet.begin(mac, ip, gateway, subnet);
  client.setTimeout(50);

  pinMode(30, OUTPUT);
  pinMode(31, OUTPUT);
  pinMode(32, OUTPUT);
  pinMode(33, OUTPUT);
  myStepper.setSpeed(80);//스텝모터 속도 설정

  pinMode(A0, INPUT);
  pinMode(ledPower, OUTPUT);

  dht.begin();

  pinMode(water_sensor , INPUT);
  pinMode(gas_sensor , INPUT);

  delay(1000);
  Serial.println("loop start");
}



void loop() {

  if (millis() - lastMillis > 1000) {//1초마다 한번 실행되는 부분
    lastMillis = millis();

    if (time_state == 1) {//time_state 값이 1이면
      real_time_h = hour();//현재 시간을 해당변수에 저장
      real_time_m = minute();
      real_time_s = second();

      //현재 시간 시리얼모니터 출력
      printDigits(real_time_h);
      Serial.print(":");
      printDigits(real_time_m);
      Serial.print(":");
      printDigits(real_time_s);
    }

    water_value = analogRead(water_sensor);//빗물센서값 저장
    gas_value = analogRead(gas_sensor);//가스센서값 저장

    temp = dht.readTemperature();//현재 온도값 저장
    humi = dht.readHumidity();//현재 습도값 저장

    //미세먼지센서값 가져오는 부분 (오픈소스)
    digitalWrite(ledPower, LOW);
    delayMicroseconds(samplingTime);
    voMeasured = analogRead(measurePin);
    delayMicroseconds(deltaTime);
    digitalWrite(ledPower, HIGH);
    delayMicroseconds(sleepTime);
    calcVoltage = voMeasured * (5.0 / 1024.0);
    dustDensity = 0.17 * calcVoltage - 0.1;
    dustDensity = dustDensity * 1000;// μg/m3
    int_dustDensity = dustDensity;

    if (int_dustDensity <= 0) {//미세먼지값이 0보다 작으면 0으로 한다
      int_dustDensity = 0;
    }

    //해당데이터를 시리얼 출력
    Serial.print("     water_value: ");
    Serial.print(water_value);
    Serial.print("     gas_value: ");
    Serial.print(gas_value);

    Serial.print("     temp: ");
    Serial.print(temp);
    Serial.print("     humi: ");
    Serial.print(humi);
    Serial.print("     dustDensity: ");
    Serial.print(int_dustDensity);

    Serial.print("     window_state: ");
    Serial.print(window_state);
    Serial.print("     manual_automatic: ");
    Serial.println(manual_automatic);


    // 실시간 데이터 전송을 위한 데이터꾸러미를 만들어둔다
    // 데이터사이마다 /부호로 나누어둔다 
    send_data  = "@/";
    send_data = send_data + (temp);
    send_data = send_data + "/";
    send_data = send_data + (int_dustDensity);
    send_data = send_data + "/";
    send_data = send_data + (water_value);
    send_data = send_data + "/";
    send_data = send_data + (gas_value);
    send_data = send_data + "/";
    send_data = send_data + (window_state);
    send_data = send_data + "/";
    send_data = send_data + (manual_automatic);
    send_data = send_data + "/#";

    if (manual_automatic == 0) {//만약 창문모드가 수동이라면

      //예약열림설정이 되어있다면 해당 시간에 창문 열림
      if (set_time_h1 + set_time_m1 + set_time_s1 != 0) {
        if (set_time_h1 == real_time_h && set_time_m1 == real_time_m && set_time_s1 == real_time_s) {
          Serial.println("automatic window on");
          window_on();
        }
      }
      //예약닫힘설정이 되어있다면 해당 시간에 창문 닫힘
      if (set_time_h2 + set_time_m2 + set_time_s2 != 0) {
        if (set_time_h2 == real_time_h && set_time_m2 == real_time_m && set_time_s2 == real_time_s) {
          Serial.println("automatic window off");
          window_off();
        }
      }

    } else {//만약 창문모드가 자동이라면

        if (gas_value > 300) { //만약 가스센서값이 300보다크면
        window_on(); //창문 ON
      } else if (water_value >= 500) {//만약 빗물센서값이 500이거나 500보다크면
        window_off();//창문 OFF
      } else if (temp <= 21) {//만약 온도값이 21이거나 21보다 작으면
        window_off();//창문 OFF
      } else if (int_dustDensity >= 300) {//만약 먼지센서값이 300이거나 300보다크면
        window_off();//창문 OFF
      } else {//위의 3가지 모두 아니라면
        window_on();//창문 ON
      }
      

    }



  }



  char temp[100];
  client = server.available();                  // 클라이언트 선언

  if (client) {                                 // 클라이언트가 있으면
    while (client.connected()) {                // 클라이언트가 연결 되면

      if (client.available()) {                 // 읽을 데이터가 있으면
        byte leng = client.readBytes(temp, 7);  // 데이터 읽기

        for (int i = 0; i < leng; i++) {
          read_data = read_data + temp[i];
        }

        if (leng == 7) {//만약 수신 받은 데이터 길이가 7이고

          if (read_data == "getdata") {//수신받은 데이터가 getdata 라면

            client.print(send_data);//클라이언트로 send_data를 보낸다

          } else if (read_data == "window1") {//수신받은 데이터가 window1 라면

            window_on();//창문 여는 함수를 실행하고
            manual_automatic = 0;//창문 모드는 수동으로 하고
            set_time_h1 = 0;//예약시간을 초기화 한다
            set_time_m1 = 0;
            set_time_s1 = 0;
            set_time_h2 = 0;
            set_time_m2 = 0;
            set_time_s2 = 0;

          } else if (read_data == "window0") {// 위와 같다

            window_off();
            manual_automatic = 0;
            set_time_h1 = 0;
            set_time_m1 = 0;
            set_time_s1 = 0;
            set_time_h2 = 0;
            set_time_m2 = 0;
            set_time_s2 = 0;

           } else if (read_data == "window3") {//수신받은 데이터가 window1 라면

            windowlittle_on();//창문 여는 함수를 실행하고
            manual_automatic = 0;//창문 모드는 수동으로 하고
            set_time_h1 = 0;//예약시간을 초기화 한다
            set_time_m1 = 0;
            set_time_s1 = 0;
            set_time_h2 = 0;
            set_time_m2 = 0;
            set_time_s2 = 0;

          } else if (read_data == "window2") {// 위와 같다

            windowlittle_off();
            manual_automatic = 0;
            set_time_h1 = 0;
            set_time_m1 = 0;
            set_time_s1 = 0;
            set_time_h2 = 0;
            set_time_m2 = 0;
            set_time_s2 = 0;

          } else if (read_data == "mode__1") {//수신받은 데이터가 mode__1 이면

            manual_automatic = 1;//manual_automatic값을 1로한다 (창문모드를 자동)
            set_time_h1 = 0;//예약시간을 초기화 한다
            set_time_m1 = 0;
            set_time_s1 = 0;
            set_time_h2 = 0;
            set_time_m2 = 0;
            set_time_s2 = 0;

          } else if (read_data == "mode__0") {//수신받은 데이터가 mode__0 이면

            manual_automatic = 0;//manual_automatic값을 0으로한다 (창문모드를 수동)

          } else if (read_data.endsWith("@") == 1) {//수신받은 데이터의 마지막 데이터가 @라면

            //예약열림시간 수신
            Serial.println("Set Time 1");
            Serial.println();
            //substring 함수를 이용하여 데이터를 파싱해서 해당 변수에 각각 시,분,초 저장
            set_time_h1 = read_data.substring(0, 2).toInt();
            set_time_m1 = read_data.substring(2, 4).toInt();
            set_time_s1 = read_data.substring(4, 6).toInt();
            Serial.print("Set H 1: "); Serial.println(set_time_h1);
            Serial.print("Set M 1: "); Serial.println(set_time_m1);
            Serial.print("Set S 1: "); Serial.println(set_time_s1);

          } else if (read_data.endsWith("#") == 1) {//수신받은 데이터의 마지막 데이터가 #라면

            //예약닫힘시간 수신
            Serial.println("Set Time 2");
            Serial.println();
            //substring 함수를 이용하여 데이터를 파싱해서 해당 변수에 각각 시,분,초 저장
            set_time_h2 = read_data.substring(0, 2).toInt();
            set_time_m2 = read_data.substring(2, 4).toInt();
            set_time_s2 = read_data.substring(4, 6).toInt();
            Serial.print("Set H 2: "); Serial.println(set_time_h2);
            Serial.print("Set M 2: "); Serial.println(set_time_m2);
            Serial.print("Set S 2: "); Serial.println(set_time_s2);

          } else if (read_data.endsWith("$") == 1) {//수신받은 데이터의 마지막 데이터가 $라면

            //스마트폰 현재 시간을 수신
            Serial.println("Real Time");
            Serial.println();
            //substring 함수를 이용하여 데이터를 파싱해서 해당 변수에 각각 시,분,초 저장
            real_time_h = read_data.substring(0, 2).toInt();
            real_time_m = read_data.substring(2, 4).toInt();
            real_time_s = read_data.substring(4, 6).toInt();
            Serial.print("Real Time H: "); Serial.println(real_time_h);
            Serial.print("Real Time M: "); Serial.println(real_time_m);
            Serial.print("Real Time S: "); Serial.println(real_time_s);

            time_state = 1;//해당 변수를 1로 변경
            //현재시간을 설정 해준다.
            setTime(real_time_h, real_time_m, real_time_s, 28, 2, 19);

          }

        }

        Serial.print("read_data : ");
        Serial.println(read_data);//수신받은 데이터를 시리얼출력 하고
        read_data = "";//read_data 초기화

        //client.stop();
        break;

      }
    }
  }
}



void window_on() {//창문 ON 함수
  if (window_state != 1) {//window_state 변수값 1 아니면
    window_state = 1;//window_state 변수값 1 변경

    for (int i = 0; i < set_step; i++) {
      myStepper.step(stepsPerRevolution);
    }
  }
  digitalWrite(30, LOW);
  digitalWrite(31, LOW);
  digitalWrite(32, LOW);
  digitalWrite(33, LOW);
}

void window_off() {//창문 OFF 함수
  if (window_state != 0) {
    window_state = 0;

    for (int i = 0; i < set_step; i++) {
      myStepper.step(-stepsPerRevolution);
    }
  }
  digitalWrite(30, LOW);
  digitalWrite(31, LOW);
  digitalWrite(32, LOW);
  digitalWrite(33, LOW);
}

//코드추가부분
void windowlittle_on() {//창문 ON 함수
  if (window_state != 3) {//window_state 변수값 3 아니면
    window_state = 3;//window_state 변수값 3 변경

    for (int i = 0; i < set_step; i++) {
      myStepper.step(stepsPerlittle);
    }
  }
  digitalWrite(30, LOW);
  digitalWrite(31, LOW);
  digitalWrite(32, LOW);
  digitalWrite(33, LOW);
}

void windowlittle_off() {//창문 OFF 함수
  if (window_state != 2) {
    window_state = 2;

    for (int i = 0; i < set_step; i++) {
      myStepper.step(-stepsPerlittle);
    }
  }
  digitalWrite(30, LOW);
  digitalWrite(31, LOW);
  digitalWrite(32, LOW);
  digitalWrite(33, LOW);
}

//입력받은 변수값이 10보다 작으면 앞에 0을 하나 더 붙여줘서 출력해주는 함수
void printDigits(int digits) {
  if (digits < 10)
    Serial.print('0');
  Serial.print(digits);
}
