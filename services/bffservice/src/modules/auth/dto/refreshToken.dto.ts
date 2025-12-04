/**
 * Data Transfer Object for logout and refresh token requests.
 *
 * <p>Contains the refresh token value used to obtain a new access token or
 * log out.</p>
 * 
 * @author Joel Salazar
 */
export class RefreshTokenDTO {

    /** Refresh token value */
    refreshToken: string;

}