/**
 * Data Transfer Object for account authentication requests.
 * 
 * <p>Contains the account identifier and password used to authenticate the
 * user and establish a session.</p>
 * 
 * @author Joel Salazar
 */
export class LoginDTO {
    
    /** Username or email */
    identifier: string; // username or email

    /** Password */
    password: string;

}