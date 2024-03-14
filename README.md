# Fetal Lite Sensor Unit Data Viewer

## Overview

This Android application is designed to read samples collected from the Fetal Lite Sensor Unit and display the channel values on the UI. It also plays a promotional video for the system. The samples are read from an input file, decoded, and displayed in real-time, while the video runs in a loop.

## Features

- Reads samples from an input file.
- Decodes each sample to extract channel values.
- Displays every 100th sample on the UI.
- Runs the video in a loop from the UI.
- Utilizes a background service and separates 4 threads for efficient processing.

## Usage

1. Clone the repository:
  ```bash
  git clone https://github.com/Fenil-13/AsyncServicesChallenge.git
  ```
3. Open the project in Android Studio.
4. Run the application on an Android device or emulator.

## Sample Structure
Each sample in the input file follows this structure:
  ```bash
  !<Sample NumberMSB><Sample NumberLSB><Channel 1 Data><Channel 2 Data><Channel 3 Data><Channel 4 Data>
  ```
Each channel data is a 6-byte hex-encoded voltage value, which needs to be converted from hex to double.

## Background Service and Threading
The application utilizes a background service to read samples from the input file and decode them. Four threads are run in a separate executor/async service to decode each channel. The UI is updated to display every 100th sample value

## Note
This application was developed as part of an assignment for an Android developer role.
