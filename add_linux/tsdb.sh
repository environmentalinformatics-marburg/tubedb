exec java -Djava.awt.headless=true -XX:-UsePerfData -Djava.io.tmpdir=/var/tmp -Xmx3g -classpath 'tubedb.jar:lib/*' tsdb.run.Terminal "$@"
