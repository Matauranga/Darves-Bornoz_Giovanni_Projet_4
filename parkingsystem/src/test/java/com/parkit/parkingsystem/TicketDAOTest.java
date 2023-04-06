package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.junit.jupiter.api.*;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;


public class TicketDAOTest {
    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private Ticket ticket;


    @BeforeAll
    private static void setUp() throws Exception {
        ticketDAO = new TicketDAO();
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() {
        //ticket = new Ticket();
    }

    @AfterAll
    private static void tearDown() {
        dataBasePrepareService.clearDataBaseEntries();
    }


    //TODO : regarder si tu peux tester plus d'assert
    @DisplayName("test if saveTicket work")
    @Test
    public void testSaveTicket() {
        //GIVEN
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setInTime(new Date(System.currentTimeMillis()));
        //WHEN
        Boolean result = ticketDAO.saveTicket(ticket);
        //THEN
        assertEquals(true, result);

        Ticket response = ticketDAO.getTicket("ABCDEF");
        assertEquals(ticket.getVehicleRegNumber(), response.getVehicleRegNumber());
        assertNotEquals(0, response.getId());


    }

    @DisplayName("test if saveTicket failed")
    @Test
    public void testSaveTicketFailed() {
        //GIVEN
        Ticket ticket = new Ticket();
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setInTime(new Date(System.currentTimeMillis()));
        //WHEN
        Boolean result = ticketDAO.saveTicket(ticket);
        //THEN
        assertEquals(false, result);

    }

    @DisplayName("test of getTicket")
    @Test
    public void testGetTicket() {
        //GIVEN
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setId(1);
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setPrice(0);
        ticket.setInTime(new Date(System.currentTimeMillis()));

        ticketDAO.saveTicket(ticket);

        //WHEN
        Ticket response = ticketDAO.getTicket("ABCDEF");
        //THEN
        assertNotNull(response);
        assertEquals(ticket.getVehicleRegNumber(), response.getVehicleRegNumber());
        assertNotEquals(0, response.getId());

    }


    @DisplayName("test of updateTicket")
    @Test
    public void TestUpdateTicket() {
        //GIVEN
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setId(1);
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setInTime(new Date(System.currentTimeMillis()));
        ticket.setParkingSpot(parkingSpot);
        ticket.setPrice(0);
        ticketDAO.saveTicket(ticket);

        ticket.setPrice(10);
        ticket.setOutTime(new Date(System.currentTimeMillis()));

        //WHEN
        Boolean result = ticketDAO.updateTicket(ticket);

        //THEN
        assertEquals(true, result);
        assertEquals(10, ticket.getPrice());

    }

    @DisplayName("test if updateTicket failed")
    @Test
    public void testUpdateTicketFailed() {
        //GIVEN

        //WHEN
        Boolean test = ticketDAO.updateTicket(ticket);
        //THEN
        assertEquals(false, test);
    }

    @DisplayName("Test of getNbTicket for 2 ticket")
    @Test
    public void testGetNbTicketForTwoTicket() {
        //GIVEN
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        String vehicleRegNumber = "BABABA";

        //create first ticket
        Ticket firstTicket = new Ticket();
        firstTicket.setParkingSpot(parkingSpot);
        firstTicket.setVehicleRegNumber(vehicleRegNumber);
        firstTicket.setInTime(new Date(System.currentTimeMillis() - (1000 * 60 * 60)));
        firstTicket.setOutTime(new Date(System.currentTimeMillis() - (1000 * 60 * 15)));
        ticketDAO.saveTicket(firstTicket);

        //create second ticket
        Ticket secondTicket = new Ticket();
        secondTicket.setParkingSpot(parkingSpot);
        secondTicket.setVehicleRegNumber(vehicleRegNumber);
        secondTicket.setInTime(new Date(System.currentTimeMillis()));
        secondTicket.setOutTime(new Date(System.currentTimeMillis() + (1000 * 60 * 60)));
        ticketDAO.saveTicket(secondTicket);
        //WHEN

        int result = ticketDAO.getNbTicket(vehicleRegNumber);

        //THEN
        assertEquals(2, result);
    }

}

