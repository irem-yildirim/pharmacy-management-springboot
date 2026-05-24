#!/bin/sh

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`/" >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

MAVEN_OPTS="${MAVEN_OPTS}"

CLASSPATH="$APP_HOME/.mvn/wrapper/maven-wrapper.jar"

WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"

if [ -n "$JAVA_HOME" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
else
  JAVACMD="`which java`"
fi

if [ ! -x "$JAVACMD" ]; then
  echo "Error: JAVA_HOME is not defined correctly." >&2
  echo "  We cannot execute $JAVACMD" >&2
  exit 1
fi

if [ ! -f "$CLASSPATH" ]; then
  echo "Downloading Maven Wrapper..."
  if command -v curl > /dev/null; then
    curl -fsSL "$WRAPPER_URL" -o "$CLASSPATH"
  elif command -v wget > /dev/null; then
    wget -q -O "$CLASSPATH" "$WRAPPER_URL"
  else
    echo "ERROR: Neither curl nor wget found. Cannot download maven-wrapper.jar." >&2
    exit 1
  fi
fi

exec "$JAVACMD" \
  $DEFAULT_JVM_OPTS \
  $MAVEN_OPTS \
  $JAVA_OPTS \
  -classpath "$CLASSPATH" \
  org.apache.maven.wrapper.MavenWrapperMain \
  "$@"
