source $HOME/.bashrc
source $HOME/.bash_profile

git pull
echo "testkey" > /tmp/.orders.key
mvn -q clean install 2>/dev/null
