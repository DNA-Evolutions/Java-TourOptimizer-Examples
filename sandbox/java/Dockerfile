FROM dnaevolutions/jopt_example_base_server:4.13.0

USER root
# Copy entrypoint for pulling example from GitHub on start up
COPY ./jopt-docker-entrypoint.sh /usr/bin/
RUN ["chmod", "+x", "/usr/bin/jopt-docker-entrypoint.sh"]

# Set password for IDE
ENV PASSWORD=jopt

USER coder

# Install java pack extension 
RUN code-server --install-extension vscjava.vscode-maven
RUN code-server --install-extension vscjava.vscode-java-dependency
RUN code-server --install-extension vscjava.vscode-java-debug
RUN code-server --install-extension redhat.java

WORKDIR /home/coder/project

# Start code-server and bind address
ENTRYPOINT ["/usr/bin/jopt-docker-entrypoint.sh", "--bind-addr", "0.0.0.0:8080", "."]