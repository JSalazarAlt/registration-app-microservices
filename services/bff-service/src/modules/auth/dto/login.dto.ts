/**
 * Data Transfer Object for account authentication requests.
 * 
 * Contains the account identifier and password used to authenticate the
 * user and establish a session.
 */
export class LoginDTO {
    
    /** Username or email */
    identifier: string; // username or email

    /** Password */
    password: string;

}