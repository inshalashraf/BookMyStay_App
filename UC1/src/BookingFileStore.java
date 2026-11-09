import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/** Stores confirmed bookings in a small local CSV-style file without external dependencies. */
public final class BookingFileStore {
    private BookingFileStore() {}

    public static List<BookingWorkflowExtensions.BookingRequest> load(Path file) throws IOException {
        List<BookingWorkflowExtensions.BookingRequest> bookings = new ArrayList<>();
        if (!Files.exists(file)) return bookings;

        int lineNumber = 0;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.isBlank()) continue;
            String[] fields = line.split(",", -1);
            if (fields.length != 3) {
                throw new IOException("Invalid booking data at line " + lineNumber);
            }
            try {
                bookings.add(new BookingWorkflowExtensions.BookingRequest(
                        decode(fields[0]), decode(fields[1]), decode(fields[2])));
            } catch (IllegalArgumentException ex) {
                throw new IOException("Invalid booking data at line " + lineNumber, ex);
            }
        }
        return bookings;
    }

    /** Writes a complete snapshot, then replaces the old file to avoid partial updates. */
    public static void save(Path file,
                            List<BookingWorkflowExtensions.BookingRequest> bookings) throws IOException {
        Path absolute = file.toAbsolutePath();
        Path parent = absolute.getParent();
        if (parent != null) Files.createDirectories(parent);

        List<String> lines = new ArrayList<>();
        for (BookingWorkflowExtensions.BookingRequest booking : bookings) {
            lines.add(encode(booking.getReservationId()) + ","
                    + encode(booking.getGuestName()) + ","
                    + encode(booking.getRoomType()));
        }

        Path temporary = Files.createTempFile(parent, "bookings-", ".tmp");
        try {
            Files.write(temporary, lines, StandardCharsets.UTF_8);
            try {
                Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
