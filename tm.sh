# ~/bin/tmux-myproject shell script
# The Project name is also used as a session name (usually shorter)

PROJECT_NAME="cloj"

tmux has-session -t $PROJECT_NAME 2>/dev/null

if [ "$?" -eq 1 ] ; then
    echo "No Session found.  Creating and configuring."
    TERM=xterm-256color tmux new-session -d -s $PROJECT_NAME
    tmux neww -n irc ir
    tmux neww -n editor "vim -f"
    tmux splitw -p 10 -v "http-server ."

    
    tmux splitw -t 1 -p 50 -h "lein cljsbuild auto"
    tmux selectp -t 0
else
    echo "Session found.  Connecting."
fi

tmux attach-session -t $PROJECT_NAME
