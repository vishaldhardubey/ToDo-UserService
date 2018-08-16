package com.bridgeit.userservice.service;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bridgeit.userservice.securityservice.security.TokenGenerator;

import com.bridgeit.userservice.model.Email;
import com.bridgeit.userservice.model.ResponseDTO;
import com.bridgeit.userservice.model.User;
import com.bridgeit.userservice.model.UserDTO;
import com.bridgeit.userservice.model.UserLoginDTO;
import com.bridgeit.userservice.model.UserResetPasswordDTO;
import com.bridgeit.userservice.repository.IUserRepository;
import com.bridgeit.userservice.utilservice.UtilService;
import com.bridgeit.userservice.utilservice.exceptions.RestPreconditions;
import com.bridgeit.userservice.utilservice.exceptions.ToDoException;
import com.bridgeit.userservice.utilservice.messageaccessor.MessageAccessor;
import com.bridgeit.userservice.utilservice.rabbitmq.RabbitMQSender;

/**
 * <p>
 * <b>User Service Implementation for the interface User service</b>
 * </p>
 * 
 * @author : Vishal Dhar Dubey
 * @version : 1.0
 * @since : 09-07-2018
 */
@Service
public class UserServiceImpl implements IUserService {

	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
	@Autowired
	private IUserRepository iUserRepository;

	@Autowired
	private TokenGenerator token;

	@Autowired
	ModelMapper modelMapper;

	@Autowired
	RabbitMQSender mqSender;

	@Autowired
	UtilService utilService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private MessageAccessor messageAccessor;

	/**
	 * <p>
	 * Function is to check and verify user from database and generate token.
	 * </p>
	 * 
	 * @param User
	 * @return User1 object
	 * @throws ToDoException
	 */
	@Override
	public String loginUser(UserLoginDTO user) throws ToDoException {
		String email = user.getEmail();
		logger.info(email);
		RestPreconditions.isPresentInDB(!iUserRepository.existsByEmail(email),
				new ResponseDTO(messageAccessor.getMessage("305"), "305"));
		Optional<User> userObject = iUserRepository.getByEmail(email);
		RestPreconditions.checkNotNull(userObject.get().getEmail(), messageAccessor.getMessage("308"));
		// RestPreconditions.isPresentInDB(!iUserRepository.existsByEmail(userObject.get().getEmail()),
		// messageAccessor.getMessage("308"));
		if (userObject.get().getStatus() == false) {
			throw new ToDoException(messageAccessor.getMessage("309"));
		}

		if (passwordEncoder.matches(user.getPassword(), userObject.get().getPassword())) {
			String validToken = token.generator(userObject.get().getId());
			logger.info(validToken);
			token.parseJWT(validToken);
			return validToken;
		} else {
			logger.error(email);
			throw new ToDoException(messageAccessor.getMessage("306"));
		}
	}

	/**
	 * <p>
	 * Function is to register the User info inside database.
	 * </p>
	 * 
	 * @param User
	 * @return boolean
	 * @throws ToDoException
	 */
	@Override
	public void registerUser(UserDTO userDto) throws ToDoException {
		utilService.emailValidation(userDto.getEmail(), userDto.getPassword());
		utilService.userNameValidator(userDto.getName());
		if (!iUserRepository.existsByEmail(userDto.getEmail())) {
			userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
			User user = modelMapper.map(userDto, User.class);
			saveUser(user);
			sendAuthenticationMail(user);
		} else {
			throw new ToDoException("User Already Exists having :" + userDto.getEmail());
		}
	}

	/**
	 * @param email
	 *            <p>
	 *            Function is to activate the user.
	 *            </p>
	 * @return boolean
	 * @throws ToDoException
	 */
	@Override
	public void activateUser(String id) throws ToDoException {
		RestPreconditions.checkNotNull(id, messageAccessor.getMessage("300"));
		RestPreconditions.isPresentInDB(!iUserRepository.existsById(id), messageAccessor.getMessage("310"));
		Optional<User> user = iUserRepository.getById(id);
		user.get().setStatus(true);
		iUserRepository.save(user.get());
	}

	/**
	 * @param User
	 *            <p>
	 *            Function is to save User into database.
	 *            </p>
	 * @return boolean
	 * @throws ToDoException
	 */
	@Override
	public boolean saveUser(User user) throws ToDoException {
		RestPreconditions.checkNotNull(user, messageAccessor.getMessage("311"));
		iUserRepository.insert(user);
		return true;
	}

	/**
	 * @param User
	 *            <p>
	 *            Function is to send Authentication Main to every User.
	 *            </p>
	 * @return boolean
	 * @throws ToDoException
	 */
	@Override
	public boolean sendAuthenticationMail(User user2) throws ToDoException {
		String email = user2.getEmail();
		String id = user2.getId();
		String validToken = token.generator(id);
		if (validToken != null) {
			logger.info(validToken + "****************************************");
			// String email1=token.parseJWT(validToken);
			Email email1 = new Email();
			email1.setTo(email);
			email1.setSubject("VALIDATION LINK");
			email1.setText("http://localhost:8080/useractivation/" + validToken);
			if (!mqSender.produceMsg(email1)) {
				throw new ToDoException(messageAccessor.getMessage("202"));
			}
			return true;
		}
		return false;
	}

	/**
	 * @param User
	 *            <p>
	 *            Function is to call send mail function present in Mailer class
	 *            </p>
	 * @return boolean
	 * @throws ToDoException
	 */
	@Override
	public void forgotPassword(String email) throws ToDoException {
		logger.info("inside forgotpassword");
		RestPreconditions.checkNotNull(email, new ResponseDTO(messageAccessor.getMessage("300"), "300"));

		RestPreconditions.isPresentInDB(!iUserRepository.existsByEmail(email),
				new ResponseDTO(messageAccessor.getMessage("305"), "305"));

		Optional<User> userObject = iUserRepository.getByEmail(email);
		String validToken = token.generator(userObject.get().getId());

		Email email1 = new Email();
		email1.setTo(email);
		email1.setSubject(validToken);
		email1.setText("http://localhost:8080/resetpassword/");

		if (!mqSender.produceMsg(email1)) {
			throw new ToDoException(messageAccessor.getMessage("202"));
		}
		logger.info("ended forgotpassword");
	}

	/**
	 * @param User
	 * @return boolean
	 * @throws ToDoException
	 *             <p>
	 *             Function is to reset the password.
	 *             </p>
	 */
	@Override
	public void resetPassword(UserResetPasswordDTO user, String id) throws ToDoException {
		if (!user.getPassword().equals(user.getConfirmPassword())) {
			throw new ToDoException(messageAccessor.getMessage("306"));
		}
		Optional<User> user1 = iUserRepository.getById(id);
		if (!user1.isPresent()) {
			throw new ToDoException("No data available having given ID");
		}
		user1.get().setPassword(passwordEncoder.encode(user.getPassword()));
		user1.get().setStatus(true);
		iUserRepository.save(user1.get());
	}
	
	/**
	 * @param userId
	 *            <p>
	 * 			Function is to get user object by user id
	 *            </p>
	 * @return user object
	 * @throws ToDoException 
	 */
	@Override
	public User getUserById(String userId) throws ToDoException {
		Optional<User> userObject=iUserRepository.findById(userId);
		if(userObject==null) {
			throw new ToDoException("User ID doest not exists.");
		}
		return userObject.get();
	}

	@Override
	public List<User> getAllUser() throws ToDoException {
		List<User> usersList=iUserRepository.findAll();
		if(usersList.isEmpty()) {
			throw new ToDoException("Exception : Database is empty");
		}
		return usersList;
	}

	/*	*//**
			 * @param User
			 * @return boolean
			 * @throws ToDoException
			 *             <p>
			 *             Function is to validate the user data
			 *             </p>
			 *//*
				 * public void checkUserData(UserDTO userRegister) throws ToDoException { if
				 * (userRegister.getEmail() == null || userRegister.getId() == null ||
				 * userRegister.getName() == null || userRegister.getPassword() == null) { throw
				 * new ToDoException("Please Fill All Inputs"); } }
				 */
}
