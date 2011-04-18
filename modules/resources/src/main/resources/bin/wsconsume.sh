#!/bin/sh

# Extract the directory and the program name
# takes care of symlinks
PRG="$0"
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG="`dirname "$PRG"`/$link"
  fi
done
DIRNAME=`dirname "$PRG"`
PROGNAME=`basename "$PRG"`

# OS specific support (must be 'true' or 'false').
cygwin=false;
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
    [ -n "$JBOSS_HOME" ] &&
        JBOSS_HOME=`cygpath --unix "$JBOSS_HOME"`
    [ -n "$JAVA_HOME" ] &&
        JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
fi

# Setup JBOSS_HOME
if [ "x$JBOSS_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    JBOSS_HOME=`cd $DIRNAME/..; pwd`
fi
export JBOSS_HOME

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
	JAVA="$JAVA_HOME/bin/java"
    else
	JAVA="java"
    fi
fi

#JPDA options. Uncomment and modify as appropriate to enable remote debugging .
#JAVA_OPTS="-classic -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=y $JAVA_OPTS"

# Setup JBoss sepecific properties
JAVA_OPTS="$JAVA_OPTS"

# Setup the java endorsed dirs
JBOSS_ENDORSED_DIRS="$JBOSS_HOME/lib/endorsed"

###
# Setup the wsconsume classpath
###

# Shared libs
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JAVA_HOME/lib/tools.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/activation.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/getopt.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/wstx.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/wstx-lgpl.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jbossall-client.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/log4j.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/mail.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jbossws-api.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jbossws-spi.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jbossws-common.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jbossws-common-tools.jar"

# Shared jaxws libs
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jaxws-tools.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jaxws-rt.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/policy.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/stax-api.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jaxb-api.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jaxb-impl.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jaxb-xjc.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/streambuffer.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/stax-ex.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jaxws-api.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jsr181-api.jar"

# Stack specific dependencies
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/javassist.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jboss-xml-binding.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/resolver.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/xercesImpl.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jbossxb.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jbossws-native-client.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jbossws-native-core.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jbossws-native-services.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/jaxrpc-api.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JBOSS_HOME/client/saaj-api.jar"

###
# Execute the JVM
###

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
    JBOSS_HOME=`cygpath --path --windows "$JBOSS_HOME"`
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
    WSCONSUME_CLASSPATH=`cygpath --path --windows "$WSCONSUME_CLASSPATH"`
    JBOSS_ENDORSED_DIRS=`cygpath --path --windows "$JBOSS_ENDORSED_DIRS"`
fi

# Execute the command
"$JAVA" $JAVA_OPTS \
   -Dlog4j.configuration=log4j.xml \
   -Djava.endorsed.dirs="$JBOSS_ENDORSED_DIRS" \
   -classpath "$WSCONSUME_CLASSPATH" \
   org.jboss.wsf.spi.tools.cmd.WSConsume "$@"
