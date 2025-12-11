#!/usr/bin/env python3
"""
Device Data Simulator for Smart Energy Management System
Generates synthetic energy consumption measurements and sends them to RabbitMQ
"""

import pika
import json
import random
import time
import math
from datetime import datetime, timedelta
from config import CONFIG

class EnergySimulator:
    def __init__(self, device_id=None):
        """
        Initialize the Energy Simulator
        
        Args:
            device_id: Device ID to simulate (uses config if not provided)
        """
        self.device_id = device_id or CONFIG['device_id']
        self.rabbitmq_host = CONFIG['rabbitmq_host']
        self.rabbitmq_port = CONFIG['rabbitmq_port']
        self.rabbitmq_username = CONFIG['rabbitmq_username']
        self.rabbitmq_password = CONFIG['rabbitmq_password']
        
        # Energy consumption pattern parameters
        self.base_load = random.uniform(0.5, 1.5)  # Base consumption in kWh
        self.peak_hours = [8, 18, 19, 20]  # Hours when consumption peaks
        self.night_hours = [0, 1, 2, 3, 4, 5]  # Night hours with lower consumption
        
        self.connection = None
        self.channel = None

        # Simulation time control: when set, each sent measurement will be stamped
        # as if `virtual_minutes` minutes passed between measurements. This allows
        # running the loop faster (e.g. every 10s) while producing timestamps and
        # scaled measurements as if they were produced every `virtual_minutes`.
        self.virtual_minutes = None
        self._sim_time = None
        self._current_interval_seconds = None
        
    def connect_to_rabbitmq(self):
        """Establish connection to RabbitMQ"""
        try:
            print(f"[DEBUG] Connecting with user: {self.rabbitmq_username}, password: {'*' * len(self.rabbitmq_password)}")
            credentials = pika.PlainCredentials(self.rabbitmq_username, self.rabbitmq_password)
            parameters = pika.ConnectionParameters(
                host=self.rabbitmq_host,
                port=self.rabbitmq_port,
                credentials=credentials,
                connection_attempts=3,
                retry_delay=2
            )
            self.connection = pika.BlockingConnection(parameters)
            self.channel = self.connection.channel()
            
            # Declare exchange and queue
            self.channel.exchange_declare(
                exchange='energy.exchange',
                exchange_type='topic',
                durable=True
            )
            self.channel.queue_declare(queue='energy.data.queue', durable=True)
            self.channel.queue_bind(
                queue='energy.data.queue',
                exchange='energy.exchange',
                routing_key='energy.data.*'
            )
            
            print(f"[✓] Connected to RabbitMQ at {self.rabbitmq_host}:{self.rabbitmq_port}")
        except Exception as e:
            print(f"[✗] Failed to connect to RabbitMQ: {e}")
            raise
    
    def generate_measurement(self):
        """Generate a synthetic energy measurement based on time patterns"""
        current_hour = datetime.now().hour
        
        # Night time: lower consumption
        if current_hour in self.night_hours:
            multiplier = random.uniform(0.3, 0.6)
        # Peak hours: higher consumption
        elif current_hour in self.peak_hours:
            multiplier = random.uniform(1.5, 2.5)
        # Regular hours: normal consumption
        else:
            multiplier = random.uniform(0.8, 1.3)
        
        # Add some randomness to simulate natural fluctuations
        noise = random.uniform(-0.1, 0.1)
        measurement = max(0.1, self.base_load * multiplier + noise)
        
        # Round to 2 decimal places
        return round(measurement, 2)
    
    def send_measurement(self):
        """Generate and send a measurement to RabbitMQ"""
        try:
            measurement_value = self.generate_measurement()
            # Determine timestamp: if virtual mode is active use simulated time,
            # otherwise use real now.
            if self.virtual_minutes is not None:
                if self._sim_time is None:
                    self._sim_time = datetime.now()
                timestamp = self._sim_time.isoformat()
                # advance simulated time by the virtual amount for next message
                self._sim_time = self._sim_time + timedelta(minutes=self.virtual_minutes)
            else:
                timestamp = datetime.now().isoformat()
            
            payload = {
                "device_id": self.device_id,
                "measurement_value": measurement_value,
                "timestamp": timestamp
            }
            
            # Optionally scale measurement so its value represents the virtual
            # time window instead of the real interval used to send.
            if self.virtual_minutes is not None and self._current_interval_seconds:
                try:
                    factor = (self.virtual_minutes * 60) / float(self._current_interval_seconds)
                    payload['measurement_value'] = round(payload['measurement_value'] * factor, 2)
                except Exception:
                    pass

            message = json.dumps(payload)
            
            self.channel.basic_publish(
                exchange='energy.exchange',
                routing_key='energy.data.device',
                body=message,
                properties=pika.BasicProperties(
                    delivery_mode=2,  # Make message persistent
                    content_type='application/json'
                )
            )
            
            print(f"[{timestamp}] Device {self.device_id}: {measurement_value} kWh")
            
        except Exception as e:
            print(f"[✗] Error sending measurement: {e}")
            if self.connection and not self.connection.is_closed:
                self.connection.close()
            self.connect_to_rabbitmq()
    
    def start_simulation(self, interval_seconds=600):
        """
        Start the simulation loop
        
        Args:
            interval_seconds: Time between measurements (default 600 = 10 minutes)
        """
        print(f"\n{'='*60}")
        print(f"Starting Energy Simulator for Device {self.device_id}")
        print(f"Measurement Interval: {interval_seconds} seconds ({interval_seconds//60} minutes)")
        print(f"{'='*60}\n")
        
        # store current real interval for optional scaling calculation
        self._current_interval_seconds = interval_seconds
        self.connect_to_rabbitmq()
        
        try:
            while True:
                self.send_measurement()
                time.sleep(interval_seconds)
        except KeyboardInterrupt:
            print("\n\n[✓] Simulator stopped by user")
        finally:
            if self.connection and not self.connection.is_closed:
                self.connection.close()
            print("[✓] Connection closed")


if __name__ == "__main__":
    import sys
    
    device_id = None
    interval = 600  # 10 minutes
    
    # Parse command line arguments
    if len(sys.argv) > 1:
        device_id = sys.argv[1]
        print(f"Using device ID from command line: {device_id}")
    
    if len(sys.argv) > 2:
        try:
            interval = int(sys.argv[2])
            print(f"Using custom interval: {interval} seconds")
        except ValueError:
            print(f"Invalid interval: {sys.argv[2]}")
            sys.exit(1)
    
    simulator = EnergySimulator(device_id=device_id)
    simulator.start_simulation(interval_seconds=interval)
