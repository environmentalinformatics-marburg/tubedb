exec java -Djava.awt.headless=true -XX:-UsePerfData -Djava.io.tmpdir=/var/tmp -Xmx3g -classpath 'tubedb.jar:lib/*:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/jfxrt.jar' tsdb.run.Terminal "$@"
