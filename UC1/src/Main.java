import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Reservation8 {
    private final String guestName;
    private final String roomType;
    private final String roomId;

    public Reservation8(String guestName, String roomType, String roomId) {
        if (guestName == null || guestName.isBlank()
                || roomType == null || roomType.isBlank()
                || roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("Guest name, room type, and room ID are required.");
        }
        this.guestName = guestName.trim();
        this.roomType = roomType.trim();
        this.roomId = roomId.trim();
    }

    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }
    public String getRoomId() { return roomId; }
}

class BookingHistory {
    private final List<Reservation8> confirmedReservations = new ArrayList<>();

    public void addReservation(Reservation8 reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation cannot be null.");
        }
        confirmedReservations.add(reservation);
    }

    public List<Reservation8> getConfirmedReservations() {
        return Collections.unmodifiableList(confirmedReservations);
    }
}

class BookingReportService {
    public void generateReport(BookingHistory history) {
        if (history == null) {
            throw new IllegalArgumentException("Booking history cannot be null.");
        }

        List<Reservation8> reservations = history.getConfirmedReservations();
        System.out.println("============================================================");
        System.out.println("       Book My Stay - Booking History Report");
        System.out.println("============================================================");
        System.out.println("Total Confirmed Bookings : " + reservations.size());
        System.out.println("------------------------------------------------------------");

        int count = 1;
        for (Reservation8 r : reservations) {
            System.out.println("Booking #" + count++);
            System.out.println("  Guest Name  : " + r.getGuestName());
            System.out.println("  Room Type   : " + r.getRoomType());
            System.out.println("  Room ID     : " + r.getRoomId());
            System.out.println();
        }

        System.out.println("============================================================");
        System.out.println("End of Report.");
        System.out.println("============================================================");
    }
}

public class Main {
    public static void main(String[] args) {
        BookingHistory history = new BookingHistory();

        history.addReservation(new Reservation8("Alice", "Single", "S-101"));
        history.addReservation(new Reservation8("Bob", "Double", "D-101"));
        history.addReservation(new Reservation8("Carol", "Suite", "SU-101"));
        history.addReservation(new Reservation8("David", "Single", "S-102"));

        new BookingReportService().generateReport(history);
    }
}