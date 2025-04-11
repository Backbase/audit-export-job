
# eval $(minikube docker-env)
mvn clean package -Pdocker-image,local-client,no-latest-tag -Ddocker.repo.url=local