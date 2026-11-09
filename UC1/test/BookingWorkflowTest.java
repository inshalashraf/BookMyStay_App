import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BookingWorkflowTest {
    public static void main(String[] args) throws Exception {
        testConfirmAndCancel();
        testDuplicateAndUnavailableRooms();
        testBookingValidation();
        testInventoryValidationAndSnapshot();
        testQueueUsesFifoOrder();
        testCancellationHistoryOrder();
        System.out.println("All booking workflow tests passed.");
    }

    private static void testConfirmAndCancel() throws Exception {
        Map<String, Integer> rooms = new LinkedHashMap<>();
        rooms.put("Single", 1);
        BookingWorkflowExtensions.RoomInventory inventory =
                new BookingWorkflowExtensions.RoomInventory(rooms);
        BookingWorkflowExtensions.BookingService service =
                new BookingWorkflowExtensions.BookingService(inventory);

        BookingWorkflowExtensions.BookingRequest booking =
                new BookingWorkflowExtensions.BookingRequest("BK-1", "Asha", "Single");
        service.confirm(booking);
        check(inventory.available("Single") == 0, "room reserved");
        check(service.confirmedBookings().size() == 1, "booking stored");
        service.cancel("BK-1");
        check(inventory.available("Single") == 1, "room restored after cancellation");
        check(service.confirmedBookings().isEmpty(), "cancelled booking removed");
        expectBookingFailure(() -> service.cancel("BK-1"), "repeat cancellation rejected");
    }

    private static void testDuplicateAndUnavailableRooms() throws Exception {
        BookingWorkflowExtensions.RoomInventory inventory =
                new BookingWorkflowExtensions.RoomInventory(Map.of("Single", 1));
        BookingWorkflowExtensions.BookingService service =
                new BookingWorkflowExtensions.BookingService(inventory);
        BookingWorkflowExtensions.BookingRequest first =
                new BookingWorkflowExtensions.BookingRequest("BK-1", "Asha", "Single");
        service.confirm(first);
        expectBookingFailure(() -> service.confirm(first), "duplicate ID rejected");
        expectBookingFailure(() -> service.confirm(
                new BookingWorkflowExtensions.BookingRequest("BK-2", "Ravi", "Single")),
                "no-room booking rejected");
        expectBookingFailure(() -> service.confirm(
                new BookingWorkflowExtensions.BookingRequest("BK-3", "Mina", "Penthouse")),
                "unknown room type rejected");
        check(inventory.available("Single") == 0, "failed bookings do not change inventory");
    }

    private static void testBookingValidation() {
        expect(IllegalArgumentException.class,
                () -> new BookingWorkflowExtensions.BookingRequest("", "Guest", "Single"),
                "blank reservation ID rejected");
        expect(IllegalArgumentException.class,
                () -> new BookingWorkflowExtensions.BookingRequest("BK-1", " ", "Single"),
                "blank guest name rejected");
        expect(IllegalArgumentException.class,
                () -> new BookingWorkflowExtensions.BookingRequest("BK-1", "Guest", null),
                "null room type rejected");
    }

    private static void testInventoryValidationAndSnapshot() {
        expect(IllegalArgumentException.class,
                () -> new BookingWorkflowExtensions.RoomInventory(Map.of("Single", -1)),
                "negative room count rejected");
        BookingWorkflowExtensions.RoomInventory inventory =
                new BookingWorkflowExtensions.RoomInventory(Map.of("Single", 2));
        Map<String, Integer> snapshot = inventory.snapshot();
        expect(UnsupportedOperationException.class,
                () -> snapshot.put("Single", 99), "inventory snapshot is immutable");
        check(inventory.available("Single") == 2, "snapshot cannot mutate inventory");
    }

    private static void testQueueUsesFifoOrder() {
        BookingWorkflowExtensions.BookingQueue queue =
                new BookingWorkflowExtensions.BookingQueue();
        BookingWorkflowExtensions.BookingRequest first =
                new BookingWorkflowExtensions.BookingRequest("BK-1", "Asha", "Single");
        BookingWorkflowExtensions.BookingRequest second =
                new BookingWorkflowExtensions.BookingRequest("BK-2", "Ravi", "Double");
        check(queue.poll() == null, "empty queue returns null");
        queue.enqueue(first);
        queue.enqueue(second);
        check(queue.size() == 2 && queue.hasPendingRequests(), "queue tracks pending requests");
        check(queue.poll() == first, "queue returns first request first");
        check(queue.poll() == second, "queue returns second request second");
        check(!queue.hasPendingRequests() && queue.size() == 0, "queue becomes empty");
    }

    private static void testCancellationHistoryOrder() throws Exception {
        BookingWorkflowExtensions.RoomInventory inventory =
                new BookingWorkflowExtensions.RoomInventory(Map.of("Single", 2));
        BookingWorkflowExtensions.BookingService service =
                new BookingWorkflowExtensions.BookingService(inventory);
        service.confirm(new BookingWorkflowExtensions.BookingRequest("BK-1", "Asha", "Single"));
        service.confirm(new BookingWorkflowExtensions.BookingRequest("BK-2", "Ravi", "Single"));
        service.cancel("BK-1");
        service.cancel("BK-2");
        check(service.cancellationHistory().equals(List.of("BK-2", "BK-1")),
                "cancellation history is newest first");
        expect(UnsupportedOperationException.class,
                () -> service.cancellationHistory().add("BK-3"),
                "cancellation history is immutable");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError("FAILED: " + message);
        System.out.println("PASS: " + message);
    }

    private static void expectBookingFailure(CheckedAction action, String message) throws Exception {
        try {
            action.run();
            throw new AssertionError("FAILED: " + message);
        } catch (BookingWorkflowExtensions.BookingException expected) {
            System.out.println("PASS: " + message);
        }
    }

    private static void expect(Class<? extends Throwable> type, Runnable action, String message) {
        try {
            action.run();
        } catch (Throwable error) {
            if (type.isInstance(error)) {
                System.out.println("PASS: " + message);
                return;
            }
            throw new AssertionError("Expected " + type.getSimpleName() + " but got " + error, error);
        }
        throw new AssertionError("Expected " + type.getSimpleName() + ": " + message);
    }

    @FunctionalInterface
    private interface CheckedAction { void run() throws Exception; }
}
