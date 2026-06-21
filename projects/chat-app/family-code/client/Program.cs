using System.Net.Sockets;

const int DefaultPort = 10140;

// 서버 주소 입력
Console.Write("Server IP or hostname: ");
string? hostInput = Console.ReadLine();

if (string.IsNullOrWhiteSpace(hostInput))
{
    Console.WriteLine("Server IP is required.");
    return;
}

string host = hostInput.Trim();

// 포트 입력. 비워두면 기본 포트 사용
Console.Write($"Port [{DefaultPort}]: ");
string? portInput = Console.ReadLine();

int port = string.IsNullOrWhiteSpace(portInput)
    ? DefaultPort
    : int.TryParse(portInput, out int parsedPort)
        ? parsedPort
        : DefaultPort;

// 가족 코드 입력
Console.Write("Family Code: ");
string? familyCodeInput = Console.ReadLine();

if (string.IsNullOrWhiteSpace(familyCodeInput))
{
    Console.WriteLine("Family code is required.");
    return;
}

string familyCode = familyCodeInput.Trim();

// 닉네임 입력
Console.Write("Nickname: ");
string? nicknameInput = Console.ReadLine();

if (string.IsNullOrWhiteSpace(nicknameInput))
{
    Console.WriteLine("Nickname is required.");
    return;
}

string nickname = nicknameInput.Trim();

// 서버와 TCP 연결을 맺기 위해 사용할 클라이언트 소켓 객체 생성
using var client = new TcpClient();

// 접속과정
try
{
    // 지정한 서버 주소와 포트로 TCP 연결 시도
    await client.ConnectAsync(host, port);
}
catch (Exception ex)
{
    // 에러 발견시 실패 공지
    Console.WriteLine($"Connection failed: {ex.Message}");
    return;
}

/// 서버와 데이터를 주고받기 위한 네트워크 스트림 생성
using NetworkStream stream = client.GetStream();
// 서버가 보낸 메시지를 읽기 위한 도구
using var reader = new StreamReader(stream);
// 서버에게 메시지를 보내기 위한 도구
using var writer = new StreamWriter(stream) { AutoFlush = true };

// 서버가 접속을 정상적으로 받아들였는지 확인
string? acceptMessage = await reader.ReadLineAsync();
if (acceptMessage != "REQUEST ACCEPTED")
{
    // 서버가 예상한 수락 메시지를 보내지 않았으면 종료
    Console.WriteLine("Failed to be accepted by server.");
    return;
}

// 서버에게 가족 코드를 먼저 전송
await writer.WriteLineAsync(familyCode);

// 서버의 가족 코드 검증 결과 확인
string? familyCodeMessage = await reader.ReadLineAsync();

if (familyCodeMessage != "FAMILY CODE ACCEPTED")
{
    Console.WriteLine(familyCodeMessage ?? "Family code authentication failed.");
    return;
}

// 가족 코드가 통과된 뒤 서버에게 사용할 닉네임을 전송
await writer.WriteLineAsync(nickname);

// 서버의 닉네임 등록 결과를 확인
string? registerMessage = await reader.ReadLineAsync();

if (registerMessage != "USERNAME REGISTERED")
{
    // 이름이 등록되지 않았을경우, 1~20범위 밖, 이미 존재하는 이름일경우
    Console.WriteLine(registerMessage ?? "Register username failed.");
    return;
}

// 환영 메시지
Console.WriteLine("-------------------------------------");
Console.WriteLine("Welcome to the chat server.");
Console.WriteLine("Commands: /nick newName, /quit");
Console.WriteLine("-------------------------------------");

// 서버 메시지 수신 작업을 나중에 중단시키기 위한 취소 신호 객체
using var cancellation = new CancellationTokenSource();
// 서버가 보내는 메시지를 계속 수신하기 위한 작업
Task receiveTask = Task.Run(async () =>
{
    try
    {
        while (!cancellation.Token.IsCancellationRequested)
        {
            // 서버가 보낸 메시지를 한 줄 읽음
            string? message = await reader.ReadLineAsync();
            // 서버와의 연결이 종료되면 수신 루프 종료
            if (message == null)
            {
                Console.WriteLine("Disconnected from server.");
                break;
            }
            // 받은 메시지를 클라이언트 콘솔에 출력
            Console.WriteLine(message);
        }
    }
    catch (IOException)
    {
        // 서버 종료 또는 네트워크 문제로 수신 중 오류가 발생한 경우
        Console.WriteLine("Connection closed.");
    }
    catch (ObjectDisposedException)
    {
        // 클라이언트 종료 과정에서 스트림이 닫힌 경우
    }
});

// 서버에게 메시지를 송신하는 작업
while (true)
{
    // input을 읽고, 전송
    string? input = Console.ReadLine();
    if (input == null)
    {
        break;
    }

    // 서버에게 전송
    await writer.WriteLineAsync(input);
    
    //종료코드가 입력되면 종료
    if (input.Equals("/quit", StringComparison.OrdinalIgnoreCase))
    {
        break;
    }
}

cancellation.Cancel();
client.Close();
await receiveTask;
