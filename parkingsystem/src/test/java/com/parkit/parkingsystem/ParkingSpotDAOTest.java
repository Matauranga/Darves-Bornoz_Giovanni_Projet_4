package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;


public class ParkingSpotDAOTest {
    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static DataBasePrepareService dataBasePrepareService;


    @BeforeAll
    private static void setUp() throws Exception {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @AfterAll
    private static void tearDown() {
        dataBasePrepareService.clearDataBaseEntries();
    }

    @DisplayName("Get the next parking number available for a car")
    @Test
    public void testGetNextAvailableSlot() {
        //GIVEN
        int expectedParkingNumber = 1;
        //WHEN
        int response = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        //THEN
        assertEquals(expectedParkingNumber, response);

    }

    @DisplayName("test getNextAvailableSlot throws exception error")
    @Test
    public void testGetNextAvailableSlotFailed() {
        //GIVEN an expected parking number if error
        int expectedParkingNumber = -1;

        //WHEN
        int response = parkingSpotDAO.getNextAvailableSlot(null);

        //THEN
        assertEquals(expectedParkingNumber, response);

    }

    @DisplayName("update the availability for a parking slot to false")
    @Test
    public void testUpdateParking() {
        //GIVEN
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        //WHEN
        Boolean result = parkingSpotDAO.updateParking(parkingSpot);
        //THEN
        assertTrue(result);

    }

    @DisplayName("test updateParking throws exception error")
    @Test
    public void testUpdateParkingFailed() {
        //GIVEN

        //WHEN
        Boolean result = parkingSpotDAO.updateParking(null);
        //THEN
        assertFalse(result);
    }
}
