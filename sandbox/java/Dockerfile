FROM dnaevolutions/jopt_example_base_server:4.91.0

USER root
# Copy entrypoint for pulling example from GitHub on start up
COPY ./jopt-docker-entrypoint.sh /usr/bin/
RUN ["chmod", "+x", "/usr/bin/jopt-docker-entrypoint.sh"]

# Set password for IDE
ENV PASSWORD=jopt

USER coder

WORKDIR /home/coder/project

# Start code-server and bind address
ENTRYPOINT ["/usr/bin/jopt-docker-entrypoint.sh", "--bind-addr", "0.0.0.0:8080", "."]