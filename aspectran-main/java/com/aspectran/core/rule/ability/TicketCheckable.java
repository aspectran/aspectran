/**
 * 
 */
package com.aspectran.core.rule.ability;

import com.aspectran.core.rule.TicketCheckRule;

/**
 *
 * @author Gulendol
 * @since 2011. 2. 21.
 *
 */
public interface TicketCheckable {

	public void addTicketCheckAction(TicketCheckRule ticketCheckRule);

}
