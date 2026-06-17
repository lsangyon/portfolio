#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <unistd.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#define MAXCLIENTS 5

void client_exit();

int main(int argc,char **argv)
{
  int sock;
  struct sockaddr_in svr;
  struct sockaddr_in clt;
  struct hostent *cp;
  char rbuf[1024];
  int nbytes;
  int reuse;
  fd_set rfds;
  struct timeval tv;


  if(argc != 3){
    printf("Wrong input. Chechk your input\n");
    exit(1);
  }

  tv.tv_sec = 2;
  tv.tv_usec = 0;
  
  if ((sock=socket(AF_INET,SOCK_STREAM,IPPROTO_TCP))<0) {
    perror("socket");
    exit(1);
  }

  reuse=1;
  if(setsockopt(sock,SOL_SOCKET,SO_REUSEADDR,&reuse,sizeof(reuse))<0) {
    perror("setsockopt");
    exit(1);
  }

  cp = gethostbyname(argv[1]);
  
  bzero(&svr,sizeof(svr));
  svr.sin_family=AF_INET;
  svr.sin_addr=*(struct in_addr*)cp->h_addr_list[0];
  svr.sin_port=htons(10140);


  if(connect(sock,(struct sockaddr *)&svr,sizeof(struct sockaddr_in))== -1){
    perror("connection failed");
    exit(1);
  }else{
    printf("connection success\n");
  }

  if((nbytes = read(sock,rbuf,17)) < 0) {
    perror("read from server");
    exit(1);
  }else{
    rbuf[nbytes] = '\0';
    if(strcmp(rbuf,"REQUEST ACCEPTED\n") != 0){
      printf("failed to be accepted\n");
      client_exit();
    }else{
      printf("REQUEST OK!\n");
    }
  }


  if((nbytes=write(sock,argv[2],strlen(argv[2])+1)) < 0) {
    perror("write to server");
    exit(1);
  }

  if((nbytes = read(sock,rbuf,20)) < 0) {
    perror("read from server");
    exit(1);
  }else{
    rbuf[nbytes] = '\0';
    if(strcmp(rbuf,"USERNAME REGISTERED\n") != 0){
      printf("Register username failed\n");
      client_exit();
    }else{
      printf("REGISTER OK!\n");
    }
  }
  
	printf("-------------------------------------\n Welcome talking server. \n-------------------------------------\n");

  do{
    FD_ZERO(&rfds);
    FD_SET(0,&rfds);
    FD_SET(sock,&rfds);
    
    if(select(sock+1,&rfds,NULL,NULL,&tv)>0) {
      if(FD_ISSET(0,&rfds)) {
        if((nbytes = read(0,rbuf,sizeof(rbuf)) ) < 0) {
          perror("reading");
        }else{
          write(sock,rbuf,nbytes);
        }	  
      }
      if(FD_ISSET(sock,&rfds)){
        if((nbytes = read(sock,rbuf,sizeof(rbuf)) ) < 0) {
	  perror("reading");
	}else{
	  write(0,rbuf,nbytes);
	}
      }
    }
  }while(nbytes != 0);

  close(sock);
  printf("closed\n");
  exit(0);
}

void client_exit(){
  printf("client exit\n");
  exit(1);
}
