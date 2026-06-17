#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <unistd.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#define MAXCLIENTS 5

int main(int argc, char** argv) {
    int sock, csock = 1;
    int psock;
    struct sockaddr_in svr;
    struct sockaddr_in clt;
    struct hostent* cp;
    int clen;
    int port = 10140;
    char rbuf[1024];
    int nbytes;
    int reuse;
    fd_set rfds;
    struct timeval tv;

    struct client {
        int socket;
        char name[1024];
        struct client* next;
    };
    struct client* top;
    struct client* now;
    struct client* prev;
    now = NULL;
    top = now;
    char name[1024];
    int flg = 0;

    int k;

    tv.tv_sec = 2;
    tv.tv_usec = 0;

    if ((sock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP)) < 0) {
        perror("socket");
        exit(1);
    }

    reuse = 1;
    if (setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &reuse, sizeof(reuse)) < 0) {
        perror("setsockopt");
        exit(1);
    }

    bzero(&svr, sizeof(svr));
    svr.sin_family = AF_INET;
    svr.sin_addr.s_addr = htonl(INADDR_ANY);
    svr.sin_port = htons(port);

    if (bind(sock, (struct sockaddr*)&svr, sizeof(svr)) < 0) {
        perror("bind");
        exit(1);
    }

    if (listen(sock, MAXCLIENTS) < 0) {
        perror("listen");
        exit(1);
    }

    k = 0;

    do {
        FD_ZERO(&rfds);
        FD_SET(sock, &rfds);

        psock = sock;
        now = top;
        while (now != NULL) {
            FD_SET(now->socket, &rfds);
            if (psock < now->socket) {
                psock = now->socket;
            }
            now = now->next;
        }

        now = top;

        if (select(psock + 1, &rfds, NULL, NULL, &tv) > 0) {
            if (FD_ISSET(sock, &rfds)) {

                clen = sizeof(clt);
                if ((csock = accept(sock, (struct sockaddr*)&clt, (socklen_t*)&clen)) < 0) {
                    perror("accept");
                    exit(2);
                }

                if (k >= MAXCLIENTS) {
                    printf("Too many clients\n");
                    close(csock);
                    continue;
                }

                cp = gethostbyaddr((char*)&clt.sin_addr, sizeof(struct in_addr), AF_INET);
                printf("[%s]\n", cp->h_name);
                printf("connected\n");

                if (write(csock, "REQUEST ACCEPTED\n", 17) < 0) {
                    perror("read from stdin");
                }

                if ((nbytes = read(csock, rbuf, sizeof(rbuf))) < 0) {
                    perror("receive username");
                    exit(1);
                }
                rbuf[nbytes - 1] = '\0';

                now = top;
                flg = 0;
                while (now != NULL) {
                    if (strcmp(rbuf, now->name) == 0) {
                        printf("this name is already existed.\n");
                        close(csock);
                        flg = 1;
                        break;
                    }
                    now = now->next;
                }
                if (flg != 0) {
                    continue;
                }
                if (write(csock, "USERNAME REGISTERED\n", 20) < 0) {
                    perror("write username");
                    exit(1);
                }

                if (top == NULL) {
                    top = malloc(sizeof(struct client));
                    now = top;
                } else {
                    now = top;
                    while (now != NULL) {
                        prev = now;
                        now = now->next;
                    }
                    prev->next = malloc(sizeof(struct client));
                    now = prev->next;
                }
                now->socket = csock;
                strcpy(now->name, rbuf);
                now->next = NULL;
                k++;
            }

            prev = NULL;
            now = top;
            while (now != NULL) {
                if (FD_ISSET(now->socket, &rfds)) {
                    nbytes = read(now->socket, rbuf, sizeof(rbuf));
                    if (nbytes < 0) {
                        perror("read from server");
                    } else if (nbytes == 0) {
                        strcpy(name, now->name);
                        printf("Client '%s' exited.\n", name);
                        close(now->socket);

                        if (prev == NULL) {
                            top = now->next;
                        } else {
                            prev->next = now->next;
                        }

                        free(now);
                        k--;

                        now = top;
                        while (now != NULL) {
                            write(now->socket, "LOGOUT: ", 8);
                            write(now->socket, name, strlen(name));
                            write(now->socket, "\n", 1);
                            now = now->next;
                        }
                        now = top;
                    } else {
                        strcpy(name, now->name);

                        // 현재 시간을 구해서 문자열로 저장
                        char time_str[50];
                        time_t now_time;
                        struct tm* timeinfo;
                        time(&now_time);
                        timeinfo = localtime(&now_time);
                        strftime(time_str, sizeof(time_str), "[%Y-%m-%d %H:%M:%S] ", timeinfo);

                        // 발언 시각과 발언 내용을 함께 보내기
                        now = top;
                        while (now != NULL) {
                            write(now->socket, time_str, strlen(time_str)); // 시각 먼저 보내기
                            write(now->socket, name, strlen(name)); // 발언자 이름 보내기
                            write(now->socket, ": ", 2); // 구분 문자열 보내기
                            write(now->socket, rbuf, nbytes); // 발언 내용 보내기
                            write(now->socket, "\n", 1); // 줄바꿈 보내기
                            now = now->next;
                        }
                        now = top;
                    }
                    break;
                }
                prev = now;
                now = now->next;
            }
            now = top;
        }
    } while (1);

    now = top;
    while (now != NULL) {
        close(now->socket);
        prev = now;
        now = now->next;
        free(prev);
    }
    printf("closed\n");
}
