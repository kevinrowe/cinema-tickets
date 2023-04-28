package uk.gov.dwp.uc.pairtest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.CinemaTicket;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.factory.CinemaTicketFactory;

public class TicketServiceImpl implements TicketService {
    private static final int _maximumNumberOfTickets = 20;

    private CinemaTicketFactory cinemaTicketFactory;
    private TicketPaymentService ticketPaymentService;
    private SeatReservationService seatReservationService;

    public TicketServiceImpl(CinemaTicketFactory cinemaTicketFactory, TicketPaymentService ticketPaymentService,
            SeatReservationService seatReservationService) {
        this.cinemaTicketFactory = cinemaTicketFactory;
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    /**
     * Should only have private methods other than the one below.
     */

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests)
            throws InvalidPurchaseException {

        validateInputs(accountId, ticketTypeRequests);

        List<CinemaTicket> tickets = getTicketsFromRequests(ticketTypeRequests);

        int amountToPay = 0;
        int noSeats = 0;

        for (CinemaTicket ticket : tickets) {
            amountToPay += ticket.getPrice();
            noSeats += ticket.getNoSeats();
        }

        ticketPaymentService.makePayment(accountId, amountToPay);
        seatReservationService.reserveSeat(accountId, noSeats);
    }

    private void validateInputs(Long accountId, TicketTypeRequest... ticketTypeRequests) {
        validateAccountId(accountId);
        validateTicketTypes(ticketTypeRequests);
        validateNoOfTickets(ticketTypeRequests);
        validateMaxTicketCount(ticketTypeRequests);
    }

    private void validateAccountId(Long accountId) {
        if (accountId <= 0) {
            throw new InvalidPurchaseException("Parameter accountId must be greater than 0");
        }
    }

    private void validateTicketTypes(TicketTypeRequest... ticketTypeRequests) {
        boolean hasAdultTicketRequests = Arrays.stream(ticketTypeRequests)
                .anyMatch(r -> r.getTicketType() == Type.ADULT);

        if (!hasAdultTicketRequests) {
            throw new InvalidPurchaseException("At least 1 adult ticket is required");
        }
    }

    private void validateNoOfTickets(TicketTypeRequest... ticketTypeRequests) {
        boolean numberOfTicketsIsInvalid = Arrays.stream(ticketTypeRequests).anyMatch(r -> r.getNoOfTickets() < 0);

        if (numberOfTicketsIsInvalid) {
            throw new InvalidPurchaseException("You cannot request to purchase a negative number of tickets");
        }
    }

    private void validateMaxTicketCount(TicketTypeRequest... ticketTypeRequests) {
        var count = Arrays.stream(ticketTypeRequests).mapToInt(TicketTypeRequest::getNoOfTickets).sum();

        if (count > _maximumNumberOfTickets) {
            throw new InvalidPurchaseException(
                    String.format("You cannot purchase more than %s tickets", _maximumNumberOfTickets));
        }
    }

    private List<CinemaTicket> getTicketsFromRequests(TicketTypeRequest... ticketTypeRequests) {
        List<CinemaTicket> tickets = new ArrayList<CinemaTicket>();

        for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
            for (int i = 0; i < ticketTypeRequest.getNoOfTickets(); i++) {
                tickets.add(cinemaTicketFactory.createCinemaTicket(ticketTypeRequest.getTicketType()));
            }
        }

        return tickets;
    }

}
