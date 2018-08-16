package com.bridgeit.userservice.utilservice.messageaccessor;

import java.util.Locale;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

/**
 * <p>
 * <b>Helper class for easy access to messages from a MessageSource, providing
 * various overloaded getMessage methods.
 * Available from ApplicationObjectSupport, but also reusable as a standalone
 * helper to delegate to in application objects.</b>
 * </p>
 * 
 * @author : Vishal Dhar Dubey
 * @version : 1.0
 * @since : 09-07-2018
 */
@Component
public class MessageAccessor {

	@Autowired
	private MessageSource messageSource;

	private MessageSourceAccessor accessor;

	/**
	 * <p>Function is to initialize the message source accessor</p>
	 */
	@PostConstruct
	private void init() {
		accessor = new MessageSourceAccessor(messageSource, Locale.ENGLISH);
	}

	/**
	 * @param messageKey
	 * <p>get message function accepts the messagekey and fetch the corresponding value.</p>
	 * @return message
	 */
	public String getMessage(String messageKey) {
		return accessor.getMessage(messageKey);
	}
}