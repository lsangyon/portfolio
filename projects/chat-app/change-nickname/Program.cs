using System.Collections.Concurrent;
using System.Net;
using System.Net.Sockets;
//포트명 생성
const int DefaultPort = 10140;
const string FamilyCode = "675246";
//실행 인자로 포트 번호가 들어왔고 숫자로 변환 가능하면 그걸 쓰고,아니면 기본 포트 10140을 쓴다.
int port = args.Length >= 1 && int.TryParse(args[0], out int parsedPort) ? parsedPort : DefaultPort;
//IPAddress.Any : 모든 address에게서 다 받겠다.
var listener = new TcpListener(IPAddress.Any, port);
//클라이언트 정보를 저장할 것, 닉네임, 접속시간 등등
var clients = new ConcurrentDictionary<string, ClientSession>(StringComparer.OrdinalIgnoreCase);
var clientLock = new object();

//서버시작 서버가 클라의 접속 준비완료
listener.Start();
Console.WriteLine($"Chat server started. Port: {port}");
Console.WriteLine("Use the server PC's LAN IP from another device on the same Wi-Fi.");

while (true)
{
    // 클라이언트가 접속할 때까지 기다린다.
    // 접속 요청이 오면 accept하고, 해당 클라이언트와 통신할 TcpClient 객체를 반환한다.
    TcpClient tcpClient = await listener.AcceptTcpClientAsync();
    // 접속한 클라이언트를 별도 작업으로 처리한다.
    // 이렇게 해야 한 클라이언트 처리 중에도 서버가 다음 클라이언트 접속을 계속 받을 수 있다.
    _ = Task.Run(() => HandleClientAsync(tcpClient));
}

// accept된 클라이언트와의 연결을 담당하는 비동기 처리 함수
async Task HandleClientAsync(TcpClient tcpClient)
{
    //기본 이름 null, 
    string? currentName = null;
    // 접속한 클라이언트의 IP:포트 정보를 로그용으로 저장
    var endpoint = tcpClient.Client.RemoteEndPoint?.ToString() ?? "unknown";

    try
    {
        // tcpClient는 이미 accept된 클라이언트 연결이다.
        // 여기서는 해당 연결에서 NetworkStream을 얻고,
        // 문자열 단위로 읽고 쓰기 위한 Reader/Writer를 준비한다.
        using var client = tcpClient;
        using var stream = client.GetStream();
        using var reader = new StreamReader(stream);
        using var writer = new StreamWriter(stream) { AutoFlush = true };

        await writer.WriteLineAsync("REQUEST ACCEPTED");

        // 클라이언트가 보낸 가족 코드를 읽고 검증
        string? requestedFamilyCode = await reader.ReadLineAsync();

        if (requestedFamilyCode?.Trim() != FamilyCode)
        {
            await writer.WriteLineAsync("INVALID FAMILY CODE");
            Console.WriteLine($"Rejected invalid family code from {endpoint}");
            return;
        }

        await writer.WriteLineAsync("FAMILY CODE ACCEPTED");

        // 가족 코드가 통과된 뒤 닉네임을 읽음
        string? requestedName = await reader.ReadLineAsync();
        //이름이 규정에 위반되는 경우, 거부
        if (!IsValidNickname(requestedName))
        {
            await writer.WriteLineAsync("INVALID USERNAME");
            return;
        }

        // 검증을 통과한 닉네임과 통신 도구를 묶어 클라이언트 세션 객체를 생성
        var session = new ClientSession(client, reader, writer, requestedName!);

        lock (clientLock)
        {
            //이미 존재하는 이름인 경우 거부 
            if (clients.ContainsKey(session.Name))
            {
                writer.WriteLine("USERNAME ALREADY EXISTS");
                return;
            }

            clients[session.Name] = session;
            currentName = session.Name;
        }

        // 서버 콘솔에 접속 로그 출력
        Console.WriteLine($"Connected: {currentName} from {endpoint}");
        // 현재 클라이언트에게 닉네임 등록 성공 응답 전송
        await writer.WriteLineAsync("USERNAME REGISTERED");
        // 전체 클라이언트에게 새 유저 입장 메시지 전송
        await BroadcastAsync($"[SYSTEM] {currentName} joined the chat");
        // 입장시, 현재 채팅방에 누가 있는지 목록 확인
        await BroadcastUserListAsync();
        string? line;
        
        while ((line = await reader.ReadLineAsync()) != null)
        {
            if (line.Length == 0)
            {
                continue;
            }
            //특수 명령어 : 퇴장
            if (line.Equals("/quit", StringComparison.OrdinalIgnoreCase))
            {
                break;
            }
            //특수명령어 : 닉네임 변경
            if (line.StartsWith("/nick ", StringComparison.OrdinalIgnoreCase))
            {
                string newName = line[6..].Trim();
                await ChangeNicknameAsync(session, newName);
                currentName = session.Name;
                continue;
            }

            // 일반대화 시간 출력기능
            string timestamp = DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss");
            await BroadcastAsync($"[{timestamp}] {session.Name}: {line}");
        }
    }
    
    catch (IOException)
    {
        // 클라이언트가 예기치 않게 연결을 끊은 경우
    }
    
    catch (SocketException ex)
    {
        // 소켓 통신 중 네트워크 오류가 발생한 경우 서버 콘솔에 기록
        Console.WriteLine($"Socket error from {endpoint}: {ex.Message}");
    }
    
    catch (Exception ex)
    {
        // 예상하지 못한 예외가 발생해도 서버 전체가 종료되지 않도록 기록
        Console.WriteLine($"Unexpected error from {endpoint}: {ex.Message}");
    }
    finally
    {
        //정식으로 등록이 끝난 클라이언트만 퇴장처리하고 알림 전송
        if (currentName != null)
        {
            RemoveClient(currentName);
            Console.WriteLine($"Disconnected: {currentName}");
            await BroadcastAsync($"[SYSTEM] {currentName} left the chat");
            // 정식등록된, 사람이 퇴장시, 현재 채팅방에 누가 있는지 목록 확인
            await BroadcastUserListAsync();
        }
    }
    
}
//낙내암 변경 명령어코드
async Task ChangeNicknameAsync(ClientSession session, string newName)
{
    //규정에 어긋나는 이름인 경우 거절처리
    if (!IsValidNickname(newName))
    {
        await session.SendAsync("[SYSTEM] Invalid nickname. Use 1-20 characters without spaces.");
        return;
    }

    string oldName;
    //규정에 어긋나지 않은 경우
    lock (clientLock)
    {
        oldName = session.Name;
        // 기존이름이랑 똑같은 이름을 설정했을때
        if (oldName.Equals(newName, StringComparison.OrdinalIgnoreCase))
        {
            session.Writer.WriteLine("[SYSTEM] You are already using that nickname.");
            return;
        }
        // 다른 유저가 사용하는 이름을 사용했을때
        if (clients.ContainsKey(newName))
        {
            session.Writer.WriteLine("[SYSTEM] Nickname already exists.");
            return;
        }
        // 통과된 경우
        clients.TryRemove(oldName, out _);
        session.Name = newName;
        clients[newName] = session;
    }
    //이름 변경 공지
    Console.WriteLine($"Nickname changed: {oldName} -> {newName}");
    await BroadcastAsync($"[SYSTEM] {oldName} changed nickname to {newName}");
    // 닉네임 변경 후 현재 접속자 목록을 전체 클라이언트에게 전송
    await BroadcastUserListAsync();
}

// 접속자 목록에서 지정한 닉네임의 클라이언트 세션을 제거
void RemoveClient(string name)
{
    lock (clientLock)
    {
        clients.TryRemove(name, out _);
    }
}

async Task BroadcastUserListAsync()
{
    string userList = string.Join(",", clients.Keys.OrderBy(name => name));
    await BroadcastAsync($"[USERS] {userList}");
}

// 현재 접속 중인 모든 클라이언트에게 메시지를 전송
async Task BroadcastAsync(string message)
{
    //clients.Values.ToArray()) : clients.Values는 현재 접속 중인 클라이언트 세션들의 모음이고,ToArray()는 그 순간의 목록을 복사해서 순회하겠다는 뜻
    //왜 복사하냐면, 브로드캐스트 도중에 누가 접속 종료하거나 닉네임을 바꾸면 clients 목록이 바뀔 수 있음
    //그 상태에서 바로 순회하면 꼬일 수 있으니까, 일단 배열로 복사한 전송
    foreach (ClientSession client in clients.Values.ToArray())
    {
        await client.SendAsync(message);
    }
}

// 닉네임 규칙 확인 코드(스페이스 유무, 닉네임의 스펠링 수가, 20이 넘는지 확인)
static bool IsValidNickname(string? name)
{
    if (string.IsNullOrWhiteSpace(name))
    {
        return false;
    }

    if (name.Length > 20)
    {
        return false;
    }

    return !name.Any(char.IsWhiteSpace);
}

// 클라이언트 세션 정보를 저장하는 클래스
sealed class ClientSession
{
    //TCP 통신 객체(이 유저와 이어진 연결 자체)
    public TcpClient TcpClient { get; }
    //클라이언트가 보낸 메시지를 읽는 도구
    public StreamReader Reader { get; }
    // 클라이언트에게 메시지를 보내는 도구
    public StreamWriter Writer { get; }
    // 클라이언트의 이름 저장하는 도구
    public string Name { get; set; }

    // 클라이언트 세션을 생성할 때 연결 객체, 입출력 도구, 닉네임을 저장
    public ClientSession(TcpClient tcpClient, StreamReader reader, StreamWriter writer, string name)
    {
        TcpClient = tcpClient;
        Reader = reader;
        Writer = writer;
        Name = name;
    }

     // 이 클라이언트에게 메시지를 전송
    public async Task SendAsync(string message)
    {
        try
        {
            await Writer.WriteLineAsync(message);
        }
        catch
        {
            // 전송 실패는 일단 무시한다.
            // 연결이 끊긴 클라이언트는 수신 루프 종료 후 finally에서 제거된다.
        }
    }
}
