package uk.gov.dwp.uc.pairtest.test;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import java.util.*;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;

import uk.gov.dwp.uc.pairtest.TicketService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.factory.CinemaTicketFactory;
import uk.gov.dwp.uc.pairtest.factory.CinemaTicketFactoryImpl;

public class TicketServiceTests {
    private TicketService ticketService;
    private TicketPaymentService ticketPaymentService;
    private SeatReservationService seatReservationService;

    private static final Long accountId = (long) 1;

    @BeforeEach
    public void setUp() {
        CinemaTicketFactory cinemaTicketFactory = new CinemaTicketFactoryImpl();
        ticketPaymentService = Mockito.mock(TicketPaymentService.class);
        seatReservationService = Mockito.mock(SeatReservationService.class);

        ticketService = new TicketServiceImpl(cinemaTicketFactory, ticketPaymentService, seatReservationService);
    }

    @ParameterizedTest
    @ValueSource(longs = { Long.MIN_VALUE, -10, -1, 0 })
    public void invalidAccountIdThrowsException(Long accountId) {
        Exception exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(accountId, new TicketTypeRequest(Type.ADULT, 1));
        });

        String expectedMessage = "Parameter accountId must be greater than 0";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contentEquals(expectedMessage));
    }

    @Test
    public void overMaximumTicketsThrowsException() {
        List<TicketTypeRequest> requests = new ArrayList<TicketTypeRequest>();
        int maxNumberOfTickets = 20;

        for (int i = 0; i <= maxNumberOfTickets; i++) {
            requests.add(new TicketTypeRequest(Type.ADULT, 1));
        }

        Exception exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(accountId, requests.toArray(new TicketTypeRequest[requests.size()]));
        });

        String expectedMessage = String.format("You cannot purchase more than %s tickets", maxNumberOfTickets);
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contentEquals(expectedMessage));
    }

    @Test
    public void noAdultTicketThrowsException() {
        Exception exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(accountId, new TicketTypeRequest(Type.CHILD, 1),
                    new TicketTypeRequest(Type.INFANT, 1));
        });

        String expectedMessage = "At least 1 adult ticket is required";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contentEquals(expectedMessage));
    }

    @ParameterizedTest
    @ValueSource(ints = { Integer.MIN_VALUE, -10, -1 })
    public void negativeNoOfTicketsThrowsException(int noOfTickets) {
        Exception exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(accountId, new TicketTypeRequest(Type.ADULT, noOfTickets));
        });

        String expectedMessage = "You cannot request to purchase a negative number of tickets";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contentEquals(expectedMessage));
    }

    @ParameterizedTest
    @MethodSource("getPaymentAmountTestData")
    public void requestsPaymentWithCorrectAmount(TicketTypeRequest[] requests, int expectedAmount) {
        ticketService.purchaseTickets(accountId, requests);

        verify(ticketPaymentService).makePayment(accountId, expectedAmount);
    }

    @ParameterizedTest
    @MethodSource("getSeatNumberTestData")
    public void requestsSeatsWithCorrectNumber(TicketTypeRequest[] requests, int expectedNumber) {
        ticketService.purchaseTickets(accountId, requests);

        verify(seatReservationService).reserveSeat(accountId, expectedNumber);
    }

    private static Stream<Arguments> getPaymentAmountTestData() {
        return Stream.of(Arguments.of(new TicketTypeRequest[] { new TicketTypeRequest(Type.ADULT, 1) }, 20),
                Arguments.of(new TicketTypeRequest[] { new TicketTypeRequest(Type.ADULT, 3) }, 60),
                Arguments.of(new TicketTypeRequest[] { new TicketTypeRequest(Type.ADULT, 1),
                        new TicketTypeRequest(Type.CHILD, 1) }, 30),
                Arguments.of(new TicketTypeRequest[] { new TicketTypeRequest(Type.ADULT, 1),
                        new TicketTypeRequest(Type.INFANT, 1) }, 20),
                Arguments.of(new TicketTypeRequest[] { new TicketTypeRequest(Type.ADULT, 2),
                        new TicketTypeRequest(Type.CHILD, 2) }, 60),
                Arguments.of(new TicketTypeRequest[] { new TicketTypeRequest(Type.ADULT, 2),
                        new TicketTypeRequest(Type.INFANT, 2) }, 40));
    }

    private static Stream<Arguments> getSeatNumberTestData() {
        return Stream.of(Arguments.of(new TicketTypeRequest[] { new TicketTypeRequest(Type.ADULT, 1) }, 1),
                Arguments.of(new TicketTypeRequest[] { new TicketTypeRequest(Type.ADULT, 3) }, 3),
                Arguments.of(new TicketTypeRequest[] { new TicketTypeRequest(Type.ADULT, 1),
                        new TicketTypeRequest(Type.CHILD, 1) }, 2),
                Arguments.of(new TicketTypeRequest[] { new TicketTypeRequest(Type.ADULT, 1),
                        new TicketTypeRequest(Type.INFANT, 1) }, 1),
                Arguments.of(new TicketTypeRequest[] { new TicketTypeRequest(Type.ADULT, 2),
                        new TicketTypeRequest(Type.CHILD, 2) }, 4),
                Arguments.of(new TicketTypeRequest[] { new TicketTypeRequest(Type.ADULT, 2),
                        new TicketTypeRequest(Type.INFANT, 2) }, 2));
    }
}
