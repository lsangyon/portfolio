# Pascal-like Compiler 구현

## Overview

Pascal 계열의 간단한 언어를 대상으로 어휘 분석, 구문 분석, 의미 검사, CASL II 코드 생성을 수행하는 컴파일러 프로젝트입니다.
입력 프로그램을 토큰 파일로 변환하고, 문법 및 의미 오류를 검사한 뒤, CASL II 어셈블리 코드로 변환하는 흐름을 구현했습니다.

프로젝트는 단계별로 모듈을 분리하여 구현했습니다.

- `s1/lexer`: 어휘 분석기
- `s2/parser`: 구문 분석기
- `s3/checker`: 의미 검사기
- `s4/compiler`: CASL II 코드 생성기
- `casl`: CASL II 실행 보조 코드

---

## Tech Stack

- Java
- Gradle
- Lexer / Parser
- Semantic Checker
- CASL II Code Generation
- Symbol Table
- Recursive Descent Parsing

---

## Features

### 1. Lexer

Pascal-like 소스 파일을 읽고, 예약어, 식별자, 상수, 문자열, 연산자 등을 토큰 단위로 분리합니다.
분석 결과는 `.ts` 토큰 파일로 출력합니다.

### 2. Parser

토큰 파일을 입력받아 프로그램 구조가 문법 규칙에 맞는지 검사합니다.
프로그램 선언, 변수 선언, 배열, 조건문, 반복문, 대입문, 입출력문, 서브프로그램 호출 등의 구문을 처리합니다.

### 3. Semantic Checker

식별자 선언 여부, 타입 일치, 배열 참조, 서브프로그램 호출 등 의미적 오류를 검사합니다.
심볼 테이블을 활용하여 변수와 타입 정보를 관리합니다.

### 4. Compiler

문법 및 의미 검사를 통과한 입력을 CASL II 코드로 변환합니다.
조건문, 반복문, 연산식, 변수 참조, 배열 접근, 입출력 처리 등을 CASL II 명령어로 생성합니다.

---

## Project Structure

```text
compiler/
├─ README.md
├─ build.gradle
├─ settings.gradle
├─ gradlew
├─ gradle/
├─ lib/
│  ├─ jcasl2.jar
│  └─ jcomet2.jar
├─ data/
│  ├─ pas/      # Pascal-like sample source files
│  ├─ ts/       # Token sample files
│  ├─ cas/      # CASL II sample files and lib.cas
│  └─ ans/      # Execution result samples
├─ src/main/java/enshud/
│  ├─ Main.java
│  ├─ casl/
│  ├─ s1/lexer/
│  ├─ s2/parser/
│  ├─ s3/checker/
│  └─ s4/compiler/
└─ tmp/
```

---

## Build

```bash
./gradlew build
```

Windows PowerShell에서는 다음 명령어를 사용할 수 있습니다.

```bash
./gradlew.bat build
```

---

## Run Examples

### Lexer

```bash
./gradlew run --args="lexer data/pas/normal01.pas tmp/out.ts"
```

### Parser

```bash
./gradlew run --args="parser data/ts/normal01.ts"
```

### Semantic Checker

```bash
./gradlew run --args="checker data/ts/normal01.ts"
```

### Compiler

```bash
./gradlew run --args="compiler data/ts/normal01.ts tmp/out.cas"
```

### Compile and Execute

```bash
./gradlew run --args="all data/pas/normal01.pas tmp/"
```

---

## What I Learned

이 프로젝트를 통해 컴파일러가 소스 코드를 한 번에 처리하는 것이 아니라, 어휘 분석, 구문 분석, 의미 검사, 코드 생성의 여러 단계로 나뉘어 동작한다는 점을 이해했습니다.
또한 심볼 테이블을 통해 식별자와 타입 정보를 관리하고, 조건문과 반복문을 라벨 기반의 저수준 코드로 변환하는 과정을 경험했습니다.

특히 오류가 발생했을 때 어느 단계에서 문제가 생겼는지 추적하면서, 복잡한 프로그램을 기능 단위로 분리하고 단계별로 검증하는 방식의 중요성을 배웠습니다.
