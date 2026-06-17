# Java GUI 기반 Life Game 구현

## Overview

Java Swing을 사용하여 Conway's Game of Life를 GUI 프로그램으로 구현한 프로젝트입니다.
사용자는 실행 시 보드의 셀 수를 입력할 수 있으며, 마우스 클릭과 드래그를 통해 셀의 생존/사망 상태를 직접 변경할 수 있습니다.

프로그램은 `Main`, `BoardModel`, `BoardView` 클래스로 역할을 분리하여 구성했습니다.
`BoardModel`은 셀 상태와 Life Game 규칙 계산을 담당하고, `BoardView`는 보드 화면 출력을 담당하며, `Main`은 GUI 생성과 버튼/마우스 이벤트 처리를 담당합니다.

---

## Tech Stack

* Java
* Java Swing
* GUI Programming
* MVC 구조
* Mouse Event
* Undo 기능
* 2차원 배열 / 3차원 배열

---

## Features

### 1. Life Game 보드 생성

프로그램 실행 시 사용자가 셀 수를 입력하면, 해당 크기의 Life Game 보드를 생성합니다.
10 이상의 정수만 입력 가능하도록 처리하여 잘못된 입력을 방지했습니다.

![Start Screen](start.png)

---

### 2. 마우스 입력을 통한 셀 상태 변경

마우스 클릭으로 특정 셀의 생존/사망 상태를 변경할 수 있습니다.
또한 마우스 드래그를 통해 여러 셀을 연속적으로 변경할 수 있도록 구현했습니다.

* 클릭: 선택한 셀의 상태 변경
* 드래그: 여러 셀의 상태 연속 변경
* 이전 좌표를 저장하여 같은 셀이 중복 변경되는 문제 방지

---

### 3. 다음 세대 계산

`next` 버튼을 누르면 현재 보드 상태를 기준으로 다음 세대를 계산합니다.
각 셀 주변의 살아있는 이웃 수를 확인하고, Life Game 규칙에 따라 다음 상태를 결정합니다.

* 죽은 셀 주변에 살아있는 셀이 3개 있으면 생존
* 살아있는 셀 주변에 살아있는 셀이 2개 또는 3개 있으면 생존 유지
* 그 외의 경우 사망

![Moving Screen](moving.png)

---

### 4. Undo 기능

이전 보드 상태를 저장하여 `undo` 버튼으로 이전 상태를 복원할 수 있도록 구현했습니다.
3차원 배열을 사용하여 과거 보드 상태를 저장하고, 최대 32회까지 되돌릴 수 있도록 관리했습니다.

![Undo Screen](undo.png)

---

### 5. New Game 기능

`New Game` 버튼을 누르면 새로운 Life Game 창을 생성할 수 있습니다.
각 창은 독립적인 `BoardModel`을 사용하므로 서로 다른 보드 상태를 따로 관리할 수 있습니다.

![Multi Window 1](multi.png)

![Multi Window 2](multi2.png)

---

## Project Structure

```text
lifegame/
├─ README.md
├─ src/
│  └─ lifegame/
│     ├─ Main.java
│     ├─ BoardModel.java
│     ├─ BoardView.java
│     └─ BoardListener.java
├─ start.png
├─ moving.png
├─ undo.png
├─ multi.png
└─ multi2.png
```

---

## Class Structure

### Main.java

GUI 실행을 담당하는 클래스입니다.
`JFrame`, `JButton`, `JOptionPane`을 사용하여 화면을 구성하고, 버튼 이벤트와 마우스 이벤트를 처리합니다.

주요 기능:

* 프로그램 실행
* 셀 수 입력 처리
* Life Game 보드 화면 생성
* `New Game`, `undo`, `next` 버튼 생성
* 마우스 클릭/드래그 이벤트 처리

### BoardModel.java

Life Game의 핵심 로직을 담당하는 클래스입니다.
현재 셀 상태, 다음 세대 상태, 이전 상태 기록을 관리합니다.

주요 기능:

* 셀 상태 저장
* 셀 상태 변경
* 다음 세대 계산
* Undo를 위한 history 저장
* 이전 상태 복원

### BoardView.java

Life Game 보드를 화면에 그리는 클래스입니다.
`JPanel`을 상속하여 격자선을 그리고, 살아있는 셀을 검은색 사각형으로 표시합니다.

주요 기능:

* 보드 격자 출력
* 살아있는 셀 표시
* `BoardModel`의 상태를 화면에 반영

### BoardListener.java

`BoardModel`의 업데이트를 알리기 위한 인터페이스입니다.
Model의 상태 변경을 View나 다른 객체에 전달할 수 있도록 하는 구조입니다.

---

## Build & Run

```bash
javac -d out src/lifegame/*.java
java -cp out lifegame.Main
```

---

## What I Learned

이 프로젝트를 통해 Java Swing을 활용한 GUI 프로그램의 기본 구조를 학습했습니다.
단순히 알고리즘만 구현하는 것이 아니라, 사용자가 직접 셀을 클릭하고 드래그하면서 상태를 변경할 수 있도록 이벤트 처리를 구현했습니다.

또한 `BoardModel`, `BoardView`, `Main`으로 역할을 분리하면서, 화면 출력과 데이터 관리, 사용자 입력 처리를 나누는 구조의 중요성을 이해했습니다.
Undo 기능을 구현하면서 과거 상태를 저장하는 방식과 배열을 활용한 상태 관리 방법도 경험했습니다.

