import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Regression tests for local booking persistence. */
public class BookingFileStoreTest {
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("bookmystay-test-");
        Path file = directory.resolve("bookings.csv");
        try {
            testRoundTrip(file);
            testMissingFile(file);
            testMalformedFile(file);
            System.out.println("All booking file-store tests passed.");
        } finally {
            Files.deleteIfExists(file);
            Files.deleteIfExists(directory);
        }
    }

    private static void testRoundTrip(Path file) throws IOException {
        List<BookingWorkflowExtensions.BookingRequest> expected = List.of(
                new BookingWorkflowExtensions.BookingRequest("BK,1", "Asha, Khan", "Single"),
                new BookingWorkflowExtensions.BookingRequest("BK-2", "Guest | Two", "Double"));
        BookingFileStore.save(file, expected);
        List<BookingWorkflowExtensions.BookingRequest> actual = BookingFileStore.load(file);
        check(actual.size() == 2, "all bookings are loaded");
        check(actual.get(0).getReservationId().equals("BK,1"), "commas are preserved");
        check(actual.get(0).getGuestName().equals("Asha, Khan"), "guest names are preserved");
        check(actual.get(1).getGuestName().equals("Guest | Two"), "special characters are preserved");
    }

    private static void testMissingFile(Path file) throws IOException {
        Files.deleteIfExists(file);
        check(BookingFileStore.load(file).isEmpty(), "missing file starts with no bookings");
    }

    private static void testMalformedFile(Path file) throws IOException {
        Files.writeString(file, "not,a,valid,record\n");
        try {
            BookingFileStore.load(file);
            throw new AssertionError("Malformed records must be rejected.");
        } catch (IOException expected) {
            System.out.println("PASS: malformed records are rejected");
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError("FAILED: " + message);
        System.out.println("PASS: " + message);
    }
}
