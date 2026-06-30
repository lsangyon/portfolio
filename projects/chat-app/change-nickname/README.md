# C# TCP 채팅 서버 - 닉네임 변경 기능 포함

기존에 C로 구현했던 TCP 채팅 서버를 C#/.NET 환경으로 포팅하고, 기능을 확장한 프로젝트입니다.

## 주요 기능

* `TcpListener`, `TcpClient` 기반 TCP 서버/클라이언트 구현
* `async / await`를 활용한 비동기 클라이언트 처리
* 다중 클라이언트 접속 처리
* 최초 접속 시 닉네임 등록
* 중복 닉네임 검사
* 채팅 메시지 브로드캐스트
* 실행 중 닉네임 변경 기능

  * `/nick 새닉네임`
* 접속 종료 명령어

  * `/quit`
* 같은 PC의 localhost 환경 또는 같은 LAN/Wi-Fi에 연결된 기기 간 통신 지원

## 실행 요구사항

* .NET SDK 설치 필요

## 서버 실행 방법

```bash
cd ChatServer
dotnet run -- 10140
```

포트를 생략하면 기본값으로 `10140`번 포트를 사용합니다.

```bash
dotnet run
```

## 같은 PC에서 클라이언트 실행

첫 번째 터미널:

```bash
cd ChatClient
dotnet run -- 127.0.0.1 alice 10140
```

다른 터미널:

```bash
cd ChatClient
dotnet run -- 127.0.0.1 bob 10140
```

## 같은 Wi-Fi에 연결된 다른 기기에서 접속하기

먼저 서버를 실행 중인 PC의 LAN IP 주소를 확인합니다.

Windows:

```bash
ipconfig
```

Linux/macOS:

```bash
ip addr
# 또는
ifconfig
```

그 다음 클라이언트에서 서버 PC의 IP 주소를 입력해 실행합니다.

```bash
dotnet run -- 192.168.0.23 alice 10140
```

## 명령어

닉네임 변경:

```bash
/nick 새닉네임
```

채팅 종료:

```bash
/quit
```

## 포트폴리오 설명

기존에 C로 만들었던 select() 기반 TCP 채팅 서버를 C#/.NET으로 다시 구현한 프로젝트입니다.

C 버전에서는 socket(), bind(), listen(), accept(), select()를 사용해 서버 소켓을 열고, 여러 클라이언트의 접속과 메시지 수신을 처리했습니다.

C# 버전에서는 TcpListener로 클라이언트 접속을 받고, 접속한 클라이언트를 세션 목록에 등록한 뒤 async / await 기반으로 메시지 수신과 종료 처리를 수행하도록 구현했습니다. 여기에 닉네임 등록, 중복 닉네임 검사, 채팅 브로드캐스트, /nick 명령을 통한 닉네임 변경, /quit 종료 기능을 추가했습니다.

이 프로젝트는 C에서 학습한 TCP 서버 구조를 C# 방식으로 옮겨 보면서, 클라이언트 세션 관리와 비동기 처리 구조를 연습하기 위해 만들었습니다.

