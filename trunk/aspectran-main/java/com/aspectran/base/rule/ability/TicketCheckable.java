/**
 * 
 */
package com.aspectran.base.rule.ability;

import com.aspectran.base.rule.TicketCheckRule;

/**
 *
 * @author Gulendol
 * @since 2011. 2. 21.
 *
 */
public interface TicketCheckable {

	public void addTicketCheckAction(TicketCheckRule ticketCheckRule);

}
