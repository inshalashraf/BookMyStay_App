import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Reusable booking-workflow extensions for Book My Stay.
 *
 * This file keeps validation, FIFO booking requests, and cancellation rollback
 * separate from the existing Main.java use-case demo.
 */
public final class BookingWorkflowExtensions {
    private BookingWorkflowExtensions() {}

    public static final class BookingException extends Exception {
        public BookingException(String message) {
            super(message);
        }
    }

    public static final class BookingRequest {
        private final String reservationId;
        private final String guestName;
        private final String roomType;

        public BookingRequest(String reservationId, String guestName, String roomType) {
            this.reservationId = requireText(reservationId, "Reservation ID");
            this.guestName = requireText(guestName, "Guest name");
            this.roomType = requireText(roomType, "Room type");
        }

        public String getReservationId() { return reservationId; }
        public String getGuestName() { return guestName; }
        public String getRoomType() { return roomType; }

        private static String requireText(String value, String label) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(label + " cannot be blank.");
            }
            return value.trim();
        }
    }

    public static final class RoomInventory {
        private final Map<String, Integer> available = new LinkedHashMap<>();

        public RoomInventory(Map<String, Integer> initialAvailability) {
            Objects.requireNonNull(initialAvailability, "Initial availability is required.");
            for (Map.Entry<String, Integer> entry : initialAvailability.entrySet()) {
                String type = BookingRequest.requireText(entry.getKey(), "Room type");
                Integer count = Objects.requireNonNull(entry.getValue(), "Room count is required.");
                if (count < 0) {
                    throw new IllegalArgumentException("Room counts cannot be negative.");
                }
                available.put(type, count);
            }
        }

        public synchronized int available(String roomType) {
            Integer count = available.get(roomType);
            if (count == null) {
                throw new IllegalArgumentException("Unknown room type: " + roomType);
            }
            return count;
        }

        public synchronized Map<String, Integer> snapshot() {
            return Collections.unmodifiableMap(new LinkedHashMap<>(available));
        }

        private synchronized void reserve(String roomType) throws BookingException {
            Integer count = available.get(roomType);
            if (count == null) {
                throw new BookingException("Invalid room type: " + roomType);
            }
            if (count == 0) {
                throw new BookingException("No rooms available for type: " + roomType);
            }
            available.put(roomType, count - 1);
        }

        private synchronized void release(String roomType) {
            Integer count = available.get(roomType);
            if (count == null) {
                throw new IllegalStateException("Cannot release an unknown room type: " + roomType);
            }
            available.put(roomType, count + 1);
        }
    }

    /** FIFO queue for requests awaiting validation and confirmation. */
    public static final class BookingQueue {
        private final Deque<BookingRequest> queue = new ArrayDeque<>();

        public synchronized void enqueue(BookingRequest request) {
            queue.addLast(Objects.requireNonNull(request, "Booking request is required."));
        }

        public synchronized BookingRequest poll() {
            return queue.pollFirst();
        }

        public synchronized boolean hasPendingRequests() {
            return !queue.isEmpty();
        }

        public synchronized int size() {
            return queue.size();
        }
    }

    /**
     * Confirms a booking only after validation and inventory reservation.
     * Cancellation restores inventory once and records the cancellation order.
     */
    public static final class BookingService {
        private final RoomInventory inventory;
        private final Map<String, BookingRequest> confirmed = new LinkedHashMap<>();
        private final Deque<String> cancellationHistory = new ArrayDeque<>();

        public BookingService(RoomInventory inventory) {
            this.inventory = Objects.requireNonNull(inventory, "Inventory is required.");
        }

        public synchronized void confirm(BookingRequest request) throws BookingException {
            Objects.requireNonNull(request, "Booking request is required.");
            if (confirmed.containsKey(request.getReservationId())) {
                throw new BookingException("Duplicate reservation ID: " + request.getReservationId());
            }
            inventory.reserve(request.getRoomType());
            confirmed.put(request.getReservationId(), request);
        }

        public synchronized BookingRequest cancel(String reservationId) throws BookingException {
            if (reservationId == null || reservationId.isBlank()) {
                throw new BookingException("Reservation ID cannot be blank.");
            }
            BookingRequest booking = confirmed.remove(reservationId.trim());
            if (booking == null) {
                throw new BookingException("Reservation not found or already cancelled: " + reservationId);
            }
            inventory.release(booking.getRoomType());
            cancellationHistory.push(booking.getReservationId());
            return booking;
        }

        public synchronized List<BookingRequest> confirmedBookings() {
            return Collections.unmodifiableList(new ArrayList<>(confirmed.values()));
        }

        /** Most recently cancelled reservation appears first. */
        public synchronized List<String> cancellationHistory() {
            return Collections.unmodifiableList(new ArrayList<>(cancellationHistory));
        }
    }
}
