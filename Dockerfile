FROM openjdk:8-jdk

WORKDIR /app

# Copy project files
COPY . /app

# Make gradlew executable
RUN chmod +x gradlew

# Build the project
RUN ./gradlew build --no-daemon

# Create output directory
RUN mkdir -p /output

# Copy the built JAR to output
RUN cp build/libs/*.jar /output/

# Set working directory for output
WORKDIR /output

# The JAR will be in /output directory
CMD ["ls", "-la"]
