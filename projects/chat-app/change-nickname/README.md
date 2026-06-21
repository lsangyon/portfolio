# C# TCP Chat Server with Nickname Change

This is a C#/.NET port and upgrade of a C TCP chat server project.

## Features

- TCP server/client using `TcpListener` and `TcpClient`
- Async client handling with `async` / `await`
- Multiple client sessions
- Nickname registration
- Duplicate nickname check
- Chat broadcast
- Runtime nickname change with `/nick newName`
- Quit command with `/quit`
- Works on localhost or devices connected to the same LAN/Wi-Fi

## Requirements

- .NET SDK installed

## Run server

```bash
cd ChatServer
dotnet run -- 10140
```

If you omit the port, it uses `10140`.

```bash
dotnet run
```

## Run client on the same PC

```bash
cd ChatClient
dotnet run -- 127.0.0.1 alice 10140
```

Open another terminal:

```bash
cd ChatClient
dotnet run -- 127.0.0.1 bob 10140
```

## Run client from another device on the same Wi-Fi

Find the server PC's LAN IP address.

Windows:

```cmd
ipconfig
```

Linux/macOS:

```bash
ip addr
# or
ifconfig
```

Then run the client with that IP:

```bash
dotnet run -- 192.168.0.23 alice 10140
```

## Commands

Change nickname:

```text
/nick newName
```

Exit:

```text
/quit
```

## Portfolio note

This project shows the transition from a C `select()` based TCP server to a C# async TCP server.
The C version demonstrates socket programming fundamentals, while the C# version demonstrates session management and asynchronous server structure.
