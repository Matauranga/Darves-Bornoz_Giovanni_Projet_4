package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.Thread;

import java.sql.*;

import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {
    private static final Logger logger = LogManager.getLogger("ParkingDataBaseIT");
    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        lenient().when(inputReaderUtil.readSelection()).thenReturn(1);
        lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown() {
        dataBasePrepareService.clearDataBaseEntries();
    }

    @DisplayName("Integration test to park a car")
    @Test
    public void testParkingACar() {
        //GIVEN an initial state
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        int initialAvailableSpot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        // AND a ticket don't exist
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertNull(ticket);

        //WHEN a car incoming
        parkingService.processIncomingVehicle();

        //THEN the spot is used
        int actualAvailableSpot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        assertNotEquals(actualAvailableSpot, initialAvailableSpot);
        // AND a ticket is created
        Ticket savedTicket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(savedTicket);
    }

    @DisplayName("Integration test for a car exiting")
    @Test
    public void testParkingLotExit() throws Exception {
        // GIVEN a car incoming
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        int parkingNumber = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        ParkingSpot parkingSpot = new ParkingSpot(parkingNumber, ParkingType.CAR, true);
        Ticket ticket = new Ticket();
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setInTime(new Date(currentTimeMillis()));
        ticket.setParkingSpot(parkingSpot);

        ticketDAO.saveTicket(ticket);

        // WHEN the car exiting
        Thread.sleep(500);
        parkingService.processExitingVehicle();

        // THEN
        Ticket actualTicket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(actualTicket);
        assertNotNull(actualTicket.getOutTime());
    }


    @DisplayName("Integration test for a car entering and exiting")
    @Disabled
    @Test
    public void testParkingLotExitRecurringUser() throws Exception {

        String vehicleRegNumber = "ABCDEF";
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);


        //Create First ticket
        int parkingNumber = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        ParkingSpot parkingSpot = new ParkingSpot(parkingNumber, ParkingType.CAR, true);
        Ticket firstTicket = new Ticket();
        firstTicket.setVehicleRegNumber(vehicleRegNumber);
        firstTicket.setInTime(new Date(currentTimeMillis() - (1000 * 60 * 60 * 24)));//-1jours
        firstTicket.setParkingSpot(parkingSpot);
        firstTicket.setOutTime(new Date(currentTimeMillis() - (1000 * 60 * 60 * 22)));//-22h
        ticketDAO.saveTicket(firstTicket);

        //Create second ticket
        parkingService.processIncomingVehicle();
        Ticket secondTicket = ticketDAO.getTicket(vehicleRegNumber);
        secondTicket.setInTime(new Date(currentTimeMillis() - (1000 * 60 * 60)));
        //secondTicket.setId(2);
        ticketDAO.saveTicket(secondTicket);
        Thread.sleep(1000);
        parkingService.processExitingVehicle();
        secondTicket = ticketDAO.getTicket(vehicleRegNumber);

        assertEquals(Math.round(((Fare.CAR_RATE_PER_HOUR) * 0.95) * 100.0) / 100.0, secondTicket.getPrice());


    }
}

