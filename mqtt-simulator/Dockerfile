# MQTT Simulator Dockerfile
FROM node:20-alpine

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci --only=production

# Copy simulator code
COPY simulator.js ./

# Use existing node user (already created in node:alpine image)
RUN chown -R node:node /app

# Switch to non-root user
USER node

# Run simulator
CMD ["node", "simulator.js"]
