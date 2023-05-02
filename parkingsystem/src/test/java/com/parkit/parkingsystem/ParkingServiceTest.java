package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {


    @InjectMocks
    private ParkingService parkingService;

    @Mock
    private InputReaderUtil inputReaderUtil;
    @Mock
    private ParkingSpotDAO parkingSpotDAO;
    @Mock
    private TicketDAO ticketDAO;

    private Ticket ticket;

    @BeforeEach
    private void setUpPerTest() {

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
    }

    private void initExistingTicket() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    @DisplayName("Vehicle exiting without discount")
    public void processExitingVehicleTest() {
        //GIVEN
        initExistingTicket();
        final int visits = 0;
        final double expectedPrice = Fare.CAR_RATE_PER_HOUR;
        //WHEN
        when(ticketDAO.getNbTicket(anyString())).thenReturn(visits);
        parkingService.processExitingVehicle();

        //THEN
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));

        ArgumentCaptor<Ticket> ticketArgumentCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketDAO, times(1)).updateTicket(ticketArgumentCaptor.capture());

        Ticket ticket = ticketArgumentCaptor.getValue();
        assertEquals(expectedPrice, ticket.getPrice());
    }

    @Test
    @DisplayName("Vehicle exiting with discount")
    public void processExitingVehicleTestWhitDiscount() {
        //GIVEN
        initExistingTicket();
        final int visits = 5;
        final double expectedPrice = Math.round((Fare.CAR_RATE_PER_HOUR * 0.95) * 100.0) / 100.0;
        //WHEN
        when(ticketDAO.getNbTicket(anyString())).thenReturn(visits);
        parkingService.processExitingVehicle();

        //THEN
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));

        ArgumentCaptor<Ticket> ticketArgumentCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketDAO, times(1)).updateTicket(ticketArgumentCaptor.capture());

        Ticket ticket = ticketArgumentCaptor.getValue();
        assertEquals(expectedPrice, ticket.getPrice());

    }

    @Test
    @DisplayName("UpdateTicket don't work")
    public void processExitingVehicleTestUnableUpdate() throws Exception {
        //GIVEN

        //WHEN
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

        parkingService.processExitingVehicle();
        //THEN
        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
    }

    @Test
    @DisplayName("Vehicle enters for the first time")
    public void testProcessIncomingVehicle() throws Exception {
        //GIVEN
        final int visits = 0;

        //WHEN
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(any())).thenReturn(5);
        when(ticketDAO.getNbTicket(anyString())).thenReturn(visits);

        parkingService.processIncomingVehicle();

        //THEN
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));

    }

    @Test
    @DisplayName("Vehicle enters for many time")
    public void testProcessIncomingRecurringVehicle() throws Exception {
        //GIVEN
        final int visits = 5;

        //WHEN
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(any())).thenReturn(1);
        when(ticketDAO.getNbTicket(anyString())).thenReturn(visits);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        parkingService.processIncomingVehicle();

        //THEN
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));

    }

    @Test
    @DisplayName("Get next parking number if available")
    public void testGetNextParkingNumberIfAvailable() {
        //GIVEN
        ParkingSpot parkingSpot;

        //WHEN
        when(inputReaderUtil.readSelection()).thenReturn(2);
        when(parkingSpotDAO.getNextAvailableSlot(any())).thenReturn(1);
        parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        //THEN
        assertThat(parkingSpot.getId()).isEqualTo(1);
        assertThat(parkingSpot.isAvailable()).isEqualTo(true);
    }


    @Test
    @DisplayName("Next parking number not found")
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        //GIVEN
        final int input = 1;

        //WHEN
        when(inputReaderUtil.readSelection()).thenReturn(input);
        when(parkingSpotDAO.getNextAvailableSlot(any())).thenReturn(-1);
        final ParkingSpot response = parkingService.getNextParkingNumberIfAvailable();

        //THEN
        assertNull(response);
    }

    @Test
    @DisplayName("Error next parking number because of wrong user argument")
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
        //GIVEN

        //WHEN
        when(inputReaderUtil.readSelection()).thenReturn(3);
        var response = parkingService.getNextParkingNumberIfAvailable();
        //THEN
        assertNull(response);

    }
}