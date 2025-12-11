"""
Configuration file for Device Data Simulator
"""

# RabbitMQ Configuration
CONFIG = {
    'rabbitmq_host': 'localhost',      # RabbitMQ server host
    'rabbitmq_port': 5672,             # RabbitMQ server port
    'rabbitmq_username': 'anna',       # RabbitMQ username
    'rabbitmq_password': 'anna',        # RabbitMQ password
    'device_id': 1,                    # Default device ID (can be overridden via command line)
}

"""
USAGE:

1. Default configuration (Device ID 1, 10-minute intervals):
   python device_simulator.py

2. Custom device ID (Device ID 42, 10-minute intervals):
   python device_simulator.py 42

3. Custom device ID and interval (Device ID 42, 5-minute intervals = 300 seconds):
   python device_simulator.py 42 300

4. For testing with 1-minute intervals:
   python device_simulator.py 1 60

Requirements:
- pip install pika
"""
