/**
 * Data Transfer Object for logout and refresh token requests.
 *
 * Contains the refresh token value used to obtain a new refresh and access
 * tokens or to be revoked.
 */
export class RefreshTokenDTO {

    /** Refresh token value */
    refreshToken: string;

}