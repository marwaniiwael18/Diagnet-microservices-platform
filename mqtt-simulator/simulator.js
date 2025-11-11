/**
 * MQTT Simulator for DiagNet Platform
 * 
 * PURPOSE:
 * Simulates real industrial machines sending sensor data via MQTT protocol
 * 
 * WHAT IT DOES:
 * 1. Creates virtual machines (MACHINE-001, MACHINE-002, etc.)
 * 2. Generates realistic sensor readings (temperature, vibration, pressure, etc.)
 * 3. Publishes data to MQTT broker every few seconds
 * 4. Simulates normal operation and occasional anomalies
 * 
 * MQTT TOPICS:
 * - machine/{machineId}/data   - Sensor readings
 * - machine/{machineId}/status - Machine status updates
 * 
 * USAGE:
 * npm start                           # Default: 3 machines, 10 second interval
 * npm start -- --machines=5           # 5 machines
 * npm start -- --interval=5000        # Publish every 5 seconds
 * npm start -- --broker=mqtt://localhost:1883
 */

const mqtt = require('mqtt');

// Configuration from command line arguments or defaults
const config = {
    broker: process.env.MQTT_BROKER || 'mqtt://localhost:1883',
    username: process.env.MQTT_USERNAME || 'admin',
    password: process.env.MQTT_PASSWORD || 'admin',
    numMachines: parseInt(process.argv.find(arg => arg.startsWith('--machines='))?.split('=')[1] || '3'),
    interval: parseInt(process.argv.find(arg => arg.startsWith('--interval='))?.split('=')[1] || '10000'), // milliseconds
    clientId: `mqtt-simulator-${Math.random().toString(16).slice(3)}`
};

// Machine state tracking
const machines = [];

/**
 * Initialize virtual machines with random initial states
 */
function initializeMachines() {
    const locations = ['Factory Floor A', 'Factory Floor B', 'Factory Floor C', 'Warehouse', 'Assembly Line'];
    
    for (let i = 1; i <= config.numMachines; i++) {
        const machineId = `MACHINE-${String(i).padStart(3, '0')}`;
        machines.push({
            machineId: machineId,
            location: locations[i % locations.length],
            baseTemperature: 70 + Math.random() * 10,      // 70-80°C normal
            baseVibration: 0.3 + Math.random() * 0.15,     // 0.3-0.45 normal
            basePressure: 2.0 + Math.random() * 0.5,       // 2.0-2.5 bar normal
            baseHumidity: 40 + Math.random() * 10,         // 40-50% normal
            basePower: 140 + Math.random() * 30,           // 140-170 watts normal
            baseRotationSpeed: 1450 + Math.random() * 100, // 1450-1550 RPM normal
            status: 'RUNNING',
            anomalyChance: 0.05,  // 5% chance of anomaly per cycle
            isAnomalous: false,
            anomalyCounter: 0
        });
    }
    
    console.log(`✅ Initialized ${config.numMachines} virtual machines`);
}

/**
 * Generate realistic sensor reading with optional anomaly
 * @param {Object} machine - Machine state object
 * @returns {Object} Sensor data
 */
function generateSensorData(machine) {
    // Decide if this cycle should be anomalous
    if (!machine.isAnomalous && Math.random() < machine.anomalyChance) {
        machine.isAnomalous = true;
        machine.anomalyCounter = 3 + Math.floor(Math.random() * 5); // Anomaly lasts 3-7 cycles
        console.log(`⚠️  ${machine.machineId} entering anomalous state!`);
    }
    
    // Countdown anomaly
    if (machine.isAnomalous) {
        machine.anomalyCounter--;
        if (machine.anomalyCounter <= 0) {
            machine.isAnomalous = false;
            console.log(`✅ ${machine.machineId} returning to normal operation`);
        }
    }
    
    // Generate readings based on state
    let temperature, vibration, status;
    
    if (machine.isAnomalous) {
        // Anomalous readings
        const severity = Math.random();
        if (severity > 0.7) {
            // CRITICAL anomaly
            temperature = machine.baseTemperature + 30 + Math.random() * 20; // 100-120°C
            vibration = machine.baseVibration + 0.5 + Math.random() * 0.2;  // 0.8-1.0
            status = 'CRITICAL';
        } else {
            // WARNING anomaly
            temperature = machine.baseTemperature + 15 + Math.random() * 10; // 85-95°C
            vibration = machine.baseVibration + 0.3 + Math.random() * 0.15; // 0.6-0.75
            status = 'WARNING';
        }
    } else {
        // Normal operation with small variations
        temperature = machine.baseTemperature + (Math.random() - 0.5) * 5; // ±2.5°C
        vibration = machine.baseVibration + (Math.random() - 0.5) * 0.1;  // ±0.05
        status = 'RUNNING';
    }
    
    // Other sensors (always slight variation)
    const pressure = machine.basePressure + (Math.random() - 0.5) * 0.2;
    const humidity = machine.baseHumidity + (Math.random() - 0.5) * 5;
    const powerConsumption = machine.basePower + (Math.random() - 0.5) * 20;
    const rotationSpeed = machine.baseRotationSpeed + (Math.random() - 0.5) * 50;
    
    return {
        machineId: machine.machineId,
        timestamp: new Date().toISOString().slice(0, 19), // Format: 2025-11-11T22:30:00
        temperature: Math.round(temperature * 10) / 10,
        vibration: Math.round(vibration * 100) / 100,
        pressure: Math.round(pressure * 100) / 100,
        humidity: Math.round(humidity * 10) / 10,
        powerConsumption: Math.round(powerConsumption * 10) / 10,
        rotationSpeed: Math.round(rotationSpeed),
        status: status,
        location: machine.location
    };
}

/**
 * Publish sensor data to MQTT broker
 */
function publishSensorData(client, machine) {
    const data = generateSensorData(machine);
    const topic = `machine/${machine.machineId}/data`;
    
    client.publish(topic, JSON.stringify(data), { qos: 1 }, (err) => {
        if (err) {
            console.error(`❌ Failed to publish for ${machine.machineId}:`, err.message);
        } else {
            const icon = data.status === 'CRITICAL' ? '🔴' : data.status === 'WARNING' ? '⚠️' : '🟢';
            console.log(`${icon} ${machine.machineId} | Temp: ${data.temperature}°C | Vib: ${data.vibration} | Status: ${data.status}`);
        }
    });
}

/**
 * Main simulator function
 */
function startSimulator() {
    console.log('\n🚀 DiagNet MQTT Simulator Starting...\n');
    console.log('Configuration:');
    console.log(`  Broker:     ${config.broker}`);
    console.log(`  Machines:   ${config.numMachines}`);
    console.log(`  Interval:   ${config.interval}ms (${config.interval / 1000}s)`);
    console.log(`  Client ID:  ${config.clientId}\n`);
    
    // Initialize machines
    initializeMachines();
    
    // Connect to MQTT broker
    console.log(`🔌 Connecting to MQTT broker at ${config.broker}...`);
    const client = mqtt.connect(config.broker, {
        clientId: config.clientId,
        username: config.username,
        password: config.password,
        clean: true,
        reconnectPeriod: 5000
    });
    
    // Connection handlers
    client.on('connect', () => {
        console.log('✅ Connected to MQTT broker!\n');
        console.log('📊 Starting data simulation...\n');
        
        // Publish initial data immediately
        machines.forEach(machine => publishSensorData(client, machine));
        
        // Set up periodic publishing
        setInterval(() => {
            machines.forEach(machine => publishSensorData(client, machine));
        }, config.interval);
    });
    
    client.on('error', (err) => {
        console.error('❌ MQTT Connection Error:', err.message);
        console.error('\n💡 Make sure MQTT broker (Mosquitto) is running!');
        console.error('   Start with: brew services start mosquitto');
        console.error('   Or Docker: docker run -d -p 1883:1883 eclipse-mosquitto\n');
    });
    
    client.on('offline', () => {
        console.log('⚠️  MQTT client is offline, attempting to reconnect...');
    });
    
    client.on('reconnect', () => {
        console.log('🔄 Reconnecting to MQTT broker...');
    });
    
    // Graceful shutdown
    process.on('SIGINT', () => {
        console.log('\n\n🛑 Shutting down simulator...');
        client.end(true, () => {
            console.log('✅ Disconnected from MQTT broker');
            console.log('👋 Goodbye!\n');
            process.exit(0);
        });
    });
}

// Start the simulator
startSimulator();
