#!/usr/bin/env python3
"""
Test script for VR Debug Server
This demonstrates how to control the VR app remotely
"""

import requests
import time
import sys

# Configuration
QUEST_IP = "192.168.1.100"  # Replace with your Quest's IP address
PORT = 8080
BASE_URL = f"http://{QUEST_IP}:{PORT}"

def wait_for_app_ready():
    """Wait for the app to be fully initialized"""
    print("Waiting for app to be ready...")
    while True:
        try:
            response = requests.get(f"{BASE_URL}/api/app/ready", timeout=2)
            data = response.json()
            if data.get("ready"):
                print("App is ready!")
                return True
        except requests.exceptions.RequestException:
            print(".", end="", flush=True)
        time.sleep(1)

def rotate_camera(pitch=0, yaw=0, roll=0):
    """Rotate the camera by specified degrees"""
    print(f"Rotating camera: pitch={pitch}, yaw={yaw}, roll={roll}")
    response = requests.post(
        f"{BASE_URL}/api/camera/rotate",
        json={"pitch": pitch, "yaw": yaw, "roll": roll}
    )
    return response.json()

def point_controller_at_screen(x, y, controller="right"):
    """Point the controller at a screen position (0-1 normalized)"""
    print(f"Pointing {controller} controller at screen position ({x}, {y})")
    response = requests.post(
        f"{BASE_URL}/api/controller/point",
        json={
            "controller": controller,
            "screen": {"x": x, "y": y}
        }
    )
    return response.json()

def trigger_press(controller="right", duration=0.1):
    """Simulate a trigger press and release"""
    print(f"Pressing {controller} trigger")
    
    # Press
    requests.post(
        f"{BASE_URL}/api/input/trigger",
        json={"controller": controller, "action": "press"}
    )
    
    time.sleep(duration)
    
    # Release
    requests.post(
        f"{BASE_URL}/api/input/trigger",
        json={"controller": controller, "action": "release"}
    )

def get_scene_info():
    """Get current scene state"""
    response = requests.get(f"{BASE_URL}/api/scene/info")
    return response.json()

def demo_painting_tour():
    """Demo: Look at each painting in the gallery"""
    print("\n=== Starting Painting Tour ===")
    
    # Look at left wall painting
    print("\n1. Looking at left wall painting")
    rotate_camera(yaw=90)  # Turn left
    time.sleep(2)
    point_controller_at_screen(0.5, 0.5)
    time.sleep(1)
    trigger_press()
    time.sleep(3)
    
    # Look at back wall painting
    print("\n2. Looking at back wall painting")
    rotate_camera(yaw=-90)  # Turn back to center
    time.sleep(2)
    trigger_press()
    time.sleep(3)
    
    # Look at right wall painting
    print("\n3. Looking at right wall painting")
    rotate_camera(yaw=-90)  # Turn right
    time.sleep(2)
    trigger_press()
    time.sleep(3)
    
    # Return to center
    print("\n4. Returning to center view")
    rotate_camera(yaw=90)  # Turn back to center
    
    print("\n=== Painting Tour Complete ===")

def main():
    """Main test function"""
    print(f"VR Debug Server Test Script")
    print(f"Connecting to: {BASE_URL}")
    print("-" * 50)
    
    # Wait for app to be ready
    if not wait_for_app_ready():
        print("Failed to connect to app")
        return
    
    # Get initial scene info
    print("\nGetting scene info...")
    scene = get_scene_info()
    print(f"Scene has {scene.get('entities', [])} entities")
    
    # Run the demo
    demo_painting_tour()
    
    print("\nTest complete!")

if __name__ == "__main__":
    if len(sys.argv) > 1:
        QUEST_IP = sys.argv[1]
        BASE_URL = f"http://{QUEST_IP}:{PORT}"
        print(f"Using Quest IP: {QUEST_IP}")
    
    main()