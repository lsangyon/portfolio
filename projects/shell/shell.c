/*
 * Mini UNIX Shell
 *
 * Restored and cleaned for portfolio use from a university programming report.
 * Features:
 *  - external command execution with fork() / execvp()
 *  - foreground / background execution using &
 *  - cd, pushd, dirs, popd
 *  - history, !!, !prefix
 *  - wildcard expansion for *
 *  - prompt change
 *  - alias / unalias
 *  - rename command
 */

#define _POSIX_C_SOURCE 200809L

#include <ctype.h>
#include <dirent.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>

#define BUFLEN 1024
#define MAXARGNUM 256
#define MAXHISTORY 32

typedef struct DirNode {
    char *path;
    struct DirNode *next;
} DirNode;

typedef struct AliasNode {
    char *name;
    char *command;
    struct AliasNode *next;
} AliasNode;

static DirNode *dir_stack = NULL;
static AliasNode *aliases = NULL;
static char *history_list[MAXHISTORY];
static int history_count = 0;
static char *prompt = NULL;

static void free_args(char *args[]);
static int parse_command(char *buffer, char *args[]);
static int execute_external(char *args[], int background);
static void add_history(const char *cmd);
static void print_history(void);
static char *previous_history(void);
static char *find_history_by_prefix(const char *prefix);
static int builtin_cd(char *args[]);
static void builtin_pushd(void);
static void builtin_dirs(void);
static void builtin_popd(void);
static void builtin_alias(char *args[]);
static void builtin_unalias(char *args[]);
static const char *resolve_alias(const char *name);
static int expand_wildcards(char *args[], char *expanded[]);
static int run_command_line(char *line);
static void cleanup(void);

static char *xstrdup(const char *s) {
    char *copy = strdup(s);
    if (copy == NULL) {
        perror("strdup");
        exit(EXIT_FAILURE);
    }
    return copy;
}

static void add_history(const char *cmd) {
    if (cmd == NULL || *cmd == '\0') {
        return;
    }

    char temp[BUFLEN];
    snprintf(temp, sizeof(temp), "%s", cmd);
    temp[strcspn(temp, "\n")] = '\0';

    if (temp[0] == '\0') {
        return;
    }

    if (history_count < MAXHISTORY) {
        history_list[history_count++] = xstrdup(temp);
        return;
    }

    free(history_list[0]);
    for (int i = 1; i < MAXHISTORY; i++) {
        history_list[i - 1] = history_list[i];
    }
    history_list[MAXHISTORY - 1] = xstrdup(temp);
}

static void print_history(void) {
    for (int i = 0; i < history_count; i++) {
        printf("%d %s\n", i + 1, history_list[i]);
    }
}

static char *previous_history(void) {
    if (history_count == 0) {
        fprintf(stderr, "Error: Event not found\n");
        return NULL;
    }
    return xstrdup(history_list[history_count - 1]);
}

static char *find_history_by_prefix(const char *prefix) {
    if (prefix == NULL || *prefix == '\0') {
        fprintf(stderr, "Error: Event not found\n");
        return NULL;
    }

    size_t len = strlen(prefix);
    for (int i = history_count - 1; i >= 0; i--) {
        if (strncmp(history_list[i], prefix, len) == 0) {
            return xstrdup(history_list[i]);
        }
    }

    fprintf(stderr, "Error: Event not found: %s\n", prefix);
    return NULL;
}

static int parse_command(char *buffer, char *args[]) {
    int argc = 0;
    char *token = strtok(buffer, " \t\n");

    while (token != NULL && argc < MAXARGNUM - 1) {
        args[argc++] = token;
        token = strtok(NULL, " \t\n");
    }
    args[argc] = NULL;

    if (argc == 0) {
        return 0;
    }

    int background = 0;
    if (strcmp(args[argc - 1], "&") == 0) {
        background = 1;
        args[argc - 1] = NULL;
    }

    return background;
}

static int builtin_cd(char *args[]) {
    const char *target = args[1];
    if (target == NULL) {
        target = getenv("HOME");
    }
    if (target == NULL) {
        target = ".";
    }

    if (chdir(target) == -1) {
        perror(target);
        return -1;
    }
    return 0;
}

static void builtin_pushd(void) {
    char cwd[BUFLEN];
    if (getcwd(cwd, sizeof(cwd)) == NULL) {
        perror("getcwd");
        return;
    }

    DirNode *node = malloc(sizeof(*node));
    if (node == NULL) {
        perror("malloc");
        return;
    }

    node->path = xstrdup(cwd);
    node->next = dir_stack;
    dir_stack = node;
}

static void builtin_dirs(void) {
    if (dir_stack == NULL) {
        printf("Directory stack is empty.\n");
        return;
    }

    int i = 1;
    for (DirNode *cur = dir_stack; cur != NULL; cur = cur->next) {
        printf("[%d] %s\n", i++, cur->path);
    }
}

static void builtin_popd(void) {
    if (dir_stack == NULL) {
        printf("Directory stack is empty.\n");
        return;
    }

    DirNode *node = dir_stack;
    dir_stack = node->next;

    if (chdir(node->path) == -1) {
        perror(node->path);
    }

    free(node->path);
    free(node);
}

static void builtin_alias(char *args[]) {
    if (args[1] == NULL) {
        for (AliasNode *cur = aliases; cur != NULL; cur = cur->next) {
            printf("alias %s='%s'\n", cur->name, cur->command);
        }
        return;
    }

    if (args[2] == NULL) {
        fprintf(stderr, "usage: alias name command\n");
        return;
    }

    for (AliasNode *cur = aliases; cur != NULL; cur = cur->next) {
        if (strcmp(cur->name, args[1]) == 0) {
            free(cur->command);
            cur->command = xstrdup(args[2]);
            return;
        }
    }

    AliasNode *node = malloc(sizeof(*node));
    if (node == NULL) {
        perror("malloc");
        return;
    }

    node->name = xstrdup(args[1]);
    node->command = xstrdup(args[2]);
    node->next = aliases;
    aliases = node;
}

static void builtin_unalias(char *args[]) {
    if (args[1] == NULL) {
        fprintf(stderr, "usage: unalias name\n");
        return;
    }

    AliasNode *prev = NULL;
    AliasNode *cur = aliases;

    while (cur != NULL) {
        if (strcmp(cur->name, args[1]) == 0) {
            if (prev == NULL) {
                aliases = cur->next;
            } else {
                prev->next = cur->next;
            }
            free(cur->name);
            free(cur->command);
            free(cur);
            return;
        }
        prev = cur;
        cur = cur->next;
    }

    fprintf(stderr, "unalias: %s: not found\n", args[1]);
}

static const char *resolve_alias(const char *name) {
    for (AliasNode *cur = aliases; cur != NULL; cur = cur->next) {
        if (strcmp(cur->name, name) == 0) {
            return cur->command;
        }
    }
    return name;
}

static int compare_names(const void *a, const void *b) {
    const char *const *sa = a;
    const char *const *sb = b;
    return strcmp(*sa, *sb);
}

static int expand_wildcards(char *args[], char *expanded[]) {
    int out = 0;

    for (int i = 0; args[i] != NULL && out < MAXARGNUM - 1; i++) {
        if (strcmp(args[i], "*") != 0) {
            expanded[out++] = xstrdup(args[i]);
            continue;
        }

        DIR *dir = opendir(".");
        if (dir == NULL) {
            perror("opendir");
            expanded[out++] = xstrdup(args[i]);
            continue;
        }

        char *names[MAXARGNUM];
        int count = 0;
        struct dirent *entry;

        while ((entry = readdir(dir)) != NULL && count < MAXARGNUM - 1) {
            if (entry->d_name[0] == '.') {
                continue;
            }
            names[count++] = xstrdup(entry->d_name);
        }
        closedir(dir);

        qsort(names, count, sizeof(char *), compare_names);

        for (int j = 0; j < count && out < MAXARGNUM - 1; j++) {
            expanded[out++] = names[j];
        }
    }

    expanded[out] = NULL;
    return out;
}

static void free_args(char *args[]) {
    for (int i = 0; args[i] != NULL; i++) {
        free(args[i]);
        args[i] = NULL;
    }
}

static int execute_external(char *args[], int background) {
    char *expanded[MAXARGNUM];
    expand_wildcards(args, expanded);

    pid_t pid = fork();
    if (pid == -1) {
        perror("fork");
        free_args(expanded);
        return -1;
    }

    if (pid == 0) {
        execvp(expanded[0], expanded);
        perror(expanded[0]);
        _exit(EXIT_FAILURE);
    }

    if (background) {
        printf("%d\n", pid);
    } else {
        int status;
        waitpid(pid, &status, 0);
    }

    free_args(expanded);
    return 0;
}

static int run_command_line(char *line) {
    char work[BUFLEN];
    char *args[MAXARGNUM];

    snprintf(work, sizeof(work), "%s", line);
    int background = parse_command(work, args);

    if (args[0] == NULL) {
        return 0;
    }

    if (strcmp(args[0], "exit") == 0) {
        return 1;
    }

    if (strcmp(args[0], "!!") == 0) {
        char *cmd = previous_history();
        if (cmd != NULL) {
            printf("%s\n", cmd);
            add_history(cmd);
            int done = run_command_line(cmd);
            free(cmd);
            return done;
        }
        return 0;
    }

    if (args[0][0] == '!' && args[0][1] != '\0') {
        char *cmd = find_history_by_prefix(args[0] + 1);
        if (cmd != NULL) {
            printf("%s\n", cmd);
            add_history(cmd);
            int done = run_command_line(cmd);
            free(cmd);
            return done;
        }
        return 0;
    }

    args[0] = (char *)resolve_alias(args[0]);

    if (strcmp(args[0], "cd") == 0) {
        builtin_cd(args);
    } else if (strcmp(args[0], "pushd") == 0) {
        builtin_pushd();
    } else if (strcmp(args[0], "dirs") == 0) {
        builtin_dirs();
    } else if (strcmp(args[0], "popd") == 0) {
        builtin_popd();
    } else if (strcmp(args[0], "history") == 0) {
        print_history();
    } else if (strcmp(args[0], "prompt") == 0) {
        free(prompt);
        prompt = args[1] ? xstrdup(args[1]) : NULL;
    } else if (strcmp(args[0], "alias") == 0) {
        builtin_alias(args);
    } else if (strcmp(args[0], "unalias") == 0) {
        builtin_unalias(args);
    } else if (strcmp(args[0], "rename") == 0) {
        if (args[1] == NULL || args[2] == NULL) {
            fprintf(stderr, "usage: rename oldname newname\n");
        } else if (rename(args[1], args[2]) == -1) {
            perror("rename");
        } else {
            printf("success\n");
        }
    } else {
        execute_external(args, background);
    }

    return 0;
}

static void cleanup(void) {
    while (dir_stack != NULL) {
        DirNode *next = dir_stack->next;
        free(dir_stack->path);
        free(dir_stack);
        dir_stack = next;
    }

    while (aliases != NULL) {
        AliasNode *next = aliases->next;
        free(aliases->name);
        free(aliases->command);
        free(aliases);
        aliases = next;
    }

    for (int i = 0; i < history_count; i++) {
        free(history_list[i]);
    }

    free(prompt);
}

int main(void) {
    char line[BUFLEN];

    for (;;) {
        printf("%s : ", prompt ? prompt : "Command");
        fflush(stdout);

        if (fgets(line, sizeof(line), stdin) == NULL) {
            printf("done.\n");
            break;
        }

        if (line[strcspn(line, "\n")] == '\0') {
            continue;
        }

        /*
         * History expansion commands (!!, !prefix) should refer to commands
         * that were executed before the current input. Therefore, they are
         * not added here; run_command_line() records the expanded command.
         */
        char *first = line;
        while (*first == ' ' || *first == '\t') {
            first++;
        }
        if (*first != '!') {
            add_history(line);
        }

        if (run_command_line(line)) {
            printf("done.\n");
            break;
        }
    }

    cleanup();
    return 0;
}
