# Lee Sangyoon Portfolio

서버/시스템 프로그래밍과 네트워크 기초를 중심으로 학습해 온 개발자 지망생입니다.
C, Linux, TCP/IP Socket, Java, Python 기반의 전공 프로젝트를 수행했습니다.

## Main Projects

### 1. C 기반 미니 UNIX Shell
- C / Linux / Process / fork / execvp
- 외부 명령어 실행, cd, pushd, dirs, popd, history, alias, wildcard 구현

### 2. TCP/IP 멀티 클라이언트 채팅 서버
- C / Linux / TCP Socket / select
- 다중 클라이언트 접속, 닉네임 중복 검증, 브로드캐스트, 접속 종료 처리

  
C / Linux 환경에서 TCP/IP Socket 기반 멀티 클라이언트 채팅 서버를 구현했습니다.
서버는 select()와 fd_set을 사용해 서버 소켓과 각 클라이언트 소켓을 함께 감시하고,
신규 접속은 accept()로 처리하며, 접속 중인 클라이언트 정보는 연결 리스트로 관리했습니다.
클라이언트 최초 접속 시 닉네임을 등록하고 중복 여부를 검사했으며,
특정 클라이언트가 메시지를 보내면 서버 기준 타임스탬프와 발신자 이름을 붙여 모든 클라이언트에게 브로드캐스트했습니다.
# I/O Multiplexing 기반 멀티 클라이언트 채팅 서버

### 3. 온라인 아이스브레이크용 그룹 매칭 툴
- Python / C++ / Tkinter / pybind11
- Google Form 데이터 기반 그룹 배정, GUI, 그룹 간 점수 편차 완화

### 4. Java GUI Life Game
- Java / GUI / MVC 구조 / Undo
- 클릭/드래그 셀 조작, Next, Undo, New Game 구현

### 5. 텍스트 단어 빈도 분석기
- C / Linked List / Binary Search Tree / File I/O
- 단어 출현 횟수, 빈도 계산, 정렬, 검색 기능 구현

### 6. 컴파일러 프로그램 제작
- Java / Parser / Code Generation
- Pascal 계열 문법 처리, 구문 분석, 코드 생성 흐름 구현
