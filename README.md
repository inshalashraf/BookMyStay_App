# BookMyStay App

A Java 17 console application for managing hotel reservations, room inventory, and cancellation history. Confirmed bookings are saved locally so they remain available after the application closes.

## Features

- Create bookings with a unique reservation ID, guest name, and room type
- Validate required booking details and reject duplicate IDs
- Check room availability before confirming a booking
- Cancel bookings and restore room inventory
- View confirmed bookings and available room counts
- Persist confirmed bookings between application runs
- Atomic file replacement to reduce the risk of partially written booking data
- Dependency-free regression tests and GitHub Actions CI

## Tech stack

- Java 17
- Java Collections Framework
- Java NIO file APIs
- Dependency-free test runners
- GitHub Actions

## Project structure

```text
BookMyStay_App/
├── UC1/
│   ├── src/
│   │   ├── Main.java
│   │   ├── BookingWorkflowExtensions.java
│   │   ├── BookingFileStore.java
│   │   └── BookingConsole.java
│   └── test/
│       ├── BookingWorkflowTest.java
│       └── BookingFileStoreTest.java
└── .github/workflows/java-tests.yml
```

## Run the original booking-history report

```bash
javac UC1/src/Main.java
java -cp UC1/src Main
```

## Run the interactive application

From the repository root:

```bash
mkdir -p build
javac -d build UC1/src/*.java
java -cp build BookingConsole
```

The application creates `data/bookings.csv` on the first successful booking or cancellation. The file is loaded at startup. Keep this file to retain local booking data; deleting it resets the saved reservations.

## Run tests

```bash
mkdir -p build
javac -d build UC1/src/*.java UC1/test/*.java
java -cp build BookingWorkflowTest
java -cp build BookingFileStoreTest
```

GitHub Actions runs both test programs on pushes and pull requests targeting `main`.

## Design

- `BookingRequest` validates the reservation ID, guest name, and room type.
- `RoomInventory` tracks available rooms and synchronizes room reservation/release.
- `BookingService` confirms bookings, prevents duplicate IDs, and handles cancellation.
- `BookingQueue` provides FIFO ordering for pending requests.
- `BookingFileStore` loads and saves confirmed bookings using UTF-8 text and URL-safe Base64 fields.
- `BookingConsole` provides the interactive command-line interface.

## Scope and limitations

Persistence is local file-based, not a database server. The application currently uses a fixed initial room inventory and a single local data file. It does not yet include booking dates, pricing, customer accounts, multi-user synchronization, or a graphical/web interface. Do not use the local file store as a production hotel booking backend.
