package edu.university.equipment;
import java.util.NoSuchElementException;

 import static org.junit.jupiter.api.Assertions.assertEquals;
 import static org.junit.jupiter.api.Assertions.assertNotNull;
 import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
class BookingServiceTest {
 private BookingService service; @BeforeEach void setup(){service=new BookingService();}
 @Test void successfulBookingReducesInventory(){service.create("STU1001","LAPTOP01",2);assertEquals(8,service.getEquipment("LAPTOP01").getAvailableQuantity());}
 @Test void zeroQuantityRejected(){assertThrows(IllegalArgumentException.class,()->service.create("STU1001","LAPTOP01",0));assertEquals(10,service.getEquipment("LAPTOP01").getAvailableQuantity());}
 @Test void blankBorrowerRejected(){assertThrows(IllegalArgumentException.class,()->service.create(" ","LAPTOP01",1));}
 @Test void unknownEquipmentRejected(){assertThrows(NoSuchElementException.class,()->service.create("STU1001","UNKNOWN",1));}
 @Test void ordinaryOverbookingRejected(){assertThrows(IllegalStateException.class,()->service.create("STU1001","PROJECTOR01",6));assertEquals(4,service.getEquipment("PROJECTOR01").getAvailableQuantity());}
 @Test void cancellationRestoresInventoryOnce(){Booking b=service.create("STU1001","TABLET01",3);service.cancel(b.getId());assertEquals(8,service.getEquipment("TABLET01").getAvailableQuantity());assertThrows(IllegalStateException.class,()->service.cancel(b.getId()));}
 @Test void bookingExactlyAvailableInventoryIsAccepted() {
    Booking b = service.create("STU1001", "PROJECTOR01", 4);

    assertNotNull(b);
    assertEquals(0, service.getEquipment("PROJECTOR01").getAvailableQuantity());
    assertEquals("CONFIRMED", b.getStatus());
}
 @Test void bookingExceedingAvailableInventoryIsRejectedWithoutChanges() {
    assertThrows(
        IllegalStateException.class,
        () -> service.create("STU1001", "PROJECTOR01", 5)
    );

    assertEquals(4, service.getEquipment("PROJECTOR01").getAvailableQuantity());
}
}