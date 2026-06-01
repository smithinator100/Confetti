#!/usr/bin/env python3

import json
import math
import os
import sys
from dataclasses import dataclass
from typing import Dict, List, Tuple

import cv2
import numpy as np


@dataclass
class VideoMetrics:
    path: str
    fps: float
    frame_count: int
    duration_s: float
    mean_motion: float
    peak_motion: float
    active_duration_s: float
    peak_time_s: float
    roi: Dict[str, int]
    roi_area_ratio: float

    def to_dict(self) -> Dict[str, float]:
        return {
            "path": self.path,
            "fps": self.fps,
            "frame_count": self.frame_count,
            "duration_s": self.duration_s,
            "mean_motion": self.mean_motion,
            "peak_motion": self.peak_motion,
            "active_duration_s": self.active_duration_s,
            "peak_time_s": self.peak_time_s,
            "roi": self.roi,
            "roi_area_ratio": self.roi_area_ratio,
        }


def detect_motion_roi(
    motion_map: np.ndarray,
    fallback_size: Tuple[int, int],
) -> Tuple[int, int, int, int]:
    height, width = motion_map.shape
    finite_values = motion_map[np.isfinite(motion_map)]
    if finite_values.size == 0:
        return center_fallback_roi(width, height, fallback_size)

    threshold = float(np.percentile(finite_values, 96))
    if threshold <= 0:
        return center_fallback_roi(width, height, fallback_size)

    mask = (motion_map >= threshold).astype(np.uint8) * 255
    kernel = np.ones((7, 7), np.uint8)
    mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, kernel)
    mask = cv2.dilate(mask, kernel, iterations=1)

    contours, _ = cv2.findContours(mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    if not contours:
        return center_fallback_roi(width, height, fallback_size)

    largest = max(contours, key=cv2.contourArea)
    x, y, w, h = cv2.boundingRect(largest)

    margin = 24
    x0 = max(0, x - margin)
    y0 = max(0, y - margin)
    x1 = min(width, x + w + margin)
    y1 = min(height, y + h + margin)

    if (x1 - x0) < 40 or (y1 - y0) < 40:
        return center_fallback_roi(width, height, fallback_size)
    return x0, y0, x1, y1


def center_fallback_roi(
    width: int,
    height: int,
    size: Tuple[int, int],
) -> Tuple[int, int, int, int]:
    roi_w = min(size[0], width)
    roi_h = min(size[1], height)
    x0 = (width - roi_w) // 2
    y0 = (height - roi_h) // 2
    return x0, y0, x0 + roi_w, y0 + roi_h


def compute_motion_series(video_path: str) -> VideoMetrics:
    capture = cv2.VideoCapture(video_path)
    if not capture.isOpened():
        raise RuntimeError(f"Could not open video: {video_path}")

    fps = capture.get(cv2.CAP_PROP_FPS)
    if fps <= 0:
        fps = 30.0
    frame_count = int(capture.get(cv2.CAP_PROP_FRAME_COUNT))
    duration_s = frame_count / fps if frame_count > 0 else 0.0

    success, first_frame = capture.read()
    if not success:
        capture.release()
        raise RuntimeError(f"Video has no frames: {video_path}")

    analysis_width, analysis_height = 360, 640
    prev_gray = cv2.cvtColor(first_frame, cv2.COLOR_BGR2GRAY)
    prev_gray = cv2.resize(prev_gray, (analysis_width, analysis_height))

    flow_magnitudes: List[np.ndarray] = []
    motion_map = np.zeros((analysis_height, analysis_width), dtype=np.float32)

    while True:
        success, frame = capture.read()
        if not success:
            break
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        gray = cv2.resize(gray, (analysis_width, analysis_height))
        flow = cv2.calcOpticalFlowFarneback(
            prev_gray,
            gray,
            None,
            pyr_scale=0.5,
            levels=3,
            winsize=15,
            iterations=3,
            poly_n=5,
            poly_sigma=1.2,
            flags=0,
        )
        magnitude = np.sqrt(flow[..., 0] ** 2 + flow[..., 1] ** 2)
        flow_magnitudes.append(magnitude)
        motion_map += magnitude.astype(np.float32)
        prev_gray = gray

    capture.release()

    if not flow_magnitudes:
        flow_magnitudes = [np.zeros((analysis_height, analysis_width), dtype=np.float32)]

    x0, y0, x1, y1 = detect_motion_roi(
        motion_map,
        fallback_size=(int(analysis_width * 0.55), int(analysis_height * 0.55)),
    )
    roi = {"x0": x0, "y0": y0, "x1": x1, "y1": y1}
    roi_area_ratio = ((x1 - x0) * (y1 - y0)) / float(analysis_width * analysis_height)

    motion_values = [float(np.mean(mag[y0:y1, x0:x1])) for mag in flow_magnitudes]

    mean_motion = float(np.mean(motion_values))
    peak_motion = float(np.max(motion_values))
    peak_index = int(np.argmax(motion_values))
    peak_time_s = peak_index / fps

    active_threshold = max(peak_motion * 0.2, 0.02)
    active_frames = sum(1 for value in motion_values if value >= active_threshold)
    active_duration_s = active_frames / fps

    return VideoMetrics(
        path=video_path,
        fps=fps,
        frame_count=frame_count,
        duration_s=duration_s,
        mean_motion=mean_motion,
        peak_motion=peak_motion,
        active_duration_s=active_duration_s,
        peak_time_s=peak_time_s,
        roi=roi,
        roi_area_ratio=roi_area_ratio,
    )


def format_ratio(a: float, b: float) -> float:
    if b == 0:
        return math.inf
    return a / b


def main() -> None:
    if len(sys.argv) != 4:
        raise SystemExit(
            "Usage: python3 tools/compare-confetti-videos.py <android.mp4> <web.mp4> <report.json>"
        )

    android_path = os.path.abspath(sys.argv[1])
    web_path = os.path.abspath(sys.argv[2])
    report_path = os.path.abspath(sys.argv[3])

    android = compute_motion_series(android_path)
    web = compute_motion_series(web_path)

    comparison = {
        "android": android.to_dict(),
        "web": web.to_dict(),
        "ratios": {
            "active_duration_android_over_web": format_ratio(
                android.active_duration_s, web.active_duration_s
            ),
            "mean_motion_android_over_web": format_ratio(
                android.mean_motion, web.mean_motion
            ),
            "peak_motion_android_over_web": format_ratio(
                android.peak_motion, web.peak_motion
            ),
        },
    }

    os.makedirs(os.path.dirname(report_path), exist_ok=True)
    with open(report_path, "w", encoding="utf-8") as file:
        json.dump(comparison, file, indent=2)

    print("Confetti video comparison")
    print(f"- Android video: {android_path}")
    print(f"- Web video: {web_path}")
    print(
        f"- Active duration ratio (Android/Web): "
        f"{comparison['ratios']['active_duration_android_over_web']:.3f}"
    )
    print(
        f"- Mean motion ratio (Android/Web): "
        f"{comparison['ratios']['mean_motion_android_over_web']:.3f}"
    )
    print(
        f"- Peak motion ratio (Android/Web): "
        f"{comparison['ratios']['peak_motion_android_over_web']:.3f}"
    )
    print(
        f"- Android ROI: {android.roi} ({android.roi_area_ratio:.3f} of frame)"
    )
    print(f"- Web ROI: {web.roi} ({web.roi_area_ratio:.3f} of frame)")
    print(f"- JSON report: {report_path}")


if __name__ == "__main__":
    main()
