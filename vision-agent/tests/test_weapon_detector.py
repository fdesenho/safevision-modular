import pytest
import numpy as np
import cv2
from detectors.weapon_detector import WeaponDetector

@pytest.fixture
def detector():
    return WeaponDetector()

@pytest.fixture
def empty_frame():
    # Create a 640x480 black image
    return np.zeros((480, 640, 3), dtype=np.uint8)

def test_no_weapon_detected_in_empty_frame(detector, empty_frame):
    """
    Ensures that a black frame returns a negative result
    and does not crash the application.
    """
    rgb_frame = cv2.cvtColor(empty_frame, cv2.COLOR_BGR2RGB)
    result = detector.process(empty_frame, rgb_frame)
    
    assert result is not None
    assert result['hasWeapon'] is False
    assert result['weaponType'] == 'NONE'

def test_detector_initialization(detector):
    """
    Verifies if YOLO and MediaPipe models are loaded.
    """
    assert detector.model is not None
    assert detector.pose is not None