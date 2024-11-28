git pull
git submodule update --init
pushd utils/dumbster
mvn install -Dmaven.test.skip=true
popd
pushd utils/selenium-helper
mvn install -Dmaven.test.skip=true -Ddependency.skip=true
popd

mvn clean install
