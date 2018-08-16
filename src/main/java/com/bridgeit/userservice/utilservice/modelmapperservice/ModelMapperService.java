package com.bridgeit.userservice.utilservice.modelmapperservice;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bridgeit.userservice.utilservice.exceptions.RestPreconditions;
import com.bridgeit.userservice.utilservice.exceptions.ToDoException;

/**
 * <p>
 * <b>ModelMapper Performs object mapping, maintains Configuration and stores
 * TypeMaps.</b>
 * </p>
 * 
 * @author : Vishal Dhar Dubey
 * @version : 1.0
 * @since : 22-07-2018
 */
@Component
public class ModelMapperService {

	@Autowired
	private ModelMapper modelMapper;

	/**
	 * @param source
	 * @param destinationType
	 *            <p>
	 *            Maps source to an instance of destinationType. Mapping is
	 *            performed according to the corresponding TypeMap.
	 *            </p>
	 * @return fully mapped instance of destinationType
	 * @throws ToDoException
	 */
	public <D> D map(D source, Class<D> destinationType) throws ToDoException {
		RestPreconditions.checkNotNull(source, "NullPointerException : source cann't be null");
		RestPreconditions.checkNotNull(destinationType, "NullPointerException : destinationType cann't be null");
		return modelMapper.map(source, destinationType);
	}

}
