# ~/bin/tmux-myproject shell script
# The Project name is also used as a session name (usually shorter)

PROJECT_NAME="cloj"

alias tmux="TERM=xterm-256color tmux"

tmux has-session -t $PROJECT_NAME 2>/dev/null

if [ "$?" -eq 1 ] ; then
    echo "No Session found.  Creating and configuring."
    tmux new-session -d -s $PROJECT_NAME
    tmux neww -n VIM "vim -f"
    tmux splitw -p 10 "lein cljsbuild auto opt"
    tmux neww -n Server "http-server ."
    tmux neww -n IRC ir
    tmux selectw -t 2
    tmux selectp -t 0
else
    echo "Session found.  Connecting."
fi

tmux attach-session -t $PROJECT_NAME
