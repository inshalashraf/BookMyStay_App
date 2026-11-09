import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

/** Interactive console for creating and managing persistent hotel reservations. */
public class BookingConsole {
    private static final Path BOOKING_FILE = Path.of("data", "bookings.csv");

    public static void main(String[] args) {
        Map<String, Integer> initialRooms = new LinkedHashMap<>();
        initialRooms.put("Single", 5);
        initialRooms.put("Double", 3);
        initialRooms.put("Suite", 2);

        BookingWorkflowExtensions.RoomInventory inventory =
                new BookingWorkflowExtensions.RoomInventory(initialRooms);
        BookingWorkflowExtensions.BookingService service =
                new BookingWorkflowExtensions.BookingService(inventory);

        try {
            for (BookingWorkflowExtensions.BookingRequest booking : BookingFileStore.load(BOOKING_FILE)) {
                service.confirm(booking);
            }
            System.out.println("Loaded " + service.confirmedBookings().size() + " saved booking(s).");
        } catch (IOException | BookingWorkflowExtensions.BookingException ex) {
            System.err.println("Could not load saved bookings: " + ex.getMessage());
            return;
        }

        try (Scanner input = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                System.out.println("\n=== BookMyStay Booking Menu ===");
                System.out.println("1. Book a room\n2. Cancel booking\n3. View bookings");
                System.out.println("4. View room availability\n5. Exit");
                System.out.print("Select: ");
                String choice = input.nextLine().trim();
                try {
                    switch (choice) {
                        case "1":
                            System.out.print("Reservation ID: ");
                            String id = input.nextLine();
                            System.out.print("Guest name: ");
                            String guest = input.nextLine();
                            System.out.print("Room type (Single/Double/Suite): ");
                            String room = input.nextLine();
                            service.confirm(new BookingWorkflowExtensions.BookingRequest(id, guest, room));
                            saveBookings(service);
                            System.out.println("Booking confirmed and saved successfully.");
                            break;
                        case "2":
                            System.out.print("Reservation ID: ");
                            service.cancel(input.nextLine());
                            saveBookings(service);
                            System.out.println("Booking cancelled and saved; room returned to inventory.");
                            break;
                        case "3":
                            if (service.confirmedBookings().isEmpty()) {
                                System.out.println("No confirmed bookings yet.");
                            }
                            for (BookingWorkflowExtensions.BookingRequest booking : service.confirmedBookings()) {
                                System.out.println(booking.getReservationId() + " | "
                                        + booking.getGuestName() + " | " + booking.getRoomType());
                            }
                            break;
                        case "4":
                            inventory.snapshot().forEach((type, count) ->
                                    System.out.println(type + ": " + count + " available"));
                            break;
                        case "5":
                            running = false;
                            System.out.println("Goodbye!");
                            break;
                        default:
                            System.out.println("Invalid option. Enter a number from 1 to 5.");
                    }
                } catch (BookingWorkflowExtensions.BookingException | IllegalArgumentException ex) {
                    System.out.println("Booking error: " + ex.getMessage());
                } catch (IOException ex) {
                    System.out.println("Could not save bookings: " + ex.getMessage());
                }
            }
        }
    }

    private static void saveBookings(BookingWorkflowExtensions.BookingService service) throws IOException {
        BookingFileStore.save(BOOKING_FILE, service.confirmedBookings());
    }
}
