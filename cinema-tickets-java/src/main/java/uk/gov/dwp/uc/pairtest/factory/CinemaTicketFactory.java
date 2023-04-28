package uk.gov.dwp.uc.pairtest.factory;

import uk.gov.dwp.uc.pairtest.domain.CinemaTicket;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;

public interface CinemaTicketFactory {

    CinemaTicket createCinemaTicket(Type type);

}