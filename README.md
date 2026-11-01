# BookMyStay App

A Java console application for hotel reservation history and booking management.

## Features
- Create bookings with a unique reservation ID, guest name, and room type
- Validate required booking details and reject duplicate IDs
- Check room availability before confirming a booking
- Cancel bookings and restore room inventory
- View confirmed bookings, available rooms, and cancellation history
- Automated regression tests with GitHub Actions

## Tech Stack
- Java 17
- Java Collections Framework
- Dependency-free Java test runner
- GitHub Actions

## Project Structure
```text
BookMyStay_App/
├── UC1/
│   ├── src/
│   │   ├── Main.java
│   │   ├── BookingWorkflowExtensions.java
│   │   └── BookingConsole.java
│   └── test/
│       └── BookingWorkflowTest.java
└── .github/workflows/java-tests.yml
```

## Run the original booking-history report
```bash
javac UC1/src/Main.java
java -cp UC1/src Main
```

## Run the interactive booking application
From the repository root:
```bash
mkdir -p build
javac -d build UC1/src/*.java
java -cp build BookingConsole
```

## Run tests
```bash
mkdir -p build
javac -d build UC1/src/*.java UC1/test/*.java
java -cp build BookingWorkflowTest
```

GitHub Actions runs the compile and test commands on pushes and pull requests targeting `main`.

## Design
- `BookingRequest` validates the reservation ID, guest name, and room type.
- `RoomInventory` tracks available rooms and synchronizes room reservation/release.
- `BookingService` confirms bookings, prevents duplicate IDs, and handles cancellation.
- `BookingQueue` is a FIFO structure for pending requests.
- `BookingConsole` provides the interactive command-line interface.

## Scope and limitations
This is an educational in-memory application. Bookings reset when the program exits. Dates, pricing, customer accounts, database persistence, and a graphical/web interface are not included yet.
