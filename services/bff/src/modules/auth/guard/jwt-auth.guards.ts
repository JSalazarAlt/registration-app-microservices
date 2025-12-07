import { CanActivate, ExecutionContext, Injectable } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';

/**
 * Guard that verifies JWTs and attaches account ID to the request.
 */
@Injectable()
export class JwtAuthGuard implements CanActivate {

  constructor(private readonly jwtService: JwtService) {}

  /**
   * Validates the authorization header and verifies the JWT.
   *
   * @param context Execution context for the incoming request.
   * @returns `true` if the token is valid, otherwise `false`.
   */
  canActivate(context: ExecutionContext): boolean {
        // Get the request fromt context
        const req = context.switchToHttp().getRequest();

        // Read authorization header
        const token = this.extractToken(req.headers['authorization']);

        // Return false if token not exists
        if (!token) return false;

        try {
            // Verify token signature and expiration
            const payload = this.jwtService.verify(token);
            // Attach accountId for controllers/services
            req.accountId = payload.sub ?? null;
            // Return true
            return true;
        } catch {
            // Return false for invalid or expired token
            return false;
        }
    }

    /**
     * Extracts the JWT token from a Bearer header.
     */
    private extractToken(header?: string): string | null {
        // Ensure token is a "Bearer" token
        return header?.startsWith('Bearer ')
            ? header.slice(7).trim()
            : null;
    }

}