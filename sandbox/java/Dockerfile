FROM codercom/code-server:latest

ARG  DEBIAN_FRONTEND=noninteractive 

# Latest Java version, change, if desired, to other version including hotspot 
ARG ADOPT_OPENJDK=adoptopenjdk-16-hotspot

# We need to be root for installing all tools :-(
USER root

# APT: Install Tools
RUN apt-get update && \
    # Basic tools
    apt-get upgrade -y && \
    apt-get install -y wget && \
    apt-get install -y gnupg && \
    apt-get install -y software-properties-common && \
    # Adoptopenjdk-16-hotspot
    wget -q -O - https://adoptopenjdk.jfrog.io/adoptopenjdk/api/gpg/key/public | apt-key add - && \
    add-apt-repository --yes https://adoptopenjdk.jfrog.io/adoptopenjdk/deb/ && \
    apt-get update && \
    apt-get install -y  ${ADOPT_OPENJDK} && \
    # Maven
    apt-get install -y maven && \ 
    apt-get clean
    
    	
# Copy entrypoint for pulling example from GitHub on start up
COPY ./jopt-docker-entrypoint.sh /usr/bin/
RUN ["chmod", "+x", "/usr/bin/jopt-docker-entrypoint.sh"]


# Set password for IDE
ENV PASSWORD=jopt

USER coder

# Install java pack extension
RUN code-server --install-extension vscjava.vscode-java-pack

WORKDIR /home/coder/project

# Start code-server and bind address
ENTRYPOINT ["/usr/bin/jopt-docker-entrypoint.sh", "--bind-addr", "0.0.0.0:8080", "."]