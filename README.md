# Trellis Control Application

## Build

```
docker run -it --rm -v $HOME/.m2:/root/.m2 -v $PWD:/root/trellis-control -w /root/trellis-control maven:3.6.3-openjdk-11 mvn clean install

```

The OAR file can be located under `app/target` and `web/target`