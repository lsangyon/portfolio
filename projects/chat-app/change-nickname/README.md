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

이 프로젝트는 기존 C 기반 `select()` TCP 채팅 서버를 C#의 비동기 TCP 서버 구조로 확장한 프로젝트입니다.

C 버전에서는 소켓 프로그래밍의 기본 흐름인 `socket()`, `bind()`, `listen()`, `accept()`, `select()`를 학습하고 구현했습니다.
C# 버전에서는 `TcpListener`, `TcpClient`, `async / await`를 활용하여 클라이언트 세션 관리, 닉네임 등록, 중복 검사, 브로드캐스트, 실행 중 닉네임 변경 기능을 구현했습니다.

이를 통해 저수준 소켓 프로그래밍의 기본 개념을 이해한 뒤, C# 환경에서 비동기 서버 구조로 확장하는 과정을 경험했습니다.

