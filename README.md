# ChokroPorno — consent-first family safety prototype

A small Android prototype for visible, child-involved family safety. It is intentionally **not** a copy of the previous monitoring implementation.

## Safety boundaries

- The app remains visible in the launcher and has no hidden launch alias, secret dial code, boot receiver, device-admin receiver, or anti-uninstall behavior.
- It requests no camera, microphone, location, accessibility, or screen-capture permissions and implements no monitoring or streaming.
- The demo lets a child approve or deny each simulated live-session request on the device. Approval is in-memory, can be stopped by the child, and expires after five minutes. No media is captured or transmitted.
- A local six-digit app PIN is a convenience gate, not a substitute for account security or Android device credentials. It is stored as a salted hash in app-private preferences.
- The disclosure explains the prototype’s behavior before use.

## Build

Requirements: JDK 17 and Android SDK Platform 34.

```bash
gradle :app:assembleDebug
