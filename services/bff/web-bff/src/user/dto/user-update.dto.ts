/**
 * Data Transfer Object for account registration requests.
 * 
 * Contains account's information and user's profile used to create a new
 * account.
 * 
 * @author Joel Salazar
 */
export class UserUpdateDTO {
    
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