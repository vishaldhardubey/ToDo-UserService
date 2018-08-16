package com.bridgeit.userservice.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bridgeit.userservice.securityservice.security.TokenGenerator;
import com.bridgeit.userservice.model.ResponseDTO;
import com.bridgeit.userservice.model.User;
import com.bridgeit.userservice.model.UserDTO;
import com.bridgeit.userservice.model.UserLoginDTO;
import com.bridgeit.userservice.model.UserResetPasswordDTO;
import com.bridgeit.userservice.repository.IRedisRepository;
import com.bridgeit.userservice.service.IUserService;
import com.bridgeit.userservice.utilservice.UtilService;
import com.bridgeit.userservice.utilservice.exceptions.ToDoException;
import com.bridgeit.userservice.utilservice.messageaccessor.MessageAccessor;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * @author : Vishal Dhar Dubey
 * @version : 1.0
 *          <p>
 *          <b>User Login controller will control the funcationality of all the
 *          API's and will be responsible for interacting with the postman.</b>
 *          </p>
 * @since : 20-07-2018
 */
@RestController
@SuppressWarnings({ "rawtypes", "unchecked" })
@RequestMapping("/user")
public class UserController {

	private final static Logger LOGGER = LoggerFactory.getLogger(UserController.class);
	@Autowired
	private IUserService iUserService;

	@Autowired
	private TokenGenerator tokenGenerator;

	@Autowired
	private IRedisRepository iRedisRepository;

	@Autowired
	private MessageAccessor messageAccessor;

	@Autowired
	private UtilService utilService;

	@Autowired
	private ResponseDTO response;

	/**
	 * @param User
	 * @return response
	 *         <p>
	 *         Function is to login.
	 *         </p>
	 * @throws ToDoException
	 */
	@HystrixCommand(fallbackMethod="fallBackLoginMethod",commandKey="login",groupKey="login")
	@RequestMapping(method = RequestMethod.POST, value = "/userlogin")
	public ResponseEntity<UserLoginDTO> loginEmail(@RequestBody UserLoginDTO user, HttpServletResponse resp)
			throws ToDoException {
		LOGGER.info("Entered in Login Function");
		utilService.emailValidation(user.getEmail(), user.getPassword());

		// iUserService.loginUser(user);
		// RestPreconditions.checkNotNull(user, "");
		String token;
		try {
			token = iUserService.loginUser(user);
			resp.addHeader("token", token);
			LOGGER.info("Successfully Logged-In");
			return new ResponseEntity(new ResponseDTO(messageAccessor.getMessage("100"), "100"), HttpStatus.OK);
		} catch (ToDoException exception) {
			response.setMessage(exception.getMessage());
			response.setCode("110");
			LOGGER.error("Exception occured during login");
			return new ResponseEntity(response, HttpStatus.CONFLICT);
		}
	}
	
	public ResponseEntity<UserLoginDTO> fallBackLoginMethod(@RequestBody UserLoginDTO user, HttpServletResponse resp)
			throws ToDoException {
				return new ResponseEntity("Something went wrong while hitting the login api", HttpStatus.CONFLICT);
	}

	/**
	 * <p>
	 * Function is to register into database
	 * </p>
	 * 
	 * @param User
	 * @param resp
	 * @return resp to postman
	 * @throws ToDoException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/userregister")
	public ResponseEntity<User> registration(@RequestBody UserDTO user, HttpServletResponse resp) {
		try {
			iUserService.registerUser(user);
			return new ResponseEntity(new ResponseDTO(messageAccessor.getMessage("103"), "103"), HttpStatus.OK);
		} catch (ToDoException exception) {
			response.setMessage(exception.getMessage());
			response.setCode("-2");
			LOGGER.error("Exception occured during registration");
			return new ResponseEntity(response, HttpStatus.CONFLICT);
		}
	}

	/**
	 * @param req
	 *            <p>
	 *            Function is to verify the user by authenticated link.
	 *            </p>
	 * @return successfull message.
	 * @throws ToDoException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/useractivation")
	public ResponseEntity<String> userActivation(@RequestParam(value = "token") String token) {
		String id;
		try {
			id = tokenGenerator.parseJWT(token);
			iUserService.activateUser(id);
			iRedisRepository.save(token);
			return new ResponseEntity(new ResponseDTO(messageAccessor.getMessage("101"), "101"), HttpStatus.OK);
		} catch (ToDoException exception) {
			response.setMessage(exception.getMessage());
			response.setCode("-2");
			LOGGER.error("Exception occured while activation");
			return new ResponseEntity(response, HttpStatus.CONFLICT);
		}
	}

	/**
	 * @param User,resp
	 *            <p>
	 *            Function is to recover password.
	 *            </p>
	 * @return response
	 * @throws ToDoException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/forgetpassword")
	public ResponseEntity<User> forgotPassword(@RequestParam(value = "email") String user) {
		try {
			iUserService.forgotPassword(user);
			return new ResponseEntity(
					new ResponseDTO("Request to reset password link has been sent to your registered Email ID", "103"),
					HttpStatus.OK);
		} catch (ToDoException exception) {
			response.setMessage(exception.getMessage());
			response.setCode("-3");
			LOGGER.error("Exception occured for forgot password");
			return new ResponseEntity(response, HttpStatus.CONFLICT);
		}
	}

	/**
	 * @param User
	 *            <p>
	 *            Function is to reset the password.
	 *            </p>
	 * @throws ToDoException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/resetpassword")
	public ResponseEntity<User> resetPassword(@RequestParam(value = "token") String token,
			@RequestBody UserResetPasswordDTO user) throws ToDoException {
		String id = tokenGenerator.parseJWT(token);
		iUserService.resetPassword(user, id);
		return new ResponseEntity(new ResponseDTO(messageAccessor.getMessage("102"), "102"), HttpStatus.OK);

	}

	/**
	 * @param userId
	 *            <p>
	 *            Function is to get user object by user id
	 *            </p>
	 * @return user object
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/getuserbyid")
	public ResponseEntity<User> getUserById(@RequestParam String userId) {
		try {
			User user = iUserService.getUserById(userId);
			return new ResponseEntity<User>(user, HttpStatus.OK);
		} catch (ToDoException exception) {
			response.setMessage(exception.getMessage());
			response.setCode("-1");
			LOGGER.error("Exception occured while getting the User object by user Id");
			return new ResponseEntity(response, HttpStatus.CONFLICT);
		}
	}

	/**
	 * <p>
	 * Function is to get all user object from user repository
	 * </p>
	 * 
	 * @return user object
	 * @throws ToDoException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/getalluserlist")
	public ResponseEntity<?> getAllUser() {
		try {
			List<User> user = iUserService.getAllUser();
			return new ResponseEntity(user, HttpStatus.OK);
		} catch (ToDoException exception) {
			response.setMessage(exception.getMessage());
			response.setCode("-1");
			LOGGER.error("Exception occured while getting all User object");
			return new ResponseEntity(response, HttpStatus.CONFLICT);
		}
	}
}
