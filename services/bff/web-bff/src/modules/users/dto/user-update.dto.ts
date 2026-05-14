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
    // PROFILE
    // ----------------------------------------------------------------

    firstName!: string;

    lastName!: string;

    phone!: string;

    profilePictureUrl!: string;

    locale!: string;
    
    timezone!: string;

}