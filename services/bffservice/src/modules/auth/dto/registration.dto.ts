/**
 * Data Transfer Object for account registration requests.
 * 
 * <p>Contains account's information and user's profile information used to
 * create a new account.</p>
 * 
 * @author Joel Salazar
 */
export class RegistrationDTO {

    // ----------------------------------------------------------------
    // ACCOUNT'S INFORMATION
    // ----------------------------------------------------------------
    
    /** Username */
    username: string;

    /** Username */
    email: string;

    /** Password */
    password: string;

    // ----------------------------------------------------------------
    // USER'S PROFILE
    // ----------------------------------------------------------------

    /** First name */
    firstName: string;

    /** Last name */
    lastName: string;

    /** Phone number */
    phone: string;
    
    /** Profile picture URL */
    profilePictureUrl: string;
    
    /** Preferred language locale */
    locale: string;
    
    /** Preferred timezone */
    timezone: string;

}