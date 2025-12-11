#!/usr/bin/env python3
"""
Standalone Device Simulator Runner
Run this script manually to simulate energy consumption measurements.

Usage:
    python run_simulator.py                    # Default: Device ID 1, 10-minute intervals
    python run_simulator.py 42                 # Custom Device ID 42
    python run_simulator.py 42 300             # Device ID 42, 5-minute intervals
    python run_simulator.py 1 60               # Device ID 1, 1-minute intervals (for testing)

Prerequisites:
    pip install pika
"""

import sys
import os

from device_simulator import EnergySimulator


def print_banner():
    """Print welcome banner"""
    print("\n")
    print("█" * 70)
    print("█" + " " * 68 + "█")
    print("█" + "  SMART ENERGY MANAGEMENT SYSTEM - DEVICE SIMULATOR".center(68) + "█")
    print("█" + " " * 68 + "█")
    print("█" * 70)
    print("\n")


def print_instructions():
    """Print usage instructions"""
    print("[ℹ] Instructions:")
    print("  - Press Ctrl+C to stop the simulator at any time")
    print("  - Measurements will be sent to RabbitMQ queue: energy.data.queue")
    print("  - Routing key: energy.data.device")
    print("  - Ensure RabbitMQ is running on localhost:5672\n")


def main():
    """Main entry point"""
    print_banner()
    
    device_id = None
    interval = 600  # Default: 10 minutes
    
    # Parse command line arguments
    if len(sys.argv) > 1:
        device_id = sys.argv[1]  # Accept any string (including UUIDs)
        print(f"[✓] Using Device ID: {device_id}")
    else:
        print("[ℹ] Using default Device ID: 1")
    
    if len(sys.argv) > 2:
        try:
            interval = int(sys.argv[2])
            print(f"[✓] Using custom interval: {interval} seconds ({interval//60} minutes)")
        except ValueError:
            print(f"[✗] Error: Invalid interval '{sys.argv[2]}' - must be an integer (seconds)")
            sys.exit(1)
    else:
        print(f"[ℹ] Using default interval: {interval} seconds ({interval//60} minutes)")
    
    print_instructions()
    
    try:
        # Create and start simulator
        simulator = EnergySimulator(device_id=device_id)
        simulator.start_simulation(interval_seconds=interval)
        
    except KeyboardInterrupt:
        print("\n\n[✓] Simulator stopped by user")
        sys.exit(0)
    except Exception as e:
        print(f"\n[✗] Fatal error: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
