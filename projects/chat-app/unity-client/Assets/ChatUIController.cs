using System;
using System.Collections;
using System.IO;
using System.Net.Sockets;
using TMPro;
using UnityEngine;
using UnityEngine.UI;

public class ChatUIController : MonoBehaviour
{
    // 로그인 화면 입력 요소
    private TMP_InputField addressInput;
    private TMP_InputField portInput;
    private TMP_InputField nameInput;
    private TMP_InputField familyCodeInput;
    private Button enterButton;

    // 채팅 화면 요소
    private GameObject loginPanel;
    private GameObject chatPanel;
    private TMP_Text chatLog;
    private TMP_InputField messageInput;
    private Button sendButton;

    // 채팅 로그 스크롤 관련 요소
    private ScrollRect chatScrollRect;
    private RectTransform chatContentRect;
    private RectTransform chatLogRect;

    // TCP 통신 객체
    private TcpClient client;
    private StreamReader reader;
    private StreamWriter writer;

    // 접속 상태 관리
    private bool isConnecting = false;
    private bool isConnected = false;

    void Start()
    {
        // 로그인 UI 연결
        addressInput = GetRequiredComponent<TMP_InputField>("InputAddress");
        portInput = GetRequiredComponent<TMP_InputField>("InputPort");
        nameInput = GetRequiredComponent<TMP_InputField>("InputName");
        familyCodeInput = GetRequiredComponent<TMP_InputField>("InputFamilyCode");
        enterButton = GetRequiredComponent<Button>("EnterButton");

        // 패널 및 채팅 UI 연결
        loginPanel = GetRequiredObject("LogInPanel");
        chatPanel = GetRequiredObject("ChatPanel");
        chatLog = GetRequiredComponent<TMP_Text>("ChatLog");
        messageInput = GetRequiredComponent<TMP_InputField>("MessageInput");
        sendButton = GetRequiredComponent<Button>("SendButton");

        // Scroll View 연결
        chatScrollRect = GetRequiredComponent<ScrollRect>("Scroll View");

        if (chatScrollRect.content == null)
        {
            throw new Exception("Scroll View Content is not assigned");
        }

        chatContentRect = chatScrollRect.content;
        chatLogRect = chatLog.GetComponent<RectTransform>();

        SetupChatScrollArea();

        // MessageInput 글자 표시 설정
        if (messageInput.textComponent != null)
        {
            messageInput.textComponent.color = Color.black;
            messageInput.textComponent.fontSize = 24;
            messageInput.textComponent.alignment = TextAlignmentOptions.MidlineLeft;
        }

        // 버튼 이벤트 연결
        enterButton.onClick.RemoveAllListeners();
        enterButton.onClick.AddListener(OnEnterButtonClicked);

        sendButton.onClick.RemoveAllListeners();
        sendButton.onClick.AddListener(OnSendButtonClicked);

        // 처음에는 로그인 화면만 표시
        loginPanel.SetActive(true);
        chatPanel.SetActive(false);

        Debug.Log("ChatUIController initialized");
    }

    // 채팅 로그 스크롤 영역 기본 설정
    private void SetupChatScrollArea()
    {
        chatScrollRect.horizontal = false;
        chatScrollRect.vertical = true;
        chatScrollRect.movementType = ScrollRect.MovementType.Clamped;
        chatScrollRect.inertia = false;

        // Content는 스크롤될 전체 영역
        chatContentRect.anchorMin = new Vector2(0f, 1f);
        chatContentRect.anchorMax = new Vector2(1f, 1f);
        chatContentRect.pivot = new Vector2(0.5f, 1f);
        chatContentRect.anchoredPosition = Vector2.zero;

        // ChatLog는 실제 글자를 표시하는 텍스트 영역
        chatLogRect.anchorMin = new Vector2(0f, 1f);
        chatLogRect.anchorMax = new Vector2(1f, 1f);
        chatLogRect.pivot = new Vector2(0.5f, 1f);
        chatLogRect.anchoredPosition = Vector2.zero;

        chatLog.alignment = TextAlignmentOptions.TopLeft;
        chatLog.overflowMode = TextOverflowModes.Overflow;
    }

    // 입장 버튼 클릭 시 서버 접속 시도
    private async void OnEnterButtonClicked()
    {
        if (isConnecting || isConnected)
        {
            Debug.Log("Already connecting or connected");
            return;
        }

        isConnecting = true;
        enterButton.interactable = false;

        string address = addressInput.text.Trim();
        string portText = portInput.text.Trim();
        string userName = nameInput.text.Trim();
        string familyCode = familyCodeInput.text.Trim();

        if (!int.TryParse(portText, out int port))
        {
            Debug.LogError("Port must be a number");
            isConnecting = false;
            enterButton.interactable = true;
            return;
        }

        try
        {
            Debug.Log("Connecting to server...");

            CloseConnection();

            client = new TcpClient();
            await client.ConnectAsync(address, port);

            NetworkStream stream = client.GetStream();
            reader = new StreamReader(stream);
            writer = new StreamWriter(stream) { AutoFlush = true };

            Debug.Log("Connected to server");

            string acceptMessage = await reader.ReadLineAsync();
            Debug.Log("Server: " + acceptMessage);

            await writer.WriteLineAsync(familyCode);

            string familyResult = await reader.ReadLineAsync();
            Debug.Log("Server: " + familyResult);

            if (familyResult != "FAMILY CODE ACCEPTED")
            {
                Debug.LogError("Family code rejected");
                isConnecting = false;
                enterButton.interactable = true;
                CloseConnection();
                return;
            }

            await writer.WriteLineAsync(userName);

            string nameResult = await reader.ReadLineAsync();
            Debug.Log("Server: " + nameResult);

            if (nameResult == "USERNAME REGISTERED")
            {
                isConnected = true;
                isConnecting = false;

                ShowChatPanel(userName);
                StartReceiveLoop();
            }
            else
            {
                Debug.LogError("Login failed: " + nameResult);
                isConnecting = false;
                enterButton.interactable = true;
                CloseConnection();
            }
        }
        catch (Exception e)
        {
            Debug.LogError("Connection failed: " + e.Message);
            isConnecting = false;
            enterButton.interactable = true;
            CloseConnection();
        }
    }

    // 로그인 성공 후 채팅 화면으로 전환
    private void ShowChatPanel(string userName)
    {
        Debug.Log("ShowChatPanel start");

        loginPanel.SetActive(false);
        chatPanel.SetActive(true);

        chatLog.text = "";
        AppendChatLog("Connected as " + userName);

        Debug.Log("ChatPanel opened");
    }

    // Send 버튼 클릭 시 메시지 전송
    private async void OnSendButtonClicked()
    {
        if (!isConnected || writer == null)
        {
            Debug.LogError("Not connected to server");
            return;
        }

        string message = messageInput.text.Trim();

        if (message.Length == 0)
        {
            return;
        }

        await writer.WriteLineAsync(message);

        messageInput.text = "";
        messageInput.ActivateInputField();
    }

    // 서버에서 오는 메시지를 계속 수신
    private async void StartReceiveLoop()
    {
        try
        {
            while (isConnected && reader != null)
            {
                string line = await reader.ReadLineAsync();

                if (line == null)
                {
                    break;
                }

                AppendChatLog(line);
            }
        }
        catch (Exception e)
        {
            Debug.LogError("Receive failed: " + e.Message);
        }

        CloseConnection();
    }

    // 채팅 로그에 메시지를 추가하고 스크롤을 아래로 이동
    private void AppendChatLog(string message)
    {
        if (string.IsNullOrEmpty(chatLog.text))
        {
            chatLog.text = message;
        }
        else
        {
            chatLog.text += "\n" + message;
        }

        Canvas.ForceUpdateCanvases();

        float viewportHeight = chatScrollRect.viewport.rect.height;
        float preferredHeight = chatLog.preferredHeight + 30f;
        float contentHeight = Mathf.Max(preferredHeight, viewportHeight);

        chatLogRect.SetSizeWithCurrentAnchors(RectTransform.Axis.Vertical, contentHeight);
        chatContentRect.SetSizeWithCurrentAnchors(RectTransform.Axis.Vertical, contentHeight);

        LayoutRebuilder.ForceRebuildLayoutImmediate(chatContentRect);
        Canvas.ForceUpdateCanvases();

        StartCoroutine(ScrollToBottomNextFrame());
    }

    // Unity UI 갱신이 끝난 다음 프레임에 스크롤을 맨 아래로 내린다.
    private IEnumerator ScrollToBottomNextFrame()
    {
        yield return null;

        Canvas.ForceUpdateCanvases();

        chatScrollRect.verticalNormalizedPosition = 0f;
        chatScrollRect.velocity = Vector2.zero;
    }

    // 이름으로 필수 GameObject 찾기
    private GameObject GetRequiredObject(string objectName)
    {
        GameObject obj = FindSceneObjectIncludingInactive(objectName);

        if (obj == null)
        {
            throw new Exception("Object not found: " + objectName);
        }

        return obj;
    }

    // 이름으로 GameObject를 찾고 필요한 컴포넌트 가져오기
    private T GetRequiredComponent<T>(string objectName) where T : Component
    {
        GameObject obj = GetRequiredObject(objectName);
        T component = obj.GetComponent<T>();

        if (component == null)
        {
            throw new Exception("Component not found on " + objectName + ": " + typeof(T).Name);
        }

        return component;
    }

    // 비활성화된 오브젝트까지 포함해서 Scene 안의 GameObject 찾기
    private GameObject FindSceneObjectIncludingInactive(string objectName)
    {
        GameObject[] allObjects = Resources.FindObjectsOfTypeAll<GameObject>();

        foreach (GameObject obj in allObjects)
        {
            if (obj.name == objectName && obj.scene.IsValid())
            {
                return obj;
            }
        }

        return null;
    }

    // 앱 종료 시 서버에 퇴장 명령 전송 후 연결 정리
    private void OnApplicationQuit()
    {
        try
        {
            if (writer != null && isConnected)
            {
                writer.WriteLine("/quit");
            }
        }
        catch
        {
        }

        CloseConnection();
    }

    // TCP 연결 객체 정리
    private void CloseConnection()
    {
        try
        {
            writer?.Close();
            reader?.Close();
            client?.Close();
        }
        catch
        {
        }

        writer = null;
        reader = null;
        client = null;

        isConnected = false;
        isConnecting = false;
    }
}