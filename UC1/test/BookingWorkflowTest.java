import java.util.LinkedHashMap;
import java.util.Map;

public class BookingWorkflowTest {
    public static void main(String[] args) throws Exception {
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

        expectFailure(() -> service.confirm(booking), "duplicate ID rejected");
        expectFailure(() -> service.confirm(new BookingWorkflowExtensions.BookingRequest(
                "BK-2", "Ravi", "Single")), "no-room booking rejected");

        service.cancel("BK-1");
        check(inventory.available("Single") == 1, "room restored after cancellation");
        check(service.confirmedBookings().isEmpty(), "cancelled booking removed");
        check(service.cancellationHistory().get(0).equals("BK-1"), "cancellation recorded");
        expectFailure(() -> service.cancel("BK-1"), "repeat cancellation rejected");

        try {
            new BookingWorkflowExtensions.BookingRequest("", "Guest", "Single");
            throw new AssertionError("blank ID should be rejected");
        } catch (IllegalArgumentException expected) { }
        System.out.println("All booking workflow tests passed.");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void expectFailure(CheckedAction action, String message) throws Exception {
        try {
            action.run();
            throw new AssertionError(message);
        } catch (BookingWorkflowExtensions.BookingException expected) { }
    }

    @FunctionalInterface
    private interface CheckedAction { void run() throws Exception; }
}
