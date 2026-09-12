# BookMyStay App

A Java console application demonstrating hotel reservation history and report generation.

## Features

- Stores confirmed reservations in memory
- Captures guest name, room type, and room ID
- Generates a formatted booking-history report
- Demonstrates separation between booking data and reporting logic

## Tech Stack

- Java
- Java Collections Framework
- IntelliJ IDEA project structure

## Project Structure

```text
BookMyStay_App/
├── UC1/
│   └── src/
│       └── Main.java
├── .gitignore
└── README.md
```

> `out/` and IDE metadata are generated project files and are excluded through `.gitignore`.

## Run

From `UC1/src`:

```bash
javac Main.java
java Main
```

## Example

The application creates sample reservations for multiple room types and prints a booking-history report containing the total number of confirmed bookings and their details.

## Design Notes

The project uses three simple responsibilities:

- `Reservation8` — represents reservation data.
- `BookingHistory` — stores confirmed reservations.
- `BookingReportService` — generates the console report.

This makes the project useful as a small Java/OOP learning example and a base for future booking features.

## Future Improvements

- Unique booking IDs
- Room availability checks
- Booking and cancellation workflows
- Input validation
- File/database persistence
- Automated tests
- Console menu or web interface
