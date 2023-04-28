package uk.gov.dwp.uc.pairtest.factory;

import uk.gov.dwp.uc.pairtest.domain.AdultTicket;
import uk.gov.dwp.uc.pairtest.domain.ChildTicket;
import uk.gov.dwp.uc.pairtest.domain.CinemaTicket;
import uk.gov.dwp.uc.pairtest.domain.InfantTicket;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;

public class CinemaTicketFactoryImpl implements CinemaTicketFactory {
	@Override
	public CinemaTicket createCinemaTicket(Type type) {
		switch (type) {
			case INFANT:
				return new InfantTicket();
			case CHILD:
				return new ChildTicket();
			case ADULT:
			default:
				return new AdultTicket();
		}
	}
}
