source $HOME/.bashrc
source $HOME/.bash_profile

git pull
echo "testkey" > $HOME/.orders.key
mvn clean install 2>/dev/null
