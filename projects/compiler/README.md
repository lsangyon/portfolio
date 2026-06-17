# Pascal-like Compiler

Java로 구현한 Pascal 계열 언어 컴파일러 프로젝트입니다.
입력으로 Pascal 계열 프로그램을 토큰화한 `.ts` 파일을 받아 문법 분석, 의미 분석, 코드 생성을 수행하고, 최종적으로 CASL II 형식의 `.cas` 파일을 출력합니다.

이 프로젝트는 기존 문법·의미 검사 프로그램을 기반으로 컴파일 기능을 확장한 전공 프로젝트입니다. 변수와 배열 처리, 식 계산, 조건문과 반복문, 프로시저 호출, 스코프 관리, CASL II 코드 생성 기능을 구현하며 컴파일러의 전체 동작 흐름을 학습했습니다.

## Tech Stack

* Java
* Gradle
* CASL II
* Pascal-like Language

## Project Goals

* Pascal 계열 문법으로 작성된 프로그램을 분석합니다.
* 문법 오류와 의미 오류를 검출합니다.
* 정상적인 입력에 대해 CASL II 코드를 생성합니다.
* 변수, 배열, 조건문, 반복문, 프로시저 호출 등 주요 언어 기능을 처리합니다.

## Main Features

### 1. Syntax / Semantic Analysis

입력된 토큰 정보를 바탕으로 프로그램 구조를 분석하고, 변수 선언, 배열, 조건문, 반복문, 프로시저 등 Pascal 계열 문법 요소를 처리했습니다.

* 프로그램 구조 분석
* 변수 선언 및 참조 검사
* 배열 선언 및 인덱스 처리
* 조건문, 반복문 구조 분석
* 프로시저 선언 및 호출 검사
* 스코프 기반 심볼 관리

### 2. CASL II Code Generation

문법 분석과 의미 분석 결과를 바탕으로 CASL II 코드를 생성했습니다.
기존 checker 구조에 코드 생성 기능을 추가하여, 입력 프로그램이 실제 CASL II 프로그램으로 변환되도록 구현했습니다.

* 기본 CASL II 시작 코드 생성
* 변수 및 배열 접근 코드 생성
* 산술식 및 비교식 처리
* if / while 문에 대한 분기 라벨 생성
* writeln 등 출력 처리
* 프로시저 호출 코드 생성

### 3. Variable and Array Handling

일반 변수와 배열 변수를 구분하여 메모리 주소 계산 및 값 참조 로직을 구현했습니다.
배열의 경우 인덱스 계산과 시작 주소 계산이 필요했기 때문에, 일반 변수 접근과 별도의 처리 흐름으로 구현했습니다.

* 일반 변수 참조 처리
* 배열 변수 참조 처리
* 배열 인덱스 계산
* 메모리 주소 기반 값 로드
* 대입문 좌변과 식 내부 변수 참조 구분

### 4. Procedure and Scope Management

프로시저 선언과 호출을 처리하기 위해 서브프로그램 라벨과 호출 코드를 생성했습니다.
또한 프로시저 내부와 외부의 변수를 구분하기 위해 스코프 관리 로직을 사용했습니다.

* 프로시저 선언 처리
* 프로시저 호출 코드 생성
* 서브프로그램 라벨 생성
* 스코프 진입 / 종료 관리
* 현재 스코프 기준 변수 참조 처리

### 5. Branch and Comparison Handling

if문과 while문을 CASL II의 라벨 기반 분기 구조로 변환했습니다.
또한 정수 비교와 Boolean 비교에서 필요한 CASL II 명령이 다른 경우가 있어, CPA와 CPL 사용 조건을 구분하여 처리했습니다.

* if문 분기 라벨 생성
* while문 LOOP / ENDLP 라벨 생성
* 정수 비교 처리
* Boolean 비교 처리
* CPA / CPL 명령 구분

## Implementation Highlights

이 프로젝트에서 특히 중점적으로 구현한 부분은 다음과 같습니다.

* 기존 checker 구조를 기반으로 CASL II 코드 생성 기능 확장
* 일반 변수와 배열 변수를 구분한 메모리 주소 계산
* 대입문 좌변과 식 내부 변수 참조 처리 분리
* 프로시저 선언과 호출을 위한 서브프로그램 라벨 관리
* if / while 문 처리를 위한 분기 라벨 생성
* 정수 비교와 Boolean 비교에 따른 CPA / CPL 명령 구분
* 오류 발생 시 입력 처리, 문법 분석, 코드 생성 단계를 나누어 원인 추적

## Project Structure

```text
compiler/
├── README.md
├── build.gradle
├── data/
│   ├── pas/      # Pascal-like input examples
│   ├── ts/       # tokenized input files
│   ├── cas/      # generated CASL II files
│   └── ans/      # expected output files
└── src/
    └── main/
        └── java/
            └── enshud/
                ├── Main.java
                ├── s1/lexer/
                ├── s2/parser/
                ├── s3/checker/
                └── s4/compiler/
```

## How to Run

### Run compiler

```bash
./gradlew run --args="compiler data/ts/normal01.ts out.cas"
```

### Run lexer

```bash
./gradlew run --args="lexer data/pas/normal01.pas out.ts"
```

### Run parser

```bash
./gradlew run --args="parser data/ts/normal01.ts"
```

### Run checker

```bash
./gradlew run --args="checker data/ts/normal01.ts"
```

명령어와 파일 경로는 실행 환경과 테스트 파일명에 맞게 조정할 수 있습니다.

## Example Flow

```text
Pascal-like source code
        ↓
Lexer
        ↓
Token file (.ts)
        ↓
Parser / Checker
        ↓
Compiler
        ↓
CASL II code (.cas)
```

## Example

이 컴파일러는 Pascal 계열 프로그램을 토큰화한 `.ts` 파일을 입력으로 받아 CASL II 코드로 변환합니다.

### Source Program

```pascal
program test;
var
  x: integer;
begin
  x := 1 + 2;
  writeln(x)
end.
```

### Compile Token File

```bash
./gradlew run --args="compiler data/ts/normal01.ts out.cas"
```

### Generated CASL II

```asm
CASL    START   BEGIN
BEGIN   LAD     GR6, 0
        LAD     GR7, LIBBUF
        ...
```

정상적으로 컴파일되면 `out.cas` 파일이 생성됩니다.
생성된 CASL II 코드는 변수 저장 영역, 산술 연산, 출력 처리, 라이브러리 호출 등을 포함합니다.


## What I Learned

이 프로젝트는 구현 범위가 넓고, 한 부분의 수정이 다른 처리 흐름에 영향을 주는 경우가 많아 가장 많은 시행착오를 겪은 전공 프로젝트였습니다.

특히 변수 참조, 배열 인덱스 계산, 조건 분기, 프로시저 호출처럼 서로 연결된 기능을 구현하면서 단순히 코드를 작성하는 것보다 전체 실행 흐름을 이해하고 단계별로 검증하는 과정이 중요하다는 점을 배웠습니다.

또한 오류가 발생했을 때 전체 코드를 막연히 수정하기보다, 입력 처리, 문법 분석, 의미 분석, 코드 생성 단계를 나누어 원인을 추적하는 방식의 중요성을 경험했습니다.

## Limitations and Future Improvements

* 코드 생성 로직과 문법 분석 로직이 강하게 연결된 부분이 있어, 향후에는 AST 기반 구조로 분리하면 유지보수성이 좋아질 수 있습니다.
* 일부 예외 상황은 조건 분기를 추가하는 방식으로 처리했기 때문에, 문법 확장 시 구조 개선이 필요합니다.
* 테스트 케이스별 실행 결과를 README에 추가하면 검증 과정을 더 명확하게 보여줄 수 있습니다.
* 변수, 배열, 프로시저 처리 로직을 더 작은 모듈로 분리하면 코드 가독성을 개선할 수 있습니다.

## Note

이 프로젝트는 전공 수업 과제로 진행한 컴파일러 제작 프로젝트입니다.
포트폴리오 공개를 위해 구현 내용과 학습 내용을 중심으로 정리했습니다.

